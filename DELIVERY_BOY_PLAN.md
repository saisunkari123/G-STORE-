# 🛵 G-Store Delivery Partner (Delivery Boy) Module — Detailed Plan

## 📌 Overview
The **Delivery Partner Module** enables delivery personnel to manage order pickups, navigate to customer addresses using OpenStreetMap / Google Maps, verify packages, collect cash for Cash-on-Delivery (COD) orders, and complete deliveries with real-time cloud synchronization.

---

## 🏗️ Architecture & Role Management

### 1. Role-Based Access Control (RBAC)
* **Role Identifier**: `role: "DELIVERY"`
* **Authentication**: Mobile Number + Password/PIN via AWS Cognito.
* **Routing**: [`MainActivity.kt`](file:///Users/saisunkari/antigravity/G-Store/app/src/main/java/com/example/MainActivity.kt) detects `AppState.activeRole == "DELIVERY"` and renders `DeliveryScreen()`.
* **Security & Scoping**: Driver only queries orders currently assigned to them or marked as `OUT_FOR_DELIVERY`.

---

## 🚀 Phase-by-Phase Implementation Blueprint

```
DELIVERY MODULE ROADMAP
├── Phase 1: Models & State Layer (User role, Order delivery fields, AppState driver actions)
├── Phase 2: Driver Dashboard UI (Online/Offline switch, active queue, delivery cards)
├── Phase 3: Navigation & Customer Contact (OSMDroid route, Google Maps voice launch, Call/WhatsApp)
├── Phase 4: Verification & Cash Handover (Item bag checklist, COD cash collector, OTP verification)
└── Phase 5: Admin & Customer Sync (Admin driver dispatch, Customer tracking card with driver info)
```

---

### 📦 Phase 1: Models & State Layer

#### 1. Domain Models ([Models.kt](file:///Users/saisunkari/antigravity/G-Store/app/src/main/java/com/example/domain/model/Models.kt))
* Add `"DELIVERY"` to supported `User.role` values.
* Extend `Order` with delivery fields:
  ```kotlin
  data class Order(
      // existing fields...
      val assignedDriverId: String = "",
      val assignedDriverName: String = "",
      val assignedDriverPhone: String = "",
      val deliveryOtp: String = "",
      val cashCollected: Double = 0.0
  )
  ```

#### 2. AppState Driver Store ([AppState.kt](file:///Users/saisunkari/antigravity/G-Store/app/src/main/java/com/example/ui/state/AppState.kt))
* **State variables**:
  - `var isDriverOnline by mutableStateOf(true)`
  - `var driverCashInHand by mutableStateOf(0.0)`
  - `var activeDeliveryOrder by mutableStateOf<Order?>(null)`
* **Driver Methods**:
  - `driverLogin(phone: String, pass: String)`
  - `markOrderPickedUp(orderId: String)` ➔ sets status to `OUT_FOR_DELIVERY`
  - `confirmDelivery(orderId: String, otp: String, cashAmount: Double)` ➔ sets status to `DELIVERED`, records cash collected
  - `reportDeliveryIssue(orderId: String, reason: String)` ➔ records delivery failure reason

---

### 🎨 Phase 2: Delivery Dashboard UI (`ui/delivery/DeliveryScreen.kt`)

#### 1. Driver Status Header
* Driver name & vehicle icon (🛵).
* Toggle Switch: **🟢 Online (Accepting Deliveries)** vs **🔴 Offline (On Break)**.
* Daily Metric Bar:
  - **Completed Trips Today**: e.g., `8 Delivered`
  - **Cash Collected Today**: e.g., `₹4,850 in hand`

#### 2. Active Orders Queue
* **High Priority Banner**: Shows the currently active delivery order with direct action buttons.
* **Pending Pickup Tab**: Orders that are packed at the store ready to be loaded onto the bike.
* **Completed History Tab**: List of orders delivered today with customer name, timestamp, and amount collected.

#### 3. Delivery Order Card Component
* **Customer Info**: Customer Name, Phone, Distance (`2.3 km away`).
* **Delivery Address**: House No, Street, Landmark.
* **Payment Badge**:
  - `💵 CASH ON DELIVERY: ₹1,480` (High visibility amber/gold pill)
  - `💳 PAID ONLINE` (Emerald green pill)
* **Action Buttons**:
  - 📞 **Call Customer**
  - 💬 **WhatsApp**
  - 🗺️ **Map Navigation**
  - ✅ **Deliver / Collect Cash**

---

### 🗺️ Phase 3: Live Navigation & Customer Contact

#### 1. In-App OpenStreetMap (OSMDroid) View
* Visual sheet displaying:
  - **Store Marker** (City Supermarket Rajam)
  - **Customer Marker** (Delivery location pin)
  - Estimated straight-line distance.

#### 2. Turn-by-Turn GPS Navigation
* **1-Tap Google Maps Button**: Launches native Google Maps app with turn-by-turn voice directions:
  ```kotlin
  val gmmIntentUri = Uri.parse("google.navigation:q=${order.latitude},${order.longitude}")
  val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
      setPackage("com.google.android.apps.maps")
  }
  context.startActivity(mapIntent)
  ```

#### 3. 1-Tap Customer Contact
* **Phone Call**: Launches dialer with customer phone number (`Intent.ACTION_DIAL`).
* **WhatsApp Message**: Pre-populates a delivery greeting:
  *`"Hello! I am on the way with your G-Store order (#GS-8291). I will arrive in approximately 5 minutes."`*

---

### 💰 Phase 4: Packing Verification & Cash Collection

#### 1. Store Packing Checklist
* A quick check modal listing all items (*e.g., 26kg HMT Rice Bag x1, 5L Freedom Oil Jar x1*) so the driver verifies everything is in the delivery bag before departure.

#### 2. Cash Collection & OTP Verification Sheet
* **If Cash on Delivery (COD)**:
  - Displays large text: **"COLLECT ₹1,480 FROM CUSTOMER"**.
  - Checkbox: `[x] Cash of ₹1,480 received in full`.
* **If Online Paid**:
  - Displays: **"DO NOT COLLECT CASH (Order Paid Online)"**.
* **Delivery Verification**:
  - Driver asks customer for 4-digit code (or last 4 digits of customer phone number).
  - Taps **"Complete Delivery & Confirm"**.
  - Order status updates in DynamoDB to `DELIVERED`.

---

### 👑 Phase 5: Admin Dispatch & Customer Tracking Sync

#### 1. Admin Order Assignment ([AdminScreen.kt](file:///Users/saisunkari/antigravity/G-Store/app/src/main/java/com/example/ui/admin/AdminScreen.kt))
* Admin can click **"Assign Driver"** on a packed order and select from registered delivery boys, or mark directly as `OUT_FOR_DELIVERY`.

#### 2. Customer Order Tracking ([OrderTrackingTimeline.kt](file:///Users/saisunkari/antigravity/G-Store/app/src/main/java/com/example/ui/components/OrderTrackingTimeline.kt))
* When order reaches `OUT_FOR_DELIVERY`, customer tracking displays:
  - **Assigned Driver**: Driver Name & photo avatar.
  - **Contact Driver**: 1-tap call button.
  - **Delivery PIN**: Shows the customer's 4-digit PIN to share with the driver.

---

## 🧪 Testing & Validation Plan
1. **Compilation**: `./gradlew assembleDebug` (Exit Code 0).
2. **Role Switching**:
   - Customer login (`+919876543210`) ➔ Customer screen.
   - Admin login (`admin@gstore.com`) ➔ Admin screen.
   - Delivery login (`+919999900001`) ➔ Delivery screen.
3. **End-to-End Delivery Simulation**:
   - Customer places order ➔ Admin marks Packed & Assigns Driver ➔ Driver starts trip, views map, calls customer, collects cash, and confirms delivery ➔ Customer and Admin receive real-time `DELIVERED` status.
