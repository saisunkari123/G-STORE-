# 01 — Real-Time Sync: Gifts & Orders

## Problem Statement

**Issue 1 — Gift Items:**
Admin adds new gift items on one device. Customer opens checkout page on another device — gift items **don't appear even after several minutes**.

**Issue 2 — Orders (Live Admin Panel):**
- When admin is logged in and a user places an order, the admin panel does NOT show it live.
- Orders only appear when the admin logs out and logs back in.
- There is also noticeable latency (~minutes) before orders refresh.

---

## Root Cause Analysis

### Gift Items Not Syncing

1. `AwsProductRepositoryImpl` runs `startPeriodicCloudSync()` every 5 seconds — but this only fetches **products**, not gift configs.
2. Gift configs are stored in a product record with id `sys_gifts` inside `nameEn` field as JSON.
3. `fetchProductsFromCloud()` fetches the product list, and gift configs ARE buried inside it — but `fetchGiftConfigsFromCloud()` **does not exist as a standalone method**.
4. When `fetchProductsFromCloud()` retrieves the product list, it filters out `sys_gifts` (system metadata products) and does NOT parse/update `giftConfigsState`.
5. **Result:** `giftConfigsState` only ever contains what was loaded at startup from the local JSON file (`aws_gifts.json`). New gift items added remotely never reach the customer's device.

### Orders Not Syncing Live

1. `AwsOrderRepositoryImpl` stores orders in a `MutableStateFlow<List<Order>>` backed by a local JSON file (`aws_orders.json`).
2. Orders ARE synced to AWS AppSync on placement, but the `ordersState` flow on the **admin's device** is never updated from the cloud — it only reflects the local file.
3. `observeOrders()` in AppState listens to `ordersState.flow` — but this flow NEVER receives remote updates because there is **no periodic cloud poll for orders**.
4. When admin logs out and back in, `initializeDatabase()` re-reads `aws_orders.json` from disk — which by then may contain older data. The only way to see fresh orders is when the `restoreSession()` flow re-triggers `observeOrders()`.
5. **Result:** Admin sees orders from local cache only. New orders placed by other customers don't appear until admin restarts.

---

## Proposed Solution

### Gift Configs — Add periodic cloud sync

**File:** `app/src/main/java/com/example/data/repository/AwsRepositories.kt`

**Change:**
- Inside `startPeriodicCloudSync()`, add a call to `fetchGiftConfigsFromCloud()` every 10 seconds.
- `fetchGiftConfigsFromCloud()` queries AppSync for the `sys_gifts` product, parses `nameEn` as JSON into `List<GiftItemConfig>`, and emits into `giftConfigsState`.
- Also call `fetchGiftConfigsFromCloud()` immediately on init.

### Orders — Add periodic cloud sync for orders

**File:** `app/src/main/java/com/example/data/repository/AwsRepositories.kt`

**Change:**
- Add `startPeriodicOrderSync()` inside `AwsOrderRepositoryImpl.init {}` — polls AppSync `listOrders` every 10 seconds.
- Merges remote orders with local orders (deduplicate by id) and emits updated list into `ordersState`.
- On admin device: all customer orders appear within 10 seconds of being placed.
- On customer device: their own order status updates appear within 10 seconds.

---

## Files to Modify

| File | Change |
|------|--------|
| `data/repository/AwsRepositories.kt` | Add `fetchGiftConfigsFromCloud()` called every 10s in `startPeriodicCloudSync()`. Add `startPeriodicOrderSync()` in `AwsOrderRepositoryImpl`. |

---

## Manual Test Cases (You test on phone)

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Admin adds a new gift item on Admin phone | Within 10 seconds, customer phone's checkout page shows the new gift item |
| 2 | Customer places an order | Within 10 seconds, admin phone's order list shows the new order without needing to logout |
| 3 | Admin changes order status to "Out for Delivery" | Within 10 seconds, customer's order page shows updated status |
| 4 | Admin adds gift item, customer is on another page, customer navigates to checkout | Gift item visible immediately |
| 5 | App is in background (minimized), customer places order | Admin brings app to foreground, within 10s order appears |

---

## Automated Test Cases (Unit Tests)

| # | Test | File |
|---|------|------|
| 1 | `gift configs parse correctly from sys_gifts product nameEn json` | `GiftConfigSyncTest.kt` |
| 2 | `orders list merges remote and local without duplicates` | `OrderSyncTest.kt` |
| 3 | `periodic sync interval is exactly 10 seconds` | `OrderSyncTest.kt` |
| 4 | `order status update reflects in ordersState flow immediately` | `OrderSyncTest.kt` |

---

## Status
- [ ] Plan reviewed and approved
- [ ] Implementation
- [ ] Automated tests pass (CI)
- [ ] Manual testing on physical devices
