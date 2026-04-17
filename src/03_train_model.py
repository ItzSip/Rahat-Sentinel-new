import pandas as pd
import numpy as np
from xgboost import XGBClassifier
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
import joblib
import os

# 1. Setup paths
INPUT_FILE = "../data/02_interim/weather_added.csv"
MODEL_DIR = "../models"
MODEL_FILE = f"{MODEL_DIR}/sentinel_v1.pkl"

os.makedirs(MODEL_DIR, exist_ok=True)

# 2. Load our enriched REAL data
print("Loading real Himalayan data...")
df_real = pd.read_csv(INPUT_FILE)

# Clean the 36 DNS dropouts (Drop rows where rain is exactly 0 for both)
df_real = df_real[~((df_real['rain_3d_mm'] == 0) & (df_real['rain_7d_mm'] == 0))]
print(f"Kept {len(df_real)} pristine landslide events (1s).")

# 3. THE ML TRAP FIX: Generate Safe Zones (0s)
# We create 1,000 dummy days where nothing happened (low rain)
print("Generating synthetic Safe Zones (0s)...")
safe_data = {
    'latitude': np.random.uniform(25.0, 35.0, 1000),
    'longitude': np.random.uniform(70.0, 85.0, 1000),
    'date': ['2023-01-01'] * 1000, 
    'is_landslide': [0] * 1000,
    'rain_3d_mm': np.random.uniform(0, 15, 1000),  # Safe zones get very little rain
    'rain_7d_mm': np.random.uniform(0, 30, 1000)
}
df_safe = pd.DataFrame(safe_data)

# Combine the real disasters with the safe days
df = pd.concat([df_real, df_safe], ignore_index=True)

# 4. Add temporary terrain features (until GEE is ready)
print("Adding terrain physics...")
# Disasters (1s) get steep slopes. Safe zones (0s) get flat slopes.
df['slope_degrees'] = np.where(df['is_landslide'] == 1, np.random.uniform(35, 60, len(df)), np.random.uniform(0, 20, len(df)))
df['elevation_m'] = np.random.uniform(1000, 3500, len(df))

# 5. Define our Features (X) and Target (y)
features = ['slope_degrees', 'elevation_m', 'rain_3d_mm', 'rain_7d_mm']
X = df[features]
y = df['is_landslide']

# 6. Train/Test Split
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 7. Train the Sentinel Brain!
print("\nTraining XGBoost...")
model = XGBClassifier(
    n_estimators=100, 
    learning_rate=0.1, 
    max_depth=4, 
    random_state=42
)
model.fit(X_train, y_train)

# 8. Test the AI
print("\n--- AI Final Exam Results ---")
predictions = model.predict(X_test)
print(classification_report(y_test, predictions, zero_division=0))

# 9. Save the Model
joblib.dump(model, MODEL_FILE)
print(f"\n✅ Brain saved to: {MODEL_FILE}")