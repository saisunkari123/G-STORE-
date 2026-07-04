# Rice Mart - Architectural Blueprint
*Author: Rice Mart CTO & Product Mentor*
*Status: Milestone 2 - Under Discussion*

Welcome to **Milestone 2: Architecture**. In this stage, we design the skeletal structure of our application. 

Think of building an app like building a high-end restaurant:
1.  **The Kitchen (Data Layer):** Where raw ingredients (raw data from Firestore or local SQLite) are stored and prepared.
2.  **The Head Chef (Domain/Repository Layer):** Who takes orders, applies business rules (e.g., "minimum order ₹500", "free delivery over ₹1000"), and arranges the food cleanly on plates.
3.  **The Waitstaff (ViewModel):** Who carries the plates of food to the customers and brings their feedback back to the chef.
4.  **The Customer's Table (UI Layer / Jetpack Compose):** What the customer actually sees, touches, and interacts with.

By dividing our code this way, if we decide to change the kitchen (e.g., swap Firebase with another database later), the Waitstaff and the Customer's Table don't have to change at all! This is called **Clean Architecture**.

---

## 1. Architectural Patterns Explained (Beginner-Friendly)

### MVVM (Model-View-ViewModel)
*   **Model:** The raw data (e.g., a `Product` data class with fields like `name`, `price`, `imageUrl`).
*   **View (Jetpack Compose):** The visual screens you see. It only draws the UI based on what the ViewModel tells it. It doesn't do calculations.
*   **ViewModel:** The brain of the screen. It holds the active screen state (e.g., "Is loading? Yes", "Cart items: 3"). It handles user actions (e.g., when a user clicks "Add to Cart", the View tells the ViewModel, and the ViewModel updates the cart).

### The Repository Pattern
*   Instead of letting our screens talk directly to the Database, we use a **Repository** as a mediator. 
*   The ViewModel asks the `ProductRepository`: *"Give me all Basmati rice products."* 
*   The Repository decides: *"Do I have them cached in my local database (Room) offline? If yes, show them. Otherwise, fetch them from the server (Firestore)."*

---

## 2. Target Package Structure
Here is where our files will live inside `/app/src/main/java/com/example/`:

```text
com.example/
│
├── data/                       # THE KITCHEN (Data Management)
│   ├── local/                  # Local database (Room) for offline-first caching
│   │   ├── db/                 # Database initialization
│   │   ├── dao/                # Data Access Objects (SQL queries)
│   │   └── entity/             # Databases tables
│   ├── repository/             # Raw repository implementations
│   └── mock/                   # Mock data generators (for rapid development)
│
├── domain/                     # THE HEAD CHEF (Business Core)
│   ├── model/                  # Clean domain data classes (User, Product, Order)
│   └── repository/             # Interfaces defining what repositories can do
│
├── ui/                         # THE CUSTOMER'S TABLE (UI & ViewModels)
│   ├── theme/                  # Colors, fonts, and M3 styling
│   ├── components/             # Reusable UI elements (Buttons, Dropdowns, Cards)
│   ├── switcher/               # Developer Switcher Panel (Customer/Admin/Delivery roles)
│   ├── customer/               # Customer-specific screens (Home, Details, Cart, Orders)
│   ├── admin/                  # Admin-specific screens (Dashboard, Edit Products, Status Updates)
│   ├── delivery/               # Delivery boy screens (Assigned Orders, Map/Call/Delivery)
│   └── state/                  # Shared ViewModels or global states (User session, active cart)
│
└── MainActivity.kt             # Main entrance point of our application
```

---

## 3. Why This Structure is Startup-Ready
1.  **Parallel Work:** If you have multiple developers in your startup, one can work on the Admin screens (`ui/admin`) while another designs the local database tables (`data/local`) without stepping on each other's toes.
2.  **Scalable Future:** If we add "Groceries" or "Fruits" next month, we just add them to the `data` and `domain` packages. The existing "Rice" logic remains completely untouched and safe.
3.  **No Lag:** Offline caching using Room means the app opens instantly, even if the customer has a spotty 3G internet connection in India.

---

## 4. Next Steps & Approval
1.  **Review the architecture proposal.**
2.  Let me know if this package structure makes sense to you as a beginner, or if you have any questions about MVVM or Repositories!
3.  **Once approved**, we will lock this blueprint and proceed to **Milestone 3: Database Design** (creating the tables, relationships, and Room setup).
