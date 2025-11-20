# 🛠️ SHAKTI AI - Complete Build Guide

This guide will help you build the SHAKTI AI project on any PC, regardless of your operating system.

## 📋 System Requirements

### Minimum Requirements

- **OS**: Windows 10/11, macOS 10.14+, or Linux (Ubuntu 18.04+)
- **RAM**: 8 GB (16 GB recommended)
- **Disk Space**: 10 GB free space
- **Java**: JDK 17 or higher
- **Android Studio**: Arctic Fox (2020.3.1) or later
- **Internet**: Required for initial Gradle sync and dependencies

### Android SDK Requirements

- **Android SDK**: API 24 (Android 7.0) minimum
- **Target SDK**: API 34 (Android 14)
- **Build Tools**: 34.0.0 or higher

## 🚀 Step-by-Step Build Instructions

### Step 1: Install Prerequisites

#### Install Android Studio

1. Download Android Studio from https://developer.android.com/studio
2. Install with default settings
3. Complete the setup wizard (download SDK, emulator, etc.)

#### Install JDK 17

- Android Studio usually includes JDK 17
- Verify: Open terminal and run `java -version`
- If not installed, download from https://adoptium.net/

### Step 2: Clone the Repository

```bash
# Using HTTPS
git clone https://github.com/yourusername/SHAKTIAI-YUVAI.git

# Or using SSH
git clone git@github.com:yourusername/SHAKTIAI-YUVAI.git

# Navigate to project directory
cd SHAKTIAI-YUVAI
```

### Step 3: Configure local.properties

Create a file named `local.properties` in the project root directory.

#### For Windows:

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

#### For macOS:

```properties
sdk.dir=/Users/YourUsername/Library/Android/sdk
```

#### For Linux:

```properties
sdk.dir=/home/YourUsername/Android/Sdk
```

**How to find your SDK path:**

1. Open Android Studio
2. Go to File → Settings (Windows/Linux) or Android Studio → Preferences (macOS)
3. Navigate to Appearance & Behavior → System Settings → Android SDK
4. Copy the "Android SDK Location" path

### Step 4: Setup Firebase (Required)

#### Option A: Use Your Own Firebase Project (Recommended)

1. Go to https://console.firebase.google.com/
2. Create a new project or use existing one
3. Add an Android app with package name: `com.shakti.ai`
4. Download `google-services.json`
5. Place it in `app/` directory

#### Option B: Use Demo Configuration (For Testing Only)

Create a file `app/google-services.json` with this content:

```json
{
  "project_info": {
    "project_number": "123456789",
    "firebase_url": "https://shakti-ai-demo.firebaseio.com",
    "project_id": "shakti-ai-demo",
    "storage_bucket": "shakti-ai-demo.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:123456789:android:abcdef123456",
        "android_client_info": {
          "package_name": "com.shakti.ai"
        }
      },
      "oauth_client": [
        {
          "client_id": "123456789-abcdefghijklmnop.apps.googleusercontent.com",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": "AIzaSyDemoKey123456789"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
```

### Step 5: Setup Keystore (Optional - Only for Release Builds)

#### For Debug Builds (Skip this step)

Debug builds use Android's default debug keystore automatically.

#### For Release Builds

1. Generate a keystore:

```bash
keytool -genkey -v -keystore shakti-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias shakti-key
```

2. Create `keystore.properties` in root directory:

```properties
storePassword=your_password
keyPassword=your_password
keyAlias=shakti-key
storeFile=../shakti-release-key.jks
```

**Note**: Keep keystore.properties and .jks file secure and never commit to Git!

### Step 6: Build the Project

#### Method 1: Using Android Studio (Easiest)

1. Open Android Studio
2. File → Open → Select SHAKTIAI-YUVAI folder
3. Wait for Gradle sync to complete (may take 5-10 minutes first time)
4. Build → Make Project (Ctrl+F9 / Cmd+F9)
5. Run → Run 'app' (Shift+F10 / Ctrl+R)

#### Method 2: Using Command Line

**For Windows (PowerShell/CMD):**

```bash
# Clean build
.\gradlew.bat clean

# Build debug APK
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug

# Build release APK (requires keystore)
.\gradlew.bat assembleRelease
```

**For macOS/Linux (Terminal):**

```bash
# Make gradlew executable (first time only)
chmod +x gradlew

# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK (requires keystore)
./gradlew assembleRelease
```

### Step 7: Find Your APK

After successful build, APK will be located at:

**Debug APK:**

```
app/build/outputs/apk/debug/app-debug.apk
```

**Release APK:**

```
app/build/outputs/apk/release/app-release.apk
```

## 🐛 Troubleshooting Common Issues

### Issue 1: "SDK location not found"

**Error**: `SDK location not found. Define location with sdk.dir in the local.properties file`

**Solution**:

1. Create `local.properties` file in project root
2. Add your SDK path (see Step 3 above)
3. Sync Gradle again

### Issue 2: "google-services.json is missing"

**Error**: `File google-services.json is missing`

**Solution**:

- Add `google-services.json` to `app/` directory (see Step 4 above)

### Issue 3: "Failed to resolve dependencies"

**Error**: `Could not resolve all dependencies for configuration`

**Solution**:

1. Check internet connection
2. Clear Gradle cache:
   ```bash
   # Windows
   rmdir /s /q %USERPROFILE%\.gradle\caches
   
   # macOS/Linux
   rm -rf ~/.gradle/caches
   ```
3. Sync Gradle again

### Issue 4: "Unsupported Java version"

**Error**: `Unsupported class file major version`

**Solution**:

1. Open Android Studio → Settings → Build Tools → Gradle
2. Set Gradle JDK to "Embedded JDK (17)"
3. Sync project

### Issue 5: "Execution failed for task ':app:kaptDebugKotlin'"

**Error**: Kapt annotation processing errors

**Solution**:

1. Clean project: `./gradlew clean`
2. Rebuild: `./gradlew build --no-daemon`
3. If still fails, run: `./gradlew build --stacktrace` to see detailed error

### Issue 6: Build is very slow

**Solutions**:

- Enable Gradle daemon in `gradle.properties`:
  ```properties
  org.gradle.daemon=true
  org.gradle.parallel=true
  org.gradle.caching=true
  ```
- Increase Gradle memory in `gradle.properties`:
  ```properties
  org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
  ```

### Issue 7: "Cannot inline bytecode built with JVM target 17 into bytecode..."

**Solution**: Ensure Java 17 is being used. Check in `build.gradle`:

```gradle
compileOptions {
    sourceCompatibility JavaVersion.VERSION_17
    targetCompatibility JavaVersion.VERSION_17
}
kotlinOptions {
    jvmTarget = '17'
}
```

## 📱 Testing on Device

### Enable Developer Options

1. Go to Settings → About Phone
2. Tap "Build Number" 7 times
3. Go back to Settings → Developer Options
4. Enable "USB Debugging"

### Install APK

```bash
# Using Gradle
./gradlew installDebug

# Or using ADB directly
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Launch App

```bash
adb shell am start -n com.shakti.ai/.ui.CalculatorActivity
```

## 🔧 Advanced Configuration

### Change App Name

Edit `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">YourAppName</string>
```

### Change Package Name

1. Rename package in all Kotlin files
2. Update `namespace` in `app/build.gradle`:
   ```gradle
   namespace 'com.yourname.appname'
   ```
3. Update in `AndroidManifest.xml`
4. Update in `google-services.json`

### Disable Features

Edit `app/build.gradle` to comment out unnecessary dependencies:

```gradle
// implementation 'com.google.firebase:firebase-analytics-ktx'  // Disable analytics
```

## 📊 Build Variants

### Debug Build (Development)

- Includes debug symbols
- Uses debug signing key
- Larger APK size (~60 MB)
- Allows ADB debugging

### Release Build (Production)

- ProGuard/R8 enabled (code shrinking)
- Release signing key required
- Smaller APK size (~45 MB)
- Optimized for performance

## 🚀 CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/android.yml`:

```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    - name: Build with Gradle
      run: ./gradlew assembleDebug
```

## 📚 Additional Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [CameraX Documentation](https://developer.android.com/training/camerax)
- [TensorFlow Lite Guide](https://www.tensorflow.org/lite/guide)

## 💡 Tips for Developers

1. **Use latest Android Studio**: Always update to the latest stable version
2. **Enable Instant Run**: Speeds up development significantly
3. **Use Android Emulator**: Test on multiple device configurations
4. **Enable Lint**: Catches bugs early (`./gradlew lint`)
5. **Version Control**: Commit often, use meaningful messages
6. **Code Style**: Follow Kotlin coding conventions

## 🆘 Getting Help

If you encounter issues not covered here:

1. Check existing GitHub Issues: https://github.com/yourusername/SHAKTIAI-YUVAI/issues
2. Stack Overflow: Tag with `android`, `kotlin`, `android-studio`
3. Android Developers Slack: https://androiddevelopers.slack.com/

## 📝 Changelog

### Version 1.0.0 (Current)

- Initial release
- All core features implemented
- Build system optimized for cross-platform compatibility

---

**Last Updated**: November 2025

**Maintained by**: SHAKTI AI Development Team

For questions or contributions, please open an issue on GitHub.
