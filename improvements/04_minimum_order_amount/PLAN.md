# 04 — Minimum Order Amount (Admin Configurable)

## Problem Statement
There is no minimum order amount enforced. Customer can place an order for ₹5. The admin needs to be able to set a minimum cart total required before checkout is allowed, and customers should see this limit clearly.

---

## Current State
- No minimum order amount exists anywhere in the codebase.
- `AppState.kt` has `MAX_DELIVERY_DISTANCE_KM = 10.0` (hardcoded) but no `MIN_ORDER_AMOUNT`.
- Checkout (`placeOrder()`) only checks delivery distance — does not check cart total.

---

## Proposed Solution

### Data Layer — Store Minimum Order Amount in AppSync (as System Config)

Similar to how gift configs are stored in a `sys_gifts` product, we store app-wide config in a new `sys_config` product with JSON in `nameEn`:

```json
{
  "minimumOrderAmount": 150.0,
  "deliveryRadiusKm": 10.0
}
```

**AppConfig model:**
```kotlin
data class AppConfig(
    val minimumOrderAmount: Double = 150.0,
    val deliveryRadiusKm: Double = 10.0
)
```

### AppState Layer

- Add `var minimumOrderAmount by mutableStateOf(150.0)` to AppState.
- Add `var deliveryRadiusKm by mutableStateOf(10.0)` to AppState (also used by Feature 05).
- On startup and every 10s sync, load `AppConfig` from `sys_config` product.
- `placeOrder()` validates: if `cartSubtotal < minimumOrderAmount` → return error message `"Minimum order amount is ₹X. Your cart total is ₹Y."`.

### Customer UI — Show minimum order amount

- In cart/checkout page, show a notice: **"Minimum order: ₹150"**
- If cart total is below minimum, the **Place Order** button shows as disabled with a tooltip `"Add ₹X more to place order"`.

### Admin UI — Settings panel to change minimum order amount

- In Admin panel, add a new **"Store Settings"** section/card.
- Two fields:
  - **Minimum Order Amount (₹):** editable number field, Save button
  - **Delivery Radius (km):** editable number field, Save button (shared with Feature 05)
- On save, updates `sys_config` in AppSync cloud and locally.

---

## Files to Modify / Create

| File | Change |
|------|--------|
| `domain/model/Models.kt` | Add `data class AppConfig` |
| `data/repository/AwsRepositories.kt` | Add `fetchAppConfigFromCloud()`, `saveAppConfig()` to `AwsProductRepositoryImpl` |
| `domain/repository/ProductRepository.kt` | Add `getAppConfig(): Flow<AppConfig>`, `saveAppConfig(config: AppConfig)` |
| `ui/state/AppState.kt` | Add `minimumOrderAmount`, `deliveryRadiusKm` state; load from cloud; validate in `placeOrder()` |
| `ui/customer/CustomerScreen.kt` | Show minimum order notice in cart; disable Place Order if below minimum |
| `ui/admin/AdminScreen.kt` | Add Store Settings section with editable minimum order amount |

---

## Manual Test Cases (You test on phone)

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Admin sets minimum order amount to ₹200 | Saved to cloud; within 10s customer app shows ₹200 minimum |
| 2 | Customer adds items totaling ₹150 (below ₹200 minimum) | Place Order button is greyed out; notice says "Add ₹50 more to place order" |
| 3 | Customer adds more items until total reaches ₹200 | Place Order button becomes active |
| 4 | Admin sets minimum order amount to ₹0 | All order totals are accepted, no minimum enforced |
| 5 | App is restarted | Minimum order amount persists correctly from cloud |

---

## Automated Test Cases (Unit Tests)

| # | Test | File |
|---|------|------|
| 1 | `order below minimum amount is rejected with correct message` | `MinimumOrderTest.kt` |
| 2 | `order at exactly minimum amount is accepted` | `MinimumOrderTest.kt` |
| 3 | `order above minimum amount is accepted` | `MinimumOrderTest.kt` |
| 4 | `app config parses correctly from sys_config json` | `AppConfigTest.kt` |
| 5 | `minimum order amount defaults to 150 when sys_config not found` | `AppConfigTest.kt` |

---

## Status
- [ ] Plan reviewed and approved
- [ ] Implementation
- [ ] Automated tests pass (CI)
- [ ] Manual testing on physical devices
