# 🛡️ SHAKTI AI - Women's Safety Application

**India's First AI-Powered Women's Safety App with Automatic Threat Detection**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 📱 About

**SHAKTI AI** is a revolutionary women's safety application designed specifically for the Indian
context. Unlike traditional safety apps that require manual activation, SHAKTI AI uses advanced
machine learning to automatically detect threats through audio analysis and immediately capture
evidence.

The app disguises itself as a calculator for protection from abusers while continuously monitoring
for threats in the background.

---

## ✨ Key Features

### 🤖 **AI-Powered Threat Detection**

- **YAMNet ML Model** - 521 audio class classification
- **Automatic detection** of screams, yells, crying, gasps
- **Real-time inference** (50-100ms latency)
- **No manual activation required**

### 📹 **Dual-Camera Evidence Capture**

- Records from **both front and back cameras** simultaneously
- **HD 720p video** with audio
- Captures attacker's face (front) and surroundings (back)
- **Legal-grade evidence** with timestamps

### 📍 **GPS Location Tracking**

- **High-accuracy GPS** with 5-second updates
- **Reverse geocoding** (coordinates → readable address)
- Location history tracking
- Real-time notification updates

### 📡 **Offline Community Network**

- **BLE mesh network** (1km radius)
- Alerts nearby SHAKTI users automatically
- **Works without internet**
- RSSI-based distance calculation

### ⚖️ **NYAY Legal AI Assistant**

- **Auto-generates FIR** (First Information Report)
- Maps to correct **IPC sections** (498A, 354, 375, etc.)
- **Case strength calculator** (with evidence bonus)
- Lawyer recommendations by specialization

### 💰 **Escape Planner**

- **Financial calculator** (₹90K-2.5L budgets)
- **Safe house finder** (sorted by distance)
- **7-step escape timeline**
- **Microfinance integration** (NRLM loans, NGO grants)

### 🧮 **Calculator Disguise**

- Fully functional calculator interface
- **Secret codes**:
    - `999=` → Dashboard
    - `911=` → Emergency SOS
    - `777=` → Settings
    - `Long-press AC` → Toggle monitoring

---

## 🏗️ Tech Stack

### **Languages & Frameworks**

- **Kotlin** - Primary language
- **Android SDK** (API 24-34)
- **Material Design 3** - UI components

### **Machine Learning**

- **TensorFlow Lite** - On-device ML inference
- **YAMNet Model** (3.94 MB) - Audio classification
- 521 audio event classes from AudioSet

### **Camera & Video**

- **CameraX** - Modern camera API
- **Recorder API** - Video recording
- Dual-camera simultaneous recording

### **Location Services**

- **FusedLocationProviderClient** - Google Play Services
- **Geocoder API** - Address lookup
- High-accuracy GPS mode

### **Bluetooth**

- **Bluetooth Low Energy (BLE)** - Community alerts
- **AdvertiseCallback** - Broadcasting
- **ScanCallback** - Receiving alerts

### **Architecture**

- **MVVM Pattern** - Clean architecture
- **ViewBinding** - Type-safe view access
- **Foreground Services** - Background tasks
- **SharedPreferences** - Data persistence

### **Permissions**

- `RECORD_AUDIO` - Threat detection
- `CAMERA` - Video evidence
- `ACCESS_FINE_LOCATION` - GPS tracking
- `BLUETOOTH_SCAN` & `BLUETOOTH_CONNECT` - Community network

---

## 📂 Project Structure

```
app/
├── src/main/
│   ├── java/com/shakti/ai/
│   │   ├── ShaktiApplication.kt          # App initialization
│   │   ├── ui/                           # Activities
│   │   │   ├── OnboardingActivity.kt     # First-time setup
│   │   │   ├── CalculatorActivity.kt     # Disguised launcher
│   │   │   ├── DashboardActivity.kt      # Control center
│   │   │   ├── NyayLegalActivity.kt      # FIR generator
│   │   │   ├── EscapePlannerActivity.kt  # Financial planner
│   │   │   ├── SettingsActivity.kt       # Configuration
│   │   │   └── IncidentReportActivity.kt # Evidence viewer
│   │   ├── services/                     # Background services
│   │   │   ├── AudioDetectionService.kt  # ML threat detection
│   │   │   ├── VideoRecorderService.kt   # Dual-camera recording
│   │   │   ├── LocationService.kt        # GPS tracking
│   │   │   ├── BluetoothService.kt       # BLE community alerts
│   │   │   ├── NyayLegalService.kt       # FIR generation
│   │   │   └── EscapePlannerService.kt   # Financial planning
│   │   ├── ml/                           # Machine Learning
│   │   │   └── AudioThreatDetector.kt    # YAMNet TFLite wrapper
│   │   └── utils/                        # Utilities
│   │       ├── Constants.kt              # App constants
│   │       └── PermissionsHelper.kt      # Permission management
│   ├── res/
│   │   ├── layout/                       # UI layouts (7 activities)
│   │   ├── values/
│   │   │   ├── colors.xml                # 65 colors
│   │   │   ├── styles.xml                # 13 styles
│   │   │   ├── themes.xml                # 2 themes
│   │   │   └── strings.xml               # String resources
│   │   └── drawable/                     # Icons & drawables
│   └── assets/
│       └── audio_threat_model.tflite     # YAMNet ML model (3.94 MB)
└── build.gradle                          # Dependencies
```

---

## 🚀 Installation & Setup

### **Prerequisites**

- Android Studio Arctic Fox or later
- Android SDK (API 24+)
- Gradle 7.0+
- Java 11+

### **Clone Repository**

```bash
git clone https://github.com/Avalanche2825/SHAKTI-AI-YUVAI.git
cd SHAKTI-AI-YUVAI
```

### **Open in Android Studio**

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the cloned directory
4. Wait for Gradle sync to complete

### **Build & Run**

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run
adb shell am start -n com.shakti.ai/.ui.CalculatorActivity
```

---

## 📖 Usage Guide

### **First Time Setup**

1. Install and open the app
2. Complete onboarding (3 pages)
3. Grant all permissions (Audio, Camera, Location, Bluetooth)
4. Learn secret codes

### **Enable Monitoring**

1. Open app (see calculator)
2. **Long-press AC button**
3. Green dot appears (monitoring active)
4. App listens for threats 24/7

### **Secret Codes**

| Code | Action |
|------|--------|
| `999=` | Open Dashboard |
| `911=` | Emergency SOS |
| `777=` | Open Settings |
| `Long-press AC` | Toggle Monitoring |

### **In Case of Threat**

1. App automatically detects threat (via ML)
2. Dual-camera recording starts
3. GPS location tracked
4. BLE alert broadcasts to nearby users
5. Evidence saved with timestamp

### **After Incident**

1. Type `999=` to open dashboard
2. View incident report
3. Generate FIR (NYAY Legal)
4. Plan escape (Financial calculator)
5. Share evidence with police/lawyer

---

## 🔒 Privacy & Security

- ✅ **All data stays on device** (no cloud storage)
- ✅ **End-to-end encryption** for sensitive data
- ✅ **Calculator disguise** hides app from abusers
- ✅ **App-private storage** for evidence files
- ✅ **No tracking or analytics**
- ✅ **Open source** (community auditable)

---

## 🇮🇳 Indian Context

### **IPC Sections Covered**

- **498A** - Cruelty by husband
- **354** - Outraging modesty
- **354A** - Sexual harassment
- **375/376** - Rape and punishment
- **503/504** - Criminal intimidation
- **67 IT Act** - Obscene online content
- And 10 more sections

### **Safe Houses (Delhi NCR)**

- Shakti Foundation (Karol Bagh)
- ARIVAA (Dwarka)
- Breakthrough India (Okhla)

### **Financial Planning**

- Based on Indian living costs (₹)
- NRLM microfinance integration
- NGO emergency grants
- Government scheme recommendations

### **Multi-Language Support**

- English
- Hindi (हिंदी)
- Bengali (বাংলা)
- Kannada (ಕನ್ನಡ)
- Tamil (தமிழ்)

---

## 📊 Statistics

- **Total Code:** ~7,500 lines
- **Activities:** 7
- **Services:** 6 (4 foreground + 2 helper)
- **ML Model:** YAMNet 3.94 MB (521 classes)
- **IPC Sections:** 16
- **Incident Types:** 8
- **Colors Defined:** 65
- **Build Time:** ~45 seconds
- **APK Size:** 54.13 MB

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Developer

**Abhyuday Jain**

- GitHub: [@Avalanche2825](https://github.com/Avalanche2825)
- Email: abhyudayjain59@gmail.com

---

## 🙏 Acknowledgments

- **Google Research** - YAMNet audio classification model
- **Android Open Source Project** - CameraX, TensorFlow Lite
- **Material Design** - UI components
- **Indian Penal Code** - Legal framework
- **Women's Safety Organizations** - Domain knowledge

---

## ⚠️ Disclaimer

This app is designed to assist in emergency situations but should not be considered a replacement
for proper safety measures, legal counsel, or emergency services. Always contact local authorities (
Police: 100, Women's Helpline: 181) in case of immediate danger.

---

## 🌟 Star This Repository

If you find this project useful, please consider giving it a ⭐ on GitHub!

---

**Built with ❤️ for the safety of Indian women**

🛡️ **SHAKTI AI** - Empowering Women Through Technology
