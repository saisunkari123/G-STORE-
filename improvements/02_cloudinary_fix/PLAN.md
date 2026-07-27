# 02 — Cloudinary Image Upload Fix

## Problem Statement
When Admin tries to upload a product image from the Admin panel, it fails with a **Cloudinary API error**.

---

## Root Cause Analysis

The `CloudinaryUploader` (`data/remote/CloudinaryUploader.kt`) reads credentials from `BuildConfig`:

```kotlin
private val CLOUD_NAME = BuildConfig.CLOUDINARY_CLOUD_NAME
private val API_KEY    = BuildConfig.CLOUDINARY_API_KEY
private val API_SECRET = BuildConfig.CLOUDINARY_API_SECRET
```

Possible causes (in order of likelihood):

1. **Missing/expired `.env` values** — `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, or `CLOUDINARY_API_SECRET` in the `.env` file are empty, wrong, or expired.
2. **GitHub Actions CI does not inject secrets at build time** — CI workflow may not pass the Cloudinary secrets as build env variables, so the release/debug APK has empty strings for credentials.
3. **Cloudinary free plan upload preset issue** — Cloudinary may require `upload_preset` for unsigned uploads if API credentials changed.
4. **SHA-1 signature mismatch** — The signing string format `folder=$FOLDER&timestamp=$timestamp` must exactly match what Cloudinary expects. If `FOLDER` or `timestamp` differ from the signed values, API returns 401.
5. **Network timeout on large images** — `connectTimeout = 30_000` and `readTimeout = 60_000` may be too short for slow mobile data connections with large image files.

---

## Investigation Steps

1. Check `.env` file to confirm all three Cloudinary credentials are set.
2. Check GitHub Actions CI workflow to confirm secrets are passed as environment variables.
3. Check Cloudinary dashboard for upload error logs.
4. Add detailed error logging in `CloudinaryUploader.upload()` to print the exact HTTP response body.

---

## Proposed Solution

### Step 1 — Better Error Logging
Add full response body logging when upload fails.

### Step 2 — Credential Validation at Upload Time
Before attempting upload, validate that `CLOUD_NAME`, `API_KEY`, and `API_SECRET` are non-empty and log a descriptive error if they are.

### Step 3 — CI Secrets Verification
Verify GitHub Actions workflow (`ci.yml`) passes `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` as environment variables to the Gradle build step.

### Step 4 — Improve Error Messages in Admin UI
Currently, the admin sees a generic "error" toast. Show the actual Cloudinary error message in the UI so the admin knows exactly what went wrong.

---

## Files to Modify

| File | Change |
|------|--------|
| `data/remote/CloudinaryUploader.kt` | Add credential validation, detailed error logging, better error message |
| `ui/admin/AdminScreen.kt` | Show full Cloudinary error message to admin |
| `.github/workflows/ci.yml` | Verify/add Cloudinary secrets in build step |

---

## Manual Test Cases (You test on phone)

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Admin tries to upload a small image (< 1 MB) for a product | Upload succeeds, image URL saved, product shows image |
| 2 | Admin tries to upload a large image (> 5 MB) | Upload succeeds (with proper timeout), or shows a clear error "Image too large, please use < 5 MB" |
| 3 | Admin uploads product image with no internet | Shows error "No internet connection" |
| 4 | Admin uploads gift item image | Upload succeeds, gift item shows image in checkout |
| 5 | Check Cloudinary dashboard | Uploaded images appear in the `products` folder |

---

## Automated Test Cases (Unit Tests)

| # | Test | File |
|---|------|------|
| 1 | `cloudinary upload url is correctly formatted with cloud name` | `CloudinaryUploaderTest.kt` |
| 2 | `cloudinary sha1 signature matches expected hash` | `CloudinaryUploaderTest.kt` |
| 3 | `empty credentials throw descriptive exception before network call` | `CloudinaryUploaderTest.kt` |

---

## Status
- [ ] Plan reviewed and approved
- [ ] Investigation complete (check .env + CI secrets)
- [ ] Implementation
- [ ] Automated tests pass (CI)
- [ ] Manual testing on physical device
