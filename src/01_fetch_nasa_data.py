import pandas as pd
import os

# 1. The direct download link to NASA's Global Landslide Catalog
NASA_CSV_URL = "https://data.nasa.gov/api/views/dd9e-wu2v/rows.csv?accessType=DOWNLOAD"
OUTPUT_CSV = "../data/01_raw/nasa_himalayas.csv"

# Make sure our raw data folder exists
os.makedirs(os.path.dirname(OUTPUT_CSV), exist_ok=True)

print("🛰️ Connecting to NASA servers (this might take a minute, it's a big file)...")

try:
    # 2. Let Pandas download the CSV directly from the internet
    df = pd.read_csv(NASA_CSV_URL)
    
    print("✅ Downloaded global data. Filtering for the Himalayas...")
    
    # 3. Filter for our specific Bounding Box (Uttarakhand & Himachal)
    # Latitude: Roughly 29.0 to 33.5 (North)
    # Longitude: Roughly 75.0 to 81.0 (East)
    himalaya_filter = (
        (df['latitude'] >= 29.0) & (df['latitude'] <= 33.5) &
        (df['longitude'] >= 75.0) & (df['longitude'] <= 81.0)
    )
    
    himalayas_df = df[himalaya_filter].copy()
    
    # 4. Clean up the NASA columns to match our pipeline
    # NASA uses 'event_date', we just want 'date'. 
    # We will format it to standard YYYY-MM-DD
    himalayas_df['date'] = pd.to_datetime(himalayas_df['event_date']).dt.strftime('%Y-%m-%d')
    himalayas_df['is_landslide'] = 1 # All of these are confirmed landslides
    
    # 5. Keep only the columns we need
    final_df = himalayas_df[['latitude', 'longitude', 'date', 'is_landslide']]
    
    # Drop any rows where the date or location is missing
    final_df = final_df.dropna()
    
    # 6. Save it to our freezer
    final_df.to_csv(OUTPUT_CSV, index=False)
    
    print(f"\n🎉 BOOM! Successfully extracted {len(final_df)} real Himalayan landslides from NASA.")
    print(f"Data saved to: {OUTPUT_CSV}")
    print("You are now ready to run your Weather script on this new file!")

except Exception as e:
    print(f"❌ Error fetching from NASA: {e}")