# SHAKTI AI - Women's Safety App 🛡️

**Your Personal Safety Companion Powered by AI**

SHAKTI AI is a disguised women's safety app that looks like a calculator but provides real-time
threat detection, automatic evidence recording, and emergency assistance.

## ✨ Key Features

### 🎤 **HELP Voice Command** (Most Important!)

- **Say "HELP" 3 times within 8 seconds** to trigger emergency
- Works even when screen is locked
- No need to touch the phone
- Instantly starts recording evidence and alerts contacts

### 🤖 AI-Powered Protection

- Real-time audio threat detection using TensorFlow Lite
- Automatic scream and distress call detection
- Voice command recognition (HELP, EMERGENCY, BACHAO)
- Dual-camera evidence recording (front + back)

### 📱 Disguised Interface

- Appears as a fully functional calculator
- Secret codes to access features:
    - **999=** → Dashboard
    - **911=** → Emergency SOS
    - **777=** → Settings
    - **Long press AC** → Toggle monitoring

### ⚖️ Legal Assistance (NYAY)

- Auto-generate FIR with correct IPC sections
- Case strength assessment
- Find nearby lawyers
- Legal rights information

### 🏠 Escape Planner

- Financial planning calculator
- Safe house locator
- Timeline generator
- Emergency funding sources

### 💜 AI Chatbot Assistant

- 24/7 emotional support
- Safety tips and advice
- Legal guidance
- Crisis intervention

## 🚀 Quick Start

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 24 or higher
- JDK 17 or higher
- Gradle 8.1.4 or higher

### Build Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/SHAKTIAI-YUVAI.git
   cd SHAKTIAI-YUVAI
   ```

2. **Set up local.properties**
   Create a `local.properties` file in the root directory with your SDK path:
   ```properties
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   # Or on Mac/Linux:
   # sdk.dir=/Users/YourUsername/Library/Android/sdk
   ```

3. **Update keystore.properties (Optional - for release builds)**
   If building release APK, update `keystore.properties`:
   ```properties
   storePassword=your_password
   keyPassword=your_password
   keyAlias=your_key_alias
   storeFile=../your-keystore.jks
   ```

   **For debug builds**, you can skip this step. The app will build without signing config.

4. **Add google-services.json**
    - Place your Firebase `google-services.json` file in `app/` directory
    - If you don't have one, create a Firebase project at https://console.firebase.google.com/

5. **Build the project**
   ```bash
   # For Windows
   .\gradlew.bat assembleDebug
   
   # For Mac/Linux
   ./gradlew assembleDebug
   ```

6. **Install on device**
   ```bash
   # For Windows
   .\gradlew.bat installDebug
   
   # For Mac/Linux
   ./gradlew installDebug
   ```

### Common Build Issues

#### Issue: "SDK location not found"

**Solution**: Create `local.properties` with your Android SDK path (see step 2 above)

#### Issue: "google-services.json not found"

**Solution**: Add your Firebase config file or create a dummy one:

```json
{
  "project_info": {
    "project_number": "123456789",
    "project_id": "shakti-ai-demo"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789:android:abcdef",
        "android_client_info": {
          "package_name": "com.shakti.ai"
        }
      }
    }
  ]
}
```

#### Issue: Signing errors in release build

**Solution**: Either create a keystore or build debug version:
```bash
# Build debug (no signing required)
.\gradlew.bat assembleDebug
```

#### Issue: Lint errors

**Solution**: Lint checks are configured to not abort the build. Warnings can be ignored for
testing.

## 📱 How to Use

1. **First Launch**
    - Grant all permissions (Microphone, Camera, Location, etc.)
    - Set up emergency contacts

2. **Enable Monitoring**
    - Long press the "AC" button on calculator
    - Green dot appears when active
    - Say "HELP" 3 times to test voice command

3. **Access Hidden Features**
    - Type `999=` to open Dashboard
    - Type `911=` for immediate SOS
    - Type `777=` for Settings

4. **In Emergency**
    - Say "HELP" 3 times (most reliable!)
    - Or use secret code `911=`
    - App will:
        - Start recording video/audio
        - Track your location
        - Alert emergency contacts
        - Save evidence securely

## 🔒 Privacy & Security

- ✅ All recordings stored in **hidden internal storage**
- ✅ Evidence encrypted and inaccessible to other apps
- ✅ Disguised as calculator app
- ✅ No external storage or cloud upload (optional)
- ✅ Works completely offline

## 👥 Contributing

Contributions are welcome! Please read CONTRIBUTING.md for details on our code of conduct and the
process for submitting pull requests.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Emergency Contacts

- **Women Helpline**: 1091
- **Police**: 100
- **Domestic Violence Helpline**: 181

## ⚠️ Disclaimer

This app is intended to assist in emergency situations but should not be relied upon as the sole
means of protection. Always seek immediate help from authorities in dangerous situations.

---

**Made with 💜 for women's safety**