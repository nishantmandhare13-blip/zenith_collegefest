import os
from datetime import datetime, date
from pathlib import Path
from typing import Optional, List, Dict
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException, Query, status
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse, JSONResponse

from backend.database import get_db_connection, init_db
from backend.seed_data import seed_events_if_needed
from backend.models import (
    EventOut,
    EventDetail,
    RegistrationIn,
    RegistrationOut,
    RegistrationDetail,
    StatsOut,
    EventCreateIn
)

PROJECT_ROOT = Path(__file__).resolve().parent.parent
FRONTEND_DIR = PROJECT_ROOT / "frontend"


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: ensure DB schema is initialized and seed data is populated
    init_db()
    try:
        seed_events_if_needed()
    except Exception as e:
        print(f"Seed note: {e}")
    yield


app = FastAPI(
    title="Zenith '26 College Fest Management System",
    description="API for student registration and festival administration at Zenith '26.",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for local development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request, exc):
    # Extract details of missing or invalid fields
    errors = exc.errors()
    first_msg = errors[0].get("msg", "Validation error") if errors else "Invalid request data"
    first_field = ".".join(str(loc) for loc in errors[0].get("loc", []) if loc != "body") if errors else ""
    detail_str = f"Missing or invalid field: {first_field} ({first_msg})" if first_field else first_msg
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={"detail": detail_str}
    )


# ---------------------------------------------------------
# API Endpoints
# ---------------------------------------------------------

@app.get("/api/events", response_model=List[EventOut], summary="List events with live available seats")
def get_events(
    category: Optional[str] = Query(None, description="Filter by event category"),
    search: Optional[str] = Query(None, description="Case-insensitive search on title")
):
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT e.*, 
               (e.capacity - (SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.id)) AS available_seats
        FROM events e
        WHERE 1=1
    """
    params = []

    if category and category.strip() and category.strip().lower() != "all":
        query += " AND LOWER(e.category) = LOWER(?)"
        params.append(category.strip())

    if search and search.strip():
        query += " AND LOWER(e.title) LIKE LOWER(?)"
        params.append(f"%{search.strip()}%")

    query += " ORDER BY e.event_date ASC, e.event_time ASC"

    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    events = []
    for r in rows:
        avail = max(0, r["available_seats"])
        events.append(EventOut(
            id=r["id"],
            title=r["title"],
            category=r["category"],
            description=r["description"],
            event_date=r["event_date"],
            event_time=r["event_time"],
            venue=r["venue"],
            capacity=r["capacity"],
            fee=r["fee"],
            icon=r["icon"],
            available_seats=avail
        ))
    return events


@app.get("/api/events/{event_id}", response_model=EventDetail, summary="Get details for one event")
def get_event_detail(event_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("""
        SELECT e.*, 
               (e.capacity - (SELECT COUNT(*) FROM registrations r WHERE r.event_id = e.id)) AS available_seats
        FROM events e
        WHERE e.id = ?
    """, (event_id,))
    row = cursor.fetchone()
    conn.close()

    if not row:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} was not found."
        )

    return EventDetail(
        id=row["id"],
        title=row["title"],
        category=row["category"],
        description=row["description"],
        event_date=row["event_date"],
        event_time=row["event_time"],
        venue=row["venue"],
        capacity=row["capacity"],
        fee=row["fee"],
        icon=row["icon"],
        available_seats=max(0, row["available_seats"])
    )


@app.post(
    "/api/events/{event_id}/register",
    response_model=RegistrationOut,
    status_code=status.HTTP_201_CREATED,
    summary="Register a student for an event"
)
def register_for_event(event_id: int, reg: RegistrationIn):
    # Server-side validation
    name = reg.student_name.strip()
    roll = reg.roll_number.strip()
    email = reg.email.strip().lower()
    phone = reg.phone.strip() if reg.phone else None

    if not name:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Student name cannot be empty.")
    if not roll:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Roll number cannot be empty.")
    if not email:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Email cannot be empty.")
    if "@" not in email or email.startswith("@") or email.endswith("@"):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Invalid email format. Must contain '@'.")

    conn = get_db_connection()
    cursor = conn.cursor()

    # 1. Event existence check
    cursor.execute("SELECT id, title, capacity FROM events WHERE id = ?", (event_id,))
    event_row = cursor.fetchone()
    if not event_row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Event with id {event_id} does not exist."
        )

    # 2. Check if user already registered for this event
    cursor.execute("SELECT id FROM registrations WHERE event_id = ? AND LOWER(email) = LOWER(?)", (event_id, email))
    existing_reg = cursor.fetchone()
    if existing_reg:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Already registered for this event with this email address."
        )

    # 3. Check live capacity
    cursor.execute("SELECT COUNT(*) AS reg_count FROM registrations WHERE event_id = ?", (event_id,))
    reg_count = cursor.fetchone()["reg_count"]
    if reg_count >= event_row["capacity"]:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Event is full. No available seats remaining."
        )

    # 4. Insert registration with server-generated timestamp
    registered_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    cursor.execute("""
        INSERT INTO registrations (event_id, student_name, roll_number, email, phone, registered_at)
        VALUES (?, ?, ?, ?, ?, ?)
    """, (event_id, name, roll, email, phone, registered_at))
    conn.commit()
    new_reg_id = cursor.lastrowid
    conn.close()

    return RegistrationOut(
        id=new_reg_id,
        event_id=event_id,
        student_name=name,
        registered_at=registered_at
    )


@app.get("/api/registrations", response_model=List[RegistrationDetail], summary="List registrations")
def get_registrations(event_id: Optional[int] = Query(None, description="Optional filter by event ID")):
    conn = get_db_connection()
    cursor = conn.cursor()

    query = """
        SELECT r.id, r.event_id, e.title AS event_title, r.student_name, 
               r.roll_number, r.email, r.phone, r.registered_at
        FROM registrations r
        JOIN events e ON r.event_id = e.id
        WHERE 1=1
    """
    params = []
    if event_id is not None:
        query += " AND r.event_id = ?"
        params.append(event_id)

    query += " ORDER BY r.id DESC"
    cursor.execute(query, params)
    rows = cursor.fetchall()
    conn.close()

    return [
        RegistrationDetail(
            id=r["id"],
            event_id=r["event_id"],
            event_title=r["event_title"],
            student_name=r["student_name"],
            roll_number=r["roll_number"],
            email=r["email"],
            phone=r["phone"],
            registered_at=r["registered_at"]
        )
        for r in rows
    ]


@app.get("/api/stats", response_model=StatsOut, summary="Live computed fest statistics")
def get_stats():
    conn = get_db_connection()
    cursor = conn.cursor()

    # Total events and total capacity
    cursor.execute("SELECT COUNT(*) AS total_events, COALESCE(SUM(capacity), 0) AS total_capacity FROM events")
    event_stats = cursor.fetchone()
    total_events = event_stats["total_events"]
    total_capacity = event_stats["total_capacity"]

    # Total registrations
    cursor.execute("SELECT COUNT(*) AS total_registrations FROM registrations")
    total_registrations = cursor.fetchone()["total_registrations"]

    # Utilization %
    if total_capacity > 0:
        overall_utilization_pct = round((total_registrations / total_capacity) * 100, 1)
    else:
        overall_utilization_pct = 0.0

    # Upcoming events count (event_date >= today)
    today_str = date.today().isoformat()
    cursor.execute("SELECT COUNT(*) AS upcoming_cnt FROM events WHERE event_date >= ?", (today_str,))
    upcoming_events_count = cursor.fetchone()["upcoming_cnt"]

    # Events by category
    cursor.execute("SELECT category, COUNT(*) AS cnt FROM events GROUP BY category ORDER BY cnt DESC")
    cat_rows = cursor.fetchall()
    events_by_category = {r["category"]: r["cnt"] for r in cat_rows}

    conn.close()

    return StatsOut(
        total_events=total_events,
        total_registrations=total_registrations,
        total_capacity=total_capacity,
        overall_utilization_pct=overall_utilization_pct,
        upcoming_events_count=upcoming_events_count,
        events_by_category=events_by_category
    )


# ---------------------------------------------------------
# Stretch Goal Endpoints: Admin CRUD
# ---------------------------------------------------------

@app.post(
    "/api/events",
    response_model=EventDetail,
    status_code=status.HTTP_201_CREATED,
    summary="Admin stretch goal: Create a new event"
)
def create_event(ev: EventCreateIn):
    title = ev.title.strip()
    category = ev.category.strip()
    date_str = ev.event_date.strip()
    time_str = ev.event_time.strip()
    venue = ev.venue.strip()

    if not title or not category or not date_str or not time_str or not venue:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Title, category, date, time, and venue are all required."
        )
    if ev.capacity <= 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Capacity must be greater than 0."
        )

    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("""
        INSERT INTO events (title, category, description, event_date, event_time, venue, capacity, fee, icon)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, (
        title,
        category,
        ev.description.strip() if ev.description else None,
        date_str,
        time_str,
        venue,
        ev.capacity,
        ev.fee,
        ev.icon.strip() if ev.icon else "🎉"
    ))
    conn.commit()
    new_id = cursor.lastrowid
    conn.close()

    return EventDetail(
        id=new_id,
        title=title,
        category=category,
        description=ev.description,
        event_date=date_str,
        event_time=time_str,
        venue=venue,
        capacity=ev.capacity,
        fee=ev.fee,
        icon=ev.icon or "🎉",
        available_seats=ev.capacity
    )


@app.delete(
    "/api/registrations/{registration_id}",
    summary="Admin stretch goal: Cancel a registration"
)
def delete_registration(registration_id: int):
    conn = get_db_connection()
    cursor = conn.cursor()

    cursor.execute("SELECT id, event_id, student_name FROM registrations WHERE id = ?", (registration_id,))
    row = cursor.fetchone()
    if not row:
        conn.close()
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Registration {registration_id} not found."
        )

    cursor.execute("DELETE FROM registrations WHERE id = ?", (registration_id,))
    conn.commit()
    conn.close()

    return {
        "message": "Registration cancelled successfully.",
        "id": registration_id,
        "event_id": row["event_id"],
        "student_name": row["student_name"]
    }


# ---------------------------------------------------------
# Static File Mounts (Step 4)
# ---------------------------------------------------------

@app.api_route("/", methods=["GET", "HEAD"], include_in_schema=False)
def serve_root():
    index_file = FRONTEND_DIR / "index.html"
    if index_file.exists():
        return FileResponse(index_file)
    return {"message": "Frontend index.html not yet created."}


@app.api_route("/admin.html", methods=["GET", "HEAD"], include_in_schema=False)
def serve_admin():
    admin_file = FRONTEND_DIR / "admin.html"
    if admin_file.exists():
        return FileResponse(admin_file)
    return {"message": "Frontend admin.html not yet created."}


# Mount js folder so files are accessible at /js/app.js, /js/admin.js
app.mount("/js", StaticFiles(directory=FRONTEND_DIR / "js"), name="js")
# Fallback mount for any extra frontend assets
app.mount("/static", StaticFiles(directory=FRONTEND_DIR), name="static")
