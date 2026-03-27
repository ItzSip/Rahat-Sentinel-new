# System Execution Trace: Mobile Integration Finalization

This document tracks the final system operations establishing persistent offline bridging between the mobile user interface and the Sentinel backend array.

---

### 1. WHAT WAS IMPLEMENTED
- Injected `AsyncStorage` persistence deeply into the standard Zustand application stores.
- Developed an auto-reconnecting Python/React-compliant `WebSocketService`.
- Established a central operational hub (`AlertManager`) resolving duplication logic across REST, Socket, and Bluetooth Mesh streams simultaneously.
- Rewrote native BLE ingestion layers mapping SOS and `CLIMATE_ALERT` arrays securely.

---

### 2. DEFINITION OF DONE (DoD)

#### Task 1: AsyncStorage Storage Support
* **Completion Definition:** Mobile application intercepts active alerts and permanently bonds them to the `alerts` target key.
* **Verification:** App forcefully closes and seamlessly repopulates threat maps directly from hardware arrays upon next launch via Zustand `persist` rehydration mechanisms.

#### Task 2: WebSocket Resilience Upgrades
* **Completion Definition:** Service tracks arbitrary `onclose` and `onerror` states flawlessly without polluting user notification screens with crashes.
* **Verification:** `[WS DISCONNECTED]` logs print smoothly and cleanly attempt TCP handshakes every *2 to 5 seconds*, instantly resolving to `[WS RECONNECTED]` when standard API availability returns.

#### Task 3: Dual-Routing Alert Manager
* **Completion Definition:** Consolidates multi-platform `alert_id` hashes dynamically without memory bloating.
* **Verification:** Incoming websocket threats securely flow downward and simultaneously sidestep `AsyncStorage` processing bounds to prevent repeating alerts.

#### Task 4: BLE Infrastructure Extensions
* **Completion Definition:** Protocol recognizes `<512-byte` Sentinel payloads independently from priority `SOS` payloads.
* **Verification:** Delays offline mesh bursts randomly between `2,000` and `5,000` milliseconds to bypass destructive Bluetooth packet collisions entirely natively.

---

### 3. FILES TOUCHED

**File:** `src/store/alertStore.ts`
* **Added:** Extended Zustand interface tightly linking `createJSONStorage(() => AsyncStorage)`.
* **Added:** Built-in deduplication filters validating against `alert_id`.
* **Why:** Necessary for offline reliability so users closing the app do not lose multi-hour threat awareness zones.

**File:** `src/services/websocketService.ts`
* **Added:** Recursive timeout blocks maintaining exponential backoff connecting to the local backend.
* **Why:** Real-world cell networks drop randomly. Hard-crashing the app or abandoning the Sentinel socket kills the fundamental product promise.

**File:** `src/managers/AlertManager.ts`
* **Added:** Abstract interface extracting source tags (`cloud` vs `ble`) securely.
* **Why:** Unites disparate routing so the database layers don't have to duplicate checking algorithms continuously.

**File:** `src/features/bluetoothMesh/bluetoothService.ts`
* **Added:** `CLIMATE_ALERT` parsing and isolated forwarding to the new `AlertManager`.
* **Added:** Random `2-5s` delays executing `setTimeout` upon BLE replay requests.
* **Why:** Avoids packet drops across localized disaster zones if multiple local cellphones receive the Wi-Fi alert at the identical millisecond and transmit Bluetooth simultaneously.

---

### 4. FINAL DATA FLOW 

This outlines the exact trajectory payloads execute throughout the Mobile array:
**Cloud Backend Pipeline** → **WebSocketService** → **AlertManager** → **Zustand memory pipeline** → (implicitly mirrored to) **AsyncStorage file cache** → **BLE Mesh Emitter Logic (delayed)**.

---

### 5. KNOWN LIMITATIONS
* **Untested Parts:** Since physical hardware Bluetooth chips cannot run strictly through simulators, the precise radio interception `[BLE ALERT RECEIVED]` requires two physical Android endpoints operating in Airplane Mode. 
* **Edge Cases:** Native OS iOS sleep behavior may arbitrarily pause the WebSocket interval loops if running natively for more than 5 minutes strictly in the background unless standard Location permissions enforce priority. Use Foreground modes for the hackathon table demo.
