import os
import shutil
import sqlite3
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent

# Detect Vercel serverless environment (root filesystem is read-only)
IS_VERCEL = os.environ.get("VERCEL") == "1" or os.environ.get("AWS_LAMBDA_FUNCTION_NAME") is not None

if IS_VERCEL:
    # Use writable /tmp directory on Vercel
    DB_PATH = Path("/tmp") / "fest.db"
    ORIGINAL_DB = PROJECT_ROOT / "fest.db"
    if not DB_PATH.exists() and ORIGINAL_DB.exists():
        try:
            shutil.copy2(ORIGINAL_DB, DB_PATH)
        except Exception as e:
            print(f"Notice copying fest.db to /tmp: {e}")
else:
    DB_PATH = PROJECT_ROOT / "fest.db"


def get_db_connection():
    """Returns a SQLite connection with row factory configured."""
    if IS_VERCEL and not DB_PATH.exists():
        ORIGINAL_DB = PROJECT_ROOT / "fest.db"
        if ORIGINAL_DB.exists():
            try:
                shutil.copy2(ORIGINAL_DB, DB_PATH)
            except Exception as e:
                print(f"Notice copying fest.db to /tmp: {e}")

    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


def init_db():
    """Initializes the database schema if tables do not already exist."""
    conn = get_db_connection()
    cursor = conn.cursor()

    # Events table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            category TEXT NOT NULL,
            description TEXT,
            event_date TEXT NOT NULL,
            event_time TEXT NOT NULL,
            venue TEXT NOT NULL,
            capacity INTEGER NOT NULL,
            fee INTEGER NOT NULL DEFAULT 0,
            icon TEXT
        )
    """)

    # Registrations table
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS registrations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            event_id INTEGER NOT NULL REFERENCES events(id) ON DELETE CASCADE,
            student_name TEXT NOT NULL,
            roll_number TEXT NOT NULL,
            email TEXT NOT NULL,
            phone TEXT,
            registered_at TEXT NOT NULL,
            UNIQUE(event_id, email)
        )
    """)

    conn.commit()
    conn.close()


if __name__ == "__main__":
    init_db()
    print("Database initialized successfully at", DB_PATH)
