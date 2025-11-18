# Testing the New Calculator Logo

## Quick Testing Checklist

Use this checklist to verify that the new calculator logo is displaying correctly across all
platforms.

---

## ✅ Android App Testing

### 1. Build the App

```bash
# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Or build and install directly
./gradlew installDebug
```

### 2. Visual Verification on Device

- [ ] **App Drawer Icon**
    - Open app drawer
    - Find "Calculator" app
    - Verify calculator icon appears (light gray with orange corner)
    - Check icon is crisp and clear

- [ ] **Home Screen Icon**
    - Long-press app in drawer
    - Drag to home screen
    - Verify icon looks good on home screen
    - Try different icon shape modes if your device supports it

- [ ] **Recent Apps Screen**
    - Open the app
    - Press recent apps button
    - Verify calculator icon shows in recent apps
    - Check app name shows as "Calculator"

- [ ] **Settings > Apps**
    - Go to Settings
    - Go to Apps or Application Manager
    - Find "Calculator"
    - Verify icon appears correctly

- [ ] **Notification Icon** (when services are running)
    - Enable monitoring in the app (long-press AC)
    - Pull down notification shade
    - Check if notification icon matches (optional - uses ic_notification.xml)

### 3. Different Device Shapes

Test on different Android devices or emulators with different icon shapes:

- [ ] **Circle** (Pixel devices)
- [ ] **Rounded Square** (Samsung, most devices)
- [ ] **Square** (Some older devices)
- [ ] **Squircle** (OnePlus, some devices)

The adaptive icon system should handle all shapes automatically.

### 4. Expected Appearance

The icon should show:

- Light gray (#E8E8E8) rounded rectangle
- Plus (+) symbol in top-left (gray)
- Division (÷) symbol in top-right (gray)
- Minus (-) symbol in middle-left (gray)
- Orange (#FF8C42) section in bottom-right
- Two white equals (=) lines in the orange section

---

## ✅ Web App Testing

### 1. Start Development Server

```bash
cd web
npm install  # if not already done
npm run dev
```

### 2. Browser Tab Icon

- [ ] **Open in Browser**
    - Navigate to http://localhost:3000
    - Check browser tab for favicon
    - Should show small calculator icon

- [ ] **Bookmark Test**
    - Bookmark the page
    - Check bookmarks bar
    - Verify calculator icon appears

### 3. Multiple Browsers

Test in different browsers to ensure compatibility:

- [ ] **Chrome/Edge** (Chromium)
- [ ] **Firefox**
- [ ] **Safari** (if on Mac)

### 4. Mobile Browser

- [ ] Open the web app on mobile browser
- [ ] Add to home screen (if PWA functionality is added)
- [ ] Check icon appearance

### 5. Production Build

```bash
npm run build
npm start
```

- [ ] Test production build
- [ ] Verify icon still appears correctly
- [ ] Check different sizes are generated

---

## 🔧 Troubleshooting

### Android Icon Not Showing

**Issue**: Old icon still appears after rebuilding

**Solutions**:

```bash
# Clear gradle cache
./gradlew clean
./gradlew cleanBuildCache

# Uninstall old app completely
adb uninstall com.shakti.ai

# Rebuild and reinstall
./gradlew installDebug
```

**Device Cache**:

- Restart the device
- Clear launcher cache (Settings > Apps > Launcher > Clear Cache)

### Web Favicon Not Showing

**Issue**: Browser still shows old favicon or no favicon

**Solutions**:

- Hard refresh: `Ctrl+Shift+R` (Windows) or `Cmd+Shift+R` (Mac)
- Clear browser cache
- Open in incognito/private window
- Wait a few seconds for Next.js to regenerate icons

### Vector Not Rendering Properly

**Issue**: Icon looks distorted or wrong colors

**Check**:

1. Verify XML syntax in `ic_launcher_foreground.xml`
2. Check that `ic_launcher_background.xml` is simple white
3. Ensure no typos in color hex codes
4. Verify path data is correct

---

## 📸 Screenshot Checklist

Take screenshots for documentation:

- [ ] Android app icon in drawer
- [ ] Android app icon on home screen
- [ ] Android app in recent apps
- [ ] Web browser tab with favicon
- [ ] Web bookmark with icon

---

## 🚀 Deployment Checklist

Before releasing to users:

### Android

- [ ] Test on at least 3 different devices
- [ ] Test on different Android versions (7.0+)
- [ ] Verify icon in Play Store listing (if published)
- [ ] Update app screenshots if they show old icon

### Web

- [ ] Deploy to production (Vercel)
- [ ] Verify favicon on live site
- [ ] Check Open Graph image (for social sharing)
- [ ] Test on mobile browsers

---

## 📝 Known Issues

### Issue: Linter Warnings in XML

**Status**: Can be ignored

The linter may show warnings about Android attributes in the vector drawable files. These are false
positives and can be safely ignored. The XML is valid and will work correctly.

### Issue: WebP Files Still Show Old Icon

**Status**: Expected behavior

The existing WebP files in the mipmap folders are bitmap fallbacks for older Android versions. They
still show the old icon. This is fine because:

1. Modern devices (API 26+) use the adaptive icon system (your new vector design)
2. Older devices (API 24-25) will see the old WebP icons, which is acceptable
3. You can optionally regenerate these WebP files if needed

To update WebP files (optional):

1. Export the vector as PNG at different sizes
2. Convert PNG to WebP
3. Replace files in mipmap-*dpi folders

---

## ✅ Success Criteria

Your logo update is successful when:

1. ✅ Android app shows calculator icon (not old Android logo)
2. ✅ App name displays as "Calculator" in launcher
3. ✅ Icon adapts to different device shapes correctly
4. ✅ Web favicon shows calculator icon in browser tab
5. ✅ Icon looks crisp and clear at all sizes
6. ✅ Colors match the design specification
7. ✅ No build errors or crashes

---

## 🎉 Verification Complete

Once you've checked all items above, your logo update is complete and ready for production!

**Next Steps**:

1. Commit changes to Git
2. Create release notes mentioning new icon
3. Build release APK
4. Deploy web app to production
5. Update any marketing materials

---

**Testing Guide Version**: 1.0  
**Last Updated**: November 18, 2025
