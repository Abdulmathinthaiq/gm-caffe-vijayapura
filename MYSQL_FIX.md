# MySQL Connection Fix for Railway Deployment

## Current Issue

The application is failing with:
```
java.sql.SQLException: Access denied for user 'root'@'100.64.0.x' (using password: YES)
```

This is a **MySQL user permission issue** - not a code issue.

## Root Cause

The MySQL database user 'root' is not configured to allow connections from Railway's internal container network (IP range 100.64.0.0/16).

## Solution

You need to grant the MySQL user permission to connect from Railway's internal network.

### Option 1: Use Railway's MySQL CLI (Recommended)

1. Go to your Railway dashboard
2. Open your MySQL database
3. Click "Connect" → "MySQL CLI"
4. Run these commands:

```sql
-- Grant access from Railway's internal network (100.64.0.0/16)
GRANT ALL PRIVILEGES ON railway.* TO 'root'@'100.64.0.%' IDENTIFIED BY 'TyuSNNlWXGgUBOXvBezwCSIYAggSAUdu';
FLUSH PRIVILEGES;
```

### Option 2: Allow Access from Any Host (Simpler)

```sql
-- Grant access from any host (less secure but works)
GRANT ALL PRIVILEGES ON railway.* TO 'root'@'%' IDENTIFIED BY 'TyuSNNlWXGgUBOXvBezwCSIYAggSAUdu';
FLUSH PRIVILEGES;
```

### Option 3: Create a New User with Proper Permissions

```sql
-- Create a dedicated user for your app
CREATE USER 'gmcaffe'@'100.64.0.%' IDENTIFIED BY 'your_new_strong_password';
GRANT ALL PRIVILEGES ON railway.* TO 'gmcaffe'@'100.64.0.%';
FLUSH PRIVILEGES;
```

Then update your Railway environment variables to use this new user.

## After Fixing Permissions

1. Redeploy your application to Railway
2. Check the logs to verify the connection is successful

## Code Changes Made

The following files have been updated to improve connection handling:

1. **startup.sh** - Added:
   - Enhanced JDBC connection parameters (`connectTimeout=30000`, `socketTimeout=60000`)
   - Better error handling and logging

2. **application-prod.properties** - Added:
   - `connectTimeout=30000` and `socketTimeout=60000` to JDBC URL
   - `leak-detection-threshold=60000` to HikariCP for better connection monitoring

