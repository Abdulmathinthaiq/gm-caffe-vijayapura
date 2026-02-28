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
java -jar target/gm-caffe-site-0.0.1-SNAPSHOT.jar
```

The application will be available at: **http://localhost:8080**

## Deployment to Cloud (Render.com - Free)

### Step 1: Prepare for Production
1. Create a MySQL database (using free tier on Render or MySQL.com)
2. Update `application.properties` with production database credentials
3. Build the JAR file

### Step 2: Deploy to Render
1. Push your code to GitHub
2. Sign up at render.com
3. Create a new "Web Service"
4. Connect your GitHub repository
5. Configure:
   - Build Command: `./mvnw clean package -DskipTests`
   - Start Command: `java -jar target/gm-caffe-site-0.0.1-SNAPSHOT.jar`
6. Add environment variables for database

## Deployment to VPS (DigitalOcean/RackSpace)

### Step 1: Upload JAR
```
bash
scp target/gm-caffe-site-0.0.1-SNAPSHOT.jar user@your-server:/home/
```

### Step 2: Run as Service
Create a systemd service file:
```
bash
sudo nano /etc/systemd/system/gmcaffe.service
```

Add content:
```
ini
[Unit]
Description=GM Cafe Application
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/home
ExecStart=java -jar /home/gm-caffe-site-0.0.1-SNAPSHOT.jar
ExecStop=kill $(pgrep -f gm-caffe)
Restart=always

[Install]
WantedBy=multi-user.target
```

### Step 3: Start Service
```
bash
sudo systemctl daemon-reload
sudo systemctl start gmcaffe
sudo systemctl enable gmcaffe
```

## Configuration for Production

### Database Setup (MySQL)
```
properties
spring.datasource.url=jdbc:mysql://localhost:3306/gm_caffe?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### Disable H2 Console in Production
```
properties
spring.h2.console.enabled=false
```

### Security
- Change default admin credentials
- Enable HTTPS/SSL
- Update CORS settings if needed

## Access the Application

After deployment:
- **Website:** http://your-domain.com:8080
- **Admin Panel:** http://your-domain.com:8080/admin
- **Default Admin:** admin / admin123

## Troubleshooting

### Port Already in Use
```
bash
# Find process using port 8080
lsof -i :8080
# Kill process
kill -9 <PID>
```

### Database Connection Issues
- Verify MySQL is running
- Check credentials in application.properties
- Ensure database exists

### Memory Issues
```
bash
java -Xmx512m -jar target/gm-caffe-site-0.0.1-SNAPSHOT.jar
```

## Support
For issues, check logs:
```
bash
tail -f /var/log/gmcaffe.log
