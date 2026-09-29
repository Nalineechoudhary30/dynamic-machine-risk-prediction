# Dynamic Machine Data Management & Local Risk Prediction

A local full-stack assessment project for configuring machine fields, maintaining machine records, and predicting machine risk with a small scikit-learn model.

## Features and stack

The UI supports dynamic TEXT, NUMBER and DROPDOWN fields, machine create/read/update/delete, and local risk prediction. It uses Java 17, Spring Boot 3, Spring Data JPA, MySQL, React, Vite, Python 3, FastAPI, pandas, scikit-learn and joblib.

## Structure

- `backend/` — Java 17, Spring Boot, Spring Data JPA and MySQL REST API.
- `frontend/` — React and Vite dashboard.
- `ml-service/` — FastAPI service that trains, loads and calls a local decision tree.
- `database/schema.sql` — equivalent MySQL table definitions.

## Architecture

```text
React UI  --REST-->  Spring Boot API  --JPA-->  MySQL
                            |
                            +--HTTP-->  FastAPI  -->  scikit-learn model
```

## Requirements

For the easiest setup on another computer, install Docker Desktop (Windows or Mac) or Docker Engine with the Docker Compose plugin (Linux). The container setup supplies MySQL, Python, Java, Node.js, and the web server; those runtimes do not need to be installed separately.

For manual development outside Docker, install Java 17, Maven, Node.js 18+, Python 3.10+, and MySQL 8+. Start MySQL, then create the database with `Get-Content database/schema.sql | mysql -u root -p` in PowerShell (or let the configured JDBC URL create it). Copy `backend/src/main/resources/application.properties.example` to `backend/src/main/resources/application.properties`, then set your local database username and password. This local properties file is ignored by Git.

## Run the full app with Docker (recommended)

From the repository root, create a local environment file and set two database passwords in it:

```powershell
Copy-Item .env.example .env
notepad .env
```

Change `MYSQL_PASSWORD` and `MYSQL_ROOT_PASSWORD` to local values. Then build and start the complete application:

```powershell
docker compose up --build -d
```

Open <http://localhost:5173>. The first build downloads base images and dependencies, so it can take a few minutes. Compose waits for MySQL and the Python prediction service before starting the backend. The browser uses the frontend container as a same-origin proxy to the backend, and the backend calls the Python model over the private Compose network. MySQL data is retained in a named volume across restarts.

To stop the app while preserving data, run `docker compose down`. To see service startup details, run `docker compose logs -f`. To access it from another device on the same network, open `http://<computer-running-Docker-LAN-IP>:5173` and allow inbound TCP port 5173 through that computer's firewall. This is intended for local assessment and LAN use, not public internet exposure.

Do not commit `.env`; it contains local credentials and is ignored by Git. On another computer, clone the repository, copy `.env.example` to `.env`, choose its own passwords, then run the same Compose command.

## Run locally

Open three terminals from the project root.

1. Python service:

   ```powershell
   cd ml-service
   python -m venv .venv
   .venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   python train_model.py
   uvicorn app:app --reload --port 8000
   ```

2. Spring Boot backend:

   ```powershell
   cd backend
   mvn spring-boot:run
   ```

   The first run creates the four default fields if the field table is empty.

3. React frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

Open <http://localhost:5173>. The API listens on port 8080; FastAPI listens on port 8000. Configure a different frontend API origin with `VITE_API_URL` if needed.

## Main API endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/api/fields` | List field definitions |
| GET | `/api/fields/{id}` | Get one field definition |
| POST | `/api/fields` | Add a TEXT, NUMBER or DROPDOWN field |
| PUT | `/api/fields/{id}` | Update a field definition |
| DELETE | `/api/fields/{id}` | Delete a field when no machine uses it |
| GET | `/api/machines` | List machine records and values |
| GET | `/api/machines/{id}` | Get one machine |
| POST | `/api/machines` | Create a machine |
| PUT | `/api/machines/{id}` | Replace a machine's current values |
| DELETE | `/api/machines/{id}` | Delete a machine and its values |
| POST | `/api/machines/{id}/predict` | Predict risk using the local ML service |

Create a machine with `{"values":{"Machine Name":"CNC-001","Temperature":85,"Pressure":120,"Vibration":"High"}}`. Add a field with `{"fieldName":"Humidity","fieldType":"NUMBER","required":false,"dropdownOptions":[]}`.

After the Python service starts, test a prediction in PowerShell with:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8000/predict -ContentType 'application/json' -Body '{"temperature":85,"pressure":120,"vibration":"High"}'
```

## Dynamic field storage

Field definitions, including dropdown choices, live in `machine_fields`. Each entered property is stored as one row in `machine_values`, related to a machine and its field definition. Values use text storage, with type validation against the current definition. Adding Humidity creates a field record; it does not add a SQL column or require a backend/frontend code change. Machine Name is stored as a dynamic value in the same way as other properties.

## How prediction works

The backend reads Temperature, Pressure and Vibration from the saved machine values, then calls `POST http://localhost:8000/predict`. The Python service maps vibration labels to 0/1/2 and calls `model.predict()` on a scikit-learn `DecisionTreeClassifier`. Its training examples are synthetic and live in `ml-service/training_data.csv`. Humidity is stored as a normal dynamic value and is intentionally not used by this model.

## Verification

Run backend tests with `cd backend; mvn test`, frontend build with `cd frontend; npm run build`, and Python model training with `cd ml-service; python train_model.py`. FastAPI exposes `/health` and `/docs` for local inspection.
