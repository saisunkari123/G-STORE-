# G-STORE — Issues Fixed Log

This document tracks issues identified and resolved in G-STORE, including their root causes, fixes applied, and modified files.

---

## 1. App Automatically Exiting / Crashing after Customer Login
* **Date Fixed:** 2026-07-04
* **Issue:** After successfully logging in or registering, the app would close/exit automatically.
* **Root Cause:** In [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt), the product card UI (`CustomerProductCard`) attempted to fetch the default variant using `product.variants.first()`. If an admin created a product without any variants defined (or if the variants were empty during fetch), this threw a `NoSuchElementException` on the Main UI thread, crashing the app.
* **Fix Applied:** 
  * Added a safety guard at the start of [CustomerProductCard](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt#L339) to return early if the variants list is empty.
  * Replaced `product.variants.first()` with `product.variants.firstOrNull() ?: return` as a redundant fallback.
* **Files Modified:**
  * [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt)

---

## 2. Firestore Sync & Permission Error App Crashes
* **Date Fixed:** 2026-07-04
* **Issue:** Uncaught Firebase exceptions (such as `PERMISSION_DENIED` due to security rules or lack of authentication) caused the app to crash in the background.
* **Root Cause:** In the repository implementation, real-time snapshot listeners using `callbackFlow` were calling `close(error)` when a Firestore error occurred. Closing the flow with an error propagates the exception up the coroutine collector, causing thread crashes if unhandled.
* **Fix Applied:** 
  * Rewrote all `callbackFlow` builders in [FirebaseRepositories.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/data/repository/FirebaseRepositories.kt) to catch Firestore errors, print their stack traces for debugging, and safely emit `emptyList()` or `null` using `trySend()`.
  * Added `runCatching` or `try-catch` inside all document-to-object deserializations (`toObject(...)`) inside listeners to protect the app from crashes caused by malformed/old Firestore data.
* **Files Modified:**
  * [FirebaseRepositories.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/data/repository/FirebaseRepositories.kt)

---

## 3. Customer Authentication Failures & Silent "No Action"
* **Date Fixed:** 2026-07-04
* **Issue:** When a customer clicked the Login or Create Account buttons, either "no action" happened or the app crashed due to network/credential issues.
* **Root Cause:** Missing robust try-catch mechanisms on Firebase Auth listener completions, and lack of clear user feedback for standard Auth exception conditions (like non-existent accounts, network timeouts).
* **Fix Applied:** 
  * Completely overhauled `customerLogin` and `customerRegister` in [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt) using robust `addOnSuccessListener` and `addOnFailureListener` blocks.
  * Mapped Firebase Auth exceptions to friendly, readable messages on the screen (e.g., *"No account found with this email. Please register first."*, *"No internet connection."*, etc.).
  * Added error handling for cases where Auth succeeds but the user's Firestore profile database entry is missing/corrupted.
* **Files Modified:**
  * [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt)

---

## 4. Text Invisible / Mixed with Background (Dark Mode Issue)
* **Date Fixed:** 2026-07-03
* **Issue:** Text in text input fields, tab selectors, and buttons was invisible (white-on-white) when the user's phone was in System Dark Mode.
* **Root Cause:** The application theme in [Theme.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/theme/Theme.kt) checked `isSystemInDarkTheme()` and used `DarkColorScheme`, which changed default text colors to white. Since the screens use explicit white backgrounds for cards, fields, and scaffolds, the text became invisible.
* **Fix Applied:**
  * Forced the application to always run in Light Theme in [Theme.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/theme/Theme.kt#L43) since G-STORE is visually built around a clean, emerald-and-gold light palette.
  * Added explicit text colors (`focusedTextColor = Color.Black`, `unfocusedTextColor = Color.Black`) to all `OutlinedTextField` instances inside [LoginScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/auth/LoginScreen.kt) to guarantee legibility under any condition.
* **Files Modified:**
  * [Theme.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/theme/Theme.kt)
  * [LoginScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/auth/LoginScreen.kt)

---

## 5. Admin New Product Image Upload Failure
* **Date Fixed:** 2026-07-03
* **Issue:** When the Admin tried to add a new product with a custom image, it failed to upload and silently reverted to a default Unsplash white rice image.
* **Root Cause:** 
  1. The app attempted to upload product images to Firebase Storage, which failed due to rules limitations or Spark plan quotas.
  2. When the upload failed, the product fell back to the default Unsplash image hardcoded in `AppState.adminAddProduct`.
* **Fix Applied:**
  * Configured [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt) to save the local gallery image URI (`content://`) directly to Firestore. Coil (`AsyncImage`) loads this URI natively on the device.
  * Removed the hardcoded Unsplash fallback image from `adminAddProduct()` in [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt#L560).
  * Added a stylized branded product placeholder in [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt) for items saved without any image.
* **Files Modified:**
  * [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt)
  * [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt)
  * [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt)

---

## 6. Added Beautiful Order Placed Success UI (with Live Confetti)
* **Date Fixed/Added:** 2026-07-04
* **Issue:** After checking out, the app cleared the cart but provided no visual confirmation, remaining on an empty cart screen.
* **Fix Applied/Implementation:**
  * Developed a premium `OrderSuccessView` matching G-STORE's Visual System (Stitch colors `RoyalEmerald` and `DeepGold`).
  * Integrated a custom, physics-based falling Confetti simulation drawn directly on a Compose `Canvas` with an frame-rate loop.
  * Added a stylized summary card containing the dynamic Order ID, Total Amount Paid, Delivery Address details, and COD payment method.
  * Modified [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt) to expose a `lastPlacedOrder` state, which is populated on checkout success.
  * Configured [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt) to intercept the UI flow and show this overlay full-screen, with a "Continue Shopping" button that brings the customer back to the storefront.
  * **Transition Bug Fix:** Postponed the `clearCart()` call until *after* the Firestore order write successfully finishes (instead of running it instantly/synchronously on button press). Added an `isPlacingOrder` flag to display a "Placing..." loading spinner and disable the place order button. This prevents the "Cart is empty" intermediate screen from showing during the 2-5 seconds Firestore write window.
* **Files Modified:**
  * [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt)
  * [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt)

---

## 7. Customer UI Enhancements: Theme Toggle, Address CRUD, and simplified Orders page
* **Date Fixed/Added:** 2026-07-04
* **Fix Applied/Implementation:**
  * **Home Page View All:** Connected the "View All" label inside [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt) to reset both search query and category filters back to "All Grains" upon click.
  * **Bottom Navigation Overhaul:** Added an "Account" navigation item. Bottom bar tabs are now: Shop (Home), Cart, Orders, and Account.
  * **Orders History Cleanup:** Removed the "Reorder All Items" button and the step-by-step progress tracking dots. Replaced them with a simplified local delivery boy indicator displaying the assigned rider's name and contact number.
  * **Address Management CRUD:** Added edit and delete buttons for each address card. Users can edit flat details and landmarks in-place, or delete existing addresses instantly.
  * **Account Screen (Profile & Settings):** Created a beautiful `CustomerAccountView` displaying profile details (Name, Email, Phone) and Settings (with a fully-functioning Dark Mode toggle switch and Log Out trigger).
  * **Log Out Dialog:** Removed the log out arrow button from the home screen top app bar. Added a Log Out row in settings that triggers a confirmation dialog in the center of the screen.
* **Files Modified:**
  * [Theme.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/theme/Theme.kt)
  * [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt)
  * [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt)

---

## 8. Specific Auth Login Error Messages and Storefront Redesign
* **Date Fixed/Added:** 2026-07-04
* **Fix Applied/Implementation:**
  * **Email-First Auth Check:** Updated `customerLogin` in [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt) to search for the customer's email in Firestore *before* calling Firebase Auth. If not found, it fails immediately with `"Email is not registered. Please register first."` to direct unregistered users.
  * **Incorrect Password Feedback:** Any Firebase Auth credential error (after confirming the email exists in our Firestore database) now yields `"Password incorrect."` instead of the generic login error.
  * **Top Bar Cleanup:** Removed the location icon and address display from G-STORE's home top app bar in [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt).
  * **FeaturedBanner Removal:** Removed the green BasmatiFeatured Reserve card.
  * **Shifted Search & Filters Up:** Shifted the search bar and category filters up directly under G-STORE title bar.
  * **Removed View All Button:** Removed the "View All" button from catalog list section headers.
* **Files Modified:**
  * [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt)
  * [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt)

---

## 9. Admin Image Upload to Firebase Storage, Order Session Isolation, and Rider Logic Clean Up
* **Date Fixed/Added:** 2026-07-04
* **Fix Applied/Implementation:**
  * **Admin Cloud Image Upload:** Re-implemented proper async Firebase Storage upload in [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt). Added a robust local error state description instead of writing local device `content://` paths to the database.
  * **Image Painter with Fallback:** Overhauled the product cards in [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt) to use `rememberAsyncImagePainter` and standard Compose `Image` component. If the image is loading, missing, or fails to fetch (due to broken URLs, network issues, or storage restrictions), it guarantees the display of the premium branded Shopping Cart placeholder instead of a blank white block.
  * **Robust Gallery Upload & Error Handlers:** Added a local cache file copy pipeline in [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt) that converts Android `content://` gallery URIs into temporary cache files before uploading to Firebase Storage, preventing permission expiration crashes. Modified the save button logic so that any upload failures keep the dialog open and show a clear error warning instead of silently closing and saving blank database records.
  * **Auto Session Restoration:** Integrated Firebase Auth session restoration on startup in [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt) so the user stays logged in and their profile/orders are fetched immediately.
  * **Auth-First Sign In & Password Visibility Toggle:** Restructured login methods in [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt) to authenticate with Firebase Auth first before reading Firestore. Optimized the default admin (`admin@gstore.com` / `Ram@123`) check to perform immediate local login transitions, delegating Firestore profile creation and product database seeding to a non-blocking background coroutine. This completely eliminates the first-click permission race condition and ensures products are seeded immediately (resolving the "0 details" empty catalog issue). Added interactive eye icon visibility toggle buttons to all password input fields in [LoginScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/auth/LoginScreen.kt) (Customer Register, Customer Login, and Admin Login).
  * **Order Isolation:** Confirmed that customers only see their own orders based on their unique Firebase UID. If a newly registered user logs in, they will correctly see `"No orders placed yet"`.
  * **Rider Features Removal:** Completely removed the rider assignment and dialog logic from [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt). Clicking "Dispatch Order" in the admin panel now directly updates the order status to `OUT_FOR_DELIVERY` without showing a dialog. Removed the rider details display from [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt).
* **Files Modified:**
  * [AppState.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt)
  * [AdminScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/admin/AdminScreen.kt)
  * [CustomerScreen.kt](file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/customer/CustomerScreen.kt)
  * `storage.rules` (Created in workspace root)
