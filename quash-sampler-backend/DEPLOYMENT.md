# Deploy to Render

## Quick Deploy Steps

### 1. Create GitHub Repository (Optional but Recommended)

Push this folder to GitHub:
```bash
cd quash-sampler-backend
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/quash-sampler-backend.git
git push -u origin main
```

### 2. Deploy to Render

1. **Sign up at Render:** https://render.com (Free with GitHub)

2. **Create New Web Service:**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Or use "Public Git Repository" and paste the URL

3. **Configure:**
   - **Name:** `quash-sampler-backend`
   - **Environment:** `Node`
   - **Build Command:** `npm install`
   - **Start Command:** `npm start`
   - **Plan:** `Free`

4. **Click "Create Web Service"**

5. **Wait for deployment** (2-3 minutes)

6. **Get your URL:** `https://quash-sampler-backend.onrender.com`

---

## Alternative: Deploy to Railway

1. **Sign up:** https://railway.app
2. **New Project** → Deploy from GitHub
3. Auto-detects Node.js and deploys
4. Get URL from Railway dashboard

---

## Alternative: Deploy to Fly.io

```bash
# Install flyctl
brew install flyctl

# Login
flyctl auth login

# Deploy
cd quash-sampler-backend
flyctl launch
# Follow prompts, select free tier

# Get URL
flyctl status
```

---

## Update Android App

Once deployed, update `gradle.properties`:
```properties
QUASH_API_BASE_URL=https://your-app.onrender.com/
```

Then rebuild:
```bash
./gradlew assembleDebug
```

---

## Important Notes

### Render Free Tier
- ✅ Free forever
- ⚠️ Spins down after 15 minutes of inactivity
- ⚠️ First request after sleep takes 30-50 seconds
- ✅ Perfect for testing/demo

### Keep Awake (Optional)
Add a cron job or use a service like UptimeRobot to ping your API every 10 minutes to keep it awake.

---

## Test Deployment

```bash
# Health check
curl https://your-app.onrender.com/health

# Login
curl -X POST https://your-app.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"9876543210"}'
```