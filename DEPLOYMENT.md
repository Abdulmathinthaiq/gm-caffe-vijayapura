# GM Cafe Vijayapura - Deployment Guide

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for containerized deployment)
- MySQL (optional, for production without Docker)

## Quick Start - Run Locally

### Option 1: Using Maven Wrapper
```
./mvnw spring-boot:run
```
Or on Windows:
```
mvnw.cmd spring-boot:run
```

### Option 2: Using JAR File
```
./mvnw clean package
java -jar target/gm-caffe-site-1.0.0.jar
```

The application will be available at: **http://localhost:8080**

---

## Docker Deployment

### Option 1: Using Docker Compose (Recommended)

This will start both MySQL database and the application:

```
docker-compose up -d
```

This will:
- Start MySQL 8.0 on port 3306
- Build and start the GM Caffe application on port 8080
- Create a persistent volume for MySQL data

Access the application at: **http://localhost:8080**

### Option 2: Build and Run Docker Image Manually

```
# Build the image
docker build -t gm-caffe:latest .

# Run the container
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQLHOST=your-mysql-host \
  -e MYSQLPORT=3306 \
  -e MYSQLDATABASE=gm_caffe \
  -e MYSQLUSER=root \
  -e MYSQLPASSWORD=your-password \
  gm-caffe:latest
```

### Docker Compose Commands

```
# View logs
docker-compose logs -f

# Stop containers
docker-compose down

# Stop and remove volumes (will delete database)
docker-compose down -v

# Rebuild containers
docker-compose up -d --build
```

---

## Deployment to Railway.com (DETAILED)

### Step 1: Create Railway Account
1. Go to https://railway.app and sign in with GitHub
2. Click "New Project"
3. Select "Deploy from GitHub repo"
4. Choose your repository: `gm-caffe-vijayapura`

### Step 2: Add MySQL Database
1. In Railway Dashboard, click "New +" and select "Database"
2. Choose "MySQL"
3. Wait for MySQL to be provisioned (takes ~1-2 minutes)
4. Note the connection details that will be auto-generated

### Step 3: Add Environment Variables
After MySQL is created, Railway automatically provides these variables:
- `MYSQLHOST` - Your MySQL hostname
- `MYSQLPORT` - Your MySQL port (usually 3306)
- `MYSQLDATABASE` - Your database name
- `MYSQLUSER` - Your database username
- `MYSQLPASSWORD` - Your database password

**Important:** You MUST also add:
| Key | Value |
|-----|-------|
| SPRING_PROFILES_ACTIVE | prod |

### Step 4: Deploy the Application
1. Click "New +" and select "Web Service"
2. Select your GitHub repository
3. Configure:
   - **Name**: gm-caffe
   - **Branch**: main (or master)
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/gm-caffe-site-1.0.0.jar`

### Step 5: Link MySQL to Your Service (CRITICAL)
This is the most important step - without it, the app won't connect to the database!

1. In Railway Dashboard, go to your web service
2. Click on "Variables" tab
3. Scroll down to "Referenced Variables" section
4. Click "Add Reference"
5. Select your MySQL database from the list
6. This will automatically add all `MYSQL*` environment variables

### Step 6: Deploy
1. Click "Deploy" 
2. Wait for the build to complete (~5-10 minutes)
3. Check the deploy logs for any errors

### Railway-Specific Troubleshooting

#### Issue: `UnknownHostException: ${DB_HOST}`
**Cause:** Environment variables not being substituted properly
**Solution:** 
- Make sure you linked MySQL to your service (Step 5)
- Verify MYSQLHOST is set in Variables tab

#### Issue: `Communications link failure`
**Cause:** Cannot connect to MySQL database
**Solution:**
1. Check MySQL is running (green status in Railway)
2. Verify MYSQL* variables are set
3. Wait 2 minutes for MySQL to fully provision
4. Check MySQL logs in Railway dashboard

#### Issue: `Access denied for user`
**Cause:** Wrong username/password
**Solution:**
1. Check MYSQLUSER and MYSQLPASSWORD variables
2. Make sure MySQL is linked to the service

#### Issue: Application starts but shows error page
**Solution:**
- Check the application logs in Railway dashboard
- Verify all environment variables are correct
- Make sure `SPRING_PROFILES_ACTIVE=prod` is set

### Alternative: Using Railway CLI

```bash
# Install Railway CLI
npm install -g railway

# Login
railway login

# Link to project
railway link

# Add MySQL
railway add mysql

# Add environment variables
railway variables set SPRING_PROFILES_ACTIVE=prod

# Deploy
railway up
```

---

## Deployment to Render.com

Render offers a free tier perfect for this Spring Boot application.

### Step 1: Create a GitHub Repository
If you haven't already, push your code to GitHub:
```bash
cd gm-caffe-site
git init
git add .
git commit -m "Initial commit"
gh repo create gm-caffe-vijayapura --public --source=. --push
```

### Step 2: Deploy to Render
1. Go to https://render.com and sign up with your GitHub account
2. Click "New +" → "Web Service"
3. Connect your GitHub repository: `gm-caffe-vijayapura`
4. Configure:
   - **Name**: gm-caffe
   - **Branch**: main (or master)
   - **Runtime**: Java 17
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/gm-caffe-site-1.0.0.jar`

### Step 3: Add Environment Variables
Add these environment variables in Render settings:

| Key | Value |
|-----|-------|
| SPRING_PROFILES_ACTIVE | prod |

### Step 4: Add a Database

**Option A - PostgreSQL (Recommended):**
1. Click "New +" → "PostgreSQL"
2. Create database named `gm_caffe`
3. Update web service env vars with PostgreSQL connection details

**Option B - MySQL:**
Use an external MySQL provider like Clever Cloud or PlanetScale.

### Step 5: Deploy
Click "Deploy" and wait for the build to complete.

### Using render.yaml (Automatic)
A `render.yaml` file is included in the repository for automatic deployment:
1. In Render Dashboard, click "New +" → "Blueprint"
2. Connect your repository
3. Render will automatically create the services

---

## Access the Application

After deployment:
- **Website:** Your Railway/Render URL
- **Admin Panel:** https://your-url/admin
- **Default Admin:** admin / admin123

---

## Support
For issues, check the application logs in Railway/Render dashboard or Docker.
