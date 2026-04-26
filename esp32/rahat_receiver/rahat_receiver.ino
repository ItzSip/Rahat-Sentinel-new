/*
 * Rahat Sentinel — ESP32 Receiver Module
 *
 * BT device name : RAHAT_RX
 * LED pin        : GPIO 2  (built-in blue LED on ESP32 DevKit V1 / DOIT board)
 *                  Change LED_PIN below if your board uses a different pin.
 *                  If your board has an active-LOW LED, flip LED_ON / LED_OFF.
 * Baud rate      : 115200
 *
 * Serial output (read by dashboard Web Serial API):
 *   LOCATION:28.614200,77.209000,RED
 *
 * ⚠️  PARTITION: Tools → Partition Scheme → Huge APP (3MB No OTA)
 *
 * PAIRING STEP (one-time, before app can send):
 *   Android Settings → Bluetooth → find "RAHAT_RX" → Pair
 *   Then the Rahat app connects automatically.
 */

#include <BluetoothSerial.h>

// ── Pin config ────────────────────────────────────────────────────────────────
#define LED_PIN  2      // GPIO 2 = built-in LED on most ESP32 DevKit boards
#define LED_ON   HIGH   // change to LOW if your board's built-in LED is active-LOW
#define LED_OFF  LOW

// ── State ─────────────────────────────────────────────────────────────────────
BluetoothSerial BT;

static char     latestLoc[80]    = "No data";
static char     latestSev[24]    = "UNKNOWN";
static bool     hasData          = false;
static bool     wasConnected     = false;
static unsigned long lastBeat    = 0;
static unsigned long lastBlinkMs = 0;
static bool     ledState         = false;

// ── LED helpers ───────────────────────────────────────────────────────────────

void ledSolid(bool on) {
  digitalWrite(LED_PIN, on ? LED_ON : LED_OFF);
  ledState = on;
}

// Non-blocking blink — call from loop() while waiting for client
void ledBlink(unsigned long intervalMs) {
  if (millis() - lastBlinkMs >= intervalMs) {
    lastBlinkMs = millis();
    ledState = !ledState;
    digitalWrite(LED_PIN, ledState ? LED_ON : LED_OFF);
  }
}

// ── Parse incoming "lat,lon,SEVERITY\n" ───────────────────────────────────────
static void processLine(const char* buf) {
  String s(buf);
  s.trim();
  if (s.length() == 0) return;

  int fc = s.indexOf(',');
  int sc = s.indexOf(',', fc + 1);

  if (fc > 0 && sc > fc) {
    s.substring(0, sc).toCharArray(latestLoc, sizeof(latestLoc));
    s.substring(sc + 1).toCharArray(latestSev, sizeof(latestSev));
  } else {
    s.toCharArray(latestLoc, sizeof(latestLoc));
    strncpy(latestSev, "UNKNOWN", sizeof(latestSev));
  }

  hasData = true;
  ledSolid(true);   // LED stays ON after first data received

  // Send to dashboard over USB Serial
  Serial.printf("LOCATION:%s,%s\n", latestLoc, latestSev);
  Serial.printf("[RX] Stored: loc=%s  sev=%s\n", latestLoc, latestSev);
}

// ── Non-blocking BT reader ────────────────────────────────────────────────────
static void handleBluetooth() {
  static char buf[80];
  static int  pos = 0;

  while (BT.available()) {
    char c = (char)BT.read();
    if (c == '\n' || c == '\r') {
      if (pos > 0) {
        buf[pos] = '\0';
        processLine(buf);
        pos = 0;
      }
    } else if (pos < (int)sizeof(buf) - 2) {
      buf[pos++] = c;
    }
  }
}

// ── Heartbeat — prints status to Serial every 4 s ────────────────────────────
static void heartbeat() {
  if (millis() - lastBeat < 4000) return;
  lastBeat = millis();

  bool connected = BT.hasClient();
  if (connected && !wasConnected) {
    Serial.println("[BT] Phone connected!");
    wasConnected = true;
    ledSolid(true);  // solid ON when phone connects
  } else if (!connected && wasConnected) {
    Serial.println("[BT] Phone disconnected.");
    wasConnected = false;
    if (!hasData) ledSolid(false);
  }

  Serial.printf("[HEARTBEAT] BT=%s  hasData=%s  loc=%s  sev=%s\n",
    connected  ? "CONNECTED" : "waiting",
    hasData    ? "YES"       : "NO",
    latestLoc, latestSev
  );
}

// ── Setup ─────────────────────────────────────────────────────────────────────
void setup() {
  Serial.begin(115200);
  delay(300);  // let Serial settle

  pinMode(LED_PIN, OUTPUT);
  ledSolid(false);

  Serial.println("\n========================================");
  Serial.println("  Rahat Sentinel — Receiver (RAHAT_RX)");
  Serial.println("========================================");
  Serial.printf("  LED pin : GPIO %d  (ON=%s)\n", LED_PIN, LED_ON == HIGH ? "HIGH" : "LOW");
  Serial.println("  Baud    : 115200");
  Serial.println("----------------------------------------");

  if (!BT.begin("RAHAT_RX")) {
    Serial.println("[ERROR] Bluetooth init FAILED!");
    // Rapid blink to signal error
    for (int i = 0; i < 10; i++) {
      ledSolid(true);  delay(100);
      ledSolid(false); delay(100);
    }
  } else {
    Serial.println("[BT]  Name     : RAHAT_RX");
    Serial.println("[BT]  Status   : Discoverable — waiting for phone...");
    Serial.println("[BT]  PAIRING  : Android Settings > Bluetooth > RAHAT_RX > Pair");
    Serial.println("----------------------------------------\n");
  }
}

// ── Loop — non-blocking ───────────────────────────────────────────────────────
void loop() {
  handleBluetooth();
  heartbeat();

  // Slow blink while no client connected and no data yet
  if (!BT.hasClient() && !hasData) {
    ledBlink(800);
  }
}
