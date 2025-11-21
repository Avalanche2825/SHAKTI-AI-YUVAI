# 🔍 Evidence Recording Debug Guide - v1.1.5

## 🎯 PURPOSE

You reported: "Only checking is showing, please resolve such issues. Are videos and audios really
captured?"

This guide will help you verify if evidence IS being recorded and why it might show "Checking..."

---

## ✅ WHAT WAS FIXED IN v1.1.5

### **Issue: Shows "Checking..." Instead of Actual Evidence**

**Improvements Made:**

1. ✅ Added comprehensive logging to track evidence recording
2. ✅ Better status indicators (🎥 Recording, ⏳ Processing, ✓ Recorded)
3. ✅ File existence verification before displaying
4. ✅ Auto-refresh on activity resume
5. ✅ Detailed error messages
6. ✅ Show file sizes when available

---

## 🔍 HOW TO VERIFY EVIDENCE IS RECORDING

### **Method 1: Check Logcat (Most Reliable)**

When you trigger an emergency and then open Incident Reports, you should see these logs:

```
Filter by: "VideoRecorder", "AudioDetection", "IncidentReport"

Expected Logs:

[VideoRecorder] ✅ Using incident_id from intent: incident_1732202340000
[VideoRecorder] 🎥 STEALTH RECORDING STARTED (Incident: incident_1732202340000)
[VideoRecorder] ✅ front camera recording active
[VideoRecorder] ✅ back camera recording active
[VideoRecorder] 📹 front video saved: /data/user/0/com.shakti.ai/files/.system_cache/sys_front_20251121_203900.dat (2048KB, 30s)
[VideoRecorder] 💾 Evidence saved to DATABASE (incident: incident_1732202340000, type: video_front)
[VideoRecorder] ✅ Verification: 1 evidence items for incident incident_1732202340000
[VideoRecorder] 📹 back video saved: /data/user/0/com.shakti.ai/files/.system_cache/sys_back_20251121_203900.dat (2048KB, 30s)
[VideoRecorder] 💾 Evidence saved to DATABASE (incident: incident_1732202340000, type: video_back)
[VideoRecorder] ✅ Verification: 2 evidence items for incident incident_1732202340000

[AudioDetection] 🎙️ Starting audio recording for incident: incident_1732202340000
[AudioDetection] 🎙️ Audio recording to HIDDEN storage: /data/user/0/com.shakti.ai/files/.system_cache/sys_audio_incident_1732202340000_20251121_203900.dat
[AudioDetection] 🛑 Audio recording stopped (512KB, 30s)
[AudioDetection] 💾 Audio evidence saved to DATABASE (incident: incident_1732202340000)
[AudioDetection] ✅ Verification: 3 evidence items for incident incident_1732202340000

[IncidentReport] ✅ Loaded incident: incident_1732202340000
[IncidentReport] 📁 Evidence count: 3
[IncidentReport]    - video_front: /data/.../sys_front_20251121_203900.dat (2097152 bytes)
[IncidentReport]    - video_back: /data/.../sys_back_20251121_203900.dat (2097152 bytes)
[IncidentReport]    - audio: /data/.../sys_audio_incident_1732202340000_20251121_203900.dat (524288 bytes)
[IncidentReport] Front video exists: true
[IncidentReport] Back video exists: true
[IncidentReport] Audio exists: true
```

**To View Logcat:**

```bash
# Connect device via USB
adb logcat | grep -E "VideoRecorder|AudioDetection|IncidentReport"

# Or filter in Android Studio Logcat
```

---

### **Method 2: Check Database Directly**

```bash
# Connect via ADB
adb shell

# Access app's database
run-as com.shakti.ai

# Navigate to database
cd databases

# Check if database exists
ls -la
# Should see: shakti_evidence_db

# Query database (requires sqlite3)
sqlite3 shakti_evidence_db

# Check incidents table
SELECT * FROM incidents;

# Check evidence table
SELECT id, incidentId, type, fileSize, duration FROM evidence;

# Exit sqlite
.quit
```

**Expected Output:**

```sql
-- Incidents Table
incident_1732202340000|1732202340000|0|0.0|0.0||voice_command|1.0||0|0

-- Evidence Table
uuid-1234|incident_1732202340000|video_front|/data/.../sys_front.dat|1732202340000|30000|2097152|
uuid-5678|incident_1732202340000|video_back|/data/.../sys_back.dat|1732202340000|30000|2097152|
uuid-9abc|incident_1732202340000|audio|/data/.../sys_audio.dat|1732202340000|30000|524288|
```

---

### **Method 3: Check Hidden Storage Directory**

```bash
# Connect via ADB
adb shell

# Access app's internal storage
run-as com.shakti.ai

# Navigate to hidden directory
cd files/.system_cache

# List all recorded files
ls -lh

# Expected output
-rw-------. 1 u0_a123 u0_a123 2.0M Nov 21 20:39 sys_front_20251121_203900.dat
-rw-------. 1 u0_a123 u0_a123 2.0M Nov 21 20:39 sys_back_20251121_203900.dat
-rw-------. 1 u0_a123 u0_a123 512K Nov 21 20:39 sys_audio_incident_1732202340000_20251121_203900.dat
-rw-------. 1 u0_a123 u0_a123    0 Nov 21 20:39 .nomedia
```

**If files exist here, evidence IS being recorded!**

---

## 🐛 COMMON ISSUES & SOLUTIONS

### **Issue 1: Shows "Checking..." Permanently**

**Possible Causes:**

1. **Recording is still in progress** (incident < 5 minutes old)
2. **Database not saving evidence** (check logcat for errors)
3. **Service not starting** (check if emergency notification appears)

**Solution:**

1. **Wait for recording to complete** (auto-stops after 3 minutes)
2. **Check logcat** for error messages
3. **Verify permissions** granted (Camera, Microphone, Storage)
4. **Restart app** and try again

---

### **Issue 2: Shows "Not recorded" for Old Incidents**

**Possible Causes:**

1. Recording failed (check logcat for errors)
2. Permissions were not granted
3. Camera/microphone in use by another app

**Solution:**

1. Grant all permissions before triggering emergency
2. Close other camera apps
3. Check logcat: `adb logcat | grep ERROR`

---

### **Issue 3: Shows "✓ Recorded" but "file moved"**

**This is NORMAL!** It means:

- Evidence was recorded successfully
- Saved to database
- File exists in database but may have been moved/compressed
- Can still be viewed via "View Evidence" button

---

### **Issue 4: Database Shows 0 Evidence**

**Possible Causes:**

1. **incident_id not being passed to services**
2. **Database insert failing**
3. **Services not starting**

**Solution - Check Logs:**

```bash
adb logcat | grep "incident_id"

# Should see:
[AudioDetection] 📝 Generated incident ID: incident_1732202340000
[VideoRecorder] ✅ Using incident_id from intent: incident_1732202340000
[AudioDetection] 💾 Evidence saved to DATABASE (incident: incident_1732202340000)
```

**If you DON'T see these logs, services aren't receiving incident_id!**

---

## 📊 NEW STATUS INDICATORS (v1.1.5)

### **What Each Status Means:**

| Status | Meaning | Action |
|--------|---------|--------|
| 🎥 Recording... | Recording in progress right now | Wait for it to finish |
| ⏳ Processing... | Recording stopped, saving to database | Wait a few seconds |
| ✓ Recorded (XXX KB) | Successfully recorded and saved | Ready to view |
| ✓ X recorded (file moved) | Saved in database, file relocated | Can still view |
| Not recorded | Evidence not captured | Check permissions/logs |

---

## 🔄 AUTO-REFRESH FEATURE (v1.1.5)

**New Behavior:**

- When you return to Incident Report screen, it automatically refreshes
- If recording was "🎥 Recording..." before, it will update to "✓ Recorded" when done
- No need to manually refresh

**How to Use:**

1. Trigger emergency
2. Open Incident Reports → See "🎥 Recording..."
3. Press Back, wait 30 seconds
4. Open Incident Reports again → Should now show "✓ Recorded"

---

## 🧪 COMPLETE TEST PROCEDURE

### **Step-by-Step Verification:**

```
1. PREPARATION
   ✓ Install APK (v1.1.5)
   ✓ Grant ALL permissions
   ✓ Open ADB logcat: adb logcat | grep -E "VideoRecorder|AudioDetection"

2. TRIGGER EMERGENCY
   ✓ Long-press % button
   ✓ Confirm "Yes - Emergency"
   ✓ See notification: "🚨 Emergency Alert Active"

3. VERIFY LOGCAT (Immediately)
   ✓ See: "🎥 STEALTH RECORDING STARTED"
   ✓ See: "✅ front camera recording active"
   ✓ See: "✅ back camera recording active"
   ✓ See: "🎙️ Audio recording to HIDDEN storage"

4. WAIT 30 SECONDS
   ✓ Let recording run for at least 30 seconds

5. STOP EMERGENCY
   ✓ Long-press . button
   ✓ Confirm "Stop"

6. VERIFY LOGCAT (After Stop)
   ✓ See: "📹 front video saved"
   ✓ See: "💾 Evidence saved to DATABASE"
   ✓ See: "✅ Verification: X evidence items"

7. OPEN INCIDENT REPORTS
   ✓ Type 999= → Dashboard
   ✓ Tap "Incident Reports"

8. VERIFY DISPLAY
   ✓ Time: Shows actual timestamp
   ✓ Trigger: Shows "Manual SOS" or "Voice Command"
   ✓ Front Camera: Shows "✓ Recorded (XXX KB)" or "🎥 Recording..."
   ✓ Back Camera: Shows "✓ Recorded (XXX KB)" or "🎥 Recording..."
   ✓ Audio: Shows "✓ Recorded (XXX KB)" or "🎙️ Recording..."

9. VERIFY DATABASE
   ✓ adb shell
   ✓ run-as com.shakti.ai
   ✓ cd files/.system_cache
   ✓ ls -lh
   ✓ Should see 3 .dat files with size > 0

10. TEST VIEW EVIDENCE
    ✓ Tap "VIEW EVIDENCE" button
    ✓ Should show list of files
    ✓ Tap a file to play
```

---

## 🔍 DEBUGGING CHECKLIST

If evidence is NOT recording, check these:

### **Permissions:**

- [ ] Camera permission granted
- [ ] Microphone permission granted
- [ ] Storage permission granted
- [ ] Location permission granted
- [ ] Notification permission granted

### **Services:**

- [ ] AudioDetectionService started (check notification)
- [ ] VideoRecorderService started (check logcat)
- [ ] Emergency notification appeared
- [ ] No errors in logcat

### **Database:**

- [ ] Incident created in database (check logcat: "Incident record created")
- [ ] incident_id passed to services (check logcat: "Using incident_id")
- [ ] Evidence saved to database (check logcat: "Evidence saved to DATABASE")

### **Files:**

- [ ] Hidden directory exists: `/data/data/com.shakti.ai/files/.system_cache`
- [ ] .nomedia file exists in hidden directory
- [ ] Video files created (sys_front_*.dat, sys_back_*.dat)
- [ ] Audio file created (sys_audio_*.dat)
- [ ] File sizes > 0

---

## 📱 LOGCAT COMMAND REFERENCE

```bash
# Full monitoring
adb logcat | grep -E "VideoRecorder|AudioDetection|IncidentReport"

# Just errors
adb logcat *:E | grep -E "VideoRecorder|AudioDetection"

# Just evidence saves
adb logcat | grep "Evidence saved"

# Just incident creation
adb logcat | grep "Incident record created"

# Verification logs
adb logcat | grep "Verification"

# File paths
adb logcat | grep "system_cache"
```

---

## ✅ EXPECTED VS ACTUAL

### **Expected Behavior (Working Correctly):**

```
Trigger Emergency
↓
Services Start
↓
Incident Created in Database (with incident_id)
↓
VideoRecorderService receives incident_id
↓
AudioDetectionService receives incident_id
↓
Recording for 30 seconds (or until stopped)
↓
Videos Saved to .system_cache/*.dat
↓
Audio Saved to .system_cache/*.dat
↓
Evidence Saved to Database (linked to incident_id)
↓
Open Incident Reports
↓
Loads incident from database
↓
Loads evidence from database
↓
Displays: "✓ Recorded (XXX KB)"
↓
Tap "VIEW EVIDENCE"
↓
Shows all files
↓
Tap file → Plays in system player
```

### **If Shows "Checking..." (Debugging):**

```
Open Incident Reports
↓
Shows "Checking..." or "🎥 Recording..."
↓
Check 1: Is incident < 5 minutes old? → YES → Normal, still recording
↓
Check 2: Is incident older? → YES → Check logcat for errors
↓
Check 3: Evidence in database? → NO → Recording failed
↓
Check 4: Files in .system_cache? → NO → Services didn't start
↓
Check 5: Permissions granted? → NO → Grant permissions
```

---

## 🚀 NEXT STEPS FOR YOU

1. **Install the NEW APK (v1.1.5)**
   ```bash
   adb install app\build\outputs\apk\release\app-release.apk
   ```

2. **Enable Logcat Monitoring**
   ```bash
   adb logcat | grep -E "VideoRecorder|AudioDetection|IncidentReport" > evidence_log.txt
   ```

3. **Test Emergency Flow**
    - Trigger emergency
    - Wait 30 seconds
    - Stop emergency
    - Open Incident Reports

4. **Check Logcat Output**
    - Look for "Evidence saved to DATABASE" messages
    - Look for "Verification: X evidence items"
    - Look for file paths

5. **Check Database**
    - Use adb shell to verify files exist
    - Check database has records

6. **Report Results**
    - If still showing "Checking...", share logcat output
    - Mention how long after stopping the alert
    - Check if files exist in .system_cache

---

## 📋 SUMMARY

**v1.1.5 Improvements:**

- ✅ Better status indicators (🎥 Recording, ⏳ Processing, ✓ Recorded)
- ✅ File existence verification
- ✅ Auto-refresh on activity resume
- ✅ Comprehensive logging for debugging
- ✅ Clear error messages

**Evidence IS being recorded** if logcat shows:

- "Evidence saved to DATABASE"
- "Verification: X evidence items"
- Files exist in `.system_cache`

**If showing "Checking...":**

- Could be **still recording** (wait longer)
- Could be **recent incident** (< 5 minutes old)
- Check logcat for actual errors

---

**Version:** 1.1.5  
**Date:** November 21, 2025  
**Focus:** Evidence Recording Verification & Debug  
**Status:** Enhanced Logging & Status Indicators

**Test the new APK and share the logcat output to verify evidence is recording!** 🔍📱
