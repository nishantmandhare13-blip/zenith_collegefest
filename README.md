# ⚡ Zenith '26 — College Fest Management System

A full-stack, production-grade College Fest Management web application built for the **Product Management II (PM2-01 & PM2-02)** Capstone Assignment at **HSB**.

Designed around the core principle of **"Never Trust the Client"** and client-server architecture, Zenith '26 demonstrates how the server acts as the single source of truth for business logic, capacity rules, and registration integrity.

---

## 🛠️ Technology Stack

| Layer | Technology | Role & Architecture |
|---|---|---|
| **Frontend** | HTML5, Tailwind CSS (Play CDN), Vanilla JavaScript (ES6+) | Responsive student event portal (`index.html`), interactive modal registration, and real-time operations dashboard (`admin.html`). |
| **Backend** | Python 3.13, FastAPI, Uvicorn | High-performance RESTful API service, server-side data validation, static asset serving, and CORS middleware. |
| **Data Layer** | SQLite (`fest.db`), OpenPyXL | Relational database schema with foreign key constraints, live capacity computation, and automated Excel spreadsheet seeder (`events_seed.xlsx`). |
| **API Testing** | Postman | Automated collection (`zenith26-api.postman_collection.json`) and 18-point verification guide (`Postman-Testing-Commands-Zenith26.md`). |

---

## 📂 Project Architecture

```
zenith26-fest-system/
├── backend/
│   ├── database.py       # SQLite connection helper & schema creation
│   ├── models.py         # Pydantic request/response schemas & data contracts
│   ├── seed_data.py      # OpenPyXL seeder reading data/events_seed.xlsx
│   └── main.py           # FastAPI app, REST endpoints & static file mounting
├── frontend/
│   ├── index.html        # Student portal: hero, live search, categories & cards
│   ├── admin.html        # Fest admin dashboard: stat cards, demand sorting & logs
│   └── js/
│       ├── app.js        # Student UI logic, modal controls, API integration
│       └── admin.js      # Admin dashboard metrics, table rendering & CRUD
├── data/
│   └── events_seed.xlsx  # 14 Fest events master spreadsheet
├── fest.db               # SQLite database file (created on initial launch)
├── zenith26-api.postman_collection.json  # Postman test collection
├── Postman-Testing-Commands-Zenith26.md   # 18-request testing guide
├── requirements.txt      # Python dependencies
└── README.md             # Project documentation
```

---

## 🚀 How to Run the Application

### 1. Set Up the Python Virtual Environment
Open your terminal inside the `zenith26-fest-system` directory:
```bash
# Windows PowerShell
python -m venv .venv
.\.venv\Scripts\Activate.ps1

# Install requirements
pip install -r requirements.txt
```

### 2. Seed the Database
The database seeds automatically upon server start, or you can run the seeder manually:
```bash
python -m backend.seed_data
```

### 3. Start the FastAPI Server
```bash
uvicorn backend.main:app --reload --port 8000
```

### 4. Open in Your Browser
- **Student Event Portal:** [http://localhost:8000/](http://localhost:8000/)
- **Admin Operations Dashboard:** [http://localhost:8000/admin.html](http://localhost:8000/admin.html)
- **Interactive Swagger API Docs:** [http://localhost:8000/docs](http://localhost:8000/docs)
- **ReDoc Documentation:** [http://localhost:8000/redoc](http://localhost:8000/redoc)

---

## 📡 API Contract & Endpoints

| Method | Endpoint | Description | Status Codes |
|---|---|---|---|
| `GET` | `/api/events` | List all events with live `available_seats`. Supports `?category=` and `?search=`. | `200 OK` |
| `GET` | `/api/events/{id}` | Get complete details for a single event. | `200 OK`, `404 Not Found` |
| `POST` | `/api/events/{id}/register` | Register a student for an event with strict server-side validation. | `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| `GET` | `/api/registrations` | List all registrations with event titles. Supports `?event_id=`. | `200 OK` |
| `GET` | `/api/stats` | Live computed festival statistics (events count, registrations, capacity, utilization %). | `200 OK` |
| `POST` | `/api/events` | *(Stretch Goal)* Admin creates a new festival event. | `201 Created`, `400 Bad Request` |
| `DELETE`| `/api/registrations/{id}`| *(Stretch Goal)* Admin cancels a registration, restoring seat availability. | `200 OK`, `404 Not Found` |

---

## 🛡️ "Never Trust the Client" Validation Rules

The backend server strictly enforces these rules independently of any browser checks:
1. **Event Existence:** Verifies the `event_id` exists in the database (`404 Not Found` if missing).
2. **Mandatory Fields:** `student_name`, `roll_number`, and `email` cannot be empty or whitespace (`400 Bad Request`).
3. **Email Formatting:** Verifies the email contains an `@` symbol (`400 Bad Request`).
4. **Capacity Protection:** Checks that live registrations have not reached the event capacity (`409 Conflict: Event is full`).
5. **No Duplicate Bookings:** Checks that the same email cannot register for the same event twice (`409 Conflict: Already registered`).

---

## 🧪 Postman API Testing

1. Open Postman and import `zenith26-api.postman_collection.json`.
2. Follow the test sequence in `Postman-Testing-Commands-Zenith26.md` covering all 18 test scenarios.
3. Test success cases (`200`, `201`) and deliberate failure edge cases (`400`, `404`, `409`).
