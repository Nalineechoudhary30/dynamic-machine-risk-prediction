# Local ML service

This small FastAPI service trains/loads a local scikit-learn decision tree. From this directory, create and activate a virtual environment, install `requirements.txt`, then run `python train_model.py` and `uvicorn app:app --reload --port 8000`. The model file is generated locally and ignored by Git.

`POST /predict` accepts temperature, pressure and vibration (`Low`, `Medium`, or `High`) and returns the predicted risk. `GET /health` reports whether the model is loaded.
