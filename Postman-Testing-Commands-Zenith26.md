# Postman Testing Guide: Zenith '26 College Fest Management System

This guide outlines every API request to test against the running FastAPI backend server (`http://localhost:8000`).

---

## 🚀 Getting Started with Postman

1. Open the **Postman desktop app** (free, no account required).
2. Click **Import** (top left).
3. Select `zenith26-api.postman_collection.json` located in your project root.
4. Verify the collection variable `baseUrl` is set to `http://localhost:8000`.
5. Ensure your FastAPI server is running:
   ```bash
   uvicorn backend.main:app --reload
   ```

---

## 📋 Comprehensive 18-Test Request Suite

### Group 1: Events Discovery (`GET /api/events`)

#### 1. Fetch All Events
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events`
- **Expected Status:** `200 OK`
- **Expected Body:** Array of 14 events. Each object includes `available_seats` equal to capacity minus existing registrations.

#### 2. Filter Events by Category (Technical)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events?category=Technical`
- **Expected Status:** `200 OK`
- **Expected Body:** Array containing only `Technical` events (Code Sprint 2.0, Robo Race, AI Pitch Fest, Web Design Rumble).

#### 3. Filter Events by Category (Cultural)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events?category=Cultural`
- **Expected Status:** `200 OK`
- **Expected Body:** Array of Cultural events (Battle of Bands, Dance Fiesta, Nukkad Natak, Fashion Fusion, Open Mic Night).

#### 4. Filter Events by Category (Gaming)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events?category=Gaming`
- **Expected Status:** `200 OK`
- **Expected Body:** Array with Valorant Showdown.

#### 5. Search by Title (`search=Sprint`)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events?search=Sprint`
- **Expected Status:** `200 OK`
- **Expected Body:** Array containing `Code Sprint 2.0`.

#### 6. Combined Search & Category
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events?category=Technical&search=design`
- **Expected Status:** `200 OK`
- **Expected Body:** Array containing `Web Design Rumble`.

#### 7. Search with Zero Matches
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events?search=NonExistentEvent`
- **Expected Status:** `200 OK`
- **Expected Body:** Empty array `[]`.

---

### Group 2: Event Details (`GET /api/events/{event_id}`)

#### 8. Valid Event Detail (ID 1)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events/1`
- **Expected Status:** `200 OK`
- **Expected Body:** Event object for Code Sprint 2.0 with description and live available seats.

#### 9. Non-Existent Event (ID 9999)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/events/9999`
- **Expected Status:** `404 Not Found`
- **Expected Body:** `{"detail": "Event with id 9999 was not found."}`

---

### Group 3: Registrations (`POST /api/events/{event_id}/register`)

#### 10. Successful Registration (201 Created)
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Nishant Mandhare",
    "roll_number": "24PM-082",
    "email": "nishant@college.edu",
    "phone": "+91 9876543210"
  }
  ```
- **Expected Status:** `201 Created`
- **Expected Body:** Object with `id`, `event_id: 1`, `student_name`, `registered_at`.

#### 11. Missing Student Name (400 Bad Request)
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "",
    "roll_number": "24PM-082",
    "email": "test@college.edu"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Body:** `{"detail": "Student name cannot be empty."}`

#### 12. Invalid Email Format without '@' (400 Bad Request)
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Aarav Sharma",
    "roll_number": "24PM-015",
    "email": "aarav_without_at_symbol"
  }
  ```
- **Expected Status:** `400 Bad Request`
- **Expected Body:** `{"detail": "Invalid email format. Must contain '@'."}`

#### 13. Registering for Non-Existent Event (404 Not Found)
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/events/9999/register`
- **Headers:** `Content-Type: application/json`
- **Body:**
  ```json
  {
    "student_name": "Priya Verma",
    "roll_number": "24PM-044",
    "email": "priya@college.edu"
  }
  ```
- **Expected Status:** `404 Not Found`
- **Expected Body:** `{"detail": "Event with id 9999 does not exist."}`

#### 14. Duplicate Registration with Same Email (409 Conflict)
- **Method:** `POST`
- **URL:** `http://localhost:8000/api/events/1/register`
- **Headers:** `Content-Type: application/json`
- **Body:** Same body as Request #10 (`nishant@college.edu`).
- **Expected Status:** `409 Conflict`
- **Expected Body:** `{"detail": "Already registered for this event with this email address."}`

---

### Group 4: Registrations Log (`GET /api/registrations`)

#### 15. List All Registrations
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/registrations`
- **Expected Status:** `200 OK`
- **Expected Body:** Array of all registrations, including `event_title`.

#### 16. Filter Registrations by Event ID
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/registrations?event_id=1`
- **Expected Status:** `200 OK`
- **Expected Body:** Array of registrations belonging only to Event 1.

---

### Group 5: Fest Analytics & Stretch CRUD

#### 17. Live Fest Statistics (`GET /api/stats`)
- **Method:** `GET`
- **URL:** `http://localhost:8000/api/stats`
- **Expected Status:** `200 OK`
- **Expected Body:**
  ```json
  {
    "total_events": 14,
    "total_registrations": 1,
    "total_capacity": 1447,
    "overall_utilization_pct": 0.1,
    "upcoming_events_count": 14,
    "events_by_category": {
      "Cultural": 5,
      "Technical": 4,
      "Sports": 2,
      "Workshop": 2,
      "Gaming": 1
    }
  }
  ```

#### 18. Admin Stretch Goal: Cancel Registration (`DELETE /api/registrations/{id}`)
- **Method:** `DELETE`
- **URL:** `http://localhost:8000/api/registrations/1`
- **Expected Status:** `200 OK`
- **Expected Body:** `{"message": "Registration cancelled successfully.", "id": 1, ...}`
- **Subsequent Check:** Re-running `GET /api/events/1` confirms `available_seats` has restored back to 100!
