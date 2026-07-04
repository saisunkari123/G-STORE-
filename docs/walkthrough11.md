# Rice Mart (G-STORE) — Developer Walkthrough Log 11

> **Author:** Senior Developer (AI Pair Programmer)
> **Date:** 2026-07-04
> **Purpose:** Codebase onboarding + ongoing bug tracking, fix logging, and feature history

---

## Session 11 — Initial Codebase Review & Onboarding

### What This Document Is
This is the living log for all bug fixes, feature additions, and architectural notes going forward. Every change will be appended here with:
- **Type** (BUG FIX / FEATURE / REFACTOR / HOTFIX)
- **File(s) Affected**
- **Root Cause / Motivation**
- **Change Made**
- **Status**

---

## Codebase Architecture Overview

### Tech Stack (As Implemented)

| Layer | Technology | Notes |
|-------|-----------|-------|
| UI | Kotlin + Jetpack Compose | Single-Activity, no Jetpack Nav |
| State | AppState singleton (Compose observable) | No ViewModel — all state in object |
| Backend | Firebase Firestore (real-time) | Replaced Room for primary storage |
| Auth | Firebase Auth (Email/Password) | Custom login screen with 2 roles |
| Image Upload | Cloudinary REST API | Firebase Storage bypassed (Spark plan limits) |
| Image Loading | Coil SubcomposeAsyncImage | Fixed deadlock from old painter pattern |
| Local DB | Room (present but unused in runtime) | DAOs and entities exist but not wired |

### Package Map

```
com.example/
├── data/
│   ├── local/         <- Room DB (entities, DAOs, converters) — DORMANT
│   ├── remote/        <- CloudinaryUploader.kt
│   └── repository/    <- FirebaseRepositories.kt (active)
├── domain/
│   ├── model/         <- Models.kt (User, Product, Order, Address, etc.)
│   └── repository/    <- Interfaces
├── ui/
│   ├── admin/         <- AdminScreen.kt (~775 lines)
│   ├── auth/          <- LoginScreen.kt (~319 lines)
│   ├── customer/      <- CustomerScreen.kt (~1701 lines)
│   ├── delivery/      <- EMPTY (role removed)
│   ├── state/         <- AppState.kt (~823 lines) — app brain
│   ├── switcher/      <- RoleSwitcherHeader.kt
│   └── theme/         <- Color.kt, Theme.kt, Type.kt
└── MainActivity.kt    <- Thin shell
```

### Key Architectural Facts
1. No ViewModel classes — AppState is a Kotlin object singleton using Compose mutableStateOf.
2. No Jetpack Navigation — routing via when(AppState.activeRole) and when(selectedTab).
3. Delivery Boy role is REMOVED — Admin handles full dispatch lifecycle.
4. Firebase Auth + Firestore drive full backend. Room exists but is NOT initialized at runtime.
5. Cloudinary is used for product image uploads.

---

## Role System

| Role | Login Method | Access |
|------|-------------|--------|
| CUSTOMER | Email + Password (Firebase Auth) | Home catalog, Cart, Orders, Account |
| ADMIN | Email + Password (admin@gstore.com / Ram@123) | Orders dashboard, Inventory |
| DELIVERY_BOY | Defined in model but UI removed | Not implemented |

---

## Data Flow

```
Login (Firebase Auth)
  -> AppState.currentUser set
  -> observeProducts() / observeAddresses() / observeOrders() triggered
  -> Firestore real-time listeners attached (callbackFlow)
  -> AppState.[productsList / addressesList / ordersList] updated
  -> Compose screens recompose automatically
```

---

## Known Issues & Technical Debt (Identified During Onboarding)

### ISSUE-001 — Admin "Dispatch Order" Skips ASSIGNED Status
- **Type:** BUG
- **Severity:** Medium
- **File:** AdminScreen.kt (line ~311)
- **Description:** The "Dispatch Order" button on PENDING orders directly sets status to OUT_FOR_DELIVERY,
  skipping ASSIGNED entirely. AppState.adminAssignRider() correctly uses ASSIGNED but is never called.
- **Impact:** ASSIGNED status is never used in the UI flow — lifecycle tracking is broken.
- **Status:** Identified — Fix pending

### ISSUE-002 — Cart Item "+" Icon Not Disabled at Max Stock
- **Type:** BUG
- **Severity:** Low-Medium
- **File:** CustomerScreen.kt (CartItemRow, line ~808)
- **Description:** The "+" icon in CartItemRow is never disabled even when qty >= maxStock. The home
  screen QtyController correctly disables the + button. Cart view does not.
- **Status:** Identified — Fix pending

### ISSUE-003 — Dark Mode Toggle Not Wired to Theme
- **Type:** BUG / Incomplete Feature
- **Severity:** Low
- **File:** CustomerScreen.kt + Theme.kt
- **Description:** AppState.isDarkMode is toggled in Account screen but MyApplicationTheme does not
  read this value to switch between light/dark color schemes. Toggle has no effect.
- **Status:** Identified — Fix pending

### ISSUE-004 — Observers Launched with Wrong Dispatcher
- **Type:** PERFORMANCE / Correctness
- **Severity:** Medium
- **File:** AppState.kt (lines ~475, ~490, ~505)
- **Description:** observeProducts(), observeAddresses(), observeOrders() all use
  ioScope.launch(Dispatchers.Main). This overrides the IO dispatcher, making the ioScope irrelevant.
  Semantically incorrect but works in practice because Firestore callbacks fire on main thread anyway.
- **Status:** Identified — Cleanup fix pending

### ISSUE-005 — Stock Deduction Uses String Matching (Fragile)
- **Type:** BUG / Reliability
- **Severity:** Medium
- **File:** AppState.kt (placeOrder, line ~720)
- **Description:** Stock deduction matches orderedQty by comparing selectedSize strings
  ("25 Kg") against "${variant.weight} ${variant.unit}". Any formatting difference silently
  causes stock to never be decremented.
- **Status:** Identified — Fix pending

### ISSUE-006 — Order ID Collision Risk
- **Type:** BUG
- **Severity:** Low (dev), High (prod)
- **File:** AppState.kt (placeOrder, line ~701)
- **Description:** Order IDs generated as "G-${(10000..99999).random()}". With only 90,000
  possibilities and no uniqueness check, Firestore will silently overwrite collisions.
- **Status:** Identified — Fix pending

### ISSUE-007 — DELIVERY_BOY Role Not Handled in MainActivity
- **Type:** BUG
- **Severity:** Low
- **File:** MainActivity.kt (line ~33)
- **Description:** when(AppState.activeRole) only handles "CUSTOMER" and "ADMIN". A user with
  role="DELIVERY_BOY" in Firestore will see a blank screen after login.
- **Status:** Identified — Needs guard

---

## Historical Fixes (Pre-Session-11)

| ID | Fix | Files |
|----|-----|-------|
| H-1 | Firestore PERMISSION_DENIED crash — products empty on home screen | AppState.kt, Firestore Rules |
| H-2 | Firebase Storage replaced with Cloudinary upload | CloudinaryUploader.kt, AdminScreen.kt |
| H-3 | Coil image deadlock — replaced rememberAsyncImagePainter with SubcomposeAsyncImage | CustomerScreen.kt |
| H-4 | Empty variants crash on customer product card | CustomerScreen.kt |
| H-5 | Password visibility toggles added to all auth fields | LoginScreen.kt |
| H-6 | Firestore field mapping (isAvailable/isEnabled/isSelected) via @PropertyName | Models.kt |
| H-7 | Telugu translation fields added to Product model | Models.kt, ProductEntity.kt |
| H-8 | Admin auto-seeds products on first login | AppState.kt |
| H-9 | Delivery Boy role removed from UI; Admin handles full dispatch | AdminScreen.kt |

---

## Change Log

> All code changes made going forward are logged below.

### [2026-07-04] Session 11 Start — Codebase Review Complete
- **Action:** Full codebase read (all Kotlin files, docs, build config)
- **Findings:** 7 issues identified (ISSUE-001 through ISSUE-007 above)
- **Next:** Awaiting developer direction on priority tasks

---

## Quick Reference

### Firebase Collections
| Collection | Purpose |
|-----------|---------|
| products | Product catalog (public read allowed) |
| orders | All orders |
| addresses | Delivery addresses per user |
| users | User profiles with roles |
| categories | Category data (c_rice) |

### Key Config Values
| Key | Value |
|-----|-------|
| Admin email | admin@gstore.com |
| Admin password | Ram@123 |
| Delivery fee | Rs.40 if subtotal < Rs.1000, else FREE |
| Store hours | 8 AM to 8 PM (bypassed: forceStoreOpen = true) |
| Cloudinary cloud | k1lw675z |
| App ID | com.aistudio.ricemart.pkqmsx |
| Min SDK | 24 |
| Target SDK | 36 |

### Business Rules (As Coded — Source of Truth)
- No minimum order value enforced
- Free delivery for orders >= Rs.1000; Rs.40 otherwise
- Out-of-stock products visible but Add to Cart is disabled
- 5 KM delivery radius is defined but NOT enforced in code (distance saved but no block)
- Store hours enforced at checkout unless forceStoreOpen = true

---

### [2026-07-04] Bug Fix Batch — ISSUE-001 through ISSUE-007

#### ISSUE-001 — Admin "Dispatch Order" Skips ASSIGNED Status
- **Decision:** CLOSED — Correct as-is. This is a local business with no delivery riders.
  Admin directly dispatches orders (PENDING -> OUT_FOR_DELIVERY -> DELIVERED). ASSIGNED status
  is intentionally unused.
- **Action:** No code change.

#### ISSUE-002 — Cart Item "+" Not Disabled at Max Stock [FIXED]
- **File:** CustomerScreen.kt (CartItemRow)
- **Fix:** Added `val atMaxStock = quantity >= variant.stockQuantity` check. The "+" icon now
  uses `.then(Modifier.clickable { ... })` only when not at max, and turns LightGray when at max.
  This matches the behavior of the QtyController on the home screen.
- **Status:** FIXED

#### ISSUE-003 — Dark Mode Toggle Not Wired to Theme
- **Decision:** CLOSED — False alarm. Theme.kt line 40 already reads `AppState.isDarkMode`
  to select DarkColorScheme vs LightColorScheme. The toggle works correctly.
- **Action:** No code change.

#### ISSUE-004 — Observers Launched with Wrong Dispatcher [FIXED]
- **File:** AppState.kt (observeProducts, observeAddresses, observeOrders)
- **Fix:** Removed the `Dispatchers.Main` override from `ioScope.launch(...)`. All three
  observers now run their Flow collection on IO. State updates inside `collect { }` are
  wrapped in `withContext(Dispatchers.Main) { }` to safely update Compose state.
- **Status:** FIXED

#### ISSUE-005 — Stock Deduction Uses Fragile String Matching [FIXED]
- **File:** AppState.kt (placeOrder)
- **Fix:** Replaced the string-based `selectedSize == "${variant.weight} ${variant.unit}"`
  matching with a `cartSnapshot` map lookup using the cart key format
  `"${prod.id}#${variant.id}"`. Stock deduction now uses the exact same variantId-based
  key that was used when adding to cart — completely format-independent.
- **Status:** FIXED

#### ISSUE-006 — Order ID Collision Risk [FIXED]
- **File:** AppState.kt (placeOrder)
- **Fix:** Changed from `"G-${(10000..99999).random()}"` (90k possibilities, collision-prone)
  to `"G-${System.currentTimeMillis()}-${(100..999).random()}"`. The timestamp component
  ensures IDs are practically unique even under concurrent orders.
  Example output: G-1751617240123-847
- **Status:** FIXED

#### ISSUE-007 — DELIVERY_BOY Role Shows Blank Screen [FIXED]
- **File:** MainActivity.kt
- **Fix:** Added `else -> CustomerScreen()` to the `when(AppState.activeRole)` block.
  Since this is a local business with no delivery riders, any unknown role (including
  DELIVERY_BOY) safely falls back to the Customer view instead of a blank screen.
- **Status:** FIXED

---

### Summary: 5 Bugs Fixed, 2 Closed (Correct Behavior / False Alarm)
| ID | Status |
|----|--------|
| ISSUE-001 | Closed — Correct behavior (local business, no riders) |
| ISSUE-002 | Fixed — CustomerScreen.kt |
| ISSUE-003 | Closed — Already wired correctly in Theme.kt |
| ISSUE-004 | Fixed — AppState.kt (all 3 observers) |
| ISSUE-005 | Fixed — AppState.kt (placeOrder) |
| ISSUE-006 | Fixed — AppState.kt (placeOrder) |
| ISSUE-007 | Fixed — MainActivity.kt |

---

### [2026-07-04] Firebase Bug Fixes — BUG-F1, BUG-F2, BUG-F3

#### BUG-F1 — Non-Default Admin saveUser() Called on Main Thread [FIXED]
- **File:** AppState.kt (~L371)
- **Root Cause:** In the non-default-admin login path, `userRepository.saveUser()` (a
  suspending Firestore call) was inside `withContext(Dispatchers.Main)`, blocking the UI thread.
- **Fix:** Restructured the `if/else` block: `saveUser()` now runs on IO (stays in ioScope.launch),
  then `withContext(Dispatchers.Main)` only for Compose state updates.

#### BUG-F2 — Triple Firestore Products Listener on Session Restore [FIXED]
- **File:** AppState.kt (currentUser setter, ~L40)
- **Root Cause:** `observeProducts()` called by (1) initializeDatabase(), (2) currentUser setter on
  login, (3) currentUser setter again on session restore. Caused brief empty product flash.
- **Fix:** Added `if (productsJob == null || !productsJob!!.isActive)` guard in the setter.
  Only starts a new listener if one isn't already running.

#### BUG-F3 — Products Firestore Listener Leaked After Logout [FIXED]
- **File:** AppState.kt (currentUser setter else-branch, ~L46)
- **Root Cause:** `currentUser = null` (on logout) cancelled addressesJob + ordersJob but NOT
  productsJob. The products listener kept running after logout, wasting battery and Firestore reads.
- **Fix:** Added `productsJob?.cancel()` and `productsList = emptyList()` to the null-user
  (logout) path in the currentUser setter.

---

### [2026-07-04] UI Review + Brand Products Setup

#### UI Consistency Issues Found & Fixed

| Issue | Fix | File |
|-------|-----|------|
| Admin Login button was `Color.Black` — inconsistent with all other primary buttons | Changed to `RoyalEmerald` | LoginScreen.kt |
| Category filter chips had hardcoded grain types (Basmati, Sona Masoori) not matching real stock | Replaced with uncle's 4 actual brands | CustomerScreen.kt |
| Filter logic matched by nameEn.contains() — wrong for brand-based filter | Changed to `brand.equals(selectedCategory)` exact match | CustomerScreen.kt |
| Search only matched product name — didn't find by brand name | Added `|| it.brand.contains(searchQuery)` | CustomerScreen.kt |

#### Uncle's Brand Products Added as Seed Data [NEW]
- **Brands added:** Akshaya, Sameera, Bell Brand, Lalitha Brand
- **Variants per brand:** 5 Kg, 10 Kg, 26 Kg (matching real bag sizes)
- **Unit:** Kg (all variants)
- **Prices:** Set to 0.0 placeholder — Admin must update via Inventory screen
- **Images:** Empty list — Admin uploads real bag photos via Inventory screen
- **File:** AppState.kt (initialProducts)
- **Note:** These seed only on first-ever Admin login when Firestore products collection is empty.
  If products already exist in Firestore, delete the old ones from Firebase Console first,
  then re-login as Admin to trigger re-seeding.

#### UI Review Summary (No Further Issues Found)
- LoginScreen: Consistent field colors, shapes, spacing ✅
- CustomerScreen navigation bar: 4 tabs consistent ✅
- Cart view: Consistent card style, price colors ✅
- Orders view: Status badges using correct brand colors ✅
- Admin dashboard: Stat cards, order cards consistent ✅
- Color palette: RoyalEmerald + DeepGold used consistently everywhere ✅
