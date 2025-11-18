# 📦 How to Upload APK to GitHub Releases

## 🎯 Quick Steps

### **Step 1: Go to GitHub Releases**

1. Open your browser
2. Go to: **https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases**
3. Click the **"Create a new release"** button (or "Draft a new release")

---

### **Step 2: Fill Release Information**

#### **Tag Version**

- Click **"Choose a tag"**
- Type: `v1.0.0`
- Click **"Create new tag: v1.0.0 on publish"**

#### **Release Title**

```
SHAKTI AI v1.0.0 - Initial Release
```

#### **Description**

- Click inside the description box
- Copy and paste the entire content from **`RELEASE_NOTES_v1.0.0.md`**
- Or write a shorter description:

```markdown
🛡️ **SHAKTI AI v1.0.0 - Initial Release**

India's first AI-powered women's safety app!

**Features:**
- 🤖 AI Threat Detection (YAMNet ML)
- 📹 Dual-Camera Recording
- 📍 GPS Location Tracking
- 📡 Offline Community Alerts
- ⚖️ Legal AI (FIR Generator)
- 💰 Escape Planner
- 🧮 Calculator Disguise

**Download:** app-release.apk (42.24 MB)
**Android:** 7.0+ (API 24+)

**Installation:**
1. Download APK
2. Enable "Unknown Sources"
3. Install
4. Grant permissions
5. Start protecting!

See full release notes below for details.
```

---

### **Step 3: Upload APK File**

1. **Find your APK**:
    - Location: `D:\5th Sem. Lab\SHAKTIAI-YUVAI\app\build\outputs\apk\release\app-release.apk`
    - Size: 42.24 MB

2. **Attach to Release**:
    - Scroll down to **"Attach binaries by dropping them here or selecting them"**
    - Click the area or drag-and-drop the APK file
    - Wait for upload to complete (may take 1-2 minutes)
    - You should see: `app-release.apk` with a checkmark

3. **Optional**: Rename the file during upload
    - Click on the uploaded filename
    - Rename to: `SHAKTI-AI-v1.0.0.apk` (optional, more descriptive)

---

### **Step 4: Set as Latest Release**

- ✅ Check the box: **"Set as the latest release"**
- ✅ Check the box: **"Create a discussion for this release"** (optional)

---

### **Step 5: Publish**

1. Click the green **"Publish release"** button
2. Wait a few seconds
3. ✅ Done! Your release is live!

---

## 🔗 After Publishing

Your APK will be available at:

```
https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/tag/v1.0.0
```

**Direct Download Link:**

```
https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/download/v1.0.0/app-release.apk
```

---

## 📊 What Users Will See

Users visiting your release page will see:

1. **Release Title**: SHAKTI AI v1.0.0 - Initial Release
2. **Tag**: v1.0.0
3. **Release Date**: Today's date
4. **Description**: All the features and instructions
5. **Assets**:
    - **app-release.apk** (42.24 MB) - Click to download
    - Source code (zip)
    - Source code (tar.gz)

---

## 📱 How Users Download

### **On Desktop:**

1. Go to releases page
2. Scroll to "Assets"
3. Click **"app-release.apk"**
4. Download starts

### **On Mobile:**

1. Open releases link on phone browser
2. Click **"app-release.apk"**
3. Download starts
4. Open downloaded file
5. Install directly

---

## ✅ Verification

After publishing, verify:

1. ✅ Release appears on releases page
2. ✅ Tag `v1.0.0` is visible
3. ✅ APK is downloadable
4. ✅ File size shows correctly (42.24 MB)
5. ✅ Description renders properly
6. ✅ Download counter works

---

## 🔄 Updating the Web Landing Page

After creating the release, update your web landing page:

1. Edit `web/app/page.tsx`
2. Find the download link (around line 360)
3. Update to:

```tsx
href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/download/v1.0.0/app-release.apk"
```

4. Commit and push:

```bash
git add web/app/page.tsx
git commit -m "Update download link to v1.0.0 release"
git push
```

5. Vercel will auto-deploy in 2 minutes

---

## 📢 Sharing Your Release

After publishing, share:

### **Social Media:**

```
🛡️ SHAKTI AI v1.0.0 is now available!

India's first AI-powered women's safety app with:
🤖 Automatic threat detection
📹 Dual-camera evidence capture
📍 GPS tracking
📡 Offline community alerts

Download: https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/latest

#WomenSafety #AI #Android #India
```

### **WhatsApp/Email:**

```
Hi everyone,

I'm excited to share SHAKTI AI - a women's safety app I built that uses AI to automatically detect threats and capture evidence.

Download: https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/latest

It's completely free and works offline. Please share with women who might benefit from it.

Stay safe!
```

---

## 🐛 Troubleshooting

### **"No permission to create release"**

- Make sure you're logged in to your GitHub account
- Verify you have write access to the repository

### **Upload fails**

- Check your internet connection
- Try a different browser (Chrome/Edge recommended)
- File might be too large - our 42 MB APK should be fine (limit is 2GB)

### **Can't find the APK file**

- Go to: `app\build\outputs\apk\release\`
- Look for `app-release.apk` (42.24 MB)
- If not there, run: `./gradlew assembleRelease`

---

## 📝 Tips

1. **Use meaningful tag names**: `v1.0.0`, `v1.1.0`, etc.
2. **Write good descriptions**: Help users understand what's new
3. **Include screenshots**: Add app screenshots to description
4. **Test the download**: Download and install from the release page
5. **Keep release notes**: Save for future reference

---

## 🎉 You're Done!

Your APK is now:

- ✅ Publicly available
- ✅ Easy to download
- ✅ Properly versioned
- ✅ Documented
- ✅ Shareable

**Now go share it with the world!** 🚀

---

**Questions?** Contact abhyudayjain59@gmail.com
