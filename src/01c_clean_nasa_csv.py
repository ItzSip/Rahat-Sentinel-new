import pandas as pd
import os

print("Script started")

# -- Paths -------------------------------------------------------------------
INPUT_CSV  = "../data/01_raw/nasa_global_raw.csv"
OUTPUT_CSV = "../data/01_raw/himalayas_real.csv"

print("Input  :", INPUT_CSV)
print("Output :", OUTPUT_CSV)

# -- Step 1: Load ------------------------------------------------------------
print("\nOpening:", INPUT_CSV)
df = pd.read_csv(INPUT_CSV)
print("Loaded", len(df), "total global rows.")

# -- Step 2: Schema inspection (CRITICAL) ------------------------------------
print("\nColumns:", list(df.columns))

# Auto-detect the date column
DATE_COL = None
for candidate in ['event_date', 'eventDate', 'date']:
    if candidate in df.columns:
        DATE_COL = candidate
        break

if DATE_COL is None:
    print("FATAL: No date column found! Available columns:", list(df.columns))
    raise SystemExit(1)

print("Using date column:", DATE_COL)

# -- Step 3: Geographic filter -----------------------------------------------
print("\nApplying Himalayan bounding box filter...")
print("Before filter:", len(df), "rows")

df_himalayas = df[
    (df['latitude'] >= 25.0) & (df['latitude'] <= 35.0) &
    (df['longitude'] >= 70.0) & (df['longitude'] <= 85.0)
].copy()

print("Filtered rows:", len(df_himalayas))

if len(df_himalayas) == 0:
    print("WARNING: Bounding box returned 0 rows!")
    print("Latitude  range in data:", df['latitude'].min(), "->", df['latitude'].max())
    print("Longitude range in data:", df['longitude'].min(), "->", df['longitude'].max())
    raise SystemExit(1)

# -- Step 4: Select & clean columns ------------------------------------------
print("\nKeeping required columns...")
df_clean = df_himalayas[['latitude', 'longitude', DATE_COL]].copy()
df_clean = df_clean.dropna(subset=['latitude', 'longitude', DATE_COL])
print("After dropna:", len(df_clean), "rows")

# -- Step 5: Date parsing ----------------------------------------------------
print("\nParsing dates...")
df_clean['date'] = pd.to_datetime(df_clean[DATE_COL], errors='coerce')

print("Sample parsed dates:")
print(df_clean['date'].head(5).to_string())

broken = df_clean['date'].isna().sum()
print("Broken dates (NaT):", broken)

df_clean = df_clean.dropna(subset=['date'])
df_clean['date'] = df_clean['date'].dt.strftime('%Y-%m-%d')

# -- Step 6: Add label & finalise --------------------------------------------
df_clean['is_landslide'] = 1

if DATE_COL != 'date':
    df_clean = df_clean.drop(columns=[DATE_COL])

df_clean = df_clean[['latitude', 'longitude', 'date', 'is_landslide']]

print("\nFinal dataset shape:", df_clean.shape)
print("Preview:")
print(df_clean.head(3).to_string())

# -- Step 7: Save & verify ---------------------------------------------------
os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)
df_clean.to_csv(OUTPUT_CSV, index=False)

print("\nSaved file exists:", os.path.exists(OUTPUT_CSV))
print("Output path:", os.path.abspath(OUTPUT_CSV))
print("\nDONE!", len(df_clean), "verified Himalayan landslides saved.")
print("Ready to run the Weather Injector (02_fetch_weather.py)")
