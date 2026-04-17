/**
 * Rahat Sentinel — Node.js Disaster Response Simulation Pipeline
 *
 * Connects to the Sentinel FastAPI WebSocket or runs entirely offline via --test.
 * Simulates the full journey: Satellite Detection → AI Grid Analysis →
 * Network Failure → BLE Mesh Propagation → Real-World Rescue deployment.
 *
 * Usage:
 *   node receiver.js            # live mode
 *   node receiver.js --test     # demo mode
 */

const WebSocket = require("ws");

// ─── Config ──────────────────────────────────────────────────────────
const WS_URL = "ws://localhost:8000/ws/alerts";
const RECONNECT_DELAY_MS = 2000;
const SIMULATE_INTERVAL_MS = 12000;
const MAX_RETRY_LOG = 3; 

// Region Bounding Box (Bhilai-Durg)
const BOUNDS = {
  minLat: 21.15,
  maxLat: 21.30,
  minLon: 81.25,
  maxLon: 81.45,
};
const GRID_SIZE = 20;

// Explanation reasons for the "AI Interpretability" log
const AI_REASONS = [
  "High surface temperature anomaly detected",
  "Vegetation index drop + sudden heat spike",
  "Rapid thermal expansion matching wildfire signature",
  "Atmospheric smoke density exceeds threshold",
  "Unseasonal thermal cluster detected near built-up area"
];

// ─── ANSI Colours ────────────────────────────────────────────────────
const C = {
  reset: "\x1b[0m",
  bold: "\x1b[1m",
  dim: "\x1b[2m",
  red: "\x1b[91m",
  yellow: "\x1b[93m",
  green: "\x1b[92m",
  cyan: "\x1b[96m",
  magenta: "\x1b[95m",
  white: "\x1b[97m",
  bgRed: "\x1b[41m",
};

// ─── Helpers ─────────────────────────────────────────────────────────

function ts() {
  return new Date().toLocaleTimeString("en-GB", { hour12: false });
}

function log(msg) {
  console.log(`  ${C.dim}[${ts()}]${C.reset}  ${msg}`);
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

// ─── Grid Logic ──────────────────────────────────────────────────────

function getGridCell(lat, lon) {
  const cLat = Math.max(BOUNDS.minLat, Math.min(BOUNDS.maxLat, lat));
  const cLon = Math.max(BOUNDS.minLon, Math.min(BOUNDS.maxLon, lon));
  const row = Math.floor(((BOUNDS.maxLat - cLat) / (BOUNDS.maxLat - BOUNDS.minLat)) * (GRID_SIZE - 1));
  const col = Math.floor(((cLon - BOUNDS.minLon) / (BOUNDS.maxLon - BOUNDS.minLon)) * (GRID_SIZE - 1));
  return { row, col, lat: cLat, lon: cLon };
}

function getGridCoords(row, col) {
  const latDelta = (BOUNDS.maxLat - BOUNDS.minLat) / (GRID_SIZE - 1);
  const lonDelta = (BOUNDS.maxLon - BOUNDS.minLon) / (GRID_SIZE - 1);
  return { lat: BOUNDS.maxLat - row * latDelta, lon: BOUNDS.minLon + col * lonDelta };
}

// ─── BLE Payload Encoder ─────────────────────────────────────────────

function encodeBLE(data, grid) {
  const compact = {
    r: "Bhilai-Durg",
    s: (typeof data.severity === "number" ? data.severity : 0).toFixed(3),
    t: data.timestamp || new Date().toISOString(),
    gx: grid.col,
    gy: grid.row,
  };
  return Buffer.from(JSON.stringify(compact)).toString("base64");
}

// ─── Alert Processing Pipeline ───────────────────────────────────────

let processing = false;

async function processAlert(data, isClustered = false) {
  if (processing) return; 
  processing = true;

  try {
    const severity = data && typeof data.severity === "number" ? data.severity : 0;
    const timestamp = (data && data.timestamp) || new Date().toISOString();

    let grid;
    if (data && typeof data.lat === "number" && typeof data.lon === "number" && !isNaN(data.lat)) {
      grid = getGridCell(data.lat, data.lon);
    } else if (data && typeof data.grid_row === "number" && typeof data.grid_col === "number") {
      grid = { row: data.grid_row, col: data.grid_col, ...getGridCoords(data.grid_row, data.grid_col) };
    } else {
      grid = getGridCell((BOUNDS.minLat + BOUNDS.maxLat) / 2, (BOUNDS.minLon + BOUNDS.maxLon) / 2);
    }

    const sc = severity >= 0.8 ? C.red : severity >= 0.5 ? C.yellow : C.green;
    const reason = AI_REASONS[Math.floor(Math.random() * AI_REASONS.length)];

    // 1. Initial Timeline
    console.log();
    log(`${C.cyan}🛰️  Satellite anomaly detected${C.reset}`);
    await sleep(600);
    log(`${C.magenta}🤖 AI model flagged risk${C.reset}`);
    await sleep(600);
    log(`${C.yellow}🧠 Analyzing spatial patterns...${C.reset}`);
    await sleep(400);

    if (isClustered) {
      console.log(`\n  ${C.bold}${C.red}⚠️ Multiple hotspots detected across grid${C.reset}`);
      await sleep(600);
    }

    // 2. Alert Generation
    console.log();
    console.log(`  ${sc}${C.bold}${"=".repeat(30)}${C.reset}`);
    console.log(`  ${sc}${C.bold}🚨 EMERGENCY ALERT TRIGGERED${C.reset}`);
    console.log(`  ${sc}${C.bold}${"=".repeat(30)}${C.reset}`);
    console.log();
    console.log(`  ${C.dim}🛰️${C.reset} Region:     ${C.cyan}Bhilai-Durg${C.reset}`);
    console.log(`  ${C.dim}📍${C.reset} Grid Cell:  [${grid.row}, ${grid.col}]   ${C.dim}(20x20 matrix)${C.reset}`);
    console.log(`  ${C.dim}🌡️${C.reset} Severity:   ${sc}${severity.toFixed(3)}${C.reset}`);
    console.log(`  ${C.dim}🕒${C.reset} Time:       ${timestamp}`);
    console.log(`  ${C.dim}📌${C.reset} Location:   ${grid.lat.toFixed(4)}°N, ${grid.lon.toFixed(4)}°E`);
    console.log();
    log(`🧠 ${C.magenta}Reason: ${C.reset}${C.dim}${reason}${C.reset}`);
    console.log();

    if (severity > 0.8) {
      console.log(`  ${C.bgRed}${C.white}${C.bold} 🔥 CRITICAL ALERT — IMMEDIATE ACTION REQUIRED ${C.reset}`);
      console.log();
    }
    
    if (isClustered && severity > 0.85) {
      await sleep(400);
      console.log(`  ${C.bgRed}${C.white}${C.bold} 🚨 EMERGENCY LEVEL ESCALATED — MULTIPLE CRITICAL ZONES 🚨 ${C.reset}`);
      console.log();
    }

    // 3. Network Failure 
    await sleep(800);
    log(`📴 ${C.red}Network failure detected in sector [${grid.row}, ${grid.col}]${C.reset}`);
    await sleep(600);
    log(`🔁 ${C.cyan}Switching to offline BLE mesh...${C.reset}`);
    console.log();

    // 4. BLE Payload & Propagation
    let payload = "";
    try {
      payload = encodeBLE(data, grid);
      log(`📡 ${C.magenta}BLE PAYLOAD READY: ${C.reset}${C.dim}${payload}${C.reset}`);
    } catch (err) {
      log(`${C.red}✖ BLE encode failed: ${err.message}${C.reset}`);
    }

    await sleep(1000);
    log(`${C.cyan}📡 Broadcasting via BLE mesh...${C.reset}`);
    console.log();
    
    await sleep(1000);
    log(`  ${C.green}📲 Device 2 received alert${C.reset}  ${C.dim}(hop 1)${C.reset}`);
    await sleep(800);
    log(`  ${C.green}📲 Device 3 received alert${C.reset}  ${C.dim}(hop 2)${C.reset}`);
    await sleep(800);
    log(`  ${C.green}📲 Device 4 received alert${C.reset}  ${C.dim}(hop 3)${C.reset}`);
    console.log();

    // 5. Real World Impact
    await sleep(800);
    log(`🚑 ${C.yellow}Rescue teams notified at staging area${C.reset}`);
    await sleep(600);
    log(`📢 ${C.yellow}Nearby civilians alerted (150m radius)${C.reset}`);
    await sleep(600);
    log(`🛟 ${C.green}${C.bold}Emergency response initiated${C.reset}`);
    console.log();

  } finally {
    processing = false;
  }
}

// ─── WebSocket Message Handler ───────────────────────────────────────

function handleMessage(raw) {
  let msg;
  try {
    msg = JSON.parse(raw);
  } catch {
    return; // graceful skip
  }

  if (msg.event && msg.data !== undefined) {
    if (msg.event === "new_alert" && typeof msg.data === "object" && !Array.isArray(msg.data)) {
      processAlert(msg.data);
    }
    return;
  }

  if (msg.region || msg.severity !== undefined) {
    processAlert(msg);
  }
}

// ─── WebSocket Connection ────────────────────────────────────────────

let retryCount = 0;

function connect() {
  log(`🔌 Connecting to WebSocket...`);

  let ws;
  try {
    ws = new WebSocket(WS_URL);
  } catch (err) {
    scheduleReconnect();
    return;
  }

  ws.on("open", () => {
    retryCount = 0;
    log(`✅ ${C.green}Connected to server${C.reset}`);

    const pingInterval = setInterval(() => {
      if (ws.readyState === WebSocket.OPEN) ws.send("ping");
    }, 25000);

    ws.on("close", () => {
      clearInterval(pingInterval);
      log(`${C.yellow}⚠ Disconnected from server${C.reset}`);
      scheduleReconnect();
    });
  });

  ws.on("message", (data) => handleMessage(data.toString()));
  ws.on("error", () => {
    if (retryCount === 0) log(`${C.yellow}⚠ WebSocket connection failed${C.reset}`);
  });
}

function scheduleReconnect() {
  retryCount++;
  if (retryCount === 1) {
    log(`${C.yellow}⚠ Server unavailable — switching to test mode${C.reset}`);
    startTestMode();
    return;
  }
  if (retryCount <= MAX_RETRY_LOG) {
    log(`🔄 Reconnecting in ${RECONNECT_DELAY_MS / 1000}s...`);
  }
  setTimeout(connect, RECONNECT_DELAY_MS);
}

// ─── Test / Demo Mode (Realistic Anomalies) ──────────────────────────

let demoInterval;
let clusterCenter = null;

function generateSimulatedAlert() {
  // 1/5 chance to trigger a hotspot cluster
  const isClustered = Math.random() > 0.8;
  
  if (isClustered && !clusterCenter) {
    clusterCenter = {
      row: Math.floor(Math.random() * (GRID_SIZE - 4)) + 2,
      col: Math.floor(Math.random() * (GRID_SIZE - 4)) + 2,
    };
  } else if (!isClustered) {
    clusterCenter = null;
  }

  let row, col, severity;

  if (clusterCenter) {
    // Generate near cluster center, high severity
    row = clusterCenter.row + Math.floor(Math.random() * 3) - 1;
    col = clusterCenter.col + Math.floor(Math.random() * 3) - 1;
    severity = +(Math.random() * 0.15 + 0.85).toFixed(3); // 0.85 to 1.00
  } else {
    // Generate randomly, lower severity
    row = Math.floor(Math.random() * GRID_SIZE);
    col = Math.floor(Math.random() * GRID_SIZE);
    severity = +(Math.random() * 0.4 + 0.4).toFixed(3); // 0.40 to 0.80
  }
  
  return { data: {
    region: "Bhilai-Durg",
    severity,
    timestamp: new Date().toISOString(),
    grid_row: row,
    grid_col: col
  }, isClustered: !!clusterCenter };
}

function startTestMode() {
  if (demoInterval) return;

  console.log();
  console.log(`  ${C.bold}${C.yellow}${"─".repeat(52)}${C.reset}`);
  console.log(`  ${C.bold}${C.yellow}  ⚡  TEST MODE — simulating full pipeline${C.reset}`);
  console.log(`  ${C.bold}${C.yellow}${"─".repeat(52)}${C.reset}`);

  // Fire one immediately, then loop
  const initial = generateSimulatedAlert();
  processAlert(initial.data, initial.isClustered);
  
  demoInterval = setInterval(() => {
    const next = generateSimulatedAlert();
    processAlert(next.data, next.isClustered);
  }, SIMULATE_INTERVAL_MS); // Spaced out to let the 11-second timeline finish printing
}

// ─── Entry Point ─────────────────────────────────────────────────────

function main() {
  console.log();
  console.log(`  ${C.bold}${"═".repeat(60)}${C.reset}`);
  console.log(`  ${C.bold}${C.cyan}  Rahat Sentinel — Disaster Response Pipeline Emulator${C.reset}`);
  console.log(`  ${C.bold}${"═".repeat(60)}${C.reset}`);
  console.log(`  ${C.dim}Target:${C.reset}  ${WS_URL}`);
  console.log();

  const testMode = process.argv.includes("--test");

  if (testMode) {
    startTestMode();
  } else {
    connect();
  }
}

main();
