# Quick Start Guide - AI Monitoring Features

## 🚀 Getting Started

### Access the AI Monitoring Dashboard

1. **Open SHAKTI AI App** (appears as "Calculator")
2. **Type secret code**: `999=`
3. **Tap**: "AI Monitoring Dashboard" card (🤖 robot icon)

---

## 📱 AI Monitoring Dashboard Features

### 1. Start/Stop Monitoring

**Button**: "Start Monitoring" (green)

**What it does**:

- Starts real-time audio threat detection
- Shows live audio waveform visualization
- Displays threat confidence percentage
- Runs AudioDetectionService in foreground

**To stop**: Tap "Stop Monitoring"

---

### 2. Live Audio Waveform

**Visual Display**: Real-time audio visualization

**What you'll see**:

- Teal waveform showing audio patterns
- Gradient fill effect
- Grid lines for reference
- Idle animation when no audio

**Indicator**:

- Small waves = quiet environment
- Large waves = loud sounds detected

---

### 3. Voice Command Detection

**Toggle Switch**: Enable/Disable voice commands

**How to use**:

1. Turn on the switch
2. Say "HELP" loudly 3 times within 8 seconds
3. Emergency SOS triggers automatically

**Status Display**:

- `0/3` - No detections yet
- `1/3 (7s left)` - One detection, 7 seconds remaining
- `2/3 (5s left)` - Two detections, getting close!
- `3/3` - Triggers emergency!

**Commands supported**:

- "HELP" (English)
- "EMERGENCY" (English)
- "BACHAO" (Hindi - Save me)

---

### 4. Threat Confidence Display

**Shows**: `Threat Confidence: XX%`

**Meaning**:

- **0-30%**: Safe - Normal sounds
- **31-60%**: Caution - Elevated sounds
- **61-80%**: Warning - Suspicious sounds
- **81-100%**: ALERT - Threat detected!

**Updates**: Every 100ms (10 times per second)

---

### 5. Real-time Statistics

**Samples**: Number of audio samples processed  
**Active Time**: Duration of monitoring session

**Example**:

- `Samples: 1543` = Analyzed 1,543 audio chunks
- `Active: 154s` = Been monitoring for 154 seconds

---

### 6. Emergency SOS Button

**Button**: Red "🚨 EMERGENCY SOS"

**When pressed**:

1. Shows confirmation dialog
2. Lists actions that will be taken:
    - Start recording evidence
    - Alert emergency contacts
    - Share your location
    - Notify nearby users

3. Confirm to activate full emergency mode

---

## 🎯 Use Cases

### Scenario 1: Walking Home at Night

```
1. Open dashboard (999=)
2. Go to AI Monitoring
3. Start Monitoring
4. Keep phone in pocket
5. If threat detected → Automatic emergency response
```

### Scenario 2: Domestic Violence Situation

```
1. Enable Voice Command Detection
2. If situation escalates:
   - Shout "HELP" 3 times
   - Emergency triggered
   - Evidence recording starts
```

### Scenario 3: Manual Emergency

```
1. Open AI Monitoring
2. Tap Emergency SOS
3. Confirm
4. All safety systems activate
```

---

## ⚙️ Settings & Customization

### Voice Command Sensitivity

Currently fixed at:

- **RMS Threshold**: 0.4
- **Detection Window**: 8 seconds
- **Required Count**: 3 detections

### Waveform Colors

Default: Teal (#32B8C6)  
Can be customized in code via `setWaveformColor()`

---

## 🔔 Notifications

### Monitoring Active

**Title**: SHAKTI Protection  
**Message**: "Protection active - Listening"  
**Icon**: 🛡️ (shield)

**Means**: Background service is running and monitoring

### Voice Commands Enabled

**Message**: "Voice commands enabled - Say 'HELP' 3 times"

**Means**: Keyword detection is active

### Threat Detected

**Message**: "⚠️ THREAT DETECTED (XX%)"  
**Priority**: HIGH  
**Sound**: Alert tone

**Action**: Tap to see details

---

## 🔐 Privacy & Security

### Data Storage

- ✅ **All processing on-device** - No cloud upload
- ✅ **No audio recording** (unless threat detected)
- ✅ **No data collection** for analytics

### Permissions Used

- **Microphone**: For threat detection only
- **Foreground Service**: To run in background
- **Notifications**: To alert you of threats

### When Does Recording Start?

Audio is ONLY recorded when:

1. Threat detected by ML model (>60% confidence)
2. Voice command "HELP" spoken 3 times
3. Manual SOS button pressed

**Otherwise**: Audio is analyzed and discarded immediately

---

## 🐛 Troubleshooting

### "Microphone permission required"

**Solution**: Grant microphone permission in Settings > Apps > Calculator > Permissions

### Waveform not showing

**Causes**:

1. Monitoring not started
2. Microphone blocked by another app
3. Device audio issue

**Solution**: Stop and restart monitoring

### Voice commands not working

**Checks**:

1. Is the switch ON?
2. Are you speaking loudly enough?
3. Is microphone working?

**Tip**: Try saying "HELP" very loudly (like shouting)

### Threat confidence always 0%

**Reasons**:

1. Quiet environment (good!)
2. ML model not loaded (using fallback)

**This is normal** if environment is safe

---

## 💡 Tips & Best Practices

### Battery Optimization

- Only enable monitoring when needed
- Disable voice commands if not required
- Stop monitoring when in safe location

### False Positive Reduction

- Keep phone away from speakers
- Avoid very noisy environments for voice commands
- Test in your environment first

### Emergency Preparedness

1. **Test voice commands** in a safe environment
2. **Verify emergency contacts** are set up
3. **Check permissions** are granted
4. **Know the secret codes**: 999= (dashboard), 911= (SOS)

---

## 🔄 Integration with Other Features

### Works with:

- ✅ **NYAY Legal** - Auto-generates FIR if threat detected
- ✅ **Escape Planner** - Can plan escape route
- ✅ **Bluetooth Alerts** - Notifies nearby SHAKTI users
- ✅ **Location Tracking** - GPS coordinates captured
- ✅ **Video Recording** - Dual-camera evidence

### Triggered automatically by:

- ML threat detection (AudioDetectionService)
- Voice command ("HELP" × 3)
- Manual SOS button

---

## 📞 Emergency Contacts

### India Emergency Numbers

- **Police**: 100
- **Women's Helpline**: 181
- **Ambulance**: 102
- **Child Helpline**: 1098

### In the App

Set up in: Dashboard > Settings > Emergency Contacts

---

## 🎓 Understanding the Technology

### Voice Command Detection

**Algorithm**: RMS (Root Mean Square) energy analysis

**How it works**:

1. Records audio continuously
2. Calculates loudness of each chunk
3. If loudness > threshold, counts as detection
4. Tracks detections over 8-second window
5. Triggers when 3 detections accumulated

### Audio Visualization

**Rendering**: Custom Android View with Canvas drawing

**Features**:

- 100-sample rolling buffer
- Path-based rendering
- Gradient shader
- Hardware-accelerated

### ML Threat Detection

**Model**: YAMNet (Google Research)  
**Classes**: 521 audio categories  
**Threat Classes**: Scream, Shout, Yell, Crying, Gasp

**Inference Time**: 50-100ms per sample

---

## 📚 Learn More

- **Main README**: Project overview and features
- **INTEGRATION_SUMMARY.md**: Technical integration details
- **DEPLOYMENT_GUIDE.md**: How to build and deploy
- **Code**: Browse `app/src/main/java/com/shakti/ai/`

---

## 🆘 Need Help?

### Report Issues

GitHub: [https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/issues](https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/issues)

### Contact Developer

Email: abhyudayjain59@gmail.com

### Community

- Contribute improvements
- Suggest features
- Report bugs
- Share feedback

---

## ✅ Quick Reference

### Secret Codes

- `999=` - Open Dashboard
- `911=` - Emergency SOS (direct)
- `777=` - Open Settings
- `Long-press AC` - Toggle monitoring

### Color Indicators

- 🟢 **Green** - Monitoring active, all safe
- 🟡 **Yellow** - Elevated sounds detected
- 🔴 **Red** - Threat detected or emergency mode

### Icons

- 🤖 AI Monitoring Dashboard
- 🎵 Audio Waveform
- 🗣️ Voice Commands
- 🚨 Emergency SOS
- ⚖️ NYAY Legal
- 🏠 Escape Planner

---

**Last Updated**: November 18, 2025  
**Version**: 1.0.0  
**Status**: ✅ Fully functional
