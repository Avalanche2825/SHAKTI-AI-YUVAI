# Calculator Logo Preview

## New SHAKTI AI App Icon Design

### Visual Description

The new logo is a **calculator icon** with the following design elements:

```
┌────────────────────┐
│   +          ÷     │  ← Mathematical symbols (gray)
│                    │
│                    │
│   -       ┌────────┤
│           │   =    │  ← Orange section with
│           │   =    │     white equals signs
└───────────┴────────┘
```

### Color Scheme

- **Background**: White (#FFFFFF)
- **Calculator Body**: Light Gray (#E8E8E8)
- **Math Symbols** (+, -, ÷): Gray (#808080)
- **Equals Section**: Orange (#FF8C42)
- **Equals Signs**: White (#FFFFFF)

### Design Elements

1. **Main Calculator Body**
    - Light gray rounded rectangle
    - 6px border radius for modern look
    - Clean, minimalist design

2. **Mathematical Symbols**
    - Plus sign (+) in top-left
    - Division sign (÷) in top-right
    - Minus sign (-) in middle-left
    - All in a subtle gray color

3. **Orange Accent**
    - Bottom-right quarter of the calculator
    - Contains two white horizontal lines (equals symbol)
    - Provides visual interest and brand color

### Icon Formats Created

#### Android App

- **Format**: Vector XML drawable
- **File**: `app/src/main/res/drawable/ic_launcher_foreground.xml`
- **Adaptive**: Yes (supports all Android adaptive icon shapes)
- **Sizes**: Automatically scaled to all required densities

#### Web App

- **Format**: SVG
- **File**: `web/app/icon.svg`
- **Size**: 64x64 viewBox (scales to any size)
- **Auto-generated**: Next.js creates favicon.ico, apple-touch-icon, etc.

### Brand Consistency

The calculator design serves multiple purposes:

1. **Disguise Function**: Matches the app's calculator interface for user safety
2. **Recognition**: Familiar calculator icon that doesn't raise suspicion
3. **Professional**: Clean, modern design that looks like a legitimate app
4. **Gender-Neutral**: Avoids colors or styles that might reveal the app's true purpose

### Comparison

| Before | After |
|--------|-------|
| Generic Android logo | Calculator icon |
| Green theme | Gray/Orange theme |
| Not aligned with app disguise | Perfect disguise reinforcement |

### Icon Appearance in Different Contexts

**Android Launcher Icons:**

- ✅ App Drawer - Shows calculator icon
- ✅ Home Screen - Shows calculator icon
- ✅ Recent Apps - Shows calculator icon with "Calculator" name
- ✅ Settings - Shows calculator icon

**Adaptive Icon Shapes:**

- ✅ Circle (some devices)
- ✅ Square (some devices)
- ✅ Rounded Square (most devices)
- ✅ Squircle (some devices)

**Web Favicon:**

- ✅ Browser Tab - Small calculator icon
- ✅ Bookmarks - Calculator icon
- ✅ PWA Install - Calculator icon (if PWA added)

### Technical Specifications

#### Android Vector Drawable

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- Optimized vector paths -->
</vector>
```

#### Web SVG Icon

```html
<svg viewBox="0 0 64 64" width="64" height="64">
    <!-- Crisp, scalable vector graphics -->
</svg>
```

### Why This Design Works

1. **Safety First**: A calculator app is the perfect disguise - common, useful, and unsuspicious
2. **Universal Recognition**: Everyone recognizes a calculator icon
3. **Clean Design**: Modern, professional appearance
4. **Scalable**: Vector format looks great at any size
5. **Consistent**: Same design across Android and web platforms

### Color Psychology

- **Gray**: Neutral, professional, trustworthy
- **Orange**: Action, energy (subtle accent, not overwhelming)
- **White**: Clean, simple, safe

The muted color palette intentionally avoids bright or stereotypically "feminine" colors that might
give away the app's true purpose to potential abusers.

---

**Design Created**: November 18, 2025  
**Status**: ✅ Implemented and ready for use
