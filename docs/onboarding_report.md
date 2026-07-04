# Rice Mart Android App Onboarding Report

## 1. Project Architecture and File Structure Overview
The application follows an Android MVVM-like **Clean Architecture** concept, visually divided into standard layers (Data, Domain, UI), though current implementation relies heavily on a global singleton pattern (`AppState.kt`) rather than formal ViewModels and Dependency Injection.

*   **Frontend**: Jetpack Compose is strictly used for the UI (`com.example.ui.*`).
*   **Domain**: Defines raw model schemas (`User`, `Product`, `Order`, `Address`) in `Models.kt` and Repository Interfaces.
*   **Data**: The app originally used Room DB but has transitioned to Firebase Firestore for its backend storage, implementing the repository interfaces inside `FirebaseRepositories.kt`.
*   **Global State Container**: `AppState.kt` is a global Kotlin `object` maintaining mutable states (e.g., `cartItems`, `productsList`, `currentUser`). While convenient for rapid prototyping, this introduces severe scaling, memory, and race-condition constraints.

## 2. Codebase Review: Bugs, Leaks, and Race Conditions Found

Based on a deep code inspection of `AppState.kt`, `CustomerScreen.kt`, and `AdminScreen.kt`, I've identified several critical issues that could impact stability in production.

### A. Hidden Bugs & Race Conditions
*   **Checkout Stock Deductions (Race Condition):** Inside `AppState.placeOrder()`, stock checks and deductions are completely performed on the client-side without using Firestore **Transactions**. If two users simultaneously buy the last bag of rice, the database will save both orders, but the final stock value will blindly overwrite, leading to ghost inventories or negative quantities.
*   **Dialog Configuration Change Crash:** Inside `AdminScreen.kt`, `isUploading` and `uploadError` are stored in a standard Compose `remember { mutableStateOf(false) }` block. If an Admin rotates the screen during an image upload, the dialog will be recreated, the upload might continue in the background, but the UI state is lost, leaving the user trapped or unaware of completion.

### B. Memory & State Leaks
*   **Global Coroutine Scope Misuse:** `AppState` uses a global `ioScope = CoroutineScope(...)` that runs forever. Dialog actions in Compose (e.g. `saveProductWithImage`) launch their callbacks using a `rememberCoroutineScope()`. Mixing UI-bound scopes with global data-bound scopes can lead to un-cancellable ghost processes when users leave the screen.
*   **State Accumulation on Logout:** `lastPlacedOrder` and checkout success states are not explicitly nulled out on logout. If a new user logs in on the same device, they might see ghost remnants of the previous user's order success states depending on lifecycle transitions.

## 3. Priority Backlog & Recommendations

To elevate this codebase to professional enterprise standards, I suggest the following backlog prioritized from High to Low.

### Priority 1: Critical Stability & Security
*   **Implement Firestore Transactions:** Overhaul `AppState.placeOrder()` to use `FirebaseFirestore.getInstance().runTransaction()`. The server must definitively read stock levels, verify they are `> qty`, deduct, and write the order in one atomic step.
*   **Introduce Dependency Injection (Hilt):** Refactor the god-object `AppState` into specific, Lifecycle-aware ViewModels (e.g., `AuthViewModel`, `CartViewModel`, `CatalogViewModel`) injected via Hilt. This solves global memory leaks and scoped coroutine cancellations.
*   **Lock Down Firestore Security Rules:** Ensure `storage.rules` and `firestore.rules` are aggressively locked to prevent malicious direct API writes to stock lists and product prices (ensuring only `role == 'ADMIN'` can write to the `products` collection).

### Priority 2: UI/UX Robustness
*   **Implement `rememberSaveable`:** Swap `remember` for `rememberSaveable` in Compose forms (like the Admin product upload dialog) to survive Android configuration changes and process deaths.
*   **Coil Image Caching Profiles:** Introduce advanced caching headers/policies in Coil `AsyncImage` across the catalog to drastically reduce Cloudinary CDN hits and improve offline perceived performance.

### Priority 3: Technical Debt Clean Up
*   **Remove Unused Legacy Code:** Clean up commented-out camera and location dependencies in `build.gradle.kts`.
*   **Clean Architecture Separation:** Migrate the hardcoded default seed products (e.g. Akshaya Rice) out of the UI state file and into a proper Mock/Seed repository in the Data layer.

---
*Prepared by Jules, Senior Android Engineer*
