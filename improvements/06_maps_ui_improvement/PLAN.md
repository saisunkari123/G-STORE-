# 06 — Maps UI Improvement

## Problem Statement
The map view:
1. Shows only the **village/locality name** — not the exact pinned location of the customer.
2. The map UI looks generic and plain.
3. Customer's precise GPS coordinates are saved in `Address.latitude` and `Address.longitude` but the map does not show a pin/marker at that location.

---

## Current State

- The app uses **Mapbox SDK** for maps.
- `Address` model stores `latitude` and `longitude`.
- The map currently centers on a general area but does not:
  - Show a distinct marker/pin at the customer's saved address location.
  - Show the shop location as a second marker.
  - Draw a route or distance line between shop and customer.

---

## Proposed Solution

### 1 — Show Customer Address Pin on Map

When admin views an order's delivery map:
- Place a **red destination pin** at `(Address.latitude, Address.longitude)`.
- Place a **green shop pin** at `(AppState.SHOP_LATITUDE, AppState.SHOP_LONGITUDE)`.
- Center and zoom the map to fit both pins.

### 2 — Show Exact Street/House Level Detail

Set Mapbox camera zoom level to **17–18** (street level) instead of the current zoom which shows village/town level.

```kotlin
// Current (shows village level — too zoomed out)
cameraPosition = CameraPosition.Builder().target(LatLng(lat, lon)).zoom(12.0).build()

// Fixed (shows exact house/street level)
cameraPosition = CameraPosition.Builder().target(LatLng(lat, lon)).zoom(17.0).build()
```

### 3 — Map UI Design Improvements

- Use Mapbox Streets style with better contrast.
- Add a floating card overlay at the bottom showing:
  - Customer name
  - Address text
  - Distance in km
  - Order ID
- Add a **"Open in Google Maps"** button that deep-links to Google Maps with the customer's coordinates.

---

## Files to Modify

| File | Change |
|------|--------|
| `ui/admin/AdminScreen.kt` | Update map camera zoom to 17; add customer pin marker; add shop pin marker; add bottom info card; add "Open in Google Maps" button |
| `ui/customer/CustomerScreen.kt` | If customer can view their address on map, show their pin at exact coordinates |

---

## Manual Test Cases (You test on phone)

| # | Scenario | Expected Result |
|---|----------|----------------|
| 1 | Admin opens an order and views map | Map shows both red pin (customer location) and green pin (shop) at street level zoom |
| 2 | Customer has saved address at specific coordinates | Map centers precisely on their house, not the village |
| 3 | Admin taps "Open in Google Maps" | Google Maps opens and navigates to customer's exact coordinates |
| 4 | Map loads for an address 8 km away | Both pins visible with clear distance shown |
| 5 | Order with no saved lat/long | Map shows a fallback error message "Location not available" |

---

## Automated Test Cases (Unit Tests)

| # | Test | File |
|---|------|------|
| 1 | `haversine distance between shop and customer coordinates is accurate` | `MapsUtilTest.kt` |
| 2 | `google maps deep link url is correctly formatted` | `MapsUtilTest.kt` |
| 3 | `camera zoom level is 17 for exact location view` | `MapsUtilTest.kt` |

---

## Status
- [ ] Plan reviewed and approved
- [ ] Implementation
- [ ] Automated tests pass (CI)
- [ ] Manual testing on physical device (test with real GPS address)
