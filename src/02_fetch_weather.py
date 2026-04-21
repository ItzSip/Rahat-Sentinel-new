import pandas as pd
import requests
import os
import time
from datetime import timedelta

# -- Paths -------------------------------------------------------------------
INPUT_FILE  = "../data/01_raw/himalayas_real.csv"   # NASA-cleaned dataset
OUTPUT_DIR  = "../data/02_interim"
OUTPUT_FILE = f"{OUTPUT_DIR}/weather_added.csv"

os.makedirs(OUTPUT_DIR, exist_ok=True)

# -- Load data ---------------------------------------------------------------
print("Loading cleaned NASA data...")
df_full = pd.read_csv(INPUT_FILE)
print(f"Total rows available: {len(df_full)}")

# ============================================================
# TEST MODE — set to True to run on 50 rows only
# Set to False AFTER the test succeeds to run full 1104
# ============================================================
TEST_MODE = False
TEST_SIZE = 50

if TEST_MODE:
    df = df_full.head(TEST_SIZE).copy()
    print(f"\n[TEST MODE] Running on {len(df)} rows only.")
    print("Set TEST_MODE = False to run full dataset.\n")
else:
    df = df_full.copy()
    print(f"\n[FULL MODE] Running on all {len(df)} rows.\n")

# -- Weather fetch loop ------------------------------------------------------
rain_3d_list = []
rain_7d_list = []
total = len(df)

print(f"Fetching weather from Open-Meteo ({total} API calls)...")
print("-" * 50)

for i, (index, row) in enumerate(df.iterrows()):
    lat = row['latitude']
    lon = row['longitude']

    event_date = pd.to_datetime(row['date'])
    start_date = (event_date - timedelta(days=7)).strftime('%Y-%m-%d')
    end_date   = event_date.strftime('%Y-%m-%d')

    url = (
        f"https://archive-api.open-meteo.com/v1/archive"
        f"?latitude={lat}&longitude={lon}"
        f"&start_date={start_date}&end_date={end_date}"
        f"&daily=precipitation_sum&timezone=auto"
    )

    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        data = response.json()

        daily_rain = data['daily']['precipitation_sum']
        rain_7d = sum(r for r in daily_rain        if r is not None)
        rain_3d = sum(r for r in daily_rain[-3:]   if r is not None)

        rain_7d_list.append(round(rain_7d, 2))
        rain_3d_list.append(round(rain_3d, 2))

        print(f"[{i+1}/{total}] OK  lat={lat:.4f} lon={lon:.4f}  "
              f"3d={rain_3d:.1f}mm  7d={rain_7d:.1f}mm")

    except Exception as e:
        print(f"[{i+1}/{total}] ERROR: {e}  -> filling 0")
        rain_7d_list.append(0)
        rain_3d_list.append(0)

    # Rate-limit guard — 0.3s between calls
    time.sleep(0.3)

# -- Assemble output ---------------------------------------------------------
df['rain_3d_mm'] = rain_3d_list
df['rain_7d_mm'] = rain_7d_list

print("-" * 50)
print(f"\nFinal columns: {list(df.columns)}")
print(f"Output shape : {df.shape}")
print("\nSample output:")
print(df[['latitude', 'longitude', 'date', 'rain_3d_mm', 'rain_7d_mm']].head(5).to_string())

# -- Save --------------------------------------------------------------------
df.to_csv(OUTPUT_FILE, index=False)

print(f"\nSaved file exists : {os.path.exists(OUTPUT_FILE)}")
print(f"Output path       : {os.path.abspath(OUTPUT_FILE)}")

mode_label = "test (50 rows)" if TEST_MODE else f"full ({len(df)} rows)"
print(f"\nDONE [{mode_label}] -> {OUTPUT_FILE}")

if TEST_MODE:
    print("\n*** TEST PASSED — next step ***")
    print("1. Open 02_fetch_weather.py")
    print("2. Set TEST_MODE = False")
    print("3. Re-run for all 1104 rows")