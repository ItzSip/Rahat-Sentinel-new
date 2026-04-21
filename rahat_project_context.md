# Rahat Sentinel — Project Context Document

> Extracted entirely from source code. No opinions or assumptions included.

---

## 1. PROJECT OVERVIEW

| Field | Value |
|---|---|
| **Project Name** | Rahat |
| **Display Name** | Rahat |
| **Package Name** | `com.rahat` (inferred from Android build) |
| **Version** | 0.0.1 |

**Short Description (from README):**
> "RAHAT is a decentralized emergency communication platform designed to provide critical assistance during disasters when cellular networks fail."

**Main Purpose:**
Provides Bluetooth Low Energy (BLE) mesh-based emergency communication and location-aware safety features for use when cellular networks are unavailable during disasters.

---

## 2. TECH STACK

### Languages
- **TypeScript** — primary app language (`src/`)
- **JavaScript** — backend simulation scripts (`backend/receiver.js`)
- **Python** — backend FastAPI server (`backend/app/`, `backend/receiver.py`)
- **Java/Kotlin** — Android native module (native BLE bridge via `NativeModules`)

### Frameworks
- **React Native** `^0.81.4` — mobile app framework
- **FastAPI** — Python backend server
- **React** `19.1.0`

### Libraries (Frontend)
| Library | Version | Purpose |
|---|---|---|
| `@maplibre/maplibre-react-native` | `^10.4.2` | Map display (OpenStreetMap tiles) |
| `@react-native-async-storage/async-storage` | `^3.0.2` | Local key-value storage |
| `@react-native-firebase/app` | `^23.7.0` | Firebase base |
| `@react-native-firebase/auth` | `^23.7.0` | Firebase authentication |
| `@react-native-firebase/firestore` | `^23.7.0` | Firebase Firestore |
| `@react-navigation/native` | `^6.1.18` | Navigation container |
| `@react-navigation/native-stack` | `^6.9.26` | Stack navigator |
| `zustand` | `^5.0.9` | State management |
| `react-native-safe-area-context` | `^5.2.0` | Safe area insets |
| `react-native-screens` | `^4.19.0` | Native screen optimization |
| `ws` | `^8.20.0` | WebSocket client (Node/backend) |

### Backend Technologies
- **FastAPI** (Python) — REST + WebSocket alert backbone
- **Redis 7** (via `redis.asyncio`) — Pub/Sub event bus on channel `sentinel:alerts`
- **Docker / Docker Compose** — Redis + FastAPI containerized deployment
- **Pydantic v2** — data validation / models
- **websockets** (Python library) — WebSocket client in `receiver.py`

---

## 3. FOLDER STRUCTURE

```
Rahat2/
├── index.js                  # App entry point — registers React Native component
├── app.json                  # App name config
├── package.json              # JS dependencies
├── tsconfig.json             # TypeScript config
├── babel.config.js           # Babel config
├── metro.config.js           # Metro bundler config
├── build.gradle              # Root Android Gradle build
├── settings.gradle           # Android settings
├── gradle.properties         # Android Gradle properties
├── local.properties          # Android SDK path
├── Gemfile                   # Ruby/CocoaPods (iOS)
│
├── android/                  # Android native project
│   ├── build.gradle          # Android app Gradle config (includes Firebase google-services)
│   ├── google-services.json  # Firebase project config
│   ├── debug.keystore        # Debug signing key
│   ├── proguard-rules.pro    # ProGuard rules
│   └── src/                  # Android native source (Java/Kotlin — BLE native module)
│
├── ios/                      # iOS native project (structure present, not primary target)
│
├── src/                      # React Native TypeScript source
│   ├── app/
│   │   ├── App.tsx           # Root React component — permissions, BLE start, navigation gate
│   │   └── navigation/
│   │       └── RootNavigator.tsx   # Stack navigator — all 9 screens defined here
│   │
│   ├── screens/              # One file per screen
│   │   ├── HomeScreen.tsx          # Map + SOS button + sidebar
│   │   ├── AlertFeedScreen.tsx     # Live alert list from alertStore
│   │   ├── NearbyDevicesScreen.tsx # BLE peer list from deviceStore
│   │   ├── SOSConfirmationScreen.tsx # SOS sent confirmation
│   │   ├── AuthScreen.tsx          # Onboarding / login
│   │   ├── EarlyWarningScreen.tsx  # Predictive hazard warnings
│   │   ├── ProfileScreen.tsx       # User profile + emergency contacts
│   │   ├── SettingsScreen.tsx      # App settings
│   │   └── SafeZoneGuideScreen.tsx # Safe zone information
│   │
│   ├── features/             # Feature modules
│   │   ├── bluetoothMesh/
│   │   │   ├── bluetoothService.ts # JS bridge to native RahatMesh module
│   │   │   └── meshTypes.ts        # MeshPayload + NormalizedMeshAlert types
│   │   ├── alerts/
│   │   │   ├── delivery/
│   │   │   ├── intelligence/
│   │   │   └── presentation/
│   │   ├── location/
│   │   │   ├── locationService.ts  # Alert relevance filter (TTL + geofence)
│   │   │   └── geoRules.ts         # Radius-based geo check
│   │   ├── locationAwareness/
│   │   │   ├── locationContext.ts  # Location context helpers
│   │   │   └── relevanceEngine.ts  # Alert relevance engine
│   │   ├── hazardLevel/
│   │   │   ├── hazardLevel.utils.ts # HazardLevel enum + priority config
│   │   │   └── hazardLevel.types.ts # HazardMetadata type
│   │   └── cellBroadcast/
│   │       └── cellBroadcast.service.ts # Cell broadcast service
│   │
│   ├── core/                 # Cross-cutting pipeline
│   │   ├── types.ts          # RahatEvent type definition
│   │   ├── ingestionLayer.ts # Parses raw BLE frames into RahatEvents
│   │   ├── eventEngine.ts    # Deduplication, hop-control, priority storage
│   │   └── syncLayer.ts      # Batch/flush outbound BLE frames
│   │
│   ├── store/                # Zustand global state
│   │   ├── deviceStore.ts    # peers[], isScanning
│   │   ├── alertStore.ts     # alerts[], addAlert, clearExpiredAlerts
│   │   ├── userStore.ts      # profile, emergencyContacts
│   │   ├── settingsStore.ts  # narrator, volume, darkMode, language
│   │   └── warningStore.ts   # EarlyWarning[] (static mock data)
│   │
│   ├── components/
│   │   └── ui/               # Shared UI components (GlassCard, PillBadge, ActionButton)
│   │
│   ├── theme/
│   │   └── colors.ts         # Color constants
│   │
│   └── types/
│       └── alerts.ts         # Alert, HazardLevel type definitions
│
└── backend/                  # Standalone Python/Node backend
    ├── .env.example          # Environment variable template
    ├── Dockerfile            # FastAPI Docker image
    ├── docker-compose.yml    # Redis + FastAPI compose
    ├── receiver.js           # Node.js WS client + demo simulator
    ├── receiver.py           # Python WS client + BLE encoder
    ├── mock_sender.py        # Mock alert sender (4KB)
    └── app/
        ├── main.py           # FastAPI app — REST + WebSocket endpoints
        ├── models.py         # Pydantic models (SentinelAlert, GeoCenter, etc.)
        ├── ble_codec.py      # 47-byte BLE binary pack/unpack
        ├── alert_cache.py    # In-memory alert cache
        ├── redis_listener.py # Redis pub/sub listener
        └── test_ble_codec.py # Unit tests for BLE codec
```

---

## 4. CORE FEATURES (FROM CODE)

### BLE Functionality
- **BLE Scanning**: `bluetoothService.ts` calls `RahatMesh.startScanning()` on a native Android module (`NativeModules.RahatMesh`).
- **Peer Discovery via Native Events**: Subscribes to `onPeersUpdated` native event emitter; receives array of peer objects `{ id, name, severity, signalLevel, signalTrend, lastSeen }`.
- **BLE Send**: `bleSend(frame)` passes a string frame directly to `RahatMesh.bleSend()`.
- **BLE Advertising**: Described in README as enforcing Legacy LE_1M advertising; implemented natively in Android (not directly in JS source).
- **Signal Trend**: Peers carry `signalTrend` values: `APPROACHING`, `RECEDING`, `STABLE`.
- **Signal Level**: Peers carry `signalLevel` values: `VERY_STRONG`, `STRONG`, `MODERATE`, `WEAK`.
- **Auto-Recovery**: README states mesh restarts automatically on Bluetooth toggle (native Android).
- **Scanning Window**: README states 15s on / 10s off duty cycle (native Android).
- **Peer TTL**: README states 60-second TTL managed by native `PeerManager`.
- **BLE Payload Encoding (Backend)**: `ble_codec.py` packs `SentinelAlert` into a fixed 47-byte binary frame (region_id uint16, severity uint8, timestamp uint32, gradcam_hash 8 bytes, district 32 bytes).

### Map Functionality
- **Map Library**: MapLibreGL (`@maplibre/maplibre-react-native`).
- **Tile Source**: OpenStreetMap (`https://tile.openstreetmap.org/{z}/{x}/{y}.png`).
- **Token**: `null` — no token required.
- **User Location**: `MapLibreGL.UserLocation` with heading indicator.
- **Peer Markers**: Up to 10 BLE peers with GPS coordinates rendered as `PointAnnotation` (orange dots).
- **Camera**: Follows user location, zoom level 13.
- **Map Start**: `MapLibreGL.locationManager.start()` called on app bootstrap; stopped on unmount.

### SOS / Alert System
- **SOS Trigger**: Pressing SOS button on HomeScreen calls `emitTestEvent('SOS')` and navigates to `SOSConfirmationScreen`.
- **SOS Event**: Creates `RahatEvent` with `type: 'SOS'`, `priority: 3`, `ttl: 30s`.
- **SOS Priority**: SOS events bypass batching in `syncLayer.ts` — sent immediately with `flushBatch()` first.
- **Alert Storage**: `alertStore.ts` (Zustand) — deduplication by `id`, max 20 displayed in AlertFeed.
- **Alert Expiry**: Alerts have a `ttl` (seconds); `clearExpiredAlerts` filters by `timestamp + ttl * 1000 > now`.
- **Alert Types**: `'SOS'`, `'LOCATION'`, `'PING'` (defined in `core/types.ts`).

### Nearby Devices Logic
- **Peer State**: Stored in `deviceStore.ts` — `peers: PeerDevice[]`, `isScanning: boolean`.
- **Peer Update**: Full peer list replaced on each `onPeersUpdated` native event.
- **Add/Update Logic**: `addOrUpdatePeer` merges by `id` into existing peer array.
- **Display**: `NearbyDevicesScreen` renders all peers with signal level, trend icon, last-seen age.

### Event Engine (Offline Mesh Logic)
- **Deduplication**: Set of seen event IDs, max 100 (LRU eviction).
- **Hop Limit**: Max 3 hops (`MAX_HOPS = 3`).
- **Event Storage**: Max 50 events (`MAX_EVENTS`), sorted descending by priority.
- **Broadcast Rule**: SOS and LOCATION events are relayed; PING only relayed at hop 0.
- **Sync/Batching**: `syncLayer.ts` batches up to 10 events, max payload 120 bytes; oversized batches sent individually; SOS always immediate.

### Early Warning / Hazard System
- **Hazard Levels**: `CRITICAL`, `SEVERE`, `MODERATE`, `LOW` — each with label, description, priority, color.
- **Warning Store**: Static mock data (one flood risk warning pre-populated in `warningStore.ts`).
- **Geo Filter**: `locationService.ts` filters alerts by TTL expiry and geographic radius (`geoRules.ts`).

---

## 5. KEY FILES

| File | Role |
|---|---|
| `index.js` | App entry point — registers `App` component with `AppRegistry` |
| `src/app/App.tsx` | Root component — requests permissions, starts BLE scan, starts MapLibre location, gates onboarding |
| `src/app/navigation/RootNavigator.tsx` | Defines all 9 routes; reads AsyncStorage for initial route (Auth vs Home) |
| `src/screens/HomeScreen.tsx` | Main screen — fullscreen map, SOS button, nearby pill, sidebar nav |
| `src/screens/AlertFeedScreen.tsx` | Displays alert list from `alertStore` |
| `src/screens/NearbyDevicesScreen.tsx` | Displays BLE peer list from `deviceStore` |
| `src/screens/SOSConfirmationScreen.tsx` | SOS sent confirmation screen |
| `src/features/bluetoothMesh/bluetoothService.ts` | JS bridge to native BLE module; subscribes to `onPeersUpdated` |
| `src/features/bluetoothMesh/meshTypes.ts` | `MeshPayload` and `NormalizedMeshAlert` interfaces |
| `src/core/types.ts` | `RahatEvent` type: `id, type, payload, timestamp, ttl, priority, origin, hops` |
| `src/core/ingestionLayer.ts` | Parses raw BLE string frame (JSON array) into `RahatEvent[]` |
| `src/core/eventEngine.ts` | Dedup, hop control, priority-sorted storage, outbound dispatch |
| `src/core/syncLayer.ts` | Batching, byte-size control, SOS override, transport strategy |
| `src/store/deviceStore.ts` | Zustand store — `peers`, `isScanning` |
| `src/store/alertStore.ts` | Zustand store — `alerts[]`, `addAlert`, `removeAlert`, `clearExpiredAlerts` |
| `src/store/userStore.ts` | Zustand store — `profile`, `contacts[]` |
| `src/store/settingsStore.ts` | Zustand store — narrator, volume, vibration, darkMode, language |
| `src/store/warningStore.ts` | Zustand store — `EarlyWarning[]` (static mock) |
| `backend/app/main.py` | FastAPI app — REST endpoints + WebSocket `/ws/alerts` |
| `backend/app/models.py` | Pydantic models — `SentinelAlert`, `GeoCenter`, `WebSocketMessage` |
| `backend/app/ble_codec.py` | 47-byte BLE binary pack/unpack |
| `backend/app/redis_listener.py` | Subscribes to Redis `sentinel:alerts` channel, calls `on_alert` callback |
| `backend/app/alert_cache.py` | In-memory cache of recent alerts |
| `backend/receiver.js` | Node.js WS client + test mode simulator (Bhilai-Durg region) |
| `backend/receiver.py` | Python WS client + 47-byte BLE encoder + test mode |

---

## 6. DATA FLOW (FACTUAL)

### Device Discovery
1. On app bootstrap (`App.tsx`), `requestPermissions()` requests Android BLE + location permissions.
2. `startScanning()` (`bluetoothService.ts`) calls native `RahatMesh.startScanning()`.
3. Native Android module performs BLE scanning and emits `onPeersUpdated` events.
4. JS bridge receives peer array and calls `useDeviceStore.getState().setPeers(mappedPeers)`.
5. Components subscribed to `deviceStore` re-render with updated peer list.

### Inbound BLE Data (from other devices)
1. Native module calls `onReceive(data: string)` in JS when a BLE mesasage arrives.
2. `ingestionLayer.ingestFrame(data)` parses JSON string → array of `RahatEvent`.
3. Each event passed to `eventEngine.processIncomingEvent(event)`:
   - Dropped if expired (timestamp + ttl < now)
   - Dropped if already seen (dedup set)
   - Dropped if hops >= 3
   - Hop count incremented
   - Priority-sorted into in-memory `storedEvents[]` (max 50)
4. If event type is `SOS` or `LOCATION`, `outboundListener` (wired to `syncLayer.handleEvent`) is called.

### Outbound BLE Data
1. `syncLayer.handleEvent(event)` receives event:
   - SOS → flushes current batch, sends immediately
   - LOCATION → pushes to batch, flushes
   - Others → batched up to 10 events or 120 bytes
2. `flushBatch()` serializes `eventBatch` to JSON string.
3. If payload > 120 bytes, events sent individually.
4. `sendFrame(frame)` calls `customTransport(frame)` if set, else `console.log`.
5. Custom transport would call `bleSend(frame)` → `RahatMesh.bleSend(frame)` → native transmit.

### Data Storage
- **In-Memory (React state)**: All peer and alert data held in Zustand stores (not persisted to disk).
- **AsyncStorage**: Onboarding flag (`has_onboarded`), user name (`@rahat_name`), phone (`@rahat_phone`), launch flag (`@rahat_launched`).
- **No local database**: No SQLite or persistent alert DB in JS layer.

### SOS Handling
1. User presses SOS button → `emitTestEvent('SOS')` called in `HomeScreen`.
2. `eventEngine.processIncomingEvent` stores and dispatches event.
3. `syncLayer.handleEvent` receives type=SOS → immediate send (bypasses batch).
4. Navigation to `SOSConfirmationScreen` (UI only, displays static confirmation).

---

## 7. API / BACKEND

**Base URL**: `http://0.0.0.0:8000` (configurable via env)  
**WebSocket URL**: `ws://localhost:8000/ws/alerts`

### REST Endpoints

| Method | Path | Description |
|---|---|---|
| `GET` | `/` | Health check — returns status, ws_clients count, cached_alerts count, redis_url, timestamp |
| `GET` | `/api/alerts/latest` | Returns last 5 cached `SentinelAlert` objects |
| `GET` | `/api/alerts/{alert_id}` | Returns single alert by ID; 404 if not found |
| `POST` | `/api/alerts/simulate` | Publishes a randomly generated mock alert to Redis; returns `{status, alert_id, risk_score}` |
| `POST` | `/api/ble/encode` | Packs a `SentinelAlert` body into BLE binary; returns `{hex, size_bytes, max_ble_bytes, decoded}` |

### WebSocket Endpoint
- **Path**: `/ws/alerts`
- **On connect**: Server immediately sends `{event: "history", data: [SentinelAlert, ...]}` (last 5 cached).
- **On new Redis message**: Server broadcasts `{event: "new_alert", data: SentinelAlert}` to all clients.
- **Client ping**: Client sends `"ping"` string; server responds `{event: "heartbeat", data: {pong: true}}`.

### WebSocket Message Structure
```json
{
  "event": "new_alert | history | heartbeat",
  "data": { ... } | [ ... ] | null
}
```

### SentinelAlert Model
```json
{
  "id": "string (12-char hex)",
  "risk_score": "float 0.0-1.0",
  "cells": "[[float x 20] x 20] (20x20 risk matrix)",
  "timestamp": "ISO-8601 UTC string",
  "district": "string",
  "region_id": "int 0-65535",
  "severity": "int 0-255",
  "gradcam_hash": "string (16-char hex)",
  "center": { "lat": float, "lon": float }
}
```

### Backend Architecture
```
Redis Pub/Sub (sentinel:alerts)
        ↓
  redis_listener.py
        ↓
  ConnectionManager.broadcast()
        ↓
  WebSocket clients
```

### BLE Binary Codec (47 bytes fixed)
| Field | Bytes | Type |
|---|---|---|
| region_id | 2 | uint16 BE |
| severity | 1 | uint8 |
| timestamp | 4 | uint32 BE (epoch seconds) |
| gradcam_hash | 8 | raw bytes |
| district | 32 | UTF-8, null-padded |

---

## 8. CONFIGURATION

### Permissions (Android)
| Permission | Condition |
|---|---|
| `ACCESS_FINE_LOCATION` | Always |
| `BLUETOOTH_SCAN` | Android API >= 31 |
| `BLUETOOTH_CONNECT` | Android API >= 31 |
| `BLUETOOTH_ADVERTISE` | Android API >= 31 |
| `POST_NOTIFICATIONS` | Android API >= 33 |

Requested at app bootstrap via `PermissionsAndroid.requestMultiple()`.

### Environment Variables (`backend/.env.example`)
```
REDIS_URL=redis://redis:6379
API_HOST=0.0.0.0
API_PORT=8000
```

### Config Files
| File | Purpose |
|---|---|
| `app.json` | React Native app name: `"Rahat"` |
| `babel.config.js` | Babel preset for React Native |
| `metro.config.js` | Metro bundler config |
| `tsconfig.json` | TypeScript compiler config |
| `.eslintrc.js` | ESLint rules |
| `.prettierrc.js` | Prettier formatting |
| `android/google-services.json` | Firebase project config (Google Services) |
| `backend/docker-compose.yml` | Docker services: Redis 7 + FastAPI on port 8000 |
| `backend/Dockerfile` | FastAPI app Docker image |
| `gradle.properties` | Android build properties |
| `local.properties` | Android SDK path (local only) |

### MapLibre
- `MapLibreGL.setAccessToken(null)` — no token required (OSM is free).
- Tile URL: `https://tile.openstreetmap.org/{z}/{x}/{y}.png`

---

## 9. DEPENDENCIES

### JavaScript / React Native (`package.json`)

**Runtime Dependencies**
| Package | Version |
|---|---|
| `react` | `19.1.0` |
| `react-native` | `^0.81.4` |
| `@maplibre/maplibre-react-native` | `^10.4.2` |
| `@react-native-async-storage/async-storage` | `^3.0.2` |
| `@react-native-firebase/app` | `^23.7.0` |
| `@react-native-firebase/auth` | `^23.7.0` |
| `@react-native-firebase/firestore` | `^23.7.0` |
| `@react-navigation/native` | `^6.1.18` |
| `@react-navigation/native-stack` | `^6.9.26` |
| `react-native-safe-area-context` | `^5.2.0` |
| `react-native-screens` | `^4.19.0` |
| `ws` | `^8.20.0` |
| `zustand` | `^5.0.9` |

**Dev Dependencies**
| Package | Version |
|---|---|
| `typescript` | `^5.8.3` |
| `@babel/core` | `^7.25.2` |
| `@react-native-community/cli` | `^20.0.0` |
| `@react-native/babel-preset` | `0.83.1` |
| `eslint` | `^8.19.0` |
| `prettier` | `2.8.8` |
| `jest` | `^29.6.3` |

**Engine Requirement**: Node >= 20

### Python Backend (from imports in source)
| Package | Purpose |
|---|---|
| `fastapi` | REST + WebSocket server |
| `pydantic` (v2) | Data validation / models |
| `redis.asyncio` | Async Redis client |
| `python-dotenv` | `.env` file loading |
| `websockets` | Python WebSocket client |
| `uvicorn` | ASGI server (implied by FastAPI + Docker) |

---

## 10. ADDITIONAL NOTES (FACTUAL)

- **Native BLE Module**: The app interacts with BLE via `NativeModules.RahatMesh`. This is a custom Android native module. The JS layer does not use any third-party RN BLE library (e.g., no `react-native-ble-plx`). The native implementation is in `android/src/`.
- **Firebase**: `@react-native-firebase/auth` and `@react-native-firebase/firestore` are declared as dependencies but no Firebase usage is visible in the JS `src/` layer beyond the package declaration and `google-services.json`.
- **EphID / HMAC-SHA256**: Described in README as native Android `IdentityManager` — 128-bit ephemeral IDs rotating every 10 minutes linked to device Keystore-backed master secret. Not present in JS source.
- **Early Warning Data**: `warningStore.ts` is populated with hardcoded mock data — no live API call present.
- **Backend ↔ App connection**: No code in `src/` connects to the FastAPI backend. The backend is a standalone pipeline for demonstrating satellite → AI → BLE alert flow. The JS app receives data only via the native BLE module.
- **Test Mode**: Both `receiver.js` and `receiver.py` support `--test` flag to simulate alerts without a live WebSocket server.
- **Target Region**: Backend simulation defaults to Bhilai-Durg, Chhattisgarh, India (lat 21.15–21.30, lon 81.25–81.45).
