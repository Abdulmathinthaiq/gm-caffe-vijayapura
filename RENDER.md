# Deploy to Render.com - Complete Guide

Render offers a free tier that works well for this Spring Boot application.

## Prerequisites
- GitHub account with your repository
- Render.com account (free)

---

## Option 1: Deploy with PostgreSQL (Recommended for Render)

### Step 1: Create a GitHub Repository
```bash
cd gm-caffe-site
git init
git add .
git commit -m "Initial commit"
gh repo create gm-caffe-vijayapura --public --source=. --push
```

### Step 2: Deploy to Render

1. Go to https://render.com and sign up with your GitHub account

2. Create a new Web Service:
   - Click "New +" → "Web Service"
   - Connect your GitHub repository: gm-caffe-vijayapura
   - Configure:
     - Name: gm-caffe
     - Branch: main or master
     - Runtime: Java 17
     - Build Command: ./mvnw clean package -DskipTests
     - Start Command: java -jar target/gm-caffe-site-1.0.0.jar

3. Add Environment Variables:
   | Key | Value |
   |-----|-------|
   | SPRING_PROFILES_ACTIVE | prod |
   | DB_HOST | dpg-d6kbc315pdvs73cn1j20-a |
   | DB_PORT | 5432 |
   | DB_NAME | gm_caffe |
   | DB_USER | gm_caffe_user |
   | DB_PASSWORD | vd31EZE6DwqjisrShaKJ0e8icpp7p5tq |

4. Click "Create Web Service"

### Step 3: Add PostgreSQL Database

1. In Render Dashboard, click "New +" → "PostgreSQL"
2. Configure:
   - Name: gm-caffe-db
   - Database Name: gm_caffe
3. Click "Create Database"

4. Copy the PostgreSQL connection string from the database page

5. Update your Web Service environment variables with the PostgreSQL details

### Step 4: Deploy
- Click "Deploy" and wait for the build to complete
- Your app will be live at: https://gm-caffe.onrender.com

---

## Option 2: Deploy with MySQL (Using Docker)

### Step 1: Create a Docker-based Web Service

1. Go to https://render.com → "New +" → "Web Service"
2. Select your repository
3. Configure:
   - Name: gm-caffe
   - Dockerfile Path: Dockerfile
   - Instance Type: Free

4. Add Environment Variables:
   | Key | Value |
   |-----|-------|
   | SPRING_PROFILES_ACTIVE | prod |
   | MYSQLHOST | (your MySQL host) |
   | MYSQLPORT | 3306 |
   | MYSQLDATABASE | gm_caffe |
   | MYSQLUSER | root |
   | MYSQLPASSWORD | (your MySQL password) |

### Step 2: Add MySQL Database

Use an external MySQL provider like:
- Clever Cloud (free MySQL)
- PlanetScale (free MySQL)
- Amazon RDS (free tier)

---

## Database Configuration

### For PostgreSQL (Recommended)
Update application-prod.properties:

```properties
# PostgreSQL Configuration for Render
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT:5432}/${DB_NAME}?sslmode=disable
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

### For MySQL (Already Configured)
The current application-prod.properties already has MySQL configuration.

---

## Troubleshooting

### Issue: Build fails
- Ensure Java 17 is selected in Render settings
- Check that Maven wrapper (mvnw) is executable
- Verify the build command is correct

### Issue: Application crashes
- Check that database credentials are correct
- Verify environment variables are set
- Check the logs in Render dashboard

### Issue: Database connection timeout
- Wait for database to fully provision (1-2 minutes)
- Ensure database is in the same region as your web service

---

## Access After Deployment

- Website: https://your-app-name.onrender.com
- Admin Panel: https://your-app-name.onrender.com/admin
- Default Credentials: 
  - Username: admin
  - Password: admin123

---

## Important Notes

1. Free Tier Limitations:
   - Web service sleeps after 15 minutes of inactivity
   - Database may have connection limits
   - Sleep mode causes first request to be slow

2. Keep Awake:
   - Use a free service like https://cron-job.org to ping your site every 5 minutes

3. Custom Domain:
   - Render allows custom domains on free tier

