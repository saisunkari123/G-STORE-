# Rice Mart - Database Design Blueprint
*Author: Rice Mart CTO & Product Mentor*
*Status: Milestone 3 - Approved & Locked*

Welcome to our database design! This is a **fully normalized, relational database schema** optimized for performance, security, and future expansion (groceries, fruits, etc.).

---

## 1. Database Schema Overview (Entity Relationship)

Below is how our tables link together. Each table is designed to keep our data compact, fast, and secure.

```text
  ┌──────────────┐          ┌────────────────┐
  │    Users     │          │   Addresses    │
  │ (Customers/  │1        N│ (User delivery │
  │ Admin/Rider) ├─────────>│   locations)   │
  └──────┬───────┘          └───────┬────────┘
         │                          │
         │1                         │1
         │                          │
         │          ┌───────────────▼┐
         │          │     Orders     │
         │         N│ (Total price,  │
         └─────────>│  status, COD)  │
                    └───────┬────────┘
                            │1
                            │
                            │N
                    ┌───────▼────────┐          ┌────────────────┐
                    │   OrderItems   │N        1│    Products    │
                    │ (Bag size qty, ├─────────>│ (Basmati, etc. │
                    │ purchase price)│          │  and category) │
                    └────────────────┘          └───────▲────────┘
                                                        │N
                                                        │
                                                        │1
                                                ┌───────┴────────┐
                                                │   Categories   │
                                                │ (Rice, future: │
                                                │  Groceries...) │
                                                └────────────────┘
```

---

## 2. Table-by-Table Breakdown

### 1. `users` (Roles & Credentials)
Stores credentials and authorization roles for customers, admins, and riders.
*   `id` (String, Primary Key) - Matches the unique user ID from Firebase.
*   `phone` (String) - Customer phone number (e.g., `+919876543210`).
*   `name` (String) - Full name.
*   `role` (String) - `'CUSTOMER'`, `'ADMIN'`, or `'DELIVERY_BOY'`.
*   `email` (String) - User email address for auth and communication.
*   `pinOrPassword` (String) - Security PIN for Customers, Password for Admin/Delivery.
*   `createdAt` (Long) - Date & time when the account was created.

### 2. `categories` (Future Expansion Ready)
Organizes our store. In Phase 1, we will only have one category: `'Rice bags'`. Later, we can add `'Groceries'`, `'Fruits'`, etc., without changing any code!
*   `id` (String, Primary Key) - Auto-generated category ID (e.g., `c_rice`).
*   `nameEn` (String) - Category name in English (e.g., `"Rice Bags"`).
*   `description` (String) - Description of the category.
*   `imageUrl` (String) - Cloud Storage URL for category icon.

### 3. `products` (Core Catalog)
Each rice product is a brand.
*   `id` (String, Primary Key) - Auto-generated ID (e.g., `p_sonamasoori`).
*   `categoryId` (String, Foreign Key) - ID of the category this belongs to.
*   `nameEn` (String) - Brand name in English.
*   `brand` (String) - Manufacturer or brand name.
*   `descriptionEn` (String) - Full description in English.
*   `shortDescriptionEn` (String) - Short UI description in English.
*   `imageUrls` (List<String>) - Array of image URLs from Firebase Storage.
*   `thumbnailIndex` (Int) - Index for the primary image.
*   `variants` (List<ProductVariant>) - Complex nested object via TypeConverter. Replaces old `sizesJson`.
    *   `ProductVariant` contains: `id`, `weight`, `unit` (e.g., "Kg"), `currentPrice`, `mrp`, `stockQuantity`, `sku`.
*   `isAvailable` (Boolean) - Whether the product can be purchased.
*   `isEnabled` (Boolean) - Admin toggle to show/hide product completely.
*   `tags` (List<String>) - Search tags.
*   `sku` (String) - Product SKU.
*   `dateCreated` (Long) - Creation timestamp.
*   `lastUpdated` (Long) - Last update timestamp.

### 4. `addresses` (Delivery Management)
Stores customer delivery locations.
*   `id` (String, Primary Key) - Auto-generated unique ID.
*   `userId` (String, Foreign Key -> `users.id`) - Links to the user.
*   `houseNo` (String) - Flat/House/Road details.
*   `landmark` (String) - Nearby prominent landmark.
*   `distanceKm` (Double) - Measured distance from store (calculated via GPS/Map mockup). Must be `<= 5.0` to check out!
*   `isSelected` (Boolean) - True if this is their primary delivery address.

### 5. `orders` (Order Ledger)
Keeps track of every transaction.
*   `id` (String, Primary Key) - Order identifier, e.g., `'RM-10029'`.
*   `userId` (String, Foreign Key -> `users.id`) - Customer ID.
*   `customerName` (String) - Snapshot of Customer's Name.
*   `customerPhone` (String) - Snapshot of Customer's Phone.
*   `addressHouseNo` (String) - Delivery house details.
*   `addressLandmark` (String) - Delivery landmark.
*   `distanceKm` (Double) - Measured distance from store.
*   `subtotal` (Double) - Sum of item costs.
*   `deliveryFee` (Double) - `₹40` if subtotal is `< 1000`, else `₹0`.
*   `totalAmount` (Double) - `subtotal + deliveryFee`.
*   `status` (String) - `'PENDING'`, `'ASSIGNED'`, `'PICKED_UP'`, `'OUT_FOR_DELIVERY'`, `'DELIVERED'`, `'CANCELLED'`.
*   `assignedRiderId` (String, Nullable, Foreign Key -> `users.id`) - Delivery Boy assigned to this order.
*   `createdAt` (Long) - Order date.

### 6. `order_items` (Order Contents Receipt)
*Why separate order items?* Because product prices and details can change over time! If a customer buys a Sona Masoori bag today for ₹850, and next month the price increases to ₹900, we must preserve historical receipts at ₹850.
*   `id` (String, Primary Key) - Auto-generated item ID.
*   `orderId` (String, Foreign Key -> `orders.id` ON DELETE CASCADE) - Links to parent order.
*   `productId` (String) - Bought product ID.
*   `productName` (String) - Snapshot of brand name.
*   `selectedSize` (String) - Snapshot of selected size (e.g., `'25kg'`).
*   `priceAtPurchase` (Double) - Historical price saved at checkout.
*   `quantity` (Int) - Amount purchased.

---

## 3. Local Caching Strategy (Offline Support)
Since local delivery boys and customers might lose network connections, we will bundle **Room SQLite Database** directly in the app.
*   **Customer:** When online, we pull from Cloud Firestore and sync immediately to Room. When offline, we load categories and products instantly from Room so the app never feels slow or broken!
*   **Rider:** The active delivery screen caches the customer address and phone number so they can complete deliveries and call customers even in basement network deadzones.
