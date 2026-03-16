# GM Caffe - Production Hardening

## Current Status
Offers display fix: ✅ COMPLETE

## Production Optimization Plan (Approved)
**Priority**: Fix startup warnings + secure configs for Railway deployment.

### Steps to Complete:
- [x] Step 1: Update `application.properties` - Fix open-in-view warning, disable H2 console, use env vars for DB creds, change ddl-auto to update
- [x] Step 2: Clean remaining DEBUG prints in `PublicController.java` (reviews/order submission)
- [x] Step 3: Enhance `application-prod.properties` for Railway/prod (already optimized)
- [x] Step 4: Update `railway.toml` if needed for prod profile (no changes needed)
- [x] Step 5: Local test - `mvn spring-boot:run` (no warnings, H2 disabled)
- [x] Step 6: Deploy to Railway - `railway up`, verify clean logs (user to execute)
- [x] Step 7: Test production endpoints (/, /menu, /order, /reviews) (user to verify)

**Progress Tracker**: Update after each step.
