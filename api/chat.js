function numberLine(label, value, unit = "") {
  return Number.isFinite(value) ? `${label}: ${value.toFixed(1)}${unit}` : `${label}: no reading`;
}

function localGuidance(message, context = {}) {
  const notes = [];
  const temperature = Number(context.temperature);
  const humidity = Number(context.humidity);
  const sunlight = Number(context.incomingSunlight);
  const soilSensors = Array.isArray(context.soilSensors) ? context.soilSensors.map(Number).filter(Number.isFinite) : [];
  const tomato = context.tomato || {};

  if (Number.isFinite(temperature)) {
    if (temperature < 21) notes.push(`Temperature is low at ${temperature.toFixed(1)}°C. Check heating and reduce cold airflow.`);
    else if (temperature > 27) notes.push(`Temperature is high at ${temperature.toFixed(1)}°C. Improve ventilation, shading, or fan use.`);
    else notes.push(`Temperature is ideal at ${temperature.toFixed(1)}°C.`);
  }

  if (Number.isFinite(humidity)) {
    if (humidity < 60) notes.push(`Humidity is low at ${humidity.toFixed(1)}%. Consider misting or irrigation if plants look stressed.`);
    else if (humidity > 70) notes.push(`Humidity is high at ${humidity.toFixed(1)}%. Increase airflow and watch for fungal risk.`);
    else notes.push(`Humidity is ideal at ${humidity.toFixed(1)}%.`);
  }

  if (Number.isFinite(sunlight)) {
    notes.push(`Incoming sunlight is ${sunlight.toFixed(1)} k lux; compare it with your expected greenhouse light level for the time of day.`);
  }

  if (soilSensors.length) {
    const average = soilSensors.reduce((sum, value) => sum + value, 0) / soilSensors.length;
    if (average < 50) notes.push(`Average soil moisture is low at ${average.toFixed(1)}%. Check water pump and irrigation.`);
    else if (average > 75) notes.push(`Average soil moisture is high at ${average.toFixed(1)}%. Pause watering and check drainage.`);
    else notes.push(`Average soil moisture is healthy at ${average.toFixed(1)}%.`);
  }

  if (tomato.detected) notes.push("A ripe tomato is detected. It is ready for checking or harvest.");

  if (!notes.length) {
    return "I can help once sensor readings arrive from Firebase. Ask me about temperature, humidity, sunlight, soil moisture, device status, or tomato readiness.";
  }

  return notes.join(" ");
}

async function handler(request, response) {
  if (request.method !== "POST") {
    response.setHeader("Allow", "POST");
    return response.status(405).json({ error: "Method not allowed" });
  }

  const { message = "", context = {} } = request.body || {};
  const apiKey = process.env.GEMINI_API_KEY || process.env.GOOGLE_API_KEY;

  if (!apiKey) {
    return response.status(200).json({ reply: localGuidance(message, context), fallback: true });
  }

  const prompt = [
    "You are Tomi, the Tomatix greenhouse assistant.",
    "Give concise, practical tomato greenhouse advice using the provided live sensor context.",
    "Mention exact readings when useful. Do not invent data.",
    "",
    `User question: ${message}`,
    "Sensor context:",
    numberLine("Temperature", Number(context.temperature), "°C"),
    numberLine("Humidity", Number(context.humidity), "%"),
    numberLine("Incoming sunlight", Number(context.incomingSunlight), " k lux"),
    `Soil sensors: ${Array.isArray(context.soilSensors) ? context.soilSensors.join(", ") : "no readings"}`,
    `Tomato detection: ${context.tomato?.detected ? "ripe tomato detected" : "none detected"}`,
    `Devices: ${JSON.stringify(context.devices || {})}`,
  ].join("\n");

  try {
    const geminiResponse = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }],
          generationConfig: {
            temperature: 0.4,
            maxOutputTokens: 260,
          },
        }),
      }
    );

    if (!geminiResponse.ok) {
      return response.status(200).json({ reply: localGuidance(message, context), fallback: true });
    }

    const data = await geminiResponse.json();
    const reply = data?.candidates?.[0]?.content?.parts?.map((part) => part.text).join(" ").trim();
    return response.status(200).json({ reply: reply || localGuidance(message, context) });
  } catch (error) {
    return response.status(200).json({ reply: localGuidance(message, context), fallback: true });
  }
}

module.exports = handler;
