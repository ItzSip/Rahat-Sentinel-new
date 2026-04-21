from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import joblib
import requests
import numpy as np
from datetime import datetime, timedelta
import os

# 1. Initialize the Server
app = FastAPI(title="Rahat Sentinel API", description="Live Landslide Prediction Engine")

# 2. Load the AI Brain from the vault
MODEL_PATH = "../models/sentinel_v1.pkl"
try:
    model = joblib.load(MODEL_PATH)
    print("✅ Sentinel AI Loaded Successfully!")
except Exception as e:
    print(f"⚠️ Warning: Could not load model. Did you run the training script? Error: {e}")

# 3. Define what the React Native app will send us
class LocationRequest(BaseModel):
    latitude: float
    longitude: float

# Helper function: Get live weather for right now
def get_live_weather(lat: float, lon: float):
    # We look at the past 7 days up to today
    today = datetime.now()
    start_date = (today - timedelta(days=7)).strftime('%Y-%m-%d')
    end_date = today.strftime('%Y-%m-%d')
    
    url = f"https://archive-api.open-meteo.com/v1/archive?latitude={lat}&longitude={lon}&start_date={start_date}&end_date={end_date}&daily=precipitation_sum&timezone=auto"
    
    response = requests.get(url)
    if response.status_code != 200:
        raise HTTPException(status_code=500, detail="Failed to fetch weather data")
        
    data = response.json()
    daily_rain = data['daily']['precipitation_sum']
    
    rain_7d = sum(r for r in daily_rain if r is not None)
    rain_3d = sum(r for r in daily_rain[-3:] if r is not None)
    
    return rain_3d, rain_7d

# 4. The main endpoint: /predict
@app.post("/predict")
def predict_risk(request: LocationRequest):
    lat = request.latitude
    lon = request.longitude
    
    # A. Get live triggers (Weather)
    rain_3d, rain_7d = get_live_weather(lat, lon)
    
    # B. Get static features (Mock terrain until GEE is ready)
    # In production, this would ping your database for the real slope
    slope = np.random.uniform(10, 45) 
    elevation = np.random.uniform(1000, 3000)
    
    # C. Prepare the "Resume" for the AI (must match training order!)
    # Order: ['slope_degrees', 'elevation_m', 'rain_3d_mm', 'rain_7d_mm']
    features = np.array([[slope, elevation, rain_3d, rain_7d]])
    
    # D. Ask the AI for a prediction
    # predict_proba returns [Probability of Safe, Probability of Landslide]
    probabilities = model.predict_proba(features)[0]
    landslide_risk = float(probabilities[1]) # Get the % chance of disaster
    
    # E. Format the alert payload exactly how your BLE mesh needs it
    risk_level = "HIGH" if landslide_risk > 0.6 else "MODERATE" if landslide_risk > 0.3 else "SAFE"
    
    return {
        "type": "LANDSLIDE_ALERT",
        "risk_level": risk_level,
        "confidence_score": round(landslide_risk * 100, 2),
        "location": {"lat": lat, "lon": lon},
        "metrics_used": {
            "rain_3d_mm": round(rain_3d, 2),
            "rain_7d_mm": round(rain_7d, 2),
            "slope_degrees": round(slope, 1)
        }
    }