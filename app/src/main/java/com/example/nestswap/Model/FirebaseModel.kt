package com.example.nestswap.Model

import android.util.Log
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.Model.dao.Rental
import com.example.nestswap.Review
import com.example.nestswap.base.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseModel {

    val database = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getAllItems(callback: (List<Item>) -> Unit) {
        database.collection(Constants.COLLECTIONS.ITEMS)
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    val id = document.getLong("id")?.toInt() ?: 0
                    val name = document.getString("name") ?: ""
                    val description = document.getString("description") ?: ""
                    val category = document.getString("category") ?: ""
                    val condition = document.getString("condition") ?: ""
                    val owner = document.getString("owner") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val imageUrl = document.getString("imageUrl")
                    val imagePublicId = document.getString("imagePublicId")
                    val item = Item(id, name, description, category, condition, owner, price, imageUrl, imagePublicId)
                    items.add(item)
                }
                callback(items)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getAllReviews(callback: (List<Review>) -> Unit) {
        database.collection(Constants.COLLECTIONS.REVIEWS)
            .get()
            .addOnSuccessListener { result ->
                val reviews = mutableListOf<Review>()
                for (document in result) {
                    val id = document.getLong("id")?.toInt() ?: 0
                    val itemId = document.getString("itemId") ?: ""
                    val itemImageUrl = document.getString("itemImageUrl")
                    val itemName = document.getString("itemName") ?: ""
                    val reviewer = document.getString("reviewer") ?: ""
                    val body = document.getString("body") ?: ""
                    val rating = document.getLong("rating")?.toInt() ?: 0
                    val review = Review(id, itemId, itemImageUrl, itemName, reviewer, body, rating)
                    reviews.add(review)
                }
                callback(reviews)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun getAllRentals(callback: (List<Rental>) -> Unit) {
        database.collection(Constants.COLLECTIONS.RENTALS)
            .get()
            .addOnSuccessListener { result ->
                val rentals = mutableListOf<Rental>()
                for (document in result) {
                    val id = document.getLong("id")?.toInt() ?: 0
                    val itemName = document.getString("itemName") ?: "Unknown"
                    val owner = document.getString("owner") ?: ""
                    val renter = document.getString("renter") ?: ""
                    val rental = Rental(id, itemName, owner, renter)
                    rentals.add(rental)
                }
                callback(rentals)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    fun addItem(item: Item, callback: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("FirebaseModel", "User not authenticated")
            callback(false)
            return
        }

        val itemWithOwner = item.copy(owner = currentUserId)
        val itemMap = mapOf(
            "id" to itemWithOwner.id,
            "name" to itemWithOwner.name,
            "description" to itemWithOwner.description,
            "category" to itemWithOwner.category,
            "condition" to itemWithOwner.condition,
            "owner" to itemWithOwner.owner,
            "price" to itemWithOwner.price,
            "imageUrl" to itemWithOwner.imageUrl,
            "imagePublicId" to itemWithOwner.imagePublicId
        )
        val itemRef = database.collection(Constants.COLLECTIONS.ITEMS).document(itemWithOwner.id.toString())
        itemRef.set(itemMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error adding item to Firestore", e)
                callback(false)
            }
    }

    fun addReview(review: Review, callback: (Boolean) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("FirebaseModel", "User not authenticated")
            callback(false)
            return
        }

        val reviewWithReviewer = review.copy(reviewer = currentUserId)
        val reviewMap = mapOf(
            "id" to reviewWithReviewer.id,
            "itemId" to reviewWithReviewer.itemId,
            "itemImageUrl" to reviewWithReviewer.itemImageUrl,
            "itemName" to reviewWithReviewer.itemName,
            "reviewer" to reviewWithReviewer.reviewer,
            "body" to reviewWithReviewer.body,
            "rating" to reviewWithReviewer.rating
        )
        database.collection(Constants.COLLECTIONS.REVIEWS).document(reviewWithReviewer.id.toString())
            .set(reviewMap)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error adding review to Firestore", e)
                callback(false)
            }
    }

    fun addRental(rental: Rental, callback: () -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.e("FirebaseModel", "User not authenticated")
            callback()
            return
        }

        val rentalWithRenter = rental.copy(renter = currentUserId)
        database.collection(Constants.COLLECTIONS.RENTALS).document(rentalWithRenter.id.toString())
            .set(rentalWithRenter)
            .addOnSuccessListener { callback() }
            .addOnFailureListener { callback() }
    }

    fun deleteItem(item: Item, callback: () -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null || item.owner != currentUserId) {
            Log.e("FirebaseModel", "User not authenticated or not authorized to delete this item")
            callback()
            return
        }

        database.collection(Constants.COLLECTIONS.ITEMS).document(item.id.toString())
            .delete()
            .addOnSuccessListener {
                Log.d("FirebaseModel", "Item deleted from Firestore")
                callback()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error deleting item", e)
                callback()
            }
    }

    fun deleteRental(rentalId: Int, callback: (Boolean) -> Unit) {
        database.collection(Constants.COLLECTIONS.RENTALS).document(rentalId.toString())
            .delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}