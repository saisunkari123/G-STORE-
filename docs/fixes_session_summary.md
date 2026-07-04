# Session Summary & Transition Guide

This document captures all the critical fixes, architectural enhancements, and key updates made in this session to carry over to a new chat context.

---

## 1. Summary of All Fixes & Updates

### 🔴 Firestore Rule Permissibility & Product Load Crash (Home Screen Empty)
* **Problem:** Firestore rules required authentication to read the `/products` collection. When the app initialized, the query listener threw `PERMISSION_DENIED` and failed silently, leaving `productsList` permanently empty.
* **Fix Applied:** 
  1. Refactored `AppState.kt` to extract the products observer into a restartable `observeProducts()` function.
  2. Scheduled `observeProducts()` to run immediately at startup (for public reads) *and* re-trigger immediately upon successful user/admin login (`currentUser` setter).
  3. **Instruction for Firebase Console:** Updated Firestore security rules to allow public reads for the `/products` collection:
     ```javascript
     match /products/{id} {
       allow read: if true;
       allow write: if request.auth != null;
     }
     ```

### 🔴 Admin Product Image Upload (Firebase Storage Spark Limit & DNS)
* **Problem:** Image uploads to Firebase Storage failed with `"Object does not exist at location"` because:
  1. The project runs on the free Firebase **Spark Plan** which restricts/does not support Cloud Storage bucket instances (requires a card for **Blaze Plan**).
  2. Native Android temporary content URIs (`content://`) expired when processed on background coroutines.
* **Fix Applied:**
  1. **Switched to Cloudinary:** Bypassed Firebase Storage limits entirely by implementing a direct upload endpoint to Cloudinary using your account details (`k1lw675z`).
  2. **Created [CloudinaryUploader.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/data/remote/CloudinaryUploader.kt):** Built a self-contained utility that performs SHA-1 signed multipart POST uploads using Android's native `HttpsURLConnection` (matching Android's native DNS/network stack, preventing DNS host resolution errors).
  3. **Updated [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt):** Integrated `CloudinaryUploader.upload(tempFile)` inside an IO-safe thread context (`withContext(Dispatchers.IO)`), copy-caching gallery URIs into temporary files on startup to avoid Android permission expiration.

### 🔴 Customer Image Rendering Bug (Coil deadlock)
* **Problem:** Product cards in `CustomerScreen.kt` used `rememberAsyncImagePainter` but nested it inside a conditional block checking `painterState is Success`. Because Coil only triggers image loading when the painter is actually drawn on the screen, this created a deadlock where the image remained in the `Loading` state forever (showing white spaces).
* **Fix Applied:** Replaced the broken painter-state conditional pattern in `CustomerProductCard` with `SubcomposeAsyncImage`. This natively handles transition animations, displays a `CircularProgressIndicator` while downloading, and renders a default stylized shopping cart placeholder on errors/missing URLs.

### 🔴 Empty Variant Layout Breaking (Admin Uploaded Cards)
* **Problem:** Products uploaded by the admin without variants crashed or returned early in `CustomerProductCard`, rendering as flat "empty description boxes" without pricing or buttons.
* **Fix Applied:** Modified `CustomerProductCard` to handle products without variants gracefully. It now displays a default "Price TBD" label and a disabled "COMING SOON" button while keeping card heights and layouts visually identical to seeded items.

### 🔴 Password Visibility Toggles
* **Feature Added:** Added visual password toggles (Eye / Eye-Off icons) to all three password input fields (Customer Login, Customer Registration, Admin Login) in `LoginScreen.kt`.

---

## 2. Active Credentials Configuration

* **Cloudinary Cloud Name:** `k1lw675z`
* **Cloudinary API Key:** `498889461713286`
* **Cloudinary API Secret:** `8SR-robZhuJf-5ehvJTFCscCatY`
* **Admin Login Details:** `admin@gstore.com` / `Ram@123`

---

## 3. What to Carry to the New Chat

1. Confirm that **Firestore rules** in the Firebase Console have been updated to allow public reads on `/products` (see Part 1).
2. Note that image uploads are processed via **Cloudinary** using Android's native `HttpsURLConnection` inside [CloudinaryUploader.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/data/remote/CloudinaryUploader.kt).
3. Image rendering has been modernized to use `SubcomposeAsyncImage` in [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt).
