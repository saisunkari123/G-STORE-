# Rice Mart - UI/UX Design System
*Author: Rice Mart CTO & Product Mentor*
*Status: Milestone 4 - Approved & Locked*

Welcome to **Milestone 4: UI Design**! This document explains the visual identity, colors, typography, layout rules, and accessibility principles we are using to make Rice Mart feel modern, premium, and easy to use.

---

## 1. Visual Theme: Stitch UI (Premium Emerald & Gold)
Our application uses the refined Stitch UI design system, focusing on a premium, clean, and highly trustworthy look using deep emerald greens and rich gold accents.

### Color Palette (Stitch UI)
*   **Primary (Royal Emerald):** `#047857` — A deep, trustworthy green used for primary buttons, headers, and key branding.
*   **Secondary (Deep Gold):** `#D97706` — A rich gold used for premium accents, badges, and highlights.
*   **Background (Soft Alabaster):** `#F8F9FF` — A slightly cool off-white background that enhances card contrast.
*   **Surface (Pure Canvas):** `#FFFFFF` — Pure white card containers with elegant, soft shadows.
*   **Text (Charcoal Onyx):** `#1E2022` — Deep charcoal for readability.
*   **Accent (Alert Red):** `#DC2626` — Used for destructive actions or important alerts.
*   **Status Indicators:** `SoftGreen` (#D1FAE5) and `SoftOrange` (#FEF3C7) used for order statuses.

---

## 2. Typography Strategy (Accessible & Large)
Older demographics often struggle with small, thin fonts. We are implementing a bold, spacious typographic hierarchy:
*   **Display / Headings:** Large, high-weight headings (`FontWeight.Bold`, `24.sp` to `28.sp`) to make sections instantly scannable.
*   **Body Copy:** Generous, crisp body text (`16.sp` to `18.sp`) with generous line-height (`24.sp`) to prevent eye fatigue.
*   **Labels:** Prominent tags and badges with clear contrast.

---

## 3. Interaction Design & Touch Targets
*   **Golden Touch Target Rule:** Every clickable button, selector, or card will have a minimum touch height of **48dp** (usually **56dp** for high ergonomics).
*   **Zero-Ambiguity Navigation:** Simple Bottom Navigation combined with our **Developer Switcher Panel** at the top, allowing you to test all roles without needing real database accounts.
*   **Visual Micro-interactions:** Tactile Material Ripples on click, subtle scale adjustments, and friendly progress loaders to show the app is working.

---

## 4. Multi-Role UI Screens Implemented in Phase 1
We will build all three role environments into a single, cohesive, fully interactive local state-driven application. You can switch roles using our floating role switcher at the top:

1.  **Customer App Experience:**
    *   *Home & Catalog:* Scroll beautifully designed product cards (Basmati & Sona Masoori) with size dropdowns, dynamic pricing, and stock status.
    *   *Cart Drawer/Sheet:* See delivery fees calculate dynamically (₹40 under ₹1000, free above, checkout blocked under ₹500).
    *   *Address & Checkout Mock:* Interactive 5 KM delivery check, flat/landmark input.
    *   *Order History & Live Tracking:* Visual tracking timeline (Pending -> Dispatched -> Delivered).
2.  **Admin App Experience:**
    *   *Stats Summary:* Cards showing Today's Sales, Orders Pending, Completed, Cancelled.
    *   *Orders List:* Interactive cards where you can view order details and assign a Delivery Boy.
    *   *Inventory Management:* Directly edit prices, update stock levels, or toggle items out-of-stock, and see the Customer Catalog update instantly!
3.  **Delivery Boy Experience:**
    *   *My Assigned Deliveries:* Simple cards showing customer name, delivery address, distance, and item breakdown.
    *   *One-Click Actions:* Fast customer phone dialer and Status Updates (Picked Up -> Out For Delivery -> Delivered).
