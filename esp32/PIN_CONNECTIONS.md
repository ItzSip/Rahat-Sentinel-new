# Rahat Sentinel — ESP32 Pin Connections & Setup Guide

---

## MODULE 1: RAHAT_NODE (Sender / WiFi)
**Sketch:** `esp32/rahat_node/rahat_node.ino`
**Role:** Receives location from phone via WiFi HTTP POST. Lights LED. No BT needed.

### Connections

```
ESP32 DevKit V1
┌─────────────────────────────────┐
│  GPIO 2  ──── Built-in LED      │  (lights when HTTP POST received)
│  USB     ──── Power only        │  (not connected to laptop)
│  3.3V    ──── [not used]        │
│  GND     ──── [not used]        │
└─────────────────────────────────┘
```

### If using an EXTERNAL LED (GPIO 2 → external circuit):
```
GPIO 2 ──→ [220Ω resistor] ──→ LED (+) ──→ LED (-) ──→ GND
```

### WiFi AP it creates:
```
SSID     : RAHAT_NODE
Password : 12345678
IP       : 192.168.4.1
Endpoint : POST http://192.168.4.1/location
```

### Phone connection:
- Connect phone WiFi to **RAHAT_NODE** → app auto-POSTs on SOS / every 8 s

---

## MODULE 2: RAHAT_RX (Receiver / Bluetooth)
**Sketch:** `esp32/rahat_receiver/rahat_receiver.ino`
**Role:** Receives location from phone via Bluetooth Classic SPP. Lights LED. Forwards over USB Serial to laptop dashboard.

### Connections

```
ESP32 DevKit V1
┌─────────────────────────────────────────────────────┐
│  GPIO 2  ──── Built-in LED                          │
│             OFF  = waiting for phone                │
│             BLINK (0.8s) = no phone connected       │
│             SOLID ON = phone connected / data rx'd  │
│                                                     │
│  USB     ──── Laptop (Serial Monitor + Dashboard)   │
│  3.3V    ──── [not used]                            │
│  GND     ──── [not used]                            │
└─────────────────────────────────────────────────────┘
```

### If using an EXTERNAL LED:
```
GPIO 2 ──→ [220Ω resistor] ──→ LED Anode (+) ──→ LED Cathode (-) ──→ GND
```

### USB Serial to Laptop:
```
ESP32 USB ──→ Laptop USB port
Baud rate : 115200
Used by   : Arduino IDE Serial Monitor  AND  Dashboard Web Serial API
```

---

## LED BEHAVIOUR GUIDE

| State | RAHAT_NODE LED | RAHAT_RX LED |
|---|---|---|
| Booting | OFF | OFF |
| Ready, no connection | OFF | Slow blink (0.8 s) |
| Phone BT connected | — | Solid ON |
| Location data received | Solid ON | Solid ON |
| BT Error on boot | — | Rapid blink (10×) |

---

## COMMON LED ISSUES

### LED not turning on — check these in order:

**1. Wrong GPIO pin**
Most ESP32 DevKit V1 (38-pin, blue board) → built-in LED = **GPIO 2**
Some boards differ:

| Board | Built-in LED pin |
|---|---|
| ESP32 DevKit V1 (DOIT) | GPIO 2 |
| ESP32 DevKit V4 | GPIO 2 |
| NodeMCU-32S | GPIO 2 |
| ESP32-WROOM-32 bare module | No built-in LED |
| ESP32-S2 | GPIO 15 (varies) |
| ESP32-C3 | GPIO 8 (varies) |
| Lolin32 | GPIO 5 |

→ Change `#define LED_PIN 2` in the sketch to match your board.

**2. Active-LOW LED**
Some boards have the LED connected so that `LOW = ON`, `HIGH = OFF`.
→ In the sketch, swap:
```cpp
#define LED_ON   LOW    // was HIGH
#define LED_OFF  HIGH   // was LOW
```

**3. External LED polarity reversed**
→ Flip the LED — longer leg (anode +) goes toward the resistor.

**4. Missing resistor**
→ Without a resistor the LED may burn out immediately (no visible light).
→ Use 220Ω–470Ω between GPIO and LED anode.

---

## BLUETOOTH PAIRING (MANDATORY — must do before app works)

The Rahat app can only connect to **already-paired** Bluetooth devices.
You MUST pair RAHAT_RX manually once:

```
1. Flash RAHAT_RX sketch → open Serial Monitor → confirm "RAHAT_RX ready"
2. On the Android phone:
     Settings → Connected devices → Bluetooth → scan
     Find "RAHAT_RX" → tap → Pair
3. Accept pairing on phone (PIN not required for SPP)
4. Open Rahat app — it auto-connects to RAHAT_RX in background
```

After pairing, the app will:
- Connect to RAHAT_RX automatically on launch
- Send location + severity via BT on every SOS tap
- Send location + severity via BT every 8 seconds (periodic beacon)

---

## SERIAL MONITOR — What you should see

### On boot (before phone connects):
```
========================================
  Rahat Sentinel — Receiver (RAHAT_RX)
========================================
  LED pin : GPIO 2  (ON=HIGH)
  Baud    : 115200
----------------------------------------
[BT]  Name     : RAHAT_RX
[BT]  Status   : Discoverable — waiting for phone...
[BT]  PAIRING  : Android Settings > Bluetooth > RAHAT_RX > Pair
----------------------------------------

[HEARTBEAT] BT=waiting  hasData=NO  loc=No data  sev=UNKNOWN
[HEARTBEAT] BT=waiting  hasData=NO  loc=No data  sev=UNKNOWN
```

### When phone connects (after pairing + app open):
```
[BT] Phone connected!
[HEARTBEAT] BT=CONNECTED  hasData=NO  loc=No data  sev=UNKNOWN
```

### When SOS tapped:
```
LOCATION:28.614200,77.209000,RED_SOS
[RX] Stored: loc=28.614200,77.209000  sev=RED_SOS
[HEARTBEAT] BT=CONNECTED  hasData=YES  loc=28.614200,77.209000  sev=RED_SOS
```

---

## PARTITION SETTINGS (Arduino IDE)

Both sketches require:
```
Tools → Board → ESP32 Dev Module
Tools → Partition Scheme → Huge APP (3MB No OTA/1MB SPIFFS)
Tools → Upload Speed → 115200 or 921600
Tools → Port → [COM port of your ESP32]
```

If you see **"Sketch too big"** → you forgot to set Huge APP partition.

---

## FULL SYSTEM WIRING DIAGRAM

```
┌──────────────────────────────────────────────────────────────────────┐
│                                                                      │
│   Android Phone                                                      │
│   ┌──────────┐                                                       │
│   │  Rahat   │──[WiFi: RAHAT_NODE]──────────→ ESP32 Sender          │
│   │   App    │                                  (RAHAT_NODE)         │
│   │          │──[Bluetooth SPP: RAHAT_RX]──→ ESP32 Receiver  ──USB──→ Laptop
│   └──────────┘                                  (RAHAT_RX)           │   │
│                                                                      │   │
│                                                    LED blinks/ON     │   ↓
│                                                                      │ Dashboard
│                                                                      │ (browser)
└──────────────────────────────────────────────────────────────────────┘

Data flow on SOS tap:
  Phone → [HTTP POST lat,lon,SEV_SOS] → RAHAT_NODE → LED ON
  Phone → [BT SPP   lat,lon,SEV_SOS] → RAHAT_RX   → LED ON → Serial → Dashboard map
```
