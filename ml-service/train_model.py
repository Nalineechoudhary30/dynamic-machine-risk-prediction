from pathlib import Path

import joblib
import pandas as pd
from sklearn.tree import DecisionTreeClassifier

BASE_DIR = Path(__file__).resolve().parent
VIBRATION_LEVELS = {"Low": 0, "Medium": 1, "High": 2}


def train_model() -> Path:
    data = pd.read_csv(BASE_DIR / "training_data.csv")
    data["vibration"] = data["vibration"].map(VIBRATION_LEVELS)
    features = data[["temperature", "pressure", "vibration"]]
    target = data["risk"]

    model = DecisionTreeClassifier(max_depth=4, random_state=42)
    model.fit(features, target)
    output = BASE_DIR / "model.joblib"
    joblib.dump(model, output)
    return output


if __name__ == "__main__":
    print(f"Trained and saved model to {train_model()}")
