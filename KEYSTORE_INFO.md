# 🔐 SHAKTI AI - Keystore Information

## ⚠️ IMPORTANT - Keep This Information Secure!

This file contains sensitive information about your app signing keystore. **DO NOT share this
publicly or commit it to GitHub.**

---

## 🔑 Keystore Details

### **File Information**

- **Keystore File**: `shakti-release-key.jks`
- **Location**: Project root directory
- **Algorithm**: RSA 2048-bit
- **Validity**: 10,000 days (~27 years)
- **Created**: November 2025

### **Credentials**

```
Store Password: shakti2025
Key Password: shakti2025
Key Alias: shakti-key
```

### **Certificate Details**

```
Common Name (CN): SHAKTI AI
Organizational Unit (OU): Development
Organization (O): SHAKTI AI
Locality (L): Delhi
State (ST): Delhi
Country (C): IN
```

---

## 📦 Building Signed APK

### **Method 1: Command Line (Recommended)**

```bash
# Clean previous builds
./gradlew clean

# Build signed release APK
./gradlew assembleRelease

# APK location:
# app/build/outputs/apk/release/app-release.apk
```

### **Method 2: Android Studio**

1. **Build** → **Generate Signed Bundle / APK**
2. Select **APK**
3. Click **Next**
4. **Key store path**: Select `shakti-release-key.jks`
5. **Key store password**: `shakti2025`
6. **Key alias**: `shakti-key`
7. **Key password**: `shakti2025`
8. Click **Next**
9. Select **release** build variant
10. Click **Finish**

---

## ✅ Verify Signed APK

```bash
# Check signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# View certificate details
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

---

## 📤 Upload to Google Play Store

When uploading to Google Play:

1. Use the signed APK from: `app/build/outputs/apk/release/app-release.apk`
2. Google Play will automatically re-sign with their key
3. Keep your keystore safe - you'll need it for updates!

---

## 🔒 Security Best Practices

### **DO:**

- ✅ Keep keystore file backed up securely (encrypted cloud storage)
- ✅ Store passwords in a password manager
- ✅ Keep this info file locally (not in Git)
- ✅ Use the same keystore for all app updates

### **DON'T:**

- ❌ Commit keystore to GitHub
- ❌ Share keystore file with anyone
- ❌ Lose the keystore (you can't update your app without it!)
- ❌ Use weak passwords in production

---

## 📁 Files Created

The following files are used for signing:

```
shakti-release-key.jks      # Keystore file (NEVER commit)
keystore.properties         # Credentials file (NEVER commit)
app/build.gradle            # Updated with signing config
.gitignore                  # Updated to ignore keystore files
```

All sensitive files are already added to `.gitignore`.

---

## 🔄 Backup Your Keystore

**CRITICAL**: Back up these files NOW:

```bash
# Copy to a secure location
shakti-release-key.jks
keystore.properties
KEYSTORE_INFO.md
```

**Recommended backup locations:**

- Encrypted USB drive
- Password-protected cloud storage (Google Drive, Dropbox)
- Password manager with file attachments
- Encrypted backup service

**If you lose the keystore, you cannot:**

- Update your app on Google Play
- Release new versions
- Fix bugs in production

---

## 🆕 Creating a New Keystore (If Needed)

If you need to create a new keystore:

```bash
keytool -genkey -v -keystore new-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias your-alias
```

Then update `keystore.properties` with new credentials.

---

## 📊 APK Information

### **Signed APK**

- **File**: app-release.apk (signed)
- **Size**: ~42 MB
- **Min SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Signature**: SHA256withRSA

### **Verification**

When installed, users can verify the signature:

- Settings → Apps → SHAKTI AI → Advanced → App Details

---

## 🚀 Distribution

### **For Testing**

- Share signed APK directly
- Install via ADB
- Upload to Firebase App Distribution

### **For Production**

- Upload to Google Play Store
- Submit for review
- Publish to production

---

## 🔧 Troubleshooting

### **"Keystore not found" error**

```bash
# Check file exists
ls shakti-release-key.jks

# Verify path in keystore.properties
cat keystore.properties
```

### **"Wrong password" error**

- Verify passwords in `keystore.properties`
- Ensure no extra spaces in the file

### **Build fails**

```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleRelease
```

---

## 📞 Support

If you have issues:

1. Check Android Studio Build logs
2. Verify keystore file exists
3. Confirm credentials are correct
4. Try building in Android Studio GUI

---

## ⚠️ REMEMBER

**Your keystore is the ONLY way to update your app on Google Play!**

If you lose it:

- You cannot update the app
- You must create a new app with a new package name
- Users must uninstall and reinstall
- You lose all reviews and downloads

**BACK IT UP NOW!** 🔐

---

**Created**: November 2025  
**For**: SHAKTI AI Women's Safety App  
**Developer**: Abhyuday Jain (abhyudayjain59@gmail.com)
