# Tomatix — Native Android App

**Smart Greenhouse Monitoring & Control System**

A native Android application for monitoring and controlling a smart greenhouse. Built with **Kotlin**, **Jetpack Compose**, and **Firebase Realtime Database**, this app replicates the layout and design of the original Tomatix web dashboard as a fully native Android experience.

Runs on **Android Studio** using **Material 3** design with the signature green agricultural color palette.

---

## Features

### 📊 Dashboard
- **Live sensor cards** — Temperature, Humidity, Soil Moisture, Sunlight Intensity (measures sun intensity inside the greenhouse)
  - Color-coded icons (orange/blue/green/yellow)
  - Ideal range indicators + trend arrows (up / down / stable)
  - Status badges & progress bars for optimal-range detection
- **System Status** — Water Pump, Irrigation, Exhaust Fan, Camera with live pulsing indicators
- **24-Hour Trends** — historical sensor chart with legend

### 🎛️ Controls
- **Chemical Distribution System** — chemical type selector (Fertilizer, Pesticide, Herbicide, Fungicide), mixing time, concentration slider, live countdown timer, and distribution controls with safety guidelines
- **Camera Module** — live feed placeholder, zoom slider, snapshot/refresh, pan controls
- **Water Pump / Irrigation / Exhaust Fan** — on/off switches + speed sliders & quick cycle actions
- **Emergency Controls** — Emergency Stop All + Reset to Default

### ⚙️ Settings
- **Thresholds** — set min/max acceptable ranges for each sensor
- **Analytics** — Day/Week/Month/Year range charts with avg/max/min stats
- **Notifications** — email, push, SMS, and critical-only preferences
- **System Logs** — activity log viewer (starts empty)

### 🤖 AI Chatbot
- Floating assistant with quick actions
- Smart responses analyzing sensor conditions: crop recommendations, soil moisture advice, temperature tuning, growth optimization
- Recommendation cards + simulated typing indicator

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Navigation | Compose Navigation |
| Database | Firebase Realtime Database |
| DI | Hilt |
| Animations | Compose Animation APIs |
| Charts | Canvas-based charts (custom) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| Build | Gradle (8.11.1) + AGP |

---

## Project Structure

```
com.tomatix.app/
├── di/                        # Hilt DI modules
├── data/
│   ├── model/                 # Data classes (SensorData, DeviceStatus, etc.)
│   ├── repository/            # SensorRepository
│   └── firebase/              # FirebaseService (RTDB flows)
└── ui/
    ├── theme/                 # Color, Type, Theme (Material 3)
    ├── navigation/            # Routes, BottomNavBar, AppNavHost
    ├── dashboard/             # DashboardScreen + ViewModel
    ├── controls/              # ControlsScreen + ViewModel
    ├── settings/              # SettingsScreen + ViewModel
    └── chatbot/               # ChatbotOverlay + ViewModel
```

---

## Getting Started

### Prerequisites
- **Android Studio** (latest stable, e.g. Ladybug / Meerkat or newer)
- **JDK 17+** (Android Studio's bundled JBR 21 works)
- Android SDK **API 35** (compileSdk) and **API 26** (minSdk)

### Run in Android Studio
1. **File → Open** → select the `TOMATIX_APP` folder
2. Wait for **Gradle sync** (GRADLE_HOME not required; wrapper included)
3. Select your emulator/device
4. Click **Run ▶**

The app starts with **empty data** — sensor cards, trends, analytics, and logs show "No data" / `--` until live data arrives from Firebase Realtime Database.

### Build from command line
```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

The APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Firebase Integration

The app is pre-wired for **Firebase Realtime Database** (data layer + repository + ViewModels are ready).

### Data structure expected in RTDB
```json
{
  "sensors": {
    "temperature": 24.5,
    "humidity": 65,
    "soilMoisture": 58,
    "lightIntensity": 7500
  },
  "devices": {
    "pumpStatus": true,
    "irrigationStatus": false,
    "fanStatus": true,
    "cameraStatus": true,
    "chemicalMixerStatus": false,
    "chemicalDistributionStatus": false
  },
  "settings": {
    "thresholds": {
      "tempMin": 22, "tempMax": 26,
      "humidityMin": 50, "humidityMax": 70,
      "soilMoistureMin": 45, "soilMoistureMax": 65,
      "lightIntensityMin": 5000
    },
    "notifications": {
      "email": true, "push": true, "sms": false, "criticalOnly": false
    }
  },
  "logs": {
    "-LOG_ID": { "time": "...", "event": "...", "type": "info" }
  }
}
```
---

## License

Provided for the Tomatix community project.
