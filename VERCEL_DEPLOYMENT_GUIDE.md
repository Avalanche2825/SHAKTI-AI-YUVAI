# 🚀 SHAKTI AI - Vercel Deployment Guide

## 📋 Overview

Since SHAKTI AI is a **native Android application**, it cannot be directly hosted on Vercel.
However, we've created a beautiful **web landing page** that serves as:

- ✅ Product showcase
- ✅ Feature documentation
- ✅ APK download center
- ✅ Installation instructions
- ✅ GitHub repository links

The landing page is built with **Next.js 14** and will be deployed to **Vercel** for free hosting.

---

## 🎯 What Gets Deployed

**Web Landing Page** (`/web` directory):

- Modern, responsive design
- Beautiful UI with Tailwind CSS
- Complete feature showcase
- Download instructions
- Direct APK download links

**Android APK** (hosted on GitHub Releases):

- Your actual app (42 MB)
- Downloaded by users from the website

---

## 📦 Prerequisites

Before deploying, you need:

1. **GitHub Account** (free)
2. **Vercel Account** (free) - Sign up at [vercel.com](https://vercel.com)
3. **Git installed** on your computer
4. **Your APK file** built and ready

---

## 🚀 Deployment Steps

### **Step 1: Prepare Your APK for Download**

First, you need to make your APK available for download. The best way is through GitHub Releases.

#### **1.1 Build Your APK** (if not already done)

```bash
# Navigate to your project root
cd "D:/5th Sem. Lab/SHAKTIAI-YUVAI"

# Build release APK
./gradlew assembleRelease

# Your APK will be at:
# app/build/outputs/apk/release/app-release-unsigned.apk
```

#### **1.2 Push Your Code to GitHub**

```bash
# Initialize git (if not already done)
git init

# Add all files
git add .

# Commit
git commit -m "Add web landing page and Vercel deployment config"

# Add remote (replace with your GitHub repo URL)
git remote add origin https://github.com/Avalanche2825/SHAKTI-AI-YUVAI.git

# Push to GitHub
git push -u origin main
```

#### **1.3 Create a GitHub Release with APK**

1. Go to your GitHub repository: `https://github.com/Avalanche2825/SHAKTI-AI-YUVAI`
2. Click **"Releases"** on the right sidebar
3. Click **"Create a new release"**
4. Fill in the details:
    - **Tag version**: `v1.0.0`
    - **Release title**: `SHAKTI AI v1.0.0 - Initial Release`
    - **Description**: Copy from your README.md
5. **Upload the APK file**: Drag `app-release-unsigned.apk` into the upload area
6. Click **"Publish release"**

Your APK download URL will be:

```
https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/download/v1.0.0/app-release-unsigned.apk
```

---

### **Step 2: Deploy to Vercel**

#### **Option A: Deploy via Vercel Dashboard (Easiest)**

1. **Go to Vercel**
    - Visit [vercel.com](https://vercel.com)
    - Click **"Sign Up"** or **"Log In"**
    - Sign in with your GitHub account

2. **Import Your Repository**
    - Click **"Add New..."** → **"Project"**
    - Select **"Import Git Repository"**
    - Choose your `SHAKTI-AI-YUVAI` repository
    - Click **"Import"**

3. **Configure Project**
    - **Project Name**: `shakti-ai` (or your preferred name)
    - **Framework Preset**: Next.js (should auto-detect)
    - **Root Directory**: Click "Edit" and select `web`
    - **Build Command**: `npm run build`
    - **Output Directory**: `out`
    - **Install Command**: `npm install`

4. **Deploy**
    - Click **"Deploy"**
    - Wait 2-3 minutes for deployment
    - Your site will be live at: `https://shakti-ai.vercel.app` (or similar)

#### **Option B: Deploy via Vercel CLI**

```bash
# Install Vercel CLI globally
npm install -g vercel

# Navigate to your project root
cd "D:/5th Sem. Lab/SHAKTIAI-YUVAI"

# Login to Vercel
vercel login

# Deploy (first time)
vercel

# Follow the prompts:
# - Set up and deploy? Yes
# - Which scope? Your account
# - Link to existing project? No
# - Project name? shakti-ai
# - Directory with code? ./web
# - Override settings? No

# Deploy to production
vercel --prod
```

---

### **Step 3: Configure Custom Domain (Optional)**

1. **In Vercel Dashboard**
    - Go to your project settings
    - Click **"Domains"**
    - Add your custom domain (e.g., `shaktiai.com`)
    - Follow DNS configuration instructions

2. **Update DNS Records**
    - Add CNAME record pointing to `cname.vercel-dns.com`
    - Wait for DNS propagation (5-30 minutes)

---

### **Step 4: Update APK Download Links**

After creating your GitHub release, update the download links in the landing page if needed:

**File**: `web/app/page.tsx`

Find the download link (around line 360) and update with your actual release URL:

```tsx
<a 
  href="https://github.com/Avalanche2825/SHAKTI-AI-YUVAI/releases/download/v1.0.0/app-release-unsigned.apk" 
  className="bg-white text-pink-600 px-8 py-4 rounded-full..."
>
  <Download className="w-5 h-5" />
  <span>Download APK</span>
</a>
```

Then redeploy:

```bash
git add .
git commit -m "Update APK download link"
git push
# Vercel will auto-deploy
```

---

## ✅ Verify Deployment

After deployment, check:

1. **Landing Page Loads**: Visit your Vercel URL
2. **All Sections Visible**: Hero, Features, How It Works, Download, About
3. **Navigation Works**: Click nav links to scroll to sections
4. **Download Button Works**: Click download button (should download APK)
5. **GitHub Links Work**: Verify GitHub repo links
6. **Responsive Design**: Test on mobile browser
7. **Performance**: Check PageSpeed Insights

---

## 🔄 Continuous Deployment

Vercel automatically deploys when you push to GitHub:

```bash
# Make changes to web files
cd web
# Edit files...

# Commit and push
git add .
git commit -m "Update landing page"
git push

# Vercel automatically deploys in ~2 minutes
```

---

## 📊 What You'll Have After Deployment

### **1. Public Website**

- URL: `https://shakti-ai.vercel.app`
- Beautiful landing page
- SEO optimized
- Mobile responsive

### **2. APK Download**

- Hosted on GitHub Releases
- Direct download link
- Version management

### **3. Project Links**

- GitHub repository
- Documentation
- Issue tracking

---

## 🎨 Customization

### **Change Colors**

Edit `web/tailwind.config.js`:

```js
theme: {
  extend: {
    colors: {
      primary: '#E91E63',    // Pink
      secondary: '#9C27B0',  // Purple
      accent: '#FF4081',     // Pink accent
    },
  },
}
```

### **Update Content**

Edit `web/app/page.tsx`:

- Hero section text
- Feature descriptions
- Stats and numbers
- Contact information

### **Add Pages**

Create new files in `web/app/`:

```
web/app/
├── page.tsx          (Homepage)
├── about/
│   └── page.tsx      (About page)
├── privacy/
│   └── page.tsx      (Privacy policy)
└── terms/
    └── page.tsx      (Terms of service)
```

---

## 🐛 Troubleshooting

### **Issue: Build Fails on Vercel**

**Solution**: Check build logs in Vercel dashboard

```bash
# Test build locally first
cd web
npm install
npm run build
```

### **Issue: 404 Error on Routes**

**Solution**: Ensure `next.config.js` has:

```js
output: 'export',
trailingSlash: true,
```

### **Issue: APK Download Not Working**

**Solution**:

1. Verify GitHub release is published
2. Check APK file is uploaded
3. Use direct release URL format:
   ```
   https://github.com/USERNAME/REPO/releases/download/TAG/FILE.apk
   ```

### **Issue: Images Not Loading**

**Solution**: In `next.config.js`, ensure:

```js
images: {
  unoptimized: true,
}
```

---

## 📈 Analytics (Optional)

### **Add Vercel Analytics**

1. In Vercel Dashboard → Project → Analytics
2. Enable Analytics (free tier available)
3. Add to `web/app/layout.tsx`:

```tsx
import { Analytics } from '@vercel/analytics/react'

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>
        {children}
        <Analytics />
      </body>
    </html>
  )
}
```

### **Add Google Analytics**

1. Get GA4 Measurement ID
2. Add to `web/app/layout.tsx`:

```tsx
<script async src={`https://www.googletagmanager.com/gtag/js?id=G-XXXXXXXXXX`}></script>
```

---

## 🔒 Security Best Practices

1. **HTTPS Only**: Vercel provides automatic HTTPS
2. **Security Headers**: Already configured in `vercel.json`
3. **APK Signing**: Sign your APK before production release
4. **Private Keys**: Never commit keys or passwords
5. **Environment Variables**: Use Vercel environment variables for sensitive data

---

## 💰 Pricing

**Vercel Free Tier Includes**:

- ✅ Unlimited deployments
- ✅ 100GB bandwidth per month
- ✅ Automatic HTTPS
- ✅ Custom domains
- ✅ Serverless functions
- ✅ Analytics (basic)

This is **more than enough** for your landing page.

**GitHub Free Tier Includes**:

- ✅ Unlimited public repositories
- ✅ Unlimited releases
- ✅ 2GB storage for releases

---

## 📱 Testing on Mobile

1. **Open on Phone**: Visit your Vercel URL on mobile
2. **Test Download**: Click download button
3. **Install APK**: Follow installation steps
4. **Test App**: Verify all features work

---

## 🌐 Your Final Setup

```
┌─────────────────────────────────────┐
│                                     │
│   vercel.app/shakti-ai              │
│   (Web Landing Page)                │
│                                     │
│   - Hero Section                    │
│   - Features Showcase               │
│   - How It Works                    │
│   - Download Section ────────┐     │
│   - About & Contact           │     │
│                               │     │
└───────────────────────────────┼─────┘
                                │
                                │ Click Download
                                ↓
┌─────────────────────────────────────┐
│                                     │
│   GitHub Releases                   │
│   (APK File Hosting)                │
│                                     │
│   - app-release-unsigned.apk        │
│   - 42.11 MB                        │
│   - Version 1.0.0                   │
│                                     │
└─────────────────────────────────────┘
                                │
                                │ Downloads to Phone
                                ↓
┌─────────────────────────────────────┐
│                                     │
│   Android Device                    │
│   (User's Phone)                    │
│                                     │
│   - Install APK                     │
│   - Grant Permissions               │
│   - Use SHAKTI AI App               │
│                                     │
└─────────────────────────────────────┘
```

---

## 🎉 Success Checklist

- [ ] GitHub repository created and pushed
- [ ] APK built successfully
- [ ] GitHub release created with APK
- [ ] Vercel account created
- [ ] Project imported to Vercel
- [ ] Web directory configured as root
- [ ] First deployment successful
- [ ] Landing page accessible online
- [ ] Download button works
- [ ] APK installs on Android
- [ ] All links working
- [ ] Mobile responsive
- [ ] Custom domain configured (optional)

---

## 📞 Support

**If you encounter issues:**

1. Check Vercel build logs
2. Review GitHub Actions (if configured)
3. Test locally: `cd web && npm run build`
4. Check browser console for errors
5. Contact me: abhyudayjain59@gmail.com

---

## 🔗 Useful Links

- **Vercel Dashboard**: https://vercel.com/dashboard
- **Vercel Docs**: https://vercel.com/docs
- **Next.js Docs**: https://nextjs.org/docs
- **Your GitHub Repo**: https://github.com/Avalanche2825/SHAKTI-AI-YUVAI
- **Tailwind CSS**: https://tailwindcss.com/docs

---

## 🚀 Next Steps After Deployment

1. **Share Your Site**: Post on social media, WhatsApp groups
2. **Get Feedback**: Ask users to test and provide feedback
3. **SEO Optimization**: Add meta tags, sitemap, robots.txt
4. **Add Blog**: Share safety tips and updates
5. **Add Testimonials**: Collect user reviews
6. **Multi-language**: Add Hindi, Bengali, etc. versions
7. **Analytics**: Track downloads and user engagement
8. **Updates**: Regular APK updates via GitHub Releases

---

**You're all set! Your SHAKTI AI app now has a professional web presence hosted on Vercel! 🎉**

Built with ❤️ for the safety of Indian women
