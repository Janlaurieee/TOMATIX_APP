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
  devices: {},
  tomato: {
    detected: false,
    count: null,
  },
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
  state.tomato = { detected, count };
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
  state.devices = devices || {};
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

function currentSensorContext() {
  return {
    temperature: state.temperature.at(-1) ?? null,
    humidity: state.humidity.at(-1) ?? null,
    incomingSunlight: state.light.at(-1) ?? null,
    soilSensors: state.soilSensors,
    tomato: state.tomato,
    devices: state.devices,
    updatedAt: new Date().toISOString(),
  };
}

function initChat() {
  const chatToggle = $("chatToggle");
  const chatClose = $("chatClose");
  const chatPanel = $("chatPanel");
  const chatForm = $("chatForm");
  const chatInput = $("chatInput");

  if (!chatToggle || !chatClose || !chatPanel || !chatForm || !chatInput) return;

  function setOpen(isOpen) {
    chatPanel.classList.toggle("open", isOpen);
    chatToggle.setAttribute("aria-expanded", String(isOpen));
    if (isOpen) chatInput.focus();
  }

  chatToggle.addEventListener("click", () => setOpen(!chatPanel.classList.contains("open")));
  chatClose.addEventListener("click", () => setOpen(false));
  chatForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const message = chatInput.value.trim();
    if (!message) return;

    appendChatMessage(message, "user");
    chatInput.value = "";
    chatInput.disabled = true;

    const thinking = appendChatMessage("Checking the greenhouse readings...", "bot");
    try {
      const answer = await askAssistant(message);
      thinking.textContent = answer;
    } catch (error) {
      console.error(error);
      thinking.textContent = buildLocalGuidance(message, currentSensorContext());
    } finally {
      chatInput.disabled = false;
      chatInput.focus();
    }
  });
}

function appendChatMessage(text, sender) {
  const messages = $("chatMessages");
  const item = document.createElement("div");
  item.className = `chat-message ${sender}`;
  item.textContent = text;
  messages.appendChild(item);
  messages.scrollTop = messages.scrollHeight;
  return item;
}

async function askAssistant(message) {
  const response = await fetch("/api/chat", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      message,
      context: currentSensorContext(),
    }),
  });
  if (!response.ok) throw new Error(`Assistant request failed: ${response.status}`);
  const data = await response.json();
  return data.reply || buildLocalGuidance(message, currentSensorContext());
}

function buildLocalGuidance(message, context) {
  const text = message.toLowerCase();
  const notes = [];
  if (Number.isFinite(context.temperature)) {
    if (context.temperature < 21) notes.push(`Temperature is low at ${context.temperature.toFixed(1)}°C; check heating and airflow.`);
    else if (context.temperature > 27) notes.push(`Temperature is high at ${context.temperature.toFixed(1)}°C; improve ventilation or shading.`);
    else notes.push(`Temperature is within range at ${context.temperature.toFixed(1)}°C.`);
  }
  if (Number.isFinite(context.humidity)) {
    if (context.humidity < 60) notes.push(`Humidity is low at ${context.humidity.toFixed(1)}%; misting or irrigation may help.`);
    else if (context.humidity > 70) notes.push(`Humidity is high at ${context.humidity.toFixed(1)}%; watch for fungal risk and increase airflow.`);
    else notes.push(`Humidity is in the ideal range at ${context.humidity.toFixed(1)}%.`);
  }
  const soilValues = context.soilSensors.filter(Number.isFinite);
  if (soilValues.length) {
    const average = soilValues.reduce((sum, value) => sum + value, 0) / soilValues.length;
    if (average < 50) notes.push(`Soil moisture average is low at ${average.toFixed(1)}%; inspect irrigation.`);
    else if (average > 75) notes.push(`Soil moisture average is high at ${average.toFixed(1)}%; avoid overwatering.`);
    else notes.push(`Soil moisture average is healthy at ${average.toFixed(1)}%.`);
  }
  if (context.tomato.detected || text.includes("ripe")) {
    notes.push(context.tomato.detected ? "A ripe tomato is detected and ready for checking." : "No ripe tomato is detected in the latest reading.");
  }
  return notes.length
    ? notes.join(" ")
    : "I am connected to the same Tomatix readings as the dashboard. Ask about temperature, humidity, sunlight, soil moisture, devices, or ripe tomatoes.";
}

window.addEventListener("resize", drawTrendChart);
initChat();
loadDashboard();
setInterval(loadDashboard, 30000);
