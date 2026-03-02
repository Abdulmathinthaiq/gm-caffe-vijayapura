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
  -e DB_HOST=your-mysql-host \
  -e DB_PORT=3306 \
  -e DB_NAME=gm_caffe \
  -e DB_USER=gmcaffe \
  -e DB_PASSWORD=your-password \
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

### Step 3: Deploy the Application
1. Click "New +" and select "Web Service"
2. Select your GitHub repository
3. Configure:
   - Name: gm-caffe
   - Branch: main (or master)
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/gm-caffe-site-1.0.0.jar`

### Step 4: Add Environment Variables
Add these environment variables:

| Key | Value |
|-----|-------|
| JAVA_VERSION | 17 |
| SPRING_PROFILES_ACTIVE | prod |

### Step 5: Deploy
Click "Deploy" and wait for the build to complete.

---

## Deployment to Render.com (Free Tier)

### Step 1: Create a MySQL Database on Render
1. Go to https://dashboard.render.com
2. Click "New +" and select "PostgreSQL" or "MySQL"
3. Configure:
   - Name: gm-caffe-db
   - Database Name: gm_caffe
   - User: gmcaffe
4. Copy the "Internal Database URL" after creation

### Step 2: Deploy Web Service
1. In Render Dashboard, click "New +" and select "Web Service"
2. Connect your GitHub repository: `Abdulmathinthaiq/gm-caffe-vijayapura`
3. Configure:
   - Name: gm-caffe
   - Branch: master
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/gm-caffe-site-1.0.0.jar`

### Step 3: Add Environment Variables
In the Render web service settings, add these environment variables:

| Key | Value |
|-----|-------|
| JAVA_VERSION | 17 |
| SPRING_PROFILES_ACTIVE | prod |
| DB_HOST | your-mysql-host.from.render.com |
| DB_PORT | 3306 |
| DB_NAME | gm_caffe |
| DB_USER | gmcaffe |
| DB_PASSWORD | (your-database-password) |

**Note:** Replace the DB values with the ones from your Render MySQL database.

### Step 4: Deploy
Click "Create Web Service" and wait for the build to complete.

## Access the Application

After deployment:
- **Website:** https://gm-caffe.onrender.com (or your custom domain)
- **Admin Panel:** https://gm-caffe.onrender.com/admin
- **Default Admin:** admin / admin123

## Troubleshooting

### Docker Issues
- Ensure Docker Desktop is running
- Check logs with `docker-compose logs -f`
- Rebuild with `docker-compose up -d --build`

### Build Fails
- Ensure JAVA_VERSION is set to 17 in environment variables
- Check that the Build Command is correct

### Database Connection Issues
- Verify MySQL database is created and running
- Check that DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD are correct
- Make sure to add the environment variables in Render dashboard

### Application Won't Start
- Check the logs in Render dashboard or Docker logs
- Verify all required environment variables are set

## Support
For issues, check the application logs in Docker or the Render dashboard.
