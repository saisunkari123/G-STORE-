# Rice Mart - Product Requirements Document (PRD)
*Author: Rice Mart CTO & Product Mentor*
*Status: Milestone 1 - Approved & Locked*

Welcome to the **Rice Mart** Product Requirements Document (PRD). This document serves as our "Single Source of Truth." Before we write a single line of code, we define exactly *what* we are building, *who* we are building it for, and *how* it should behave. This saves hundreds of hours of rewriting code later!

---

## 1. Executive Summary
Rice Mart is a premium, highly localized hyper-delivery mobile application designed for supermarkets. Phase 1 focuses exclusively on selling high-quality rice bags (such as Sona Masoori and Basmati) to local households within a strict delivery radius, supporting local languages, and providing a seamless, simple experience tailored even for older demographics.

---

## 2. Core Business Constraints
*   **Delivery Radius:** Hyperlocal delivery tracking distance (Note: 5KM constraint logic is currently bypassed in dev mode).
*   **Minimum Order Value (MOV):** No minimum order value currently enforced.
*   **Supported Languages:** English only.
*   **Payment Method (Phase 1):** Cash on Delivery (COD) only.
*   **Delivery Logistics:** Hyperlocal delivery fulfilled by in-house Delivery Boys.

---

## 3. User Personas & Roles
We have three distinct users in our ecosystem. Each user will have a tailored mobile interface or views:

1.  **The Customer:**
    *   *Need:* Easily browse, select the right size/brand of rice, input address, and place an order with minimal clicks.
    *   *Design Focus:* Large, high-contrast text, simple dropdowns, and large interactive buttons.
2.  **The Admin (Supermarket Owner):**
    *   *Need:* Real-time dashboard to accept orders, manage prices, update stock, and assign orders to Delivery Boys.
3.  **The Delivery Boy:**
    *   *Need:* Simple, distraction-free view showing assigned orders, customer addresses, one-click calling, and instant status updates.

---

## 4. Product Catalog Rules
*   **Brands Supported (Initial):** Basmati Rice, Sona Masoori Rice.
*   **Product Selection:** Customers will select a specific Brand (e.g., *Basmati*, *Sona Masoori*) and then choose their desired Bag Size (weight, e.g., 5kg, 10kg, 25kg) using a clean dropdown.
*   **Pricing Structure:** Pricing varies dynamically based on the **Brand** and the selected **Size**.
*   **Discounts:** No bulk discounts are offered in Phase 1 (pricing is linear per item based on selected size).

---

## 5. Technology Stack Recommendation
For a beginner starting their startup journey, we want a tech stack that is **industry-standard**, **highly secure**, **cost-effective (generous free tiers)**, and **avoids double-learning** (writing Kotlin for Android and learning a completely different language for the server).

| Component | Technology | Why We Selected It (Beginner-Friendly Explanation) |
| :--- | :--- | :--- |
| **Frontend (App)** | **Kotlin + Jetpack Compose** | Google's modern, declarative UI framework. It allows us to build beautiful screens with less code, and it handles screen resizing (like tablets or larger text fonts for older users) beautifully. |
| **Backend (API)** | **Firebase Cloud Functions (Node/Kotlin)** | Runs serverless—it spins up automatically when needed, has a huge free tier, and scales to thousands of users effortlessly. |
| **Database** | **Room (Local) + Firestore (Cloud)** | **Room** allows the app to work offline (caching products). **Firestore** is a real-time cloud database that syncs instantly between the Customer, Admin, and Delivery Boy apps. |
| **Authentication** | **Firebase Phone Auth** | Extremely secure. It handles sending SMS text messages with OTP codes globally. |
| **Push Notifications** | **Firebase Cloud Messaging (FCM)** | The industry standard for sending instant notifications (e.g., "Your order has been dispatched!"). |
| **Hosting & Images** | **Firebase Cloud Storage** | Highly secure file storage where we can upload and retrieve product images instantly. |

---

## 6. Decided Business Logic & Configurations
The following architectural and business choices have been locked after consultation:

1.  **Delivery Fee Structure:**
    *   There is currently no Minimum Order Value enforced.
    *   Delivery is **free** for orders of **₹1000** and above.
    *   For orders under **₹1000**, a delivery fee of **₹40** is charged.
2.  **Out-of-Bounds Address Handling:**
    *   Currently, the 5 KM hard block is relaxed for development and testing. Any selected address is allowed to proceed to checkout.
3.  **Store Timings & Order Placement:**
    *   The store is typically open from **8:00 AM to 8:00 PM**.
    *   Orders cannot be placed outside these hours unless the Developer Override (`forceStoreOpen`) is enabled.
4.  **Out-of-Stock Products:**
    *   Products that are out of stock remain visible on the catalog but the "Add to Cart" button is disabled and shows an "OUT OF STOCK" indicator. This prevents unfulfillable orders.
5.  **Authentication Mode (Email & PIN):**
    *   The app currently uses a custom Email & Security PIN authentication system. Users register with Name, Email, Phone, and a secure PIN, allowing for fully offline local testing without Firebase dependencies.
6.  **Multi-Role Test Experience (Role Switcher):**
    *   **Yes:** We will implement an elegant, floating developer panel/role switcher at the top of the interface. This will allow you to instantly switch your active role between **Customer**, **Admin**, and **Delivery Boy**. You can place an order as a Customer, switch to Admin to assign it, and switch to Delivery Boy to deliver it, making full-cycle testing incredibly fun and visual!

---

## 7. Next Steps & Milestone 2: Architecture
Now that Milestone 1 (PRD) is complete and approved:
1.  We will present **Milestone 2 (Architecture)**. We will design the clean-architecture package structure and files.
2.  We will explain how MVVM and the Repository pattern work in a way that is easy for a complete beginner to understand.
3.  We will then ask for your approval to proceed to database design!
