# 🚀 Deploy GeoReport to the Cloud (Free)

## Recommended: Railway.app (Easiest)

Railway offers **free hosting** with MySQL database included!

### Step 1: Create Railway Account
1. Go to **https://railway.app**
2. Click "Login" → Sign in with GitHub
3. Authorize Railway to access your repositories

### Step 2: Create New Project
1. Click **"New Project"**
2. Select **"Deploy from GitHub repo"**
3. Choose **`sakthivikramthavamani-code/geo_location`**

### Step 3: Add MySQL Database
1. In your project, click **"+ New"**
2. Select **"Database"** → **"MySQL"**
3. Railway will create a MySQL instance automatically

### Step 4: Connect App to Database
1. Click on your **geo_location** service
2. Go to **"Variables"** tab
3. Click **"Add Variable Reference"**
4. Add these from your MySQL service:
   - `DATABASE_URL` = `${{MySQL.MYSQL_URL}}`
   - `DATABASE_USER` = `${{MySQL.MYSQL_USER}}`
   - `DATABASE_PASSWORD` = `${{MySQL.MYSQL_PASSWORD}}`

5. Add these additional variables:
   ```
   SPRING_PROFILES_ACTIVE = prod
   JWT_SECRET = your-super-secret-key-here-make-it-long
   ```

### Step 5: Deploy!
1. Railway will automatically detect the Dockerfile
2. Click **"Deploy"** or wait for auto-deploy
3. Once deployed, click **"Generate Domain"** to get a public URL

### Your app will be live at:
`https://your-app-name.up.railway.app`

---

## Alternative: Render.com

### Step 1: Create Account
1. Go to **https://render.com**
2. Sign up with GitHub

### Step 2: Create Web Service
1. Click **"New +"** → **"Web Service"**
2. Connect your GitHub repo
3. Configure:
   - **Name:** georeport
   - **Runtime:** Docker
   - **Instance Type:** Free

### Step 3: Add Database
1. Create a **MySQL database** on a free service like:
   - Aiven (free tier)
   - PlanetScale (free tier)
2. Add environment variables in Render dashboard

---

## Alternative: Koyeb.com

1. Go to **https://koyeb.com**
2. Sign up with GitHub
3. Create new app from GitHub repo
4. Add PostgreSQL database (free 50 hrs/month)
5. Configure environment variables
6. Deploy!

---

## Environment Variables Needed

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | MySQL connection URL |
| `DATABASE_USER` | Database username |
| `DATABASE_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for JWT tokens |
| `SPRING_PROFILES_ACTIVE` | Set to `prod` |
| `PORT` | Usually auto-set by platform |

---

## Files Added for Deployment

- **Dockerfile** - Multi-stage build configuration
- **railway.toml** - Railway-specific settings
- **application-prod.yml** - Production configuration
