# Branchmaster - Docker Setup

This project includes:
- **Frontend** (React / Vite)
- **Backend** (Spring Boot)
- **Database** (PostgreSQL)

All components are run together using **Docker Compose** with a single command.

---
## Prerequisites

You need the following installed:
- Docker (Docker Desktop recommended)
- Docker Compose (included with Docker Desktop)
---

## How to Run the Application

From the project root directory, run:

docker compose up --build

This command will:
- Build the frontend
- Build the backend
- Start PostgreSQL
- Start the backend service
- Start the frontend service

---

## Application URLs

- **Frontend:** http://localhost:5173
- **Backend:** http://localhost:8080
- **Database:** localhost:5432

Ensure no other applications are using these ports.

---

## Admin Login Credentials

Use the following credentials to log in as an administrator:

- **Username:** admin@local
- **Password:** admin123

---

## Database Configuration

The application uses PostgreSQL with the following defaults:

- Database name: branchmaster
- Username: branchmaster
- Password: branchmaster
- Port: 5432

The database schema and tables are created automatically on startup using **Flyway migrations**.
No manual database setup is required.

---