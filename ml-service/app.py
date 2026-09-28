from contextlib import asynccontextmanager
from pathlib import Path
from typing import Literal

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

BASE_DIR = Path(__file__).resolve().parent
VIBRATION_LEVELS = {"Low": 0, "Medium": 1, "High": 2}
model = None


@asynccontextmanager
async def lifespan(_: FastAPI):
    global model
    model_path = BASE_DIR / "model.joblib"
    if not model_path.exists():
        from train_model import train_model

        train_model()
    model = joblib.load(model_path)
    yield


app = FastAPI(title="Local Machine Risk Prediction", lifespan=lifespan)


class PredictionRequest(BaseModel):
    temperature: float = Field(..., allow_inf_nan=False)
    pressure: float = Field(..., allow_inf_nan=False)
    vibration: Literal["Low", "Medium", "High"]


@app.get("/health")
def health():
    return {"status": "ok", "modelLoaded": model is not None}


@app.post("/predict")
def predict(request: PredictionRequest):
    if model is None:
        raise HTTPException(status_code=503, detail="Model is not ready")
    vibration_value = VIBRATION_LEVELS[request.vibration]
    features = pd.DataFrame(
        [[request.temperature, request.pressure, vibration_value]],
        columns=["temperature", "pressure", "vibration"],
    )
    risk = model.predict(features)[0]
    return {"risk": str(risk)}
