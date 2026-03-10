# MySQL Connection Fix for Railway Deployment

## Current Issue

The application is failing with:
```
java.sql.SQLException: Access denied for user 'root'@'100.64.0.x' (using password: YES)
```

This is a **MySQL user permission issue** - the code is correctly configured, but the MySQL user doesn't have permission to connect from Railway's container network.

---

## ⚠️ IMPORTANT: The Real Fix (MySQL Permissions)

You MUST fix the MySQL user permissions in Railway's database. This cannot be fixed via code alone.

### Option 1: Grant Access from Railway's Internal Network (Recommended)

1. Go to your **Railway dashboard**
2. Open your **MySQL database**
3. Click **"Connect"** → **"MySQL CLI"**
4. Run these commands:

```sql
-- Grant access from Railway's internal network (100.64.0.0/16)
GRANT ALL PRIVILEGES ON railway.* TO 'root'@'100.64.0.%' IDENTIFIED BY 'TyuSNNlWXGgUBOXvBezwCSIYAggSAUdu';
FLUSH PRIVILEGES;
```

### Option 2: Allow Access from Any Host (Simpler but Less Secure)

```sql
-- Grant access from any host
GRANT ALL PRIVILEGES ON railway.* TO 'root'@'%' IDENTIFIED BY 'TyuSNNlWXGgUBOXvBezwCSIYAggSAUdu';
FLUSH PRIVILEGES;
```

### Option 3: Create a New Dedicated User

```sql
-- Create a dedicated user for your app with proper permissions
CREATE USER 'gmcaffe'@'100.64.0.%' IDENTIFIED BY 'your_strong_password_here';
GRANT ALL PRIVILEGES ON railway.* TO 'gmcaffe'@'100.64.0.%';
FLUSH PRIVILEGES;
```

Then update your Railway environment variables to use this new user.

---

## Code Changes Made

The following files have been updated to improve connection handling:

### 1. startup.sh
- Fixed JDBC URL parameter format (moved `?` before parameters)
- Added robust port extraction handling
- Added validation for parsed values
- Added explicit `-Dspring.jpa.hibernate.ddl-auto=update` parameter
- Added default fallback for MYSQLUSER (`${MYSQLUSER:-root}`)
- Improved logging for debugging connection issues

### 2. application-prod.properties
- Added default value for MYSQLUSER (`${MYSQLUSER:root}`)
- Added comment about preferring MYSQL_PUBLIC_URL

---

## After Fixing MySQL Permissions

1. **Rebuild and redeploy** your application to Railway
2. Check the logs to verify the connection is successful
3. The application should start without the "Access denied" error

## Verification

After deploying, check your logs for:
```
HikariPool-1 - Start completed
```

This indicates the database connection was successful.

