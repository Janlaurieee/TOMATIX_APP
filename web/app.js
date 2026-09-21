const DATABASE_URL = "https://tomatix-8a161-default-rtdb.asia-southeast1.firebasedatabase.app";
const APP_PATH = "tomatix";
const LIGHT_KEYS = ["light", "incomingSunlight", "incoming_sunlight", "sunlight", "lightSensor", "light_sensor"];
const SENSOR_COUNT = 8;
const TREND_LIMIT = 24;

const defaultThresholds = {
  tempMin: 21,
  tempMax: 27,
  humidityMin: 60,
  humidityMax: 70,
  soilMoistureMin: 50,
  soilMoistureMax: 75,
  lightIntensityMin: 21,
  lightIntensityMax: 27,
};

const thresholdFields = [
  ["Temperature", "tempMin", "tempMax", "C"],
  ["Humidity", "humidityMin", "humidityMax", "%"],
  ["Incoming Sunlight", "lightIntensityMin", "lightIntensityMax", "k lux"],
  ["Soil Moisture", "soilMoistureMin", "soilMoistureMax", "%"],
];

const state = {
  temperature: [],
  humidity: [],
  light: [],
  soilSensors: Array(SENSOR_COUNT).fill(null),
  devices: {},
  controls: {
    pumpSpeed: 50,
    fanSpeed: 60,
    chemicalType: "Fertilizer",
    mixingTime: 30,
    concentration: 25,
    cameraZoom: 100,
  },
  thresholds: { ...defaultThresholds },
  notifications: {
    email: true,
    push: true,
    sms: false,
    criticalOnly: false,
  },
  logs: [],
  analyticsRange: "day",
  analyticsDate: todayString(),
  analytics: {
    temperature: [],
    humidity: [],
    light: [],
    soil: [],
    records: [],
  },
  tomato: {
    detected: false,
    count: null,
  },
  mixing: {
    interval: null,
    remaining: 0,
    paused: false,
  },
};

const colors = {
  temperature: "#f28b35",
  humidity: "#3487d8",
  light: "#d9a915",
  soil: "#2f8f46",
  grid: "#dce8de",
  text: "#65756c",
};

const $ = (id) => document.getElementById(id);

function endpoint(path, query = "") {
  return `${DATABASE_URL}/${APP_PATH}/${path}.json${query}`;
}

async function firebaseRequest(path, options = {}, query = "") {
  const response = await fetch(endpoint(path, query), {
    cache: "no-store",
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {}),
    },
  });
  if (!response.ok) throw new Error(`Firebase request failed: ${response.status}`);
  return response.json();
}

function fetchJson(path, query = "") {
  return firebaseRequest(path, {}, query);
}

function patchJson(path, value) {
  return firebaseRequest(path, { method: "PATCH", body: JSON.stringify(value) });
}

function putJson(path, value) {
  return firebaseRequest(path, { method: "PUT", body: JSON.stringify(value) });
}

function deleteJson(path) {
  return firebaseRequest(path, { method: "DELETE" });
}

function postJson(path, value) {
  return firebaseRequest(path, { method: "POST", body: JSON.stringify(value) });
}

function recordsToArray(records) {
  if (!records || typeof records !== "object") return [];
  return Object.entries(records)
    .map(([id, value]) => ({ id, ...(value || {}) }))
    .sort(compareRecordDate);
}

function compareRecordDate(a, b) {
  const aKey = `${a.date || ""} ${a.time || ""} ${a.id || ""}`;
  const bKey = `${b.date || ""} ${b.time || ""} ${b.id || ""}`;
  return aKey.localeCompare(bKey);
}

function timestampKey(record) {
  return `${record.date || ""} ${record.time || ""}`.trim() || record.id || "";
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

function todayString() {
  return new Date().toISOString().slice(0, 10);
}

function nowParts() {
  const now = new Date();
  return {
    date: now.toISOString().slice(0, 10),
    time: now.toTimeString().slice(0, 8),
    full: `${now.toISOString().slice(0, 10)} ${now.toTimeString().slice(0, 8)}`,
  };
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
    await Promise.all([loadSensors(), loadDevices(), loadSettings(), loadLogs()]);
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
    fetchJson("sensors/temperatureHumidity/records", latestQuery).catch(() => null),
    fetchJson("sensors/soilMoisture/records", latestQuery).catch(() => null),
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

  $("temperatureValue").textContent = formatNumber(latestTemperature, " C");
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
  const devices = await fetchJson("devices").catch(() => null);
  state.devices = {
    pumpStatus: devices?.pumpStatus ?? true,
    irrigationStatus: devices?.irrigationStatus ?? false,
    fanStatus: devices?.fanStatus ?? true,
    cameraStatus: devices?.cameraStatus ?? true,
    chemicalMixerStatus: devices?.chemicalMixerStatus ?? false,
    chemicalDistributionStatus: devices?.chemicalDistributionStatus ?? false,
  };
  syncDeviceUi();
}

function syncDeviceUi() {
  setDevice("pumpStatus", state.devices.pumpStatus);
  setDevice("irrigationStatus", state.devices.irrigationStatus);
  setDevice("fanStatus", state.devices.fanStatus);
  setDevice("cameraStatus", state.devices.cameraStatus);
  setChecked("pumpControl", state.devices.pumpStatus);
  setChecked("irrigationControl", state.devices.irrigationStatus);
  setChecked("fanControl", state.devices.fanStatus);
  setChecked("chemicalDistributionToggle", state.devices.chemicalDistributionStatus);
  setText("pumpControlText", state.devices.pumpStatus ? "Pump Active" : "Pump Off");
  setText("irrigationControlText", state.devices.irrigationStatus ? "Irrigating" : "Irrigation Off");
  setText("fanControlText", state.devices.fanStatus ? "Fan Running" : "Fan Off");
  setText("chemicalDistributionText", state.devices.chemicalDistributionStatus ? "Distribution On" : "Distribution Off");
}

function setDevice(id, value) {
  const element = $(id);
  if (!element) return;
  element.textContent = typeof value === "boolean" ? statusText(value) : "--";
  element.className = value ? "on" : "off";
}

function setChecked(id, value) {
  const element = $(id);
  if (element) element.checked = Boolean(value);
}

function setText(id, value) {
  const element = $(id);
  if (element) element.textContent = value;
}

async function updateDevice(key, value, label) {
  state.devices[key] = value;
  syncDeviceUi();
  await patchJson("devices", { [key]: value });
  await addLog(`${label} turned ${value ? "on" : "off"}`, "info");
}

async function loadSettings() {
  const [thresholds, notifications, controls] = await Promise.all([
    fetchJson("settings/thresholds").catch(() => null),
    fetchJson("settings/notifications").catch(() => null),
    fetchJson("controls").catch(() => null),
  ]);
  state.thresholds = { ...defaultThresholds, ...(thresholds || {}) };
  state.notifications = { ...state.notifications, ...(notifications || {}) };
  state.controls = { ...state.controls, ...(controls || {}) };
  renderThresholds();
  syncSettingsUi();
}

function syncSettingsUi() {
  setChecked("notifyEmail", state.notifications.email);
  setChecked("notifyPush", state.notifications.push);
  setChecked("notifySms", state.notifications.sms);
  setChecked("notifyCritical", state.notifications.criticalOnly);
  setRange("pumpSpeed", state.controls.pumpSpeed, "pumpSpeedValue");
  setRange("fanSpeed", state.controls.fanSpeed, "fanSpeedValue");
  setRange("concentration", state.controls.concentration, "concentrationValue");
  setRange("cameraZoom", state.controls.cameraZoom, "cameraZoomValue");
  if ($("chemicalType")) $("chemicalType").value = state.controls.chemicalType || "Fertilizer";
  if ($("mixingTime")) $("mixingTime").value = state.controls.mixingTime || 30;
  if ($("analyticsDate")) $("analyticsDate").value = state.analyticsDate;
}

function setRange(id, value, labelId) {
  const input = $(id);
  if (input) input.value = value;
  setText(labelId, `${value}%`);
}

function renderThresholds() {
  const grid = $("thresholdGrid");
  if (!grid) return;
  grid.innerHTML = thresholdFields
    .map(([label, minKey, maxKey, unit]) => `
      <div class="threshold-box">
        <strong>${label}</strong>
        <div class="threshold-inputs">
          <label class="field-label">Min ${unit}<input id="threshold_${minKey}" type="number" value="${state.thresholds[minKey]}"></label>
          <label class="field-label">Max ${unit}<input id="threshold_${maxKey}" type="number" value="${state.thresholds[maxKey]}"></label>
        </div>
      </div>
    `)
    .join("");
}

async function saveThresholds() {
  thresholdFields.forEach(([, minKey, maxKey]) => {
    state.thresholds[minKey] = Number($(`threshold_${minKey}`).value);
    state.thresholds[maxKey] = Number($(`threshold_${maxKey}`).value);
  });
  await putJson("settings/thresholds", state.thresholds);
  await addLog("Environmental thresholds saved", "success");
}

async function resetThresholds() {
  state.thresholds = { ...defaultThresholds };
  renderThresholds();
  await putJson("settings/thresholds", state.thresholds);
  await addLog("Environmental thresholds reset to defaults", "info");
}

async function saveNotifications() {
  state.notifications = {
    email: $("notifyEmail").checked,
    push: $("notifyPush").checked,
    sms: $("notifySms").checked,
    criticalOnly: $("notifyCritical").checked,
  };
  await putJson("settings/notifications", state.notifications);
  await addLog("Notification preferences saved", "success");
}

async function loadLogs() {
  const logs = await fetchJson("logs").catch(() => null);
  state.logs = recordsToArray(logs).sort((a, b) => `${b.time || ""}`.localeCompare(`${a.time || ""}`));
  renderLogs();
}

function renderLogs() {
  const list = $("logsList");
  if (!list) return;
  $("logCount").textContent = `${state.logs.length} entries`;
  list.innerHTML = state.logs.length
    ? state.logs.slice(0, 80).map((log) => `
      <div class="log-entry">
        <strong>${escapeHtml(log.event || "System event")}</strong>
        <span>${escapeHtml(log.time || "")} - ${escapeHtml(log.type || "info")}</span>
      </div>
    `).join("")
    : "<div class=\"log-entry\"><strong>No records yet.</strong><span>Firebase logs will appear here.</span></div>";
}

async function addLog(event, type = "info") {
  const time = nowParts().full;
  await postJson("logs", { time, event, type });
  await loadLogs();
}

async function clearLogs() {
  await deleteJson("logs");
  state.logs = [];
  renderLogs();
}

function exportLogs() {
  const csv = ["Time,Type,Event", ...state.logs.map((log) => `${csvCell(log.time)},${csvCell(log.type)},${csvCell(log.event)}`)].join("\n");
  downloadFile("tomatix_logs.csv", csv, "text/csv");
}

function selectedDateRange() {
  const date = new Date(`${state.analyticsDate}T00:00:00`);
  const start = new Date(date);
  if (state.analyticsRange === "week") {
    const day = start.getDay() || 7;
    start.setDate(start.getDate() - day + 1);
  } else if (state.analyticsRange === "month") {
    start.setDate(1);
  } else if (state.analyticsRange === "year") {
    start.setMonth(0, 1);
  }

  const end = new Date(start);
  if (state.analyticsRange === "day") end.setDate(start.getDate() + 1);
  if (state.analyticsRange === "week") end.setDate(start.getDate() + 7);
  if (state.analyticsRange === "month") end.setMonth(start.getMonth() + 1);
  if (state.analyticsRange === "year") end.setFullYear(start.getFullYear() + 1);
  return [start.toISOString().slice(0, 10), new Date(end.getTime() - 86400000).toISOString().slice(0, 10)];
}

function dateQuery() {
  const [start, end] = selectedDateRange();
  return `?orderBy=%22date%22&startAt=%22${start}%22&endAt=%22${end}%22`;
}

async function loadAnalytics() {
  const query = dateQuery();
  const [temperatureHumidityRecords, soilRecords, ...lightRecordSets] = await Promise.all([
    fetchJson("sensors/temperatureHumidity/records", query).catch(() => null),
    fetchJson("sensors/soilMoisture/records", query).catch(() => null),
    ...LIGHT_KEYS.map((key) => fetchJson(`sensors/${key}/records`, query).catch(() => null)),
  ]);
  const tempHum = recordsToArray(temperatureHumidityRecords);
  const soil = recordsToArray(soilRecords);
  const light = lightRecordSets.flatMap(recordsToArray).sort(compareRecordDate);

  state.analytics.temperature = tempHum.map((record) => readNumber(record, ["temperature", "temperatureC", "temperature_c", "temp", "tempC", "value"])).filter(Number.isFinite);
  state.analytics.humidity = tempHum.map((record) => readNumber(record, ["humidity", "humidityPercent", "humidity_percent", "humid", "value"])).filter(Number.isFinite);
  state.analytics.light = light.map((record) => readNumber(record, ["incomingSunlight", "incoming_sunlight", "sunlight", "lightIntensity", "light", "lux", "value", "reading"])).filter(Number.isFinite);
  state.analytics.soil = soil
    .map((record) => buildSoilSensors([record]).filter(Number.isFinite))
    .filter((values) => values.length)
    .map((values) => values.reduce((sum, value) => sum + value, 0) / values.length);
  state.analytics.records = mergeAnalyticsRecords(tempHum, light, soil);
  renderAnalytics();
}

function mergeAnalyticsRecords(tempHum, light, soil) {
  const map = new Map();
  function ensure(record) {
    const key = timestampKey(record);
    if (!map.has(key)) {
      map.set(key, { timestamp: key, temperature: "", humidity: "", light: "", soilSensors: Array(SENSOR_COUNT).fill("") });
    }
    return map.get(key);
  }
  tempHum.forEach((record) => {
    const row = ensure(record);
    row.temperature = readNumber(record, ["temperature", "temperatureC", "temperature_c", "temp", "tempC", "value"]) ?? "";
    row.humidity = readNumber(record, ["humidity", "humidityPercent", "humidity_percent", "humid", "value"]) ?? "";
  });
  light.forEach((record) => {
    const row = ensure(record);
    row.light = readNumber(record, ["incomingSunlight", "incoming_sunlight", "sunlight", "lightIntensity", "light", "lux", "value", "reading"]) ?? "";
  });
  soil.forEach((record, index) => {
    const row = ensure(record);
    const readings = buildSoilSensors([record]);
    const plot = readNumber(record, ["plot", "plotIndex", "plot_index", "plotNumber", "plot_number"]) || index + 1;
    readings.forEach((value, sensorIndex) => {
      if (Number.isFinite(value)) row.soilSensors[sensorIndex] = value;
    });
    if (readings.every((value) => value == null)) {
      const first = (plot - 1) * 2;
      row.soilSensors[first] = readNumber(record, ["sensor1", "moisture1", "leftSensor", "left", "value"]) ?? "";
      row.soilSensors[first + 1] = readNumber(record, ["sensor2", "moisture2", "rightSensor", "right"]) ?? "";
    }
  });
  return Array.from(map.values()).sort((a, b) => `${b.timestamp}`.localeCompare(`${a.timestamp}`));
}

function renderAnalytics() {
  const grid = $("analyticsGrid");
  if (!grid) return;
  const cards = [
    ["Temperature", state.analytics.temperature, " C", colors.temperature],
    ["Humidity", state.analytics.humidity, "%", colors.humidity],
    ["Incoming Sunlight", state.analytics.light, " k lux", colors.light],
    ["Soil Moisture", state.analytics.soil, "%", colors.soil],
  ];
  grid.innerHTML = cards.map(([label, values, unit, color]) => analyticsCard(label, values, unit, color)).join("");
}

function analyticsCard(label, values, unit, color) {
  const hasData = values.length > 0;
  const avg = hasData ? values.reduce((sum, value) => sum + value, 0) / values.length : 0;
  const max = hasData ? Math.max(...values) : 0;
  const min = hasData ? Math.min(...values) : 0;
  const maxBar = Math.max(...values, 1);
  const bars = values.slice(-12).map((value) => `<span style="height:${Math.max((value / maxBar) * 100, 8)}%;background:${color}"></span>`).join("");
  return `
    <div class="analytics-box">
      <strong>${label}</strong>
      <span>Avg ${hasData ? avg.toFixed(1) + unit : "--"} | Max ${hasData ? max.toFixed(1) + unit : "--"} | Min ${hasData ? min.toFixed(1) + unit : "--"}</span>
      <div class="mini-bars">${bars || "<span style=\"height:8%\"></span>"}</div>
    </div>
  `;
}

function saveSensorCsv() {
  const header = ["Timestamp", "Temperature (C)", "Humidity (%)", "Incoming Sunlight (k lux)", ...Array.from({ length: SENSOR_COUNT }, (_, i) => `sensor${i + 1} (%)`)];
  const rows = state.analytics.records.map((record) => [
    record.timestamp,
    record.temperature,
    record.humidity,
    record.light,
    ...record.soilSensors,
  ].map(csvCell).join(","));
  downloadFile(`tomatix_sensors_${Date.now()}.csv`, [header.join(","), ...rows].join("\n"), "text/csv");
}

function csvCell(value) {
  const text = value == null ? "" : String(value);
  return `"${text.replaceAll("\"", "\"\"")}"`;
}

function downloadFile(filename, content, type) {
  const blob = new Blob([content], { type });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  link.click();
  URL.revokeObjectURL(url);
}

function drawTrendChart() {
  const canvas = $("trendCanvas");
  if (!canvas) return;
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

function initControls() {
  document.querySelectorAll("[data-device-toggle]").forEach((input) => {
    input.addEventListener("change", async () => {
      const key = input.dataset.deviceToggle;
      await updateDevice(key, input.checked, deviceLabel(key));
    });
  });
  document.querySelectorAll("[data-quick-toggle]").forEach((button) => {
    button.addEventListener("click", async () => {
      const key = button.dataset.quickToggle;
      await updateDevice(key, !state.devices[key], deviceLabel(key));
    });
  });

  bindRange("pumpSpeed", "pumpSpeedValue", (value) => saveControl({ pumpSpeed: value }, `Water pump speed set to ${value}%`));
  bindRange("fanSpeed", "fanSpeedValue", (value) => saveControl({ fanSpeed: value }, `Exhaust fan speed set to ${value}%`));
  bindRange("cameraZoom", "cameraZoomValue", (value) => saveControl({ cameraZoom: value }, `Camera zoom set to ${value}%`));
  bindRange("concentration", "concentrationValue", (value) => saveControl({ concentration: value }, `Chemical concentration set to ${value}%`));

  $("chemicalType")?.addEventListener("change", () => saveControl({ chemicalType: $("chemicalType").value }, `Chemical type changed to ${$("chemicalType").value}`));
  $("mixingTime")?.addEventListener("change", () => saveControl({ mixingTime: Math.max(Number($("mixingTime").value), 1) }, `Mixing time set to ${$("mixingTime").value} minutes`));

  document.querySelectorAll("[data-log-action]").forEach((button) => {
    button.addEventListener("click", () => addLog(button.dataset.logAction, "info"));
  });
  document.querySelectorAll("[data-cycle]").forEach((button) => {
    button.addEventListener("click", () => runIrrigationCycle(button.dataset.cycle));
  });
  document.querySelectorAll("[data-mix-action]").forEach((button) => {
    button.addEventListener("click", () => handleMixAction(button.dataset.mixAction));
  });
  $("stopAll")?.addEventListener("click", stopAll);
  $("resetControls")?.addEventListener("click", resetControls);
}

function bindRange(id, labelId, onCommit) {
  const input = $(id);
  if (!input) return;
  input.addEventListener("input", () => setText(labelId, `${input.value}%`));
  input.addEventListener("change", () => onCommit(Number(input.value)));
}

function deviceLabel(key) {
  return {
    pumpStatus: "Water pump",
    irrigationStatus: "Irrigation",
    fanStatus: "Exhaust fan",
    cameraStatus: "Camera",
    chemicalDistributionStatus: "Chemical distribution",
    chemicalMixerStatus: "Chemical mixer",
  }[key] || key;
}

async function saveControl(values, logMessage) {
  state.controls = { ...state.controls, ...values };
  await patchJson("controls", values);
  await addLog(logMessage, "info");
}

async function runIrrigationCycle(cycle) {
  await updateDevice("irrigationStatus", true, "Irrigation");
  await addLog(`${cycle} irrigation cycle started`, "success");
  setTimeout(async () => {
    await updateDevice("irrigationStatus", false, "Irrigation");
    await addLog(`${cycle} irrigation cycle completed`, "success");
  }, 5000);
}

function handleMixAction(action) {
  if (action === "start") startMixing();
  if (action === "pause") pauseMixing();
  if (action === "resume") resumeMixing();
  if (action === "reset") resetMixing();
}

async function startMixing() {
  if (!state.devices.chemicalDistributionStatus || state.mixing.interval) return;
  state.mixing.remaining = Math.max(Number($("mixingTime").value), 1) * 60;
  state.mixing.paused = false;
  await updateDevice("chemicalMixerStatus", true, "Chemical mixer");
  await addLog(`Chemical mixing started for ${$("mixingTime").value} minutes`, "success");
  tickMixing();
  state.mixing.interval = setInterval(tickMixing, 1000);
}

function tickMixing() {
  renderMixingTimer();
  if (state.mixing.paused) return;
  state.mixing.remaining -= 1;
  if (state.mixing.remaining <= 0) resetMixing("Chemical mixing completed", "success");
}

function pauseMixing() {
  if (!state.mixing.interval) return;
  state.mixing.paused = true;
  renderMixingTimer();
  addLog("Chemical mixing paused", "info");
}

function resumeMixing() {
  if (!state.mixing.interval || state.mixing.remaining <= 0) return;
  state.mixing.paused = false;
  renderMixingTimer();
  addLog("Chemical mixing resumed", "info");
}

async function resetMixing(message = "Chemical mixing stopped", type = "info") {
  clearInterval(state.mixing.interval);
  state.mixing.interval = null;
  state.mixing.remaining = 0;
  state.mixing.paused = false;
  renderMixingTimer();
  await updateDevice("chemicalMixerStatus", false, "Chemical mixer");
  await addLog(message, type);
}

function renderMixingTimer() {
  const minutes = Math.floor(state.mixing.remaining / 60);
  const seconds = Math.max(state.mixing.remaining % 60, 0);
  const suffix = state.mixing.paused ? " Paused" : state.mixing.interval ? " Mixing" : " Ready";
  setText("mixingTimer", `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}${suffix}`);
}

async function stopAll() {
  clearInterval(state.mixing.interval);
  state.mixing.interval = null;
  state.mixing.remaining = 0;
  state.mixing.paused = false;
  await patchJson("devices", {
    pumpStatus: false,
    irrigationStatus: false,
    fanStatus: false,
    cameraStatus: false,
    chemicalMixerStatus: false,
    chemicalDistributionStatus: false,
  });
  await addLog("Emergency stop activated", "warning");
  await loadDevices();
}

async function resetControls() {
  state.controls = {
    pumpSpeed: 50,
    fanSpeed: 60,
    chemicalType: "Fertilizer",
    mixingTime: 30,
    concentration: 25,
    cameraZoom: 100,
  };
  await putJson("controls", state.controls);
  syncSettingsUi();
  await addLog("Manual controls reset to defaults", "info");
}

function initSettings() {
  $("saveThresholds")?.addEventListener("click", saveThresholds);
  $("resetThresholds")?.addEventListener("click", resetThresholds);
  $("saveNotifications")?.addEventListener("click", saveNotifications);
  $("exportLogs")?.addEventListener("click", exportLogs);
  $("clearLogs")?.addEventListener("click", clearLogs);
  $("saveData")?.addEventListener("click", saveSensorCsv);
  $("analyticsDate")?.addEventListener("change", async () => {
    state.analyticsDate = $("analyticsDate").value || todayString();
    await loadAnalytics();
  });
  document.querySelectorAll("[data-range]").forEach((button) => {
    button.addEventListener("click", async () => {
      state.analyticsRange = button.dataset.range;
      document.querySelectorAll("[data-range]").forEach((item) => item.classList.toggle("active", item === button));
      await loadAnalytics();
    });
  });
}

function currentSensorContext() {
  return {
    temperature: state.temperature.at(-1) ?? null,
    humidity: state.humidity.at(-1) ?? null,
    incomingSunlight: state.light.at(-1) ?? null,
    soilSensors: state.soilSensors,
    thresholds: state.thresholds,
    notifications: state.notifications,
    tomato: state.tomato,
    devices: state.devices,
    controls: state.controls,
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
    if (context.temperature < state.thresholds.tempMin) notes.push(`Temperature is low at ${context.temperature.toFixed(1)} C; check heating and airflow.`);
    else if (context.temperature > state.thresholds.tempMax) notes.push(`Temperature is high at ${context.temperature.toFixed(1)} C; improve ventilation or shading.`);
    else notes.push(`Temperature is within range at ${context.temperature.toFixed(1)} C.`);
  }
  if (Number.isFinite(context.humidity)) {
    if (context.humidity < state.thresholds.humidityMin) notes.push(`Humidity is low at ${context.humidity.toFixed(1)}%; misting or irrigation may help.`);
    else if (context.humidity > state.thresholds.humidityMax) notes.push(`Humidity is high at ${context.humidity.toFixed(1)}%; watch for fungal risk and increase airflow.`);
    else notes.push(`Humidity is in the ideal range at ${context.humidity.toFixed(1)}%.`);
  }
  const soilValues = context.soilSensors.filter(Number.isFinite);
  if (soilValues.length) {
    const average = soilValues.reduce((sum, value) => sum + value, 0) / soilValues.length;
    if (average < state.thresholds.soilMoistureMin) notes.push(`Soil moisture average is low at ${average.toFixed(1)}%; inspect irrigation.`);
    else if (average > state.thresholds.soilMoistureMax) notes.push(`Soil moisture average is high at ${average.toFixed(1)}%; avoid overwatering.`);
    else notes.push(`Soil moisture average is healthy at ${average.toFixed(1)}%.`);
  }
  if (context.tomato.detected || text.includes("ripe")) {
    notes.push(context.tomato.detected ? "A ripe tomato is detected and ready for checking." : "No ripe tomato is detected in the latest reading.");
  }
  return notes.length
    ? notes.join(" ")
    : "I am connected to the same Tomatix readings as the dashboard. Ask about temperature, humidity, sunlight, soil moisture, controls, devices, logs, or ripe tomatoes.";
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (char) => ({
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    "\"": "&quot;",
    "'": "&#39;",
  }[char]));
}

window.addEventListener("resize", drawTrendChart);
initControls();
initSettings();
initChat();
syncSettingsUi();
renderThresholds();
loadDashboard().then(loadAnalytics);
setInterval(loadDashboard, 30000);
