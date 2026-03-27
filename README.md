# Rahat Sentinel
## AI-Powered Climate Early Warning + Offline Disaster Communication

---

### The Problem

Disasters are predictable, but saving lives is rarely about prediction—it's about **delivery**.

When a severe climate anomaly strikes, what's the first thing to fail? The cell towers. The internet. The power grid.

Governments broadcast warnings into a void, hoping they reach the villages in the crosshairs. But when infrastructure crumbles, those warnings don't reach the people who need them most. 

Communication fails precisely when it matters the most.

---

### The Solution

**Rahat Sentinel** bridges the deadly gap between global AI prediction and last-mile offline delivery.

By combining cutting-edge satellite machine learning with an unstoppable Bluetooth Mesh network, Rahat ensures that no community is left disconnected in an emergency. 
- **Sentinel** predicts disasters early while the grid is still up.
- **Rahat** physically delivers the alerts device-to-device even when the grid is completely destroyed.

Mobiles transform into a living, breathing mesh network that propagates high-risk warnings deep into disconnected regions offline.

---

### How It Works

**1. Satellite Sees Anomaly:** Global datasets monitor the environment.
**2. AI Detects Risk:** Our ML models instantly recognize impending heatwaves, floods, or heavy rainfall.
**3. Backend Sends Alert:** The Sentinel API instantly drops the climate alert into a real-time Redis queue.
**4. Phones Spread It Offline:** Any mobile touching the internet catches the alert and silently broadcasts `<512-byte` compressed payloads over Bluetooth. A cascade of offline phones receive the warnings, saving lives.

---

### Key Features

* **Real-time Climate Anomaly Detection:** Constant monitoring for impending threats.
* **Offline BLE Mesh Communication:** Zero-internet propagation.
* **Geospatial Risk Maps:** Intuitive UI for responders and citizens.
* **Fail-Safe Reliability:** Works when standard infrastructure collapses.

---

### Tech Stack

- **Data** → Google Earth Engine
- **AI** → ConvLSTM + GradCAM
- **Backend** → FastAPI + PostgreSQL + PostGIS + Redis
- **Mobile** → React Native + Bluetooth Low Energy (BLE)

---

### Demo Flow

During our live test, here is exactly what happens behind the scenes:

1. **Trigger Alert:** We force the AI pipeline to fire a critical anomaly into the database.
2. **Dashboard Updates:** The high-accuracy B-Tree indexed database logs the event and the cloud dashboard reflects it.
3. **Phone Receives Alert:** A cloud-connected mobile phone captures the Redis message globally via zero-latency WebSockets.
4. **Offline Phone Receives via BLE:** Instantly, the phone translates the JSON payload into hexadecimal formats and fires a Bluetooth burst. Nearby devices in Airplane Mode intercept the warning. The chain continues.

---

### Why This Matters

This isn't just an app—it's a fundamentally new approach to **last-mile alert delivery**. By removing the reliance on stable internet, emergency response teams and governments can deploy this nationwide. It scales predictably and refuses to fail under environmental strain.

---

### Team

- **ML Engineers** - Building the Earth models.
- **Backend Engineers** - Delivering the fast geospatial PostGIS pipelines.
- **Mobile Developers** - Wiring the React Native offline Mesh.
- **UI/UX** - Creating the disaster dashboards.

---

### Future Vision

Today, Rahat Sentinel spots extreme flooding and heat waves. Tomorrow, it becomes the backbone of multi-disaster rapid response grids globally. With direct integration into government crisis systems and nationwide deployment contracts, Sentinel stands to redefine survival strategy in the 21st century.
