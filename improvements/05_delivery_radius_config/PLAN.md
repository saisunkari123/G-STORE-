# 05 — Admin-Configurable Delivery Radius

## Problem Statement
The delivery radius is currently hardcoded at **10 km** in `AppState.kt`:

```kotlin
const val MAX_DELIVERY_DISTANCE_KM = 10.0
```

Admin needs to be able to change this value from the app without code changes.

---

## Current State
- `MAX_DELIVERY_DISTANCE_KM = 10.0` is a compile-time constant in `AppState.kt`.
- `placeOrder()` checks `selectedAddr.distanceKm > MAX_DELIVERY_DISTANCE_KM` and blocks the order.
- The delivery fee calculation also uses this value.
- There is no way for the admin to change this at runtime.

---

## Proposed Solution

This feature is **shared with Feature 04 (Minimum Order Amount)** — both are stored in `sys_config` in AppSync:

```json
{
  "minimumOrderAmount": 150.0,
  "deliveryRadiusKm": 10.0
}
```

### AppState Layer

- Replace `const val MAX_DELIVERY_DISTANCE_KM = 10.0` with `var deliveryRadiusKm by mutableStateOf(10.0)`.
- Load `deliveryRadiusKm` from `AppConfig` on startup and every 10s sync.
- `placeOrder()` uses `deliveryRadiusKm` instead of the constant.
- Address validation `getDeliveryFee()` uses `deliveryRadiusKm`.

### Admin UI

- In the **Store Settings** section (added in Feature 04):
  - **Delivery Radius (km):** editable number field with Save button.
  - On save, updates `AppConfig` in AppSync cloud.
  - Admin can set values like 5, 10, 15, 20 km.

### Customer UI

- On address selection/checkout page, show:
  - `"We deliver within X km of the shop"` — where X comes from `deliveryRadiusKm`.
  - If selected address is outside radius, show clear error: `"Sorry, your location (Y km) is outside our delivery area (X km)."`.

---

## Files to Modify

| File | Change |
|------|--------|
| `ui/state/AppState.kt` | Replace `const val MAX_DELIVERY_DISTANCE_KM` with `var deliveryRadiusKm`; use it in order validation |
| `ui/admin/AdminScreen.kt` | Add delivery radius field in Store Settings section (shared with Feature 04) |
| `ui/customer/CustomerScreen.kt` | Show dynamic delivery radius info on checkout page |
| `data/repository/AwsRepositories.kt` | `saveAppConfig()` includes deliveryRadiusKm (shared with Feature 04) |

---

## Manual Test Cases (You test on phone)

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Admin sets delivery radius to 5 km | Within 10s, customer app shows "We deliver within 5 km" |
| 2 | Customer at 8 km tries to place order when radius is 5 km | Order blocked: "Your location (8 km) is outside our delivery area (5 km)" |
| 3 | Admin increases radius to 15 km | Same customer at 8 km can now place order |
| 4 | Admin sets radius to 0 | No deliveries allowed (edge case — UI should warn admin) |
| 5 | App restarts | Delivery radius persists from cloud config |

---

## Automated Test Cases (Unit Tests)

| # | Test | File |
|---|------|------|
| 1 | `address within delivery radius is accepted` | `DeliveryRadiusTest.kt` |
| 2 | `address outside delivery radius is rejected` | `DeliveryRadiusTest.kt` |
| 3 | `delivery radius update propagates to order validation` | `DeliveryRadiusTest.kt` |
| 4 | `app config defaults to 10km when sys_config not found` | `AppConfigTest.kt` |

---

## Status
- [ ] Plan reviewed and approved
- [ ] Implementation (shared with Feature 04 — implement together)
- [ ] Automated tests pass (CI)
- [ ] Manual testing on physical devices
