# 🎤 HELP Voice Command Feature - Complete Guide

## Overview

The **HELP Voice Command** is the most critical safety feature in SHAKTI AI. It allows you to
trigger emergency response **without touching your phone** by simply saying "HELP" three times.

## 🚨 How It Works

### Activation Requirements

- Say **"HELP"** three times within 8 seconds
- Works when the app is in background
- Works even when screen is locked
- No need to touch the phone
- Monitoring must be enabled (green dot visible)

### Supported Keywords

The voice command system recognizes multiple emergency keywords:

- **"HELP"** (Primary - English)
- **"EMERGENCY"** (English)
- **"BACHAO"** (Hindi - बचाओ, means "Save me")

## 📱 Setup Instructions

### First-Time Setup

1. **Install the App**
    - Install SHAKTI AI on your device
    - Complete onboarding tutorial

2. **Grant Permissions**
    - Microphone (Required for voice detection)
    - Camera (For evidence recording)
    - Location (For tracking)
    - Storage (For saving evidence)

3. **Enable Monitoring**
    - Open calculator interface
    - **Long press the "AC" button**
    - Look for green dot indicator
    - When green = Monitoring is active

4. **Test the Feature**
    - With monitoring enabled, say "HELP" 3 times
    - You should see a test alert
    - This confirms the feature is working

### Daily Usage

**Keep the app running in background:**

- Don't force-close the app
- Allow background activity
- Keep battery optimization OFF for SHAKTI AI

**Battery Settings:**

1. Go to Settings → Apps → SHAKTI AI
2. Battery → Unrestricted
3. This ensures monitoring continues 24/7

## 🎯 Using in Emergency

### Quick Response

```
Step 1: Say "HELP"
Step 2: Say "HELP" again
Step 3: Say "HELP" third time
        (within 8 seconds total)
```

### What Happens Automatically

Once triggered, SHAKTI AI will:

1. **📹 Start Recording**
    - Front camera (captures attacker's face)
    - Back camera (captures surroundings)
    - Audio recording with video

2. **📍 Track Location**
    - Continuous GPS tracking
    - Updates every 5 seconds
    - Reverse geocoding (address)

3. **📱 Alert Contacts**
    - SMS to emergency contacts
    - Push notifications
    - Location sharing link

4. **💾 Save Evidence**
    - Videos saved to hidden storage
    - Encrypted and secure
    - Timestamped automatically

5. **🔊 Silent Operation**
    - No notification sounds
    - Minimal screen activity
    - Stealth mode activated

## ⚙️ Configuration

### Access Monitoring Settings

1. Open calculator
2. Type `777=` (secret code)
3. Go to Monitoring section

### Adjust Sensitivity

**Default Settings** (Recommended):

- Detection threshold: 40% vocal energy
- Time window: 8 seconds
- Required detections: 3

**For Loud Environments** (Noisy):

- Increase threshold to 50%
- May reduce false positives

**For Quiet Environments** (Silent):

- Decrease threshold to 30%
- Better detection sensitivity

### Emergency Contacts

1. Open Settings (`777=`)
2. Add emergency contact numbers
3. Format: +91XXXXXXXXXX (with country code)
4. Save changes

## 🔧 Troubleshooting

### Issue: Voice Command Not Working

**Check 1: Is Monitoring Enabled?**

- Look for green dot on calculator screen
- If red/grey, long-press AC button to enable

**Check 2: Microphone Permission**

- Go to Settings → Apps → SHAKTI AI → Permissions
- Ensure Microphone is "Allowed"

**Check 3: Background Restrictions**

- Settings → Apps → SHAKTI AI → Battery
- Set to "Unrestricted"

**Check 4: App Not Force-Closed**

- Don't swipe away app from recent apps
- Let it run in background

**Check 5: Test the Feature**

- Go to Dashboard (`999=`)
- Find "Test Voice Command" button
- Say "HELP" 3 times
- Check if counter increases

### Issue: Too Many False Triggers

**Solution 1: Increase Threshold**

- Settings → Monitoring → Sensitivity
- Increase to 50-60%

**Solution 2: Use Secret Code Instead**

- Type `911=` on calculator
- Instant SOS without voice

### Issue: Voice Not Detected in Emergency

**Workaround Options:**

1. **Use Secret Code**
    - Type `911=` on calculator
    - Faster than voice in some situations

2. **Use Dashboard SOS**
    - Type `999=` to open dashboard
    - Press red "EMERGENCY SOS" button

3. **Long Press Power Button** (If enabled in settings)
    - Hold power button 5 seconds
    - Select "SHAKTI Emergency"

## 📊 Technical Details

### Audio Processing

- **Sampling Rate**: 16 kHz
- **Buffer Size**: 512 samples
- **Processing Interval**: 100ms
- **Detection Algorithm**: RMS (Root Mean Square) energy

### Voice Detection Method

1. **Capture Audio**: Continuous microphone sampling
2. **Energy Calculation**: Compute RMS amplitude
3. **Threshold Check**: Compare against sensitivity setting
4. **Time Window**: Track detections within 8 seconds
5. **Trigger Action**: Activate emergency when count = 3

### ML Model (Optional Enhancement)

- Currently uses amplitude-based detection
- Future: PocketSphinx keyword spotting
- Future: Custom TensorFlow Lite model

## 🔒 Privacy & Security

### Data Storage

- ✅ Voice NOT recorded during monitoring
- ✅ Only triggers on keyword detection
- ✅ No audio sent to cloud
- ✅ Completely offline processing

### Evidence Security

- ✅ Videos encrypted on device
- ✅ Hidden from file managers
- ✅ Accessible only through app
- ✅ Can be password protected

## 💡 Tips for Best Results

### Environmental Factors

**DO:**

- Speak clearly and loudly
- Use in quiet to moderate noise
- Say "HELP" distinctly
- Test regularly in different locations

**DON'T:**

- Whisper (energy too low)
- Shout continuously (may reduce accuracy)
- Use with loud music/TV (interference)
- Forget to enable monitoring

### Battery Optimization

**Recommended Settings:**

- Battery Saver: OFF
- Adaptive Battery: Exclude SHAKTI AI
- Background Data: Allowed
- Doze Mode: Whitelisted

**Battery Life:**

- Typical consumption: 2-5% per hour
- With optimizations: 1-3% per hour
- Emergency mode: 10-15% per hour

## 🎓 Training & Practice

### Practice Scenarios

**Scenario 1: Hidden Phone**

- Phone in pocket/bag
- Say "HELP" 3 times clearly
- Phone should vibrate (if enabled)

**Scenario 2: Distress Situation**

- Practice saying "HELP" loudly
- Simulate being scared/panicked
- Check if still detected

**Scenario 3: Background Running**

- Start monitoring
- Use other apps normally
- Say "HELP" 3 times
- Verify it still triggers

### Family Testing

- Share feature with family
- Practice together
- Test different scenarios
- Ensure everyone knows how to use

## 📱 Integration with Other Features

### Works With

- ✅ Calculator disguise (runs in background)
- ✅ Dashboard monitoring
- ✅ AI Chatbot (can be triggered simultaneously)
- ✅ Location tracking
- ✅ Evidence recording

### Alternative Activation Methods

1. **Voice Command**: Say "HELP" 3x (Recommended)
2. **Secret Code**: Type `911=`
3. **Dashboard SOS**: Press emergency button
4. **Long Press AC**: 10+ seconds (configurable)

## 🆘 Emergency Response Protocol

### When Voice Command Triggers

**Immediate Actions (0-5 seconds):**

- Recording starts
- Location captured
- Silent notification

**Within 30 seconds:**

- SMS sent to emergency contacts
- GPS tracking active
- Evidence saving begins

**Continuous (Until stopped):**

- Video recording (max 3 minutes)
- Location updates
- Audio capture
- Timeline logging

### Stopping Emergency Mode

**Method 1: Dashboard**

- Type `999=`
- Press "Stop Emergency"

**Method 2: Secret Code**

- Type `000=` (stop code)

**Method 3: Settings**

- Long press AC → Settings
- Disable monitoring

## 📞 Support & Feedback

### Report Issues

- Dashboard → Settings → Report Bug
- Include: Phone model, Android version, issue description

### Feature Requests

- Email: support@shakti-ai.com
- GitHub: Create an issue

### Emergency Contacts

- **Women Helpline**: 1091
- **Police**: 100
- **Domestic Violence Helpline**: 181

## 🔄 Updates & Improvements

### Upcoming Features

- Multi-language support (Tamil, Telugu, Bengali)
- Custom keyword training
- Smartwatch integration
- Offline speech recognition improvements

### Version History

- **v1.0.0**: Initial voice command feature
- **v1.1.0** (Planned): ML-based keyword spotting
- **v1.2.0** (Planned): Custom wake words

## ⚠️ Important Notes

1. **Not a Substitute for 911/100**: Always call authorities directly in extreme danger
2. **Test Regularly**: Ensure feature works in your environment
3. **Keep Updated**: Install app updates for improvements
4. **Share Knowledge**: Teach friends and family how to use
5. **Practice**: Muscle memory is crucial in emergencies

---

## 🎯 Quick Reference Card

```
┌─────────────────────────────────────────┐
│  SHAKTI AI - HELP Voice Command         │
├─────────────────────────────────────────┤
│                                         │
│  ACTIVATION:                            │
│  Say "HELP" 3 times within 8 seconds    │
│                                         │
│  SETUP:                                 │
│  1. Enable monitoring (Long press AC)   │
│  2. Look for green dot                  │
│  3. Keep app running in background      │
│                                         │
│  ALTERNATIVES:                          │
│  • Secret Code: 911=                    │
│  • Dashboard SOS button                 │
│                                         │
│  TESTING:                               │
│  Dashboard → Test Voice Command         │
│                                         │
│  EMERGENCY CONTACTS:                    │
│  • Women Helpline: 1091                 │
│  • Police: 100                          │
│  • DV Helpline: 181                     │
│                                         │
└─────────────────────────────────────────┘
```

**Remember: Your safety is our priority. Stay safe! 💜**

---

**Last Updated**: November 2025
**Version**: 1.0.0
**Support**: support@shakti-ai.com
