# Developer Notes - Emergency Features

## 🚀 Quick Start for Developers

### Testing Voice Commands

**Method 1: ADB Command**

```bash
# Enable monitoring
adb shell am start -n com.shakti.ai/.ui.CalculatorActivity

# Simulate emergency (for testing without voice)
adb shell am broadcast -a com.shakti.ai.MANUAL_SOS
```

**Method 2: Logcat Monitoring**

```bash
# Watch for voice command detection
adb logcat -s AudioDetectionService VoiceCommandDetector

# Expected output:
# AudioDetectionService: Voice command detected: HELP (HELP said 3 times!)
# AudioDetectionService: THREAT CONFIRMED with confidence: 1.0
# VideoRecorderService: STEALTH RECORDING STARTED
```

**Method 3: Test in App**

```kotlin
// In AIMonitoringActivity, toggle voice commands and test
binding.switchVoiceCommand.isChecked = true
// Say "HELP" 3 times or use test button
```

---

## 📁 File Locations

### Source Code

```
app/src/main/java/com/shakti/ai/
├── services/
│   ├── AudioDetectionService.kt      (Voice commands + threat detection)
│   ├── VideoRecorderService.kt       (Dual camera recording)
│   └── LocationService.kt            (GPS tracking)
├── ml/
│   ├── VoiceCommandDetector.kt       (3x HELP detection)
│   └── AudioThreatDetector.kt        (ML threat analysis)
└── ui/
    ├── CalculatorActivity.kt          (Main disguised UI)
    └── AIMonitoringActivity.kt        (Voice command testing UI)
```

### Hidden Storage

```
Device Path: /data/data/com.shakti.ai/files/.system_cache/

Files:
- sys_front_YYYYMMDD_HHMMSS.dat (front camera video)
- sys_back_YYYYMMDD_HHMMSS.dat (back camera video)
- sys_audio_[incident_id]_YYYYMMDD_HHMMSS.dat (audio)
- .nomedia (prevents media scanning)
```

---

## 🔧 Key Code Snippets

### 1. Hidden Storage Directory

```kotlin
// VideoRecorderService.kt & AudioDetectionService.kt
private const val HIDDEN_DIR_NAME = ".system_cache"

private fun getHiddenStorageDir(): File {
    return File(filesDir, HIDDEN_DIR_NAME)
}

private fun createHiddenStorageDirectory() {
    val hiddenDir = getHiddenStorageDir()
    if (!hiddenDir.exists()) {
        hiddenDir.mkdirs()
        File(hiddenDir, ".nomedia").createNewFile()
    }
}
```

### 2. Voice Command Detection

```kotlin
// VoiceCommandDetector.kt
private val detectionWindow = 8000L // 8 seconds
private val requiredDetections = 3

fun startListening(onCommandDetected: (command: String) -> Unit) {
    // Listen for audio
    // Detect "HELP" keyword
    // Count detections in 8-second window
    // Trigger callback when count >= 3
}
```

### 3. Emergency Trigger

```kotlin
// AudioDetectionService.kt
private fun triggerEmergencyResponse(confidence: Float) {
    // 1. Start video recording
    val videoIntent = Intent(this, VideoRecorderService::class.java)
    videoIntent.action = VideoRecorderService.ACTION_START_RECORDING
    startForegroundService(videoIntent)
    
    // 2. Start audio recording
    startAudioRecording(incidentId)
    
    // 3. Start location tracking
    val locationIntent = Intent(this, LocationService::class.java)
    startForegroundService(locationIntent)
    
    // 4. Send alerts
    sendEmergencyAlerts(confidence, incidentId)
}
```

---

## 🧪 Testing Checklist

### Unit Tests Needed

- [ ] VoiceCommandDetector: Detection accuracy
- [ ] VoiceCommandDetector: 8-second window
- [ ] VoiceCommandDetector: Cooldown period
- [ ] AudioDetectionService: Emergency trigger
- [ ] VideoRecorderService: Hidden storage creation
- [ ] File naming: Innocuous names generated

### Integration Tests Needed

- [ ] Voice command → Emergency trigger
- [ ] Emergency → Video recording
- [ ] Emergency → Audio recording
- [ ] Emergency → Location tracking
- [ ] Evidence → Hidden storage
- [ ] Hidden storage → Not visible in Gallery

### Manual Tests Required

- [ ] Say "HELP" 3x → Emergency triggers
- [ ] Check Gallery → No videos visible
- [ ] Check File Manager → No files visible
- [ ] Check Dashboard → Evidence accessible
- [ ] Battery usage → Acceptable range
- [ ] Background operation → Works when screen off

---

## 🐛 Debugging Tips

### Check if Voice Commands Active

```bash
adb logcat | grep "Voice commands ENABLED"
# Should see: "✅ Voice commands ENABLED - Say 'HELP' 3 times to trigger SOS"
```

### Check Hidden Storage Created

```bash
adb shell "ls -la /data/data/com.shakti.ai/files/.system_cache/"
# Should see: .nomedia file and recording files
```

### Check Recording Files

```bash
adb shell "ls -la /data/data/com.shakti.ai/files/.system_cache/"
# Should see: sys_front_*.dat, sys_back_*.dat, sys_audio_*.dat
```

### Pull Files for Testing

```bash
# Pull video to computer
adb pull /data/data/com.shakti.ai/files/.system_cache/sys_front_20241118_143022.dat ./test_video.mp4

# Pull audio to computer
adb pull /data/data/com.shakti.ai/files/.system_cache/sys_audio_123_20241118_143022.dat ./test_audio.m4a

# Play to verify
vlc test_video.mp4
vlc test_audio.m4a
```

### Check Service Running

```bash
adb shell "dumpsys activity services | grep AudioDetectionService"
adb shell "dumpsys activity services | grep VideoRecorderService"
```

---

## 🔍 Common Issues & Solutions

### Issue: Voice Commands Not Detecting

**Possible Causes:**

1. Microphone permission not granted
2. Service not started
3. Voice too quiet
4. Background noise too high

**Debug:**

```bash
# Check permissions
adb shell "dumpsys package com.shakti.ai | grep RECORD_AUDIO"

# Check service
adb logcat -s AudioDetectionService:V

# Check RMS values
adb logcat | grep "RMS energy"
```

**Solution:**

- Request permissions in app
- Start service explicitly
- Increase voice volume threshold in VoiceCommandDetector
- Improve noise filtering algorithm

### Issue: Files Not Hidden

**Possible Causes:**

1. Missing `.nomedia` file
2. Wrong directory location
3. File names too obvious

**Debug:**

```bash
# Check .nomedia exists
adb shell "ls -la /data/data/com.shakti.ai/files/.system_cache/.nomedia"

# Check files
adb shell "ls -la /data/data/com.shakti.ai/files/.system_cache/"
```

**Solution:**

- Ensure `.nomedia` file created in `createHiddenStorageDirectory()`
- Verify using internal storage, not external
- Use innocuous file names (`sys_*.dat`)

### Issue: MediaRecorder Crash on Android 12+

**Error:**

```
java.lang.RuntimeException: setOutputFile failed
```

**Solution:**
Already fixed in code:

```kotlin
recorder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
    MediaRecorder(this@AudioDetectionService)
} else {
    @Suppress("DEPRECATION")
    MediaRecorder()
}
```

---

## 📊 Performance Monitoring

### Battery Usage

```bash
# Monitor battery consumption
adb shell "dumpsys batterystats | grep com.shakti.ai"
```

**Expected:**

- Monitoring: 3-5% per hour
- Recording: 10-15% per 3 minutes
- Total: Acceptable for safety app

### Memory Usage

```bash
# Monitor memory
adb shell "dumpsys meminfo com.shakti.ai"
```

**Expected:**

- AudioDetectionService: ~20MB
- VoiceCommandDetector: ~10MB
- VideoRecorderService: ~30MB
- Total: < 100MB during emergency

### CPU Usage

```bash
# Monitor CPU
adb shell "top -n 1 | grep shakti"
```

**Expected:**

- Background: < 5% CPU
- Recording: < 20% CPU

---

## 🔐 Security Audit

### Hidden Storage Security

- [x] Internal storage (not external)
- [x] Hidden directory (starts with ".")
- [x] Innocuous name (".system_cache")
- [x] .nomedia file present
- [x] Innocuous file names ("sys_*.dat")
- [x] No obvious extensions (.mp4 → .dat)

### Permission Security

- [x] RECORD_AUDIO - Required for voice commands
- [x] CAMERA - Required for video evidence
- [x] ACCESS_FINE_LOCATION - Required for GPS
- [x] FOREGROUND_SERVICE_MICROPHONE - Required for background
- [x] FOREGROUND_SERVICE_CAMERA - Required for stealth recording

### Code Security

- [x] No hardcoded secrets
- [x] No API keys in code
- [x] Proper error handling
- [x] Memory leaks prevented
- [x] Service lifecycle managed

---

## 📝 Code Maintenance

### Adding New Voice Commands

**Steps:**

1. Update `VoiceCommandDetector.kt`
2. Add new keyword detection logic
3. Update callback to differentiate commands
4. Test with different accents/languages

**Example:**

```kotlin
private fun detectKeyword(audioBuffer: ShortArray): String? {
    val rms = calculateRMS(audioBuffer)
    
    if (rms > 0.4f) {
        // Could use frequency analysis here
        // For now, return generic "HELP"
        return "HELP"
    }
    
    return null
}
```

### Changing Storage Location

**If needed to change hidden directory:**

```kotlin
// Update constant
private const val HIDDEN_DIR_NAME = ".your_new_name"

// Migration code (add to onCreate)
private fun migrateStorage() {
    val oldDir = File(filesDir, ".system_cache")
    val newDir = File(filesDir, ".your_new_name")
    
    if (oldDir.exists() && !newDir.exists()) {
        oldDir.renameTo(newDir)
    }
}
```

### Adding Encryption

**For future enhancement:**

```kotlin
// Add dependency
implementation "androidx.security:security-crypto:1.1.0-alpha06"

// Encrypt files
private fun encryptFile(inputFile: File, outputFile: File) {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
        
    val encryptedFile = EncryptedFile.Builder(
        context,
        outputFile,
        masterKey,
        EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
    ).build()
    
    // Copy and encrypt
    inputFile.inputStream().use { input ->
        encryptedFile.openFileOutput().use { output ->
            input.copyTo(output)
        }
    }
}
```

---

## 📚 Related Documentation

- `EMERGENCY_FEATURES_IMPLEMENTATION.md` - Technical details
- `VOICE_COMMAND_QUICK_GUIDE.md` - User guide
- `IMPLEMENTATION_SUMMARY.md` - Overview of changes
- `README.md` - Project documentation

---

## 🎯 TODO (Future Enhancements)

### High Priority

- [ ] Add TensorFlow Lite speech recognition model
- [ ] Implement file encryption for evidence
- [ ] Add multi-language support (Hindi, Bengali, Tamil)
- [ ] Create evidence export feature with password protection

### Medium Priority

- [ ] Add unit tests for VoiceCommandDetector
- [ ] Implement cloud backup with E2E encryption
- [ ] Add face detection in video for better evidence
- [ ] Optimize battery usage further

### Low Priority

- [ ] Add voice training for better accuracy
- [ ] Support for custom wake words
- [ ] Integration with smart home devices
- [ ] Wearable device support (smartwatch trigger)

---

## ✅ Deployment Checklist

Before deploying to production:

- [ ] Test on multiple Android versions (8.0 - 14.0)
- [ ] Test on different device manufacturers (Samsung, Xiaomi, OnePlus)
- [ ] Verify battery optimization exclusion
- [ ] Test in various noise environments
- [ ] Verify hidden storage on different devices
- [ ] Check Gallery/File Manager visibility
- [ ] Test voice commands with different accents
- [ ] Verify emergency contacts notification
- [ ] Test offline functionality
- [ ] Security audit completed
- [ ] Privacy policy updated
- [ ] User documentation finalized

---

**Last Updated:** November 18, 2025  
**Maintained By:** Development Team  
**Version:** 1.0.0
