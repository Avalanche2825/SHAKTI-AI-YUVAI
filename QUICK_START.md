# ⚡ SHAKTI AI - Quick Start Guide

## 🎯 What We've Created

✅ **Web Landing Page** - Beautiful, modern website to showcase your app
✅ **Vercel Deployment Ready** - Configuration files for one-click deployment
✅ **APK Download Center** - Users can download your Android app from the website

---

## 🚀 Deploy to Vercel in 3 Steps

### **Step 1: Test Locally (Optional)**

```bash
# Install dependencies
cd web
npm install

# Run development server
npm run dev
```

Open `http://localhost:3000` to see your landing page!

### **Step 2: Push to GitHub**

```bash
# Go back to project root
cd ..

# Add all files
git add .

# Commit
git commit -m "Add web landing page for Vercel deployment"

# Push to GitHub
git push
```

### **Step 3: Deploy to Vercel**

1. Go to [vercel.com](https://vercel.com)
2. Sign in with GitHub
3. Click **"Add New Project"**
4. Select your `SHAKTI-AI-YUVAI` repository
5. Configure:
    - **Root Directory**: Select `web`
    - Framework: Next.js (auto-detected)
6. Click **"Deploy"**
7. Wait 2-3 minutes ⏰
8. Your site is live! 🎉

**Your URL will be**: `https://shakti-ai-XXXX.vercel.app`

---

## 📦 Before Deploying: Upload Your APK

Your website will link to download your APK. Upload it to GitHub Releases:

### **Option 1: Via GitHub Website**

1. Go to your GitHub repo: `https://github.com/Avalanche2825/SHAKTI-AI-YUVAI`
2. Click **"Releases"** → **"Create a new release"**
3. Tag: `v1.0.0`
4. Upload: `app/build/outputs/apk/release/app-release-unsigned.apk`
5. Click **"Publish release"**

### **Option 2: Build APK First**

```bash
# Build your Android APK
./gradlew assembleRelease

# APK location: app/build/outputs/apk/release/app-release-unsigned.apk
```

---

## ✅ What You Get

### **1. Beautiful Landing Page**

- Professional design with pink/purple theme
- Mobile responsive
- SEO optimized

### **2. Key Sections**

- 🦸 Hero section with call-to-action
- ⚡ Feature showcase (6 main features)
- 📱 How it works (4-step guide)
- 💾 Download section with instructions
- 📊 Stats and metrics
- 👨‍💻 About section
- 📞 Footer with links

### **3. Download Button**

- Links to your GitHub Release
- Shows installation instructions
- Works on desktop and mobile

---

## 🎨 Customize Your Site

### **Change Colors**

Edit `web/tailwind.config.js`:

```js
colors: {
  primary: '#E91E63',    // Change this
  secondary: '#9C27B0',  // And this
}
```

### **Update Content**

Edit `web/app/page.tsx`:

- Line 41: Hero heading
- Line 44: Hero description
- Line 115: Feature descriptions
- Line 451: Footer links

### **Update Download Link**

Find line ~360 in `web/app/page.tsx`:

```tsx
href="https://github.com/YOUR-USERNAME/REPO/releases/download/v1.0.0/app-release.apk"
```

Replace with your actual GitHub release URL.

---

## 📱 Test on Phone

1. Deploy to Vercel
2. Open the Vercel URL on your phone
3. Click "Download APK"
4. Install the app
5. Test all features!

---

## 🐛 Troubleshooting

### **Issue: npm install fails**

```bash
# Delete node_modules and try again
cd web
rm -rf node_modules package-lock.json
npm install
```

### **Issue: Build fails on Vercel**

Check the build logs in Vercel dashboard. Common issues:

- Wrong root directory (should be `web`)
- Missing dependencies (npm should auto-install)

### **Issue: APK download doesn't work**

Make sure:

1. GitHub release is published (not draft)
2. APK file is uploaded to the release
3. Download URL in `page.tsx` matches your release URL

---

## 🎓 Commands Cheat Sheet

```bash
# Test locally
cd web && npm run dev

# Build for production
cd web && npm run build

# Build Android APK
./gradlew assembleRelease

# Push to GitHub
git add . && git commit -m "Update" && git push

# Deploy to Vercel (auto-deploys on push to main)
# Or use: npx vercel --prod
```

---

## 🌟 What's Next?

After deploying:

1. ✅ Share your Vercel URL on social media
2. ✅ Get feedback from users
3. ✅ Add custom domain (optional)
4. ✅ Enable analytics to track downloads
5. ✅ Update APK regularly via GitHub Releases
6. ✅ Add more pages (privacy policy, terms, etc.)

---

## 📞 Need Help?

- **Vercel Docs**: https://vercel.com/docs
- **Next.js Docs**: https://nextjs.org/docs
- **Email**: abhyudayjain59@gmail.com
- **GitHub Issues**: Create an issue on your repo

---

## 🎉 You're Ready!

Your SHAKTI AI app is ready to be shared with the world!

**Steps to deploy:**

1. Push code to GitHub ✅
2. Connect to Vercel ✅
3. Configure root directory as `web` ✅
4. Click deploy ✅
5. Share your URL! 🚀

**Built with ❤️ for women's safety**
