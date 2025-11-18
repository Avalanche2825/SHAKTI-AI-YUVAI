# Logo Update Summary

## Overview

Updated the SHAKTI AI app logo to a calculator icon design to better reinforce the app's disguise as
a calculator for user safety.

## Changes Made

### 1. Android App Launcher Icon

#### Foreground Icon (`app/src/main/res/drawable/ic_launcher_foreground.xml`)

- ✅ Replaced with a calculator icon design
- Features:
    - Light gray calculator body (#E8E8E8)
    - Orange equals section in bottom-right (#FF8C42)
    - Gray mathematical symbols (plus, divide, minus) (#808080)
    - White equals symbols on orange section (#FFFFFF)
- Design matches the provided calculator icon reference

#### Background Icon (`app/src/main/res/drawable/ic_launcher_background.xml`)

- ✅ Simplified to a clean white background (#FFFFFF)
- Removed the complex green grid pattern
- Provides better contrast for the calculator foreground

#### Adaptive Icons (Already Configured)

The existing adaptive icon configurations in these directories will automatically use the new
drawables:

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`

### 2. Web App Icon

#### Favicon (`web/app/icon.svg`)

- ✅ Created new SVG icon matching the Android design
- Next.js 13+ will automatically generate favicons from this file
- Includes:
    - 64x64 SVG viewBox
    - Same calculator design as Android
    - Same color scheme for brand consistency

## Color Palette

| Element | Color | Hex Code |
|---------|-------|----------|
| Calculator Body | Light Gray | #E8E8E8 |
| Equals Section | Orange | #FF8C42 |
| Math Symbols | Gray | #808080 |
| Equals Sign | White | #FFFFFF |
| Background | White | #FFFFFF |

## App Name Consistency

The app is already configured with the correct disguise name:

- **Android App Label**: "Calculator" (in AndroidManifest.xml)
- **Web App Title**: "SHAKTI AI - Women's Safety App" (in web/app/layout.tsx)

Note: The web app shows the real name since it's a public-facing landing page for downloads, while
the Android app maintains the calculator disguise for user safety.

## Icon Files Structure

```
Android Launcher Icons:
├── drawable/
│   ├── ic_launcher_foreground.xml ✅ UPDATED
│   └── ic_launcher_background.xml ✅ UPDATED
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml (references drawables)
│   └── ic_launcher_round.xml (references drawables)
└── mipmap-*dpi/
    ├── ic_launcher.webp (existing bitmap fallbacks)
    └── ic_launcher_round.webp (existing bitmap fallbacks)

Web App Icons:
└── web/app/
    └── icon.svg ✅ CREATED
```

## Testing Recommendations

### Android

1. Clean and rebuild the project:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

2. Install on a test device:
   ```bash
   ./gradlew installDebug
   ```

3. Verify the calculator icon appears:
    - Check app drawer
    - Check recent apps screen
    - Check home screen (if pinned)

### Web

1. Run the Next.js development server:
   ```bash
   cd web
   npm run dev
   ```

2. Check the favicon in browser:
    - Look at the browser tab icon
    - Check bookmark icon
    - View source and verify `<link rel="icon">` is present

3. For production, build and deploy:
   ```bash
   npm run build
   npm start
   ```

## Design Rationale

The calculator icon design was chosen to:

1. **Maintain Disguise**: App appears as a simple calculator to protect users from abusers
2. **Be Recognizable**: Calculator is a common, innocuous app that won't raise suspicion
3. **Match Identity**: Reinforces the app's primary interface (CalculatorActivity)
4. **Stay Gender-Neutral**: Avoids stereotypical colors or designs that might give away the app's
   true purpose
5. **Look Professional**: Clean, modern design that fits with other system apps

## Technical Details

### Android Vector Drawables

- Uses standard Android `<vector>` format
- Adaptive icon system supports multiple shapes (circle, square, rounded square, squircle)
- Vector format ensures crisp rendering at any size
- No need for multiple PNG densities

### Web SVG Icon

- Next.js 13+ app directory metadata convention
- Automatically generates multiple sizes
- Works with PWA manifest if added in the future
- Scalable for retina displays

## Notes

- The existing mipmap WebP files can be kept as fallbacks for older Android versions
- Consider regenerating the mipmap WebP files from the new vector design for consistency
- The orange accent color (#FF8C42) can be incorporated into the app's color scheme if desired

## Next Steps (Optional)

1. **Update Mipmap Images**: Generate new WebP/PNG files for all density buckets from the vector
   design
2. **Notification Icon**: Consider updating `drawable/ic_notification.xml` to match
3. **Splash Screen**: Update splash screen icon if one exists
4. **Web PWA Manifest**: Add `manifest.json` for PWA support with calculator icon
5. **App Store Assets**: Create marketing materials with the new calculator icon design

---

**Updated**: November 18, 2025
**Status**: ✅ Complete - Ready for testing
