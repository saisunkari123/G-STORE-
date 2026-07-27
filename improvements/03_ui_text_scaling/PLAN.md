# 03 — UI Text Scaling & Card Size Fix

## Problem Statement
On client's phone, text appears **too big** and home page product cards appear **disproportionately large** compared to expected design.

---

## Root Cause Analysis

Android has a system-level **Font Scale** setting (under Accessibility → Text Size). When a user sets this to 125% or 150%, Compose/View `sp` (scale-independent pixels) values grow proportionally.

**Specific issues:**

1. **Text sizes use `sp` units** — `sp` respects the system font scale, so 44.sp on a device with 150% font scale becomes effectively 66sp. This is the correct Android behavior for accessibility, but if the app UI is not designed to handle it, text overflows and cards become oversized.

2. **Card sizes use fixed `dp` heights** — If product cards have a fixed height in `dp` but the text inside uses `sp`, text can overflow or force the card to expand, making it look huge.

3. **No `fontScale` clamping** — The app does not cap the font scale (e.g. at 1.0f or 1.2f), so system settings directly control all text sizes.

4. **Grid columns are fixed** — If cards use a fixed 2-column grid and don't adapt to text size, large text pushes cards to be very tall.

---

## Proposed Solution

### Option A — Clamp fontScale in App Theme (Recommended)
Override the `LocalDensity` composition in `MyApplicationTheme` to clamp the font scale to a maximum of `1.1f`:

```kotlin
// In MyApplicationTheme
val fontScale = minOf(LocalDensity.current.fontScale, 1.1f)
CompositionLocalProvider(
    LocalDensity provides Density(LocalDensity.current.density, fontScale)
) {
    MaterialTheme(...) { content() }
}
```

**This ensures:** Text never exceeds 110% of the designed size regardless of system accessibility settings.

### Option B — Use `sp` → `dp` conversion for critical UI elements
For splash screen, card titles, and header — convert key text sizes from `sp` to `dp` (which ignores font scale):

```kotlin
// Instead of: fontSize = 44.sp
// Use: fontSize = with(LocalDensity.current) { 44.dp.toSp() }
```

### Recommended: Option A (Clamp fontScale) + fix card min/max heights

---

## Files to Modify

| File | Change |
|------|--------|
| `ui/theme/MyApplicationTheme.kt` | Clamp fontScale to max 1.1f via LocalDensity override |
| `ui/customer/CustomerScreen.kt` | Set `minHeight` instead of fixed `height` for product cards |

---

## Manual Test Cases (You test on phone)

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Go to Settings → Accessibility → Font Size → set to Largest | Open G-STORE — text is readable but NOT excessively large |
| 2 | Home page product grid | Cards show 2 columns with normal proportions, images not distorted |
| 3 | Checkout page | All text fits within containers, nothing overflows |
| 4 | Admin page | All labels, buttons, and input fields display correctly |
| 5 | Go to Settings → Font Size → set back to Default | App looks exactly as designed |

---

## Automated Test Cases (Unit Tests)

| # | Test | File |
|---|------|------|
| 1 | `font scale is clamped to maximum 1.1f` | `ThemeTest.kt` |
| 2 | `product card height expands gracefully with long product names` | `ProductCardTest.kt` |

---

## Status
- [ ] Plan reviewed and approved
- [ ] Implementation
- [ ] Automated tests pass (CI)
- [ ] Manual testing — specifically test on client's device with large font setting
