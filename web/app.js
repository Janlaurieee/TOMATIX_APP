const DATABASE_URL = "https://tomatix-8a161-default-rtdb.asia-southeast1.firebasedatabase.app";
const APP_PATH = "tomatix";
const LIGHT_KEYS = ["light", "incomingSunlight", "incoming_sunlight", "sunlight", "lightSensor", "light_sensor"];
const SENSOR_COUNT = 8;
const TREND_LIMIT = 24;

const state = {
  temperature: [],
  humidity: [],
  light: [],
  soilSensors: Array(SENSOR_COUNT).fill(null),
};

const colors = {
  temperature: "#f28b35",
  humidity: "#3487d8",
  light: "#d9a915",
  grid: "#dce8de",
  text: "#65756c",
};

const $ = (id) => document.getElementById(id);

function endpoint(path, query = "") {
  return `${DATABASE_URL}/${APP_PATH}/${path}.json${query}`;
}

async function fetchJson(path, query = "") {
  const response = await fetch(endpoint(path, query), { cache: "no-store" });
  if (!response.ok) throw new Error(`Firebase request failed: ${response.status}`);
  return response.json();
}

function recordsToArray(records) {
  if (!records || typeof records !== "object") return [];
  return Object.entries(records)
    .map(([id, value]) => ({ id, ...(value || {}) }))
    .sort((a, b) => compareRecordDate(a, b));
}

function compareRecordDate(a, b) {
  const aKey = `${a.date || ""} ${a.time || ""} ${a.id || ""}`;
  const bKey = `${b.date || ""} ${b.time || ""} ${b.id || ""}`;
  return aKey.localeCompare(bKey);
}

function readNumber(record, keys) {
  for (const key of keys) {
    const value = record?.[key];
    if (typeof value === "number" && Number.isFinite(value)) return value;
    if (typeof value === "string" && Number.isFinite(Number(value))) return Number(value);
  }
  return null;
}

function formatNumber(value, suffix = "") {
  return Number.isFinite(value) ? `${value.toFixed(1)}${suffix}` : "--";
}

function statusText(value) {
  return value ? "On" : "Off";
}

function setConnection(isOnline) {
  $("connectionDot").className = `status-dot ${isOnline ? "online" : "offline"}`;
  $("connectionLabel").textContent = isOnline ? "Online" : "Offline";
  $("lastUpdated").textContent = isOnline
    ? `Updated ${new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`
    : "Could not reach Firebase";
}

async function loadDashboard() {
  try {
    await Promise.all([loadSensors(), loadDevices()]);
    setConnection(true);
    drawTrendChart();
  } catch (error) {
    console.error(error);
    setConnection(false);
  }
}

async function loadSensors() {
  const latestQuery = "?orderBy=%22%24key%22&limitToLast=24";
  const [temperatureHumidityRecords, soilRecords, ...lightRecordSets] = await Promise.all([
    fetchJson("sensors/temperatureHumidity/records", latestQuery),
    fetchJson("sensors/soilMoisture/records", latestQuery),
    ...LIGHT_KEYS.map((key) => fetchJson(`sensors/${key}/records`, latestQuery).catch(() => null)),
  ]);

  const temperatureHumidity = recordsToArray(temperatureHumidityRecords);
  const soil = recordsToArray(soilRecords);
  const light = lightRecordSets.flatMap(recordsToArray).sort(compareRecordDate).slice(-TREND_LIMIT);

  state.temperature = temperatureHumidity
    .map((record) => readNumber(record, ["temperature", "temperatureC", "temperature_c", "temp", "tempC", "value"]))
    .filter(Number.isFinite)
    .slice(-TREND_LIMIT);
  state.humidity = temperatureHumidity
    .map((record) => readNumber(record, ["humidity", "humidityPercent", "humidity_percent", "humid", "value"]))
    .filter(Number.isFinite)
    .slice(-TREND_LIMIT);
  state.light = light
    .map((record) => readNumber(record, ["incomingSunlight", "incoming_sunlight", "sunlight", "lightIntensity", "light", "lux", "value", "reading"]))
    .filter(Number.isFinite)
    .slice(-TREND_LIMIT);
  state.soilSensors = buildSoilSensors(soil);

  const latestTemperature = state.temperature.at(-1);
  const latestHumidity = state.humidity.at(-1);
  const latestLight = state.light.at(-1);

  $("temperatureValue").textContent = formatNumber(latestTemperature, "°C");
  $("humidityValue").textContent = formatNumber(latestHumidity, "%");
  $("lightValue").textContent = formatNumber(latestLight, " k lux");

  const latestTomato = temperatureHumidity.at(-1) || {};
  const count = readNumber(latestTomato, ["ripeTomatoCount", "ripe_tomato_count", "tomatoCount", "detections"]);
  const detected = Boolean(latestTomato.ripeTomatoDetected || latestTomato.ripe_tomato_detected || count > 0);
  $("tomatoValue").textContent = detected ? count || "Detected" : "None";
  $("tomatoHint").textContent = detected ? "Ready for checking" : "No ripe tomato detected";

  renderPlots();
}

function buildSoilSensors(records) {
  const values = Array(SENSOR_COUNT).fill(null);
  records.slice(-8).forEach((record, fallbackIndex) => {
    const plot = readNumber(record, ["plot", "plotIndex", "plot_index", "plotNumber", "plot_number"]);
    const indexed = Array.from({ length: SENSOR_COUNT }, (_, index) =>
      readNumber(record, [`sensor${index + 1}`, `sensor_${index + 1}`, `soilSensor${index + 1}`, `moisture${index + 1}`, `s${index + 1}`])
    );

    indexed.forEach((value, index) => {
      if (Number.isFinite(value)) values[index] = value;
    });

    if (plot && indexed.every((value) => value == null)) {
      const start = (plot - 1) * 2;
      values[start] = readNumber(record, ["sensor1", "moisture1", "leftSensor", "left", "value"]);
      values[start + 1] = readNumber(record, ["sensor2", "moisture2", "rightSensor", "right"]);
    } else if (!plot && indexed.every((value) => value == null)) {
      const value = readNumber(record, ["soilMoisture", "soil_moisture", "moisture", "value", "reading"]);
      if (Number.isFinite(value) && fallbackIndex < SENSOR_COUNT) values[fallbackIndex] = value;
    }
  });
  return values;
}

function renderPlots() {
  const grid = $("plotGrid");
  grid.innerHTML = "";
  for (let plot = 0; plot < 4; plot += 1) {
    const first = plot * 2;
    const card = document.createElement("article");
    card.className = "plot-card";
    card.innerHTML = `
      <h4>Plot ${plot + 1}</h4>
      <div class="sensor-row"><span>Sensor ${first + 1}</span><strong>${formatNumber(state.soilSensors[first], "%")}</strong></div>
      <div class="sensor-row"><span>Sensor ${first + 2}</span><strong>${formatNumber(state.soilSensors[first + 1], "%")}</strong></div>
    `;
    grid.appendChild(card);
  }
}

async function loadDevices() {
  const devices = await fetchJson("devices");
  setDevice("pumpStatus", devices?.pumpStatus);
  setDevice("irrigationStatus", devices?.irrigationStatus);
  setDevice("fanStatus", devices?.fanStatus);
  setDevice("cameraStatus", devices?.cameraStatus);
}

function setDevice(id, value) {
  const element = $(id);
  element.textContent = typeof value === "boolean" ? statusText(value) : "--";
  element.className = value ? "on" : "off";
}

function drawTrendChart() {
  const canvas = $("trendCanvas");
  const ctx = canvas.getContext("2d");
  const width = canvas.width;
  const height = canvas.height;
  ctx.clearRect(0, 0, width, height);

  ctx.fillStyle = "#fbfdfb";
  ctx.fillRect(0, 0, width, height);

  const padding = { top: 34, right: 30, bottom: 42, left: 46 };
  const chartWidth = width - padding.left - padding.right;
  const chartHeight = height - padding.top - padding.bottom;

  ctx.strokeStyle = colors.grid;
  ctx.lineWidth = 1;
  for (let i = 0; i <= 4; i += 1) {
    const y = padding.top + (chartHeight / 4) * i;
    ctx.beginPath();
    ctx.moveTo(padding.left, y);
    ctx.lineTo(width - padding.right, y);
    ctx.stroke();
  }

  drawLine(ctx, state.temperature, colors.temperature, padding, chartWidth, chartHeight);
  drawLine(ctx, state.humidity, colors.humidity, padding, chartWidth, chartHeight);
  drawLine(ctx, state.light, colors.light, padding, chartWidth, chartHeight);

  ctx.fillStyle = colors.text;
  ctx.font = "600 22px Inter, sans-serif";
  if (!state.temperature.length && !state.humidity.length && !state.light.length) {
    ctx.fillText("No trend data available yet", padding.left, height / 2);
  }
}

function drawLine(ctx, values, color, padding, chartWidth, chartHeight) {
  if (!values.length) return;
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = Math.max(max - min, 0.01);
  const step = chartWidth / Math.max(values.length - 1, 1);

  ctx.beginPath();
  values.forEach((value, index) => {
    const x = padding.left + step * index;
    const y = padding.top + chartHeight - ((value - min) / range) * chartHeight;
    if (index === 0) ctx.moveTo(x, y);
    else ctx.lineTo(x, y);
  });
  ctx.strokeStyle = color;
  ctx.lineWidth = 5;
  ctx.lineCap = "round";
  ctx.lineJoin = "round";
  ctx.stroke();

  const last = values.at(-1);
  const x = padding.left + step * (values.length - 1);
  const y = padding.top + chartHeight - ((last - min) / range) * chartHeight;
  ctx.beginPath();
  ctx.arc(x, y, 7, 0, Math.PI * 2);
  ctx.fillStyle = color;
  ctx.fill();
}

window.addEventListener("resize", drawTrendChart);
loadDashboard();
setInterval(loadDashboard, 30000);
