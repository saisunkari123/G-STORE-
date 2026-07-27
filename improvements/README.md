# G-STORE App Improvements

This folder contains improvement plans discussed with the client on **2026-07-27**.
Each sub-folder contains a detailed plan, root cause analysis, proposed implementation, and test cases.

## Improvements Index

| # | Folder | Issue | Priority |
|---|--------|-------|----------|
| 01 | `01_real_time_sync` | Gift items & Orders not reflecting live across devices (Admin ↔ Customer) | 🔴 Critical |
| 02 | `02_cloudinary_fix` | Product image upload failing with Cloudinary API error | 🔴 Critical |
| 03 | `03_ui_text_scaling` | Big text and big cards on client's phone | 🟡 High |
| 04 | `04_minimum_order_amount` | Add configurable minimum delivery amount (Admin sets, Customer sees) | 🟡 High |
| 05 | `05_delivery_radius_config` | Admin-configurable delivery radius in km (currently hardcoded 10 km) | 🟢 Medium |
| 06 | `06_maps_ui_improvement` | Maps UI shows village name, not exact customer location pin | 🟢 Medium |

## Status

- [ ] 01 — Real-Time Sync (Gifts + Orders)
- [ ] 02 — Cloudinary Image Upload Fix
- [ ] 03 — UI Text Scaling Fix
- [ ] 04 — Minimum Order Amount
- [ ] 05 — Delivery Radius Config
- [ ] 06 — Maps UI Improvement

## Workflow
1. Review plan in each sub-folder
2. Approve → Implementation begins
3. Automated CI pipeline runs on every push
4. Manual test cases listed in each plan
