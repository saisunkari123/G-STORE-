# Conversation Summary: Map UI, Admin Features, and AWS Sync Fixes

## Overview
This document summarizes the features, UI adjustments, bug fixes, and architectural improvements implemented during this conversation for G-STORE Rice-Mart. 

## Features & Improvements

### 1. Map Picker & Layout Engine
* **Floating Map Dialog Layout:** Converted the map dialog (`AWSMapPickerDialog`) into a true full-screen floating `Box` layout.
* **Map Search Bar:** Implemented a new search bar at the top of the map allowing users to search via place name (using `Amplify.Geo.searchByText`) or paste raw coordinates directly.
* **My Location FAB:** Added a floating action button (crosshair icon) that fetches the device's current GPS location and automatically animates the map camera to it.
* **Raw Coordinate Copying:** Adjusted the copy-to-clipboard functionality to copy purely raw `latitude,longitude` values (e.g., `18.448200,83.661600`) so they can be easily pasted into Google Maps or other external applications by admins and delivery drivers.
* **Streamlined Address Selection:** Removed the manual text input fields ("Flat/House No" and "Landmark"). The app now automatically saves the geocoded address upon map confirmation and closes the dialog seamlessly.

### 2. Admin Dashboard Enhancements
* **Listed Switch Confirmation:** Added a safety confirmation dialog prompt ("Are you sure you want to hide this product?") when an admin attempts to toggle a product's visibility off.
* **Hidden Product Banner:** Restructured the "Hidden from customers" badge on the `AdminScreen` to render as a sleek full-width banner at the bottom of the product card, preventing awkward text wrapping on smaller screens.
* **Space-Insensitive Search:** Updated search logic across the Customer Catalog, Admin Inventory, and Admin Live Orders. The search now strips all whitespaces and ignores casing, allowing for robust query matching (e.g., searching "sona masuri" as "sonamasuri" or "Sona  Masuri" works instantly).

## Bug Fixes & Problem Solving

### 1. The "Bottom Padding / Cut Off" Issue in Maps
* **Issue:** On multiple devices (especially those using gesture pill navigation), the "Confirm Location" button and coordinates display at the bottom of the map were partially or fully cut off and hidden.
* **How it was fixed:** 
  * Jetpack Compose's `Dialog` behaves as a separate window and defaults to Android platform padding. To bypass this, we used a runtime `SideEffect` to set `window.setBackgroundDrawableResource(android.R.color.transparent)`.
  * We shifted the bottom panel upwards significantly by applying `.navigationBarsPadding()` combined with an additional `64.dp` bottom padding.
  * We also shifted the My Location FAB to `270.dp` bottom padding to ensure it floats perfectly above the raised bottom panel.
  
### 2. Hiding Overlapping Amplify Map Controls
* **Issue:** `AmplifyMapView` contains its own internal search bar and map controls that overlapped with our custom UI and couldn't be disabled via public APIs.
* **How it was fixed:** We used Kotlin Reflection at runtime to fetch the private views (`getSearchField` and `getControls`) from the `AmplifyMapView` instance and forced their visibility to `View.GONE`.

### 3. "Dummy Ricebags" & Fast Syncing Issues
* **Issue:** When a new user created an account, they saw "default ricebags" instead of live products. Furthermore, when an admin added or hid a product, the customer screens did not update reliably, leading to broken data sync.
* **How it was fixed:**
  * **Removed Dummy Data Injection:** We discovered that the `AwsRepositories` was falling back to a hardcoded `initialProducts` list if the local cache was empty. We replaced this fallback with an `emptyList()`. We also removed a bug where admin logins were explicitly saving these dummy products over the local cache.
  * **Pure Sync Model:** The cloud data merging logic was keeping deleted/hidden items locally permanently. We rewrote the `fetchProductsFromCloud` method to completely replace the local state with the AWS AppSync data.
  * **5-Second Automatic Sync:** We reduced the background polling interval from 30 seconds to 5 seconds, ensuring customers see live updates almost instantly.
  * **Instant Mutation Refresh:** We hooked the admin `saveProduct` and `deleteProduct` mutations to trigger an immediate `forceRefreshFromCloud()` the moment they succeed, keeping the admin UI in perfect lockstep with AWS.
