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

