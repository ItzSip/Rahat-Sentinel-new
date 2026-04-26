/*
 * Rahat Sentinel — ESP32 Node  (WiFi-only build, ~350 KB)
 *
 * Phone sends location + severity via HTTP POST to /location
 * Payload: "lat,lon,SEVERITY"   e.g.  "28.614200,77.209000,RED"
 *
 * WiFi AP:  SSID=RAHAT_WIFI  pass=12345678  IP=192.168.4.1
 *
 * Endpoints:
 *   POST /location  → store latest location
 *   GET  /          → show latest location as plain HTML
 *   GET  /data      → raw "lat,lon,SEVERITY" for machine reads
 */

#include <WiFi.h>
#include <WebServer.h>

static const char* AP_SSID = "RAHAT_WIFI";
static const char* AP_PASS = "12345678";

static char latestLoc[80] = "No data";
static char latestSev[24] = "UNKNOWN";
static unsigned long lastRxMs = 0;

WebServer server(80);

// ── Helpers ───────────────────────────────────────────────────────────────────

static void parsePayload(const String& body) {
  int fc = body.indexOf(',');
  int sc = body.indexOf(',', fc + 1);
  if (fc > 0 && sc > fc) {
    body.substring(0, sc).toCharArray(latestLoc, sizeof(latestLoc));
    body.substring(sc + 1).toCharArray(latestSev, sizeof(latestSev));
  } else {
    body.toCharArray(latestLoc, sizeof(latestLoc));
    latestSev[0] = '\0';
  }
  lastRxMs = millis();
  Serial.printf("[LOC] %s  sev=%s\n", latestLoc, latestSev);
}

// ── HTTP handlers ─────────────────────────────────────────────────────────────

void handleRoot() {
  unsigned long ago = lastRxMs ? (millis() - lastRxMs) / 1000 : 0;
  char page[512];
  snprintf(page, sizeof(page),
    "<!DOCTYPE html><html><head>"
    "<meta http-equiv='refresh' content='5'>"
    "<style>body{font-family:monospace;background:#111;color:#0f0;padding:24px}"
    "h2{color:#0ff}b{color:#ff0}</style></head><body>"
    "<h2>&#9651; Rahat Sentinel Node</h2>"
    "<p>Location : <b>%s</b></p>"
    "<p>Severity : <b>%s</b></p>"
    "<p>Updated  : <b>%lus ago</b></p>"
    "</body></html>",
    latestLoc, latestSev, ago
  );
  server.send(200, "text/html", page);
}

void handleData() {
  char buf[128];
  snprintf(buf, sizeof(buf), "%s,%s", latestLoc, latestSev);
  server.send(200, "text/plain", buf);
}

void handleLocationPost() {
  if (!server.hasArg("plain")) {
    server.send(400, "text/plain", "No body");
    return;
  }
  String body = server.arg("plain");
  body.trim();
  parsePayload(body);
  server.send(200, "text/plain", "OK");
}

// ── Setup ─────────────────────────────────────────────────────────────────────

void setup() {
  Serial.begin(115200);
  Serial.println("\n[Rahat] Booting...");

  WiFi.softAP(AP_SSID, AP_PASS);
  Serial.printf("[WiFi] AP: %s  IP: %s\n", AP_SSID,
                WiFi.softAPIP().toString().c_str());

  server.on("/",         HTTP_GET,  handleRoot);
  server.on("/data",     HTTP_GET,  handleData);
  server.on("/location", HTTP_POST, handleLocationPost);
  server.begin();
  Serial.println("[HTTP] Ready on port 80");
}

// ── Loop — non-blocking ───────────────────────────────────────────────────────

void loop() {
  server.handleClient();
}
