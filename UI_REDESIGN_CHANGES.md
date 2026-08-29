# G-Store Quick-Commerce UI Redesign Documentation

## Overview
The customer shopping interface of G-Store has been transformed into a modern quick-commerce UI inspired by **Blinkit, Swiggy Instamart, Zepto, and Flipkart Quick**.

---

## 🔒 Scope & Boundary Protection
- **Untouched Components**:
  - Admin Panel (`AdminScreen.kt`)
  - Login & Authentication Screens (`LoginScreen.kt`)
  - Registration Screen
  - AWS AppSync / DynamoDB backend models and security logic
- **Redesigned Customer Components**:
  - Customer Home & Catalog View
  - Product Cards
  - Customer Cart View
  - Product Details Bottom Sheet View
  - Order Placed Success Confirmation

---

## 🎨 UI Redesign Details

### 1. Clutter-Free Grid Cards (`CustomerProductCard`)
- **Clean 2-Element Text Layout**:
  - Only displays **Product Name** (2 lines max) and **Pack Size / Weight** (e.g. `500 ml` or `26 kg`).
  - **Removed Text Clutter**: Removed multi-line descriptions and watermark brand badges from the small card to keep the grid clean, modern, and easily scannable.
- **Fixed Uniform Image Canvas (115dp)**:
  - Every card features a fixed 115dp image container with `ContentScale.Fit` and 6dp padding inside `#F8FAF9`.
  - Ensures milk packets, rice bags, and bottles sit in a consistent, uniform canvas with **zero image distortion or uneven card heights**.
- **Interactive Multi-Size Pill**:
  - If a product has multiple sizes, a clean `X Sizes ▾` chip allows switching variants directly or viewing pack options.
- **Instant `+ ADD` $\rightarrow$ `- 1 +` Stepper**:
  - Crisp emerald border that transforms dynamically into an emerald stepper pill when added.

---

### 2. Rich Product Details Bottom Sheet (`ProductDetailBottomSheet`)
- **Full View on Tap**:
  - Tapping any product image or title on the **Home Grid** or in the **Cart** opens a full quick-commerce bottom sheet.
- **Rich Information Displayed**:
  - Large 200dp high-res image hero with `ContentScale.Fit` and discount badge.
  - English and Telugu Product Names (e.g. *లలిత హెచ్.ఎమ్.టి రైస్*).
  - Brand Tag (e.g. `Brand: AMUL`, `Brand: HERITAGE`).
  - Horizontal pack size selector cards showing weight, selling price, MRP, and savings.
  - "About Product" section with full English & Telugu descriptions.
  - Sticky bottom action bar with live price, strikethrough MRP, and a large **"ADD TO CART"** button / **`- qty +` stepper**.

---

### 3. Customer Cart Screen (`CustomerCartView` & `CartItemRow`)
- **Clickable Cart Thumbnails**:
  - Tapping the 64dp product thumbnail or name in the cart immediately opens the `ProductDetailBottomSheet`.
- **Compact Cart Item Cards**:
  - 64dp thumbnail, bold title, size tag, bold emerald price, and compact `- 1 +` stepper pill.
- **Address Selection Card**:
  - Clear pin icon with house number and landmark, with one-tap "Change" action.
- **Clear Bill Details**:
  - Subtotal, gift packaging (if selected), and Grand Total breakdown.
- **Sticky Slide/Tap to Pay Bottom Bar**:
  - Store open/closed validation (8 AM - 8 PM), minimum order amount alert, and one-tap checkout.

---

### 4. Home Products Catalog (`CustomerCatalogView`)
- **2-Column Responsive Grid**:
  - `LazyVerticalGrid(columns = GridCells.Fixed(2))` fitting **4 to 6 products simultaneously**.
- **Horizontal Category Carousel Chips**:
  - `All Products`, `Rice Bags`, `Cooking Oils`, `Dairy Essentials`, `Dals & Pulses`, `Spices & Masalas`.
- **Floating Quick-Cart Bottom Bar (`FloatingQuickCartBar`)**:
  - Automatically pops up whenever `cartItems.isNotEmpty()` with `X ITEMS • ₹TOTAL` and `View Cart ➔`.

---

## 📱 APK Build Artifact
- **APK Path**: `app/build/outputs/apk/debug/app-debug.apk`
- **Build Status**: Verified with `./gradlew assembleDebug` (Exit Code 0).
