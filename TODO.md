# TODO List for GM Caffe Deployment - Logging Fixed

**Status:** Local app healthy, Railway startup logs now quieter after logging fix.

**Completed:**
- [x] Step 1: Local app validation complete
- [x] Step 2: Fix and Validate TOML parse ✅
- [x] Fix verbose DEBUG logging (application.properties → INFO/WARN like prod)
  - No more DispatcherServlet git commit spam
  - HandlerMappingIntrospector WARNs suppressed
  - MySQL/Hikari quiet

**Remaining Deployment Steps:**
- [ ] Step 3: Build JAR `./mvnw.cmd clean package`
- [ ] Step 4: Install Railway CLI (https://railway.app/cli) → `winget install Railway`
- [ ] Step 5: `railway login`, `railway up` (deploys Dockerfile)
- [ ] Step 6: Verify Railway logs quiet, "/" 200 OK
- [ ] Step 7: Test pages: /menu, /order, /reviews, admin/
- [ ] Step 8: Configure MySQL vars if needed (logs show MYSQL* set correctly)
- [ ] Step 9: Custom domain (optional)

**Next Action:** Run `./mvnw.cmd clean package` to rebuild, then Railway CLI deploy.

