# Rahat Sentinel - Project Status Tracker

This document cleanly tracks the implementation state of the Rahat Sentinel system for the hackathon. 

---

## 1. Backend (FastAPI + DB)

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| `/predict` endpoint | Backend Team | ✅ DONE | Normalizes severity, deduplicates, and hashes polygons. |
| Redis integration | Backend Team | ✅ DONE | Try/except error resilient, `<512-byte` compressed payload. |
| WebSocket pipeline | Backend Team | ✅ DONE | `lifespan` hook bridging Redis -> WS instantly. |
| Geo queries | Backend Team | ✅ DONE | PostGIS `ST_DWithin` with Geography type casting. |
| Demo endpoints | Backend Team | ✅ DONE | `/demo/trigger` (BackgroundTasks) and `/debug/redis-test`. |

---

## 2. Real-Time System (Redis + WebSocket)

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| Redis pub/sub | Backend Team | ✅ DONE | `sentinel:alerts` channel actively routes JSON streams. |
| WebSocket broadcasting | Backend Team | ✅ DONE | Connection manager tracks multiple active clients. |
| Debug pipeline | Backend Team | ✅ DONE | `test_ws_client.py` auto-reconnects and logs events. |

---

## 3. Mobile App (React Native)

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| WebSocket client | Mobile Team | 🟡 IN PROGRESS | Connecting `SentinelService` to `ws://localhost:8000/ws/alerts`. |
| Alert UI | UI/UX Team | 🟡 IN PROGRESS | Popups and Map overlays being drafted. |
| Storage | Mobile Team | 🟡 IN PROGRESS | Integrating `AsyncStorage` with Zustand `alertStore`. |

---

## 4. BLE Mesh System

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| Broadcast logic | BLE Team | 🟡 IN PROGRESS | Adding `CLIMATE_ALERT` messaging format alongside SOS. |
| Scan + receive | BLE Team | 🟡 IN PROGRESS | Extracting Sentinel hashes and routing to `AlertManager`. |
| Mesh relay | BLE Team | ⚠️ NEEDS TESTING | Time-to-Live (TTL) rebroadcast bounding logic. |

---

## 5. ML Pipeline

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| Model training | ML Team | ✅ DONE | ConvLSTM Earth Engine datasets validated. |
| ONNX export | ML Team | ✅ DONE | Model optimized for fast inference routing. |
| Integration | ML Team | 🟡 IN PROGRESS | Bridging ONNX outputs to the FastAPI `POST /predict`. |

---

## 6. UI / Frontend

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| Geospatial Map View | UI/UX Team | 🟡 IN PROGRESS | Red overlays indicating severity bounds. |
| Nearby Alert Badges | UI/UX Team | 🟡 IN PROGRESS | "HIGH RISK" visual cues on Bluetooth targets. |
| Web Dashboard | Frontend Team | ❌ NOT STARTED | Admin panel for government monitors (if applicable). |

---

## 7. Demo Readiness

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| End-to-end flow | All | 🟡 IN PROGRESS | ML -> DB -> Redis -> WS -> Mobile -> BLE cascade. |
| 2-device BLE test | QA / Mobile | ⚠️ NEEDS TESTING | Checking if Device B receives Device A's offline relay. |
| Hackathon Pitch | Team Lead | 🟡 IN PROGRESS | Finalizing slide deck and problem/solution hooks. |

---

## 8. Documentation

| Feature | Owner | Status | Notes |
| ------- | ----- | ------ | ----- |
| Root README | Technical Writer | ✅ DONE | Humanized story-driven pitch finalized. |
| Backend Walkthrough | Backend Team | ✅ DONE | cURL commands and architecture mapped perfectly. |
| Setup configurations | Backend Team | ✅ DONE | `config.py` centrally injected into `database.py`. |

---

## WHAT I CAN PICK UP NEXT

**Pending Tasks:**
* Finish binding the React Native `SentinelService` WebSocket to global React state.
* Inject `AsyncStorage` into `useAlertStore.ts` to retain alarms through app reboots.
* Route the parsed WebSocket payload straight into the Bluetooth Mesh adapter.

**High Priority Fixes:**
* Ensure Mobile App handles WebSocket drops (auto-reconnect every 2-5 seconds).
* Prevent the `AlertManager` from overriding or blocking native SOS Mesh signals.

**Demo Risks:**
* If the 2-device BLE relay fails, rely heavily on the `/demo/trigger` and WebSocket live logs to prove pipeline routing to judges. Ensure mobile background tasks keep JS listeners alive during demo.
