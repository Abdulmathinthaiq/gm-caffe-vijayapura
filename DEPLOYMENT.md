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

## Deployment to Railway.com

### Step 1: Create a Railway Project
1. Go to https://railway.app and sign in
2. Click "New Project" 
3. Select "Deploy GitHub repo"
4. Connect your GitHub repository

### Step 2: Add MySQL Database
1. In Railway Dashboard, click "New +" and select "Database"
2. Choose "MySQL"
3. Wait for MySQL to be provisioned

### Step 3: Deploy the Application
1. Click "New +" and select "Web Service"
2. Select your GitHub repository
3. Configure:
   - Name: gm-caffe
   - Branch: main (or master)
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/gm-caffe-site-1.0.0.jar`

### Step 4: Add Environment Variables
In the Railway service settings, add these environment variables:

| Key | Value |
|-----|-------|
| SPRING_PROFILES_ACTIVE | prod |

**Important:** Railway automatically provides MySQL environment variables:
- MYSQLHOST
- MYSQLPORT  
- MYSQLDATABASE
- MYSQLUSER
- MYSQLPASSWORD

These are automatically injected by Railway when you link your MySQL database to the service.

### Step 5: Link MySQL Database to Service
1. In Railway Dashboard, go to your web service
2. Click on "Variables" tab
3. Scroll down to "Referenced Variables"
4. Click "Add Reference" and select your MySQL database
5. This will automatically add the MYSQL* environment variables

### Step 6: Deploy
Click "Deploy" and wait for the build to complete.

---

## Railway Deployment Checklist

Before deploying, ensure these files are in your repository:

- [ ] `pom.xml` - Maven configuration
- [ ] `src/` - Java source code
- [ ] `mvnw` and `mvnw.cmd` - Maven wrapper files
- [ ] `Dockerfile` - Docker build configuration
- [ ] `railway.toml` - Railway deployment configuration

---

## Troubleshooting Railway Deployment

### Issue: Application crashes on startup
**Solution:** 
1. Check that MySQL database is linked properly
2. Verify environment variables are set (MYSQLHOST, MYSQLPORT, etc.)
3. Check the build logs for errors

### Issue: Build fails
**Solution:**
- Ensure JAVA_VERSION is set to 17 in environment variables
- Verify the Build Command is correct

### Issue: Database connection errors
**Solution:**
1. Verify MySQL database is running
2. Check that MYSQL* environment variables are present
3. Make sure the database is linked to the service
4. Wait for MySQL to fully provision (can take 1-2 minutes)

### Issue: Application won't start
**Solution:**
- Check the logs in Railway dashboard
- Verify all required environment variables are set
- Ensure the JAR file was built successfully

---

## Access the Application

After Railway deployment:
- **Website:** Your Railway URL (e.g., https://gm-caffe-production.up.railway.app)
- **Admin Panel:** https://your-url/admin
- **Default Admin:** admin / admin123

---

## Support
For issues, check the application logs in Railway dashboard or Docker.

