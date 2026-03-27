# Rahat Sentinel - Master System Test Plan

This document outlines the end-to-end system validation checks required before the final hackathon demo. It verifies the stability of the decoupled Sentinel backend, real-time WebSocket distribution, and the core Rahat BLE propagation network.

---

## SECTION 1 — BACKEND TESTS

### 1.1 Health Check
* **Steps:** Run `curl -X GET "http://localhost:8000/health"`
* **Expected Output:** `{"status": "ok"}`
* **Pass Condition:** HTTP 200 within 50ms.

### 1.2 Alert Creation & Deduplication
* **Steps:** Run `POST /predict` twice sequentially with `severity: 8.9` and `region_id: 105`.
* **Expected Output:** `{"status": "alert_created"}`
* **Pass Condition:** Alert severity normalizes to `0.89`, and the database only stores 1 row (second request executes an `UPDATE` on the `updated_at` column instead of an `INSERT`). 

### 1.3 Demo Trigger Sprint 
* **Steps:** Call `GET /demo/trigger` via browser or cURL.
* **Expected Output:** `{"status": "success", "message": "Demo alert triggered successfully via background task."}`
* **Pass Condition:** Response must return in `<100ms`. Terminal must display the explicit `[ALERT CREATED]` backend hook.

### 1.4 Redis Debug Verification
* **Steps:** Call `GET /debug/redis-test`.
* **Expected Output:** Flat payload JSON object returned in response body.
* **Pass Condition:** Terminal must instantly log `[REDIS PUBLISHED: alert_id=999]`.

---

## SECTION 2 — REAL-TIME PIPELINE TEST

### 2.1 Redis Pub/Sub Queue
* **Steps:** 
  1. Open a new terminal and run: `redis-cli SUBSCRIBE sentinel:alerts`
  2. Trigger the `GET /debug/redis-test` endpoint.
* **Expected Output:** JSON broadcast payload appears in the Redis terminal immediately.
* **Pass Condition:** 
  - Payload appears instantly `<50ms`. 
  - Calculated size of JSON string is strictly `<512 bytes`.
  - JSON format explicitly matches the schema: `{v, alert_id, region_id, severity, type, timestamp, ttl, source}` without geometry objects.

---

## SECTION 3 — WEBSOCKET TEST

### 3.1 Headless WS Tracking
* **Steps:** 
  1. Execute `python test_ws_client.py` in the backend directory.
  2. Call `GET /debug/redis-test`.
* **Expected Output:** `[WS ALERT RECEIVED] {"v":1,"alert_id":999...}` and `[BLE BROADCAST]` logs pop up on the Python test tracker.
* **Pass Condition:** End-to-end traversal delay is invisible (`<200ms`). 
* **Failure Hook:** Kill the Uvicorn server briefly; verify the script outputs `Server disconnected. Reconnecting in 2 seconds...` and re-attaches cleanly when the server comes back online.

---

## SECTION 4 — MOBILE APP TEST

### 4.1 React Native Alert Interception
* **Steps:** 
  1. Open the Rahat app on the Simulator or physical device with Wi-Fi enabled.
  2. Trigger `GET /demo/trigger` from the local backend.
* **Expected Output:** The Sentinel Alert popup aggressively captures screen attention.
* **Pass Condition:** 
  - UI triggers immediately.
  - Severity color mappings are exact (Critical vs Warning).
  - The map overlay refreshes without hard crashing the React Native context.

---

## SECTION 5 — ZUSTAND STATE TEST

### 5.1 In-Memory Handling
* **Steps:** Review the `AlertFeedScreen` after multiple triggers of the same target region.
* **Expected Output:** The list cleanly shows unique threats.
* **Pass Condition:** The `useAlertStore` maintains exactly one entry for region `105`, relying exclusively on the backend `alert_id` to block local duplicate pushing. UI updates synchronously without jitter.

---

## SECTION 6 — ASYNC STORAGE TEST (CRITICAL)

### 6.1 State Rehydration
* **Steps:** 
  1. Trigger an alert via `/demo/trigger` and verify it displays on the map.
  2. Force close the Rahat mobile application completely.
  3. Re-launch the mobile application.
* **Expected Output:** The map dynamically paints the previous alert polygon bounds upon first boot.
* **Pass Condition:** `Zustand` successfully rehydrates state from `AsyncStorage`. Alerts are permanently burned into local files until their strictly defined `expiresAt` or 6-hour TTL cycle overrides them.

---

## SECTION 7 — BLE SYSTEM TEST

### 7.1 Offline Network Propagation
* **Scenario Requirements:** 
  - **Device A (Online Router):** Internet enabled, Bluetooth Enabled.
  - **Device B (Offline Victim):** Airplane Mode explicitly ON, Bluetooth solely Enabled.
* **Steps:**
  1. Dispatch `GET /debug/redis-test` into the backend.
  2. Verify Device A receives the WebSocket feed normally.
  3. Device A triggers the `[BLE BROADCAST]` logic locally.
* **Expected Output:** Device B receives the serialized mesh packet out of thin air.
* **Pass Condition:** 
  - Device B decodes the `CLIMATE_ALERT` message correctly.
  - Device B triggers its own UI alert popups autonomously.
  - Device B inserts the alert tightly into its own `AsyncStorage` cache to prepare for subsequent hop propagation natively without cloud contact.

---

## SECTION 8 — FAILURE TESTS

### 8.1 Backend Fault Isolation
* **Test:** Kill the local Redis Docker container.
* **Response Expected:** Backend API (`/health` and `/predict`) still successfully logs standard queries and responses. No `Internal Server Error 500`. 
* **Pass Condition:** Backend logs the explicit "Failed to publish to Redis" warning gracefully.

### 8.2 WebSocket Fault Isolation
* **Test:** Drop backend Wi-Fi.
* **Response Expected:** Mobile application detects TCP failure. 
* **Pass Condition:** Mobile application continuously queries for a WebSocket reconnect every `2-5` seconds cleanly in the background without raising unhandled Promise rejections to the user view.

### 8.3 Network Isolation
* **Test:** Disconnect Device A entirely from the Internet.
* **Response Expected:** Native `SOS` functions and `DEVICE_DISCOVERY` continue.
* **Pass Condition:** Real-time BLE mesh propagation operates indefinitely inside a void until battery drain limits it.

---

## SECTION 9 — FINAL DEMO TEST

### 9.1 The "Golden Path" End-to-End
* **Steps:** 
  Execute the final presentation test with multiple judges actively watching. Run:
  `curl -X GET "http://localhost:8000/demo/trigger"`
* **Expected Cascade:**
  `ML Mock` → `FastAPI/PostGIS` → `Redis Core` → `WebSocket Bridge` → `Phone A (Cloud)` → `BLE Mesh Burst` → `Phone B (Offline)`
* **Pass Condition:** From hitting **ENTER** on the backend command line to **Phone B** lighting up completely offline... total traversal time must span less than **<5 seconds**.

---

### Known Issues & Demo Risks
1. **Bluetooth Pairing Limits:** Depending on the Android/iOS version, background BLE scans might throttle if not permitted explicit "Always On" location overrides. Keep the screens active during the hackathon pitch to prevent OS battery optimizations from swallowing packets.
2. **WebSocket Sleep Locks:** iOS Safari/React Native heavily throttles active WebSocket connections when sent to the background. 
* **Mitigation:** Ensure the app sits perfectly in the foreground on the demonstration desk prior to executing `/demo/trigger`. 
