# Bug Logs & Fixes

## Bug: No parameter with name 'assignedRiderId' found
**Date:** 2024-10-24
**File:** `app/src/main/java/com/example/ui/state/AppState.kt`

### Issue
The project failed to compile with the following error:
```

## Bug: Place Order clicked but no action/transition
**Date:** 2024-10-24
**File:** `app/src/main/java/com/example/ui/state/AppState.kt`

### Issue
When the "Place Order" button was clicked, the UI would sometimes show the "Placing..." state but never transition to the success screen, even if the order was saved in the background.

### Root Cause
In `placeOrder()`, the `ioScope.launch` block was using `launch(Dispatchers.Main)` inside it to update UI state. If the parent scope (`ioScope`) had issues or the `launch` call didn't behave as expected in certain coroutine contexts, the state updates (`lastPlacedOrder`, `isPlacingOrder`) might not trigger recomposition correctly or in time.

### Fix
Switched from `launch(Dispatchers.Main)` to `withContext(Dispatchers.Main)` inside the `ioScope.launch` block. This ensures that the code waits for the main thread update to complete before finishing the coroutine work, making the state transition more reliable.

```kotlin
// Before
ioScope.launch {
    try {
        orderRepository.saveOrder(newOrder)
        launch(Dispatchers.Main) {
            lastPlacedOrder = newOrder
            // ...
        }
    } catch (e: Exception) { ... }
}

// After
ioScope.launch {
    try {
        orderRepository.saveOrder(newOrder)
        withContext(Dispatchers.Main) {
            lastPlacedOrder = newOrder
            // ...
        }
    } catch (e: Exception) { ... }
}
```
e: file:///Users/saisunkari/antigravity/Rice-Mart/app/src/main/java/com/example/ui/state/AppState.kt:792:13 No parameter with name 'assignedRiderId' found.
```

## Bug: Place Order clicked but no action/transition
**Date:** 2024-10-24
**File:** `app/src/main/java/com/example/ui/state/AppState.kt`

### Issue
When the "Place Order" button was clicked, the UI would sometimes show the "Placing..." state but never transition to the success screen, even if the order was saved in the background.

### Root Cause
In `placeOrder()`, the `ioScope.launch` block was using `launch(Dispatchers.Main)` inside it to update UI state. If the parent scope (`ioScope`) had issues or the `launch` call didn't behave as expected in certain coroutine contexts, the state updates (`lastPlacedOrder`, `isPlacingOrder`) might not trigger recomposition correctly or in time.

### Fix
Switched from `launch(Dispatchers.Main)` to `withContext(Dispatchers.Main)` inside the `ioScope.launch` block. This ensures that the code waits for the main thread update to complete before finishing the coroutine work, making the state transition more reliable.

```kotlin
// Before
ioScope.launch {
    try {
        orderRepository.saveOrder(newOrder)
        launch(Dispatchers.Main) {
            lastPlacedOrder = newOrder
            // ...
        }
    } catch (e: Exception) { ... }
}

// After
ioScope.launch {
    try {
        orderRepository.saveOrder(newOrder)
        withContext(Dispatchers.Main) {
            lastPlacedOrder = newOrder
            // ...
        }
    } catch (e: Exception) { ... }
}
```

### Root Cause
The `Order` data class in `domain/model/Models.kt` did not define a parameter named `assignedRiderId`. However, the `placeOrder()` function in `AppState.kt` was attempting to instantiate an `Order` object using this named parameter.

### Fix
Removed the `assignedRiderId = null` argument from the `Order` constructor call in `AppState.kt`.

```kotlin
// Before
val newOrder = Order(
    id = "G-${System.currentTimeMillis()}-${(100..999).random()}",
    userId = currentUser?.id ?: "guest",
    // ...
    status = OrderStatus.PENDING,
    assignedRiderId = null, // <--- Removed this
    createdAt = System.currentTimeMillis(),
    items = currentItems
)

// After
val newOrder = Order(
    id = "G-${System.currentTimeMillis()}-${(100..999).random()}",
    userId = currentUser?.id ?: "guest",
    // ...
    status = OrderStatus.PENDING,
    createdAt = System.currentTimeMillis(),
    items = currentItems
)
```

## Bug: Place Order clicked but no action/transition
**Date:** 2024-10-24
**File:** `app/src/main/java/com/example/ui/state/AppState.kt`

### Issue
When the "Place Order" button was clicked, the UI would sometimes show the "Placing..." state but never transition to the success screen, even if the order was saved in the background.

### Root Cause
In `placeOrder()`, the `ioScope.launch` block was using `launch(Dispatchers.Main)` inside it to update UI state. If the parent scope (`ioScope`) had issues or the `launch` call didn't behave as expected in certain coroutine contexts, the state updates (`lastPlacedOrder`, `isPlacingOrder`) might not trigger recomposition correctly or in time.

### Fix
Switched from `launch(Dispatchers.Main)` to `withContext(Dispatchers.Main)` inside the `ioScope.launch` block. This ensures that the code waits for the main thread update to complete before finishing the coroutine work, making the state transition more reliable.

```kotlin
// Before
ioScope.launch {
    try {
        orderRepository.saveOrder(newOrder)
        launch(Dispatchers.Main) {
            lastPlacedOrder = newOrder
            // ...
        }
    } catch (e: Exception) { ... }
}

// After
ioScope.launch {
    try {
        orderRepository.saveOrder(newOrder)
        withContext(Dispatchers.Main) {
            lastPlacedOrder = newOrder
            // ...
        }
    } catch (e: Exception) { ... }
}
```
