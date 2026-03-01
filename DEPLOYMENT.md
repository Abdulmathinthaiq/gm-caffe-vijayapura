# GM Cafe Vijayapura - Deployment Guide

## Prerequisites
- Java 17 or higher
- Maven 3.6+
- MySQL (optional, for production)

## Quick Start - Run Locally

### Option 1: Using Maven Wrapper
```
bash
cd gm-caffe-site
./mvnw spring-boot:run
```
Or on Windows:
```
bash
cd gm-caffe-site
mvnw.cmd spring-boot:run
```

### Option 2: Using JAR File
```
bash
cd gm-caffe-site
./mvnw clean package
java -jar target/gm-caffe-site-1.0.0.jar
```

The application will be available at: **http://localhost:8080**

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

### Build Fails
- Ensure JAVA_VERSION is set to 17 in environment variables
- Check that the Build Command is correct

### Database Connection Issues
- Verify MySQL database is created and running
- Check that DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD are correct
- Make sure to add the environment variables in Render dashboard

### Application Won't Start
- Check the logs in Render dashboard
- Verify all required environment variables are set

## Support
For issues, check the application logs in the Render dashboard.
