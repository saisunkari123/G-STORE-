package com.example.data.repository

import com.example.domain.model.*
import com.example.domain.repository.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseProductRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProductRepository {

    override fun getAllCategories(): Flow<List<Category>> = callbackFlow {
        val listener = firestore.collection("categories").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val categories = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Category::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                trySend(categories)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun saveCategories(categories: List<Category>) {
        val batch = firestore.batch()
        categories.forEach { cat ->
            val ref = firestore.collection("categories").document(cat.id.ifEmpty { UUID.randomUUID().toString() })
            val catToSave = cat.copy(id = ref.id)
            batch.set(ref, catToSave)
        }
        batch.commit().await()
    }

    override suspend fun clearCategories() {
        val snapshot = firestore.collection("categories").get().await()
        val batch = firestore.batch()
        snapshot.documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    override fun getAllProducts(): Flow<List<Product>> = callbackFlow {
        val listener = firestore.collection("products").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Product::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getProductById(productId: String): Product? {
        return try {
            val snapshot = firestore.collection("products").document(productId).get().await()
            snapshot.toObject(Product::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveProducts(products: List<Product>) {
        val batch = firestore.batch()
        products.forEach { prod ->
            val ref = firestore.collection("products").document(prod.id.ifEmpty { UUID.randomUUID().toString() })
            val prodToSave = prod.copy(id = ref.id)
            batch.set(ref, prodToSave)
        }
        batch.commit().await()
    }

    override suspend fun saveProduct(product: Product) {
        val ref = firestore.collection("products").document(product.id.ifEmpty { UUID.randomUUID().toString() })
        val prodToSave = product.copy(id = ref.id)
        ref.set(prodToSave).await()
    }

    override suspend fun deleteProduct(productId: String) {
        firestore.collection("products").document(productId).delete().await()
    }

    override fun getGiftConfigs(): Flow<List<GiftItemConfig>> = callbackFlow {
        // Dummy implementation since AWS is primarily used now
        trySend(emptyList())
        awaitClose { }
    }

    override suspend fun saveGiftConfigs(configs: List<GiftItemConfig>) {
        // Dummy implementation since AWS is primarily used now
    }

    override suspend fun clearGiftConfigs() {
        // Dummy implementation since AWS is primarily used now
    }
}

class FirebaseOrderRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("orders").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val orders = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Order::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                trySend(orders.sortedByDescending { it.createdAt })
            }
        }
        awaitClose { listener.remove() }
    }

    override fun getOrdersByUserId(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Order::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    trySend(orders.sortedByDescending { it.createdAt })
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveOrder(order: Order) {
        val ref = firestore.collection("orders").document(order.id.ifEmpty { UUID.randomUUID().toString() })
        val orderToSave = order.copy(id = ref.id)
        ref.set(orderToSave).await()
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        firestore.collection("orders").document(orderId).update("status", newStatus).await()
    }
}

class FirebaseUserRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : UserRepository {

    override fun getUserById(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users").document(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(null)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val user = try {
                    snapshot.toObject(User::class.java)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                trySend(user)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun getUserByEmailAndRole(email: String, role: String): User? {
        val snapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .whereEqualTo("role", role)
            .get().await()
        return try {
            snapshot.documents.firstOrNull()?.toObject(User::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun saveUser(user: User) {
        val ref = firestore.collection("users").document(user.id.ifEmpty { UUID.randomUUID().toString() })
        val userToSave = user.copy(id = ref.id)
        ref.set(userToSave).await()
    }

    override suspend fun deleteUser(userId: String) {
        firestore.collection("users").document(userId).delete().await()
    }

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                error.printStackTrace()
                trySend(emptyList())
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(User::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                trySend(users)
            }
        }
        awaitClose { listener.remove() }
    }
}

class FirebaseAddressRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AddressRepository {

    override fun getAddressesByUserId(userId: String): Flow<List<Address>> = callbackFlow {
        val listener = firestore.collection("addresses")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val addresses = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Address::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    trySend(addresses)
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveAddress(address: Address) {
        val ref = firestore.collection("addresses").document(address.id.ifEmpty { UUID.randomUUID().toString() })
        val addressToSave = address.copy(id = ref.id)
        ref.set(addressToSave).await()
    }

    override suspend fun saveAndSelectAddress(address: Address) {
        val batch = firestore.batch()
        // Unselect others
        val snapshot = firestore.collection("addresses").whereEqualTo("userId", address.userId).get().await()
        snapshot.documents.forEach { doc ->
            batch.update(doc.reference, "isSelected", false)
        }
        
        // Save new
        val ref = firestore.collection("addresses").document(address.id.ifEmpty { UUID.randomUUID().toString() })
        val addressToSave = address.copy(id = ref.id, isSelected = true)
        batch.set(ref, addressToSave)
        batch.commit().await()
    }

    override suspend fun selectAddress(userId: String, addressId: String) {
        val batch = firestore.batch()
        val snapshot = firestore.collection("addresses").whereEqualTo("userId", userId).get().await()
        snapshot.documents.forEach { doc ->
            batch.update(doc.reference, "isSelected", doc.id == addressId)
        }
        batch.commit().await()
    }

    override suspend fun deleteAddress(addressId: String) {
        firestore.collection("addresses").document(addressId).delete().await()
    }
}
