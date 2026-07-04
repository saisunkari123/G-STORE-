# Phase 3 Tasks: UI Redesign & Firebase Auth

## Authentication
- [x] Add Firebase Auth dependency (if not present)
- [x] Implement Firebase Phone Auth in `AppState.kt`
- [x] Rewrite `LoginScreen.kt` to default to Registration/Phone Entry and handle OTP input

## Customer UI Overhaul (`CustomerScreen.kt`)
- [x] Fix header and search bar (colors, contrast, padding)
- [x] Remove mock GPS button
- [x] Fix Category click filtering
- [x] Redesign Product Cards (strikethrough MRP, variants selectors)
- [x] Fix Address Selection logic in modal

## Admin Fixes (`AdminScreen.kt`)
- [x] Add loading indicators/overlays during product upload
- [x] Fix the logic that is currently preventing new products from being saved.

## Phase 4: Revert to Email Authentication
- [x] Remove Phone Auth variables and logic from AppState.kt
- [x] Add customerLogin and customerRegister to AppState.kt using Firebase Email Auth
- [x] Overhaul LoginScreen.kt Customer tab for Login vs Registration form
