<<<<<<< HEAD
This is a new [**React Native**](https://reactnative.dev) project, bootstrapped using [`@react-native-community/cli`](https://github.com/react-native-community/cli).

# Getting Started

> **Note**: Make sure you have completed the [Set Up Your Environment](https://reactnative.dev/docs/set-up-your-environment) guide before proceeding.

## Step 1: Start Metro

First, you will need to run **Metro**, the JavaScript build tool for React Native.

To start the Metro dev server, run the following command from the root of your React Native project:

```sh
# Using npm
npm start

# OR using Yarn
yarn start
```

## Step 2: Build and run your app

With Metro running, open a new terminal window/pane from the root of your React Native project, and use one of the following commands to build and run your Android or iOS app:

### Android

```sh
# Using npm
npm run android

# OR using Yarn
yarn android
```

### iOS

For iOS, remember to install CocoaPods dependencies (this only needs to be run on first clone or after updating native deps).

The first time you create a new project, run the Ruby bundler to install CocoaPods itself:

```sh
bundle install
```

Then, and every time you update your native dependencies, run:

```sh
bundle exec pod install
```

For more information, please visit [CocoaPods Getting Started guide](https://guides.cocoapods.org/using/getting-started.html).

```sh
# Using npm
npm run ios

# OR using Yarn
yarn ios
```

If everything is set up correctly, you should see your new app running in the Android Emulator, iOS Simulator, or your connected device.

This is one way to run your app — you can also build it directly from Android Studio or Xcode.

## Step 3: Modify your app

Now that you have successfully run the app, let's make changes!

Open `App.tsx` in your text editor of choice and make some changes. When you save, your app will automatically update and reflect these changes — this is powered by [Fast Refresh](https://reactnative.dev/docs/fast-refresh).

When you want to forcefully reload, for example to reset the state of your app, you can perform a full reload:

- **Android**: Press the <kbd>R</kbd> key twice or select **"Reload"** from the **Dev Menu**, accessed via <kbd>Ctrl</kbd> + <kbd>M</kbd> (Windows/Linux) or <kbd>Cmd ⌘</kbd> + <kbd>M</kbd> (macOS).
- **iOS**: Press <kbd>R</kbd> in iOS Simulator.

## Congratulations! :tada:

You've successfully run and modified your React Native App. :partying_face:

### Now what?

- If you want to add this new React Native code to an existing application, check out the [Integration guide](https://reactnative.dev/docs/integration-with-existing-apps).
- If you're curious to learn more about React Native, check out the [docs](https://reactnative.dev/docs/getting-started).

# Troubleshooting

If you're having issues getting the above steps to work, see the [Troubleshooting](https://reactnative.dev/docs/troubleshooting) page.

# Learn More

To learn more about React Native, take a look at the following resources:

- [React Native Website](https://reactnative.dev) - learn more about React Native.
- [Getting Started](https://reactnative.dev/docs/environment-setup) - an **overview** of React Native and how setup your environment.
- [Learn the Basics](https://reactnative.dev/docs/getting-started) - a **guided tour** of the React Native **basics**.
- [Blog](https://reactnative.dev/blog) - read the latest official React Native **Blog** posts.
- [`@facebook/react-native`](https://github.com/facebook/react-native) - the Open Source; GitHub **repository** for React Native.
=======
# RAHAT: Emergency Mesh & Disaster Response

RAHAT is a decentralized emergency communication platform designed to provide critical assistance during disasters when cellular networks fail. This repository contains the Android implementation focusing on a resilient Bluetooth Low Energy (BLE) mesh network and location-aware safety features.

## 🚀 Key Features & Stability Improvements

### 1. Resilient BLE Mesh (Legacy Hardened)
- **Universal Discovery**: Enforced **Legacy LE_1M** advertising to ensure 100% compatibility across all Android hardware (older and specialized devices).
- **Auto-Recovery**: Integrated Bluetooth state monitoring. The mesh network automatically restarts and recovers discovery within seconds of a Bluetooth toggle.
- **Finite Scanning Windows**: Optimized discovery loop (15s on / 10s off) to maximize battery life while maintaining high-frequency peer updates.
- **Peer Lifecycle Management**: Implemented `PeerManager` with median RSSI filtering and a 60-second TTL to ensure only active, nearby devices are displayed.

### 2. Hardened Security & Identity
- **128-bit Ephemeral IDs**: Replaced basic IDs with HMAC-SHA256 derived 128-bit EphIDs, ensuring user privacy and mesh security.
- **Dynamic Rotation**: EphIDs rotate every **10 minutes** linked to the device's Keystore-backed master secret.
- **SOS Payload**: Hardened advertising payload includes explicit status flags for instant SOS recognition without requiring a full connection.

### 3. Location & Map Stability
- **Instant Map Centering**: Resolved the "Africa Default" bug. The map now recovers the user's last known location immediately on launch.
- **Centering Gate**: Implemented a one-time centering logic to prevent jitter and UI instability during movement.
- **Approximate Distance Rings**: For peers without GPS visibility, the map renders relative distance rings based on signal strength (RSSI) trends.

### 4. Safety & UX
- **Permission Gate**: A "Loud Failure" UI enforces mandatory Bluetooth and Location permissions, preventing app usage in an unsafe state.
- **Prioritized Alert Feed**: Real-time filtering and sorting of nearby devices based on SOS status and signal proximity.

## 🛠 Architecture

- **`MeshRepository`**: Single Source of Truth (SSOT) for all discovered peers, consumed by the Map, Alert Feed, and Nearby Help screens.
- **`PeerManager`**: The "Brain" that processes raw RSSI data, performs trend analysis (Approaching vs. Receding), and manages peer expiry.
- **`BleScanner` & `BleAdvertiser`**: Low-level communication layer optimized for maximum penetration and discovery speed.
- **`IdentityManager`**: Manages secure hardware-backed secrets and ephemeral ID generation.

## 📦 Building and Running

1. **Prerequisites**: Android Studio Ladybug+ and an Android device (API 26+ recommended for `AdvertisingSet` support).
2. **Build**:
   ```bash
   ./gradlew assembleDebug
   ```
3. **Install**:
   ```bash
   ./gradlew installDebug
   ```

## 📜 Recent Verification
- [x] Verified build success on multiple devices.
- [x] Confirmed 128-bit EphID discovery in local mesh logs.
- [x] Validated auto-recovery of mesh service on Bluetooth state transitions.
>>>>>>> 2c66089 (Initial commit - added Rahat Sentinel project)
# Sentinel Core

A production-oriented machine learning pipeline for geospatial intelligence and prediction, designed with scalable architecture and clean separation of concerns.

---

## 📌 Overview

Sentinel Core is structured to reflect real-world ML systems used in industry. It separates data processing, experimentation, model training, and API serving into well-defined layers, enabling maintainability and scalability.

The system integrates:

* Geospatial data (Google Earth Engine)
* Weather data APIs
* Machine learning models (XGBoost, Scikit-learn)
* API layer for deployment (FastAPI)

---

## 🏗️ Project Structure

```
sentinel-core/
│
├── data/                       # Data pipeline storage (excluded from version control)
│   ├── 01_raw/                 # Immutable raw datasets (source-of-truth, no modifications)
│   ├── 02_interim/             # Intermediate transformed datasets
│   └── 03_processed/           # Final datasets ready for model training
│
├── notebooks/                  # Exploratory analysis and experimentation
│   ├── 01_explore_data.ipynb   # Exploratory Data Analysis (EDA)
│   ├── 02_feature_eng.ipynb    # Feature engineering experiments
│   └── 03_train_xgboost.ipynb  # Model training and evaluation experiments
│
├── src/                        # Core application logic (modular and reusable)
│   ├── config.py               # Configuration management and environment loading
│   ├── fetch_gee.py            # Google Earth Engine data ingestion
│   ├── fetch_weather.py        # Weather data ingestion utilities
│   └── build_features.py       # Feature construction and data processing pipeline
│
├── api/                        # API layer for model serving
│   ├── main.py                 # FastAPI application entry point
│   ├── schemas.py              # Request/response data validation schemas
│   └── ml_service.py           # Model inference and prediction logic
│
├── models/                     # Serialized models and evaluation artifacts
│   ├── sentinel_v1.pkl         # Trained machine learning model
│   └── metrics.json            # Model performance metrics (e.g., F1-score, Recall)
│
├── .env                        # Environment variables (secrets, credentials)
├── .gitignore                  # Specifies files/directories excluded from Git
├── requirements.txt            # Project dependencies
└── README.md                   # Project documentation and setup guide

---

## ⚙️ Environment Setup

### 1. Create Virtual Environment

```bash
python -m venv venv
```

### 2. Activate Environment

**Windows**

```bash
.\venv\Scripts\activate
```

**Mac/Linux**

```bash
source venv/bin/activate
```

---

### 3. Install Dependencies

```bash
pip install -r requirements.txt
```

---

## 🔐 Environment Variables

Create a `.env` file in the root directory:

```
GEE_PROJECT_ID="your-google-cloud-project-id"
ENVIRONMENT="development"
```

---

## 🚫 .gitignore (Important)

Ensure the following are ignored:

```
venv/
__pycache__/
data/
*.csv
*.tif
.env
```

---

## 🧠 ML Workflow

1. **Data Collection**

   * Fetch geospatial data (GEE)
   * Fetch weather data (Open-Meteo)

2. **Data Processing**

   * Clean and merge datasets
   * Store in `/data/03_processed`

3. **Experimentation**

   * Use notebooks for feature engineering and model testing

4. **Model Training**

   * Train models (XGBoost, etc.)
   * Save in `/models`

5. **Serving**

   * FastAPI exposes predictions via REST endpoints

---

## 🔌 APIs Used

* **Google Earth Engine (GEE)**
  Requires approval (apply at https://earthengine.google.com)

* **Open-Meteo**
  Free weather API (no API key required)

---

## 🚀 Running the API

```bash
uvicorn api.main:app --reload
```

---

## ⚠️ Best Practices

* Do not commit:

  * `.env`
  * `data/`
  * `venv/`
* Keep notebooks for experimentation only
* Move reusable logic to `src/`
* Version models properly

---

## 📈 Goal

To build a scalable, production-grade AI pipeline capable of handling geospatial intelligence tasks and delivering predictions through a clean API interface.