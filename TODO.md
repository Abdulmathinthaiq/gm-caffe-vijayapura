# GM Caffe - Fix Offers Not Displaying in Browser

## Steps to Complete:

- [x] Step 1: Update DataInitializer.java to force activate all existing offers with detailed logging

- [x] Step 2: Add debug logging in PublicController.home() for offers fetching
- [x] Step 3: Verify OfferRepository methods and add if needed (existing findByActiveTrueOrderByDisplayOrderAsc() is correct)
- [x] Step 4: Local test - FIXED dev DB URL, banner confirmed working locally with 4 offers
- [ ] Step 5: Deploy changes to Railway and verify logs/deployment
- [ ] Step 6: Test production site - confirm offers banner displays
- [ ] Step 7: Clean up debug logs if desired

**Current Progress:** Steps 1-3 complete. Proceed to Step 4: Local test with `./mvnw.cmd spring-boot:run` then visit http://localhost:8080 and check console logs for [OFFERS INIT] and [PUBLIC HOME]. Report logs here if issues.

