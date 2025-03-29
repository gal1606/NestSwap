package com.example.nestswap.Model

import android.graphics.Bitmap
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import com.example.nestswap.Model.dao.AppLocalDb
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.Model.dao.ItemDao
import com.example.nestswap.Model.dao.Rental
import com.example.nestswap.Model.dao.RentalDao
import com.example.nestswap.Model.dao.ReviewDao
import com.example.nestswap.Review
import com.example.nestswap.base.Constants
import com.example.nestswap.base.EmptyCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idz.colman24class2.model.CloudinaryModel
import java.util.concurrent.Executors

class Model private constructor() {

    private val firebaseModel = FirebaseModel()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val db = AppLocalDb.database
    private val itemDao: ItemDao = db.itemDao()
    private val reviewDao: ReviewDao = db.reviewDao()
    private val rentalDao: RentalDao = db.rentalDao()

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    companion object {
        val instance = Model()
    }

    enum class DataSource {
        LOCAL,
        FIREBASE
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun signIn(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, null)
                else callback(false, task.exception?.message ?: "Sign-in failed")
            }
    }

    fun signUp(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) callback(true, null)
                else callback(false, task.exception?.message ?: "Sign-up failed")
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getAllItems(callback: (List<Item>, DataSource) -> Unit) {
        executor.execute {
            try {
                val localItems = itemDao.getAllItems()
                mainHandler.post { callback(localItems, DataSource.LOCAL) }

                firebaseModel.getAllItems { items ->
                    executor.execute {
                        try {
                            items.forEach { itemDao.insert(it) }
                            val updatedItems = itemDao.getAllItems()
                            mainHandler.post { callback(updatedItems, DataSource.FIREBASE) }
                        } catch (e: Exception) {
                            mainHandler.post { callback(localItems, DataSource.LOCAL) }
                        }
                    }
                }
            } catch (e: Exception) {
                mainHandler.post { callback(emptyList(), DataSource.LOCAL) }
            }
        }
    }

    fun getAllReviews(callback: (List<Review>) -> Unit) {
        executor.execute {
            try {
                val localReviews = reviewDao.getAllReviews()
                mainHandler.post { callback(localReviews) }

                firebaseModel.getAllReviews { reviews ->
                    executor.execute {
                        try {
                            reviews.forEach { reviewDao.insert(it) }
                            val updatedReviews = reviewDao.getAllReviews()
                            mainHandler.post { callback(updatedReviews) }
                        } catch (e: Exception) {
                            mainHandler.post { callback(localReviews) }
                        }
                    }
                }
            } catch (e: Exception) {
                mainHandler.post { callback(emptyList()) }
            }
        }
    }

    fun getReviewsForUser(userId: String, callback: (List<Review>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("reviews")
            .whereEqualTo("reviewee", userId)
            .get()
            .addOnSuccessListener { result ->
                val reviews = result.mapNotNull { it.toObject(Review::class.java) }
                callback(reviews)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    fun getAllRentals(callback: (List<Rental>, DataSource) -> Unit) {
        executor.execute {
            try {
                val localRentals = rentalDao.getAllRentals()
                mainHandler.post { callback(localRentals, DataSource.LOCAL) }

                firebaseModel.getAllRentals { rentals ->
                    executor.execute {
                        try {
                            val fetchedIds = rentals.map { it.id }
                            rentalDao.deleteRentalsNotIn(fetchedIds)
                            for (rental in rentals) {
                                try {
                                    rentalDao.insert(rental)
                                } catch (e: android.database.sqlite.SQLiteConstraintException) {
                                    Log.w("Model", "Duplicate rental ID ${rental.id} detected, updating instead")
                                    rentalDao.update(rental) // Update if insert fails due to duplicate ID
                                }
                            }
                            val updatedRentals = rentalDao.getAllRentals()
                            mainHandler.post { callback(updatedRentals, DataSource.FIREBASE) }
                        } catch (e: Exception) {
                            Log.e("Model", "Error syncing rentals: ${e.message}", e)
                            mainHandler.post { callback(localRentals, DataSource.LOCAL) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Model", "Error fetching local rentals: ${e.message}", e)
                mainHandler.post { callback(emptyList(), DataSource.LOCAL) }
            }
        }
    }

    fun addItem(item: Item, bitmap: Bitmap?, callback: (Boolean) -> Unit) {
        if (bitmap != null) {
            CloudinaryModel().uploadBitmap(bitmap, { url, publicId ->
                val itemWithImage = item.copy(imageUrl = url, imagePublicId = publicId)
                firebaseModel.addItem(itemWithImage) { success ->
                    if (success) {
                        executor.execute {
                            try {
                                itemDao.insert(itemWithImage)
                                mainHandler.post { callback(true) }
                            } catch (e: Exception) {
                                Log.e("Model", "Failed to insert item into Room: ${e.message}", e)
                                mainHandler.post { callback(false) }
                            }
                        }
                    } else {
                        callback(false)
                    }
                }
            }, { error ->
                Log.e("Model", "Error uploading image to Cloudinary: $error")
                callback(false)
            })
        } else {
            firebaseModel.addItem(item) { success ->
                if (success) {
                    executor.execute {
                        try {
                            itemDao.insert(item)
                            mainHandler.post { callback(true) }
                        } catch (e: Exception) {
                            Log.e("Model", "Failed to insert item into Room: ${e.message}", e)
                            mainHandler.post { callback(false) }
                        }
                    }
                } else {
                    callback(false)
                }
            }
        }
    }

    fun deleteItem(item: Item, callback: EmptyCallback) {
        firebaseModel.database.collection(Constants.COLLECTIONS.ITEMS).document(item.id.toString())
            .delete()
            .addOnSuccessListener {
                executor.execute {
                    try {
                        itemDao.delete(item)
                        mainHandler.post { callback(true) }
                    } catch (e: Exception) {
                        mainHandler.post { callback(false) }
                    }
                }
            }
            .addOnFailureListener {
                executor.execute {
                    try {
                        itemDao.delete(item)
                        mainHandler.post { callback(false) }
                    } catch (e: Exception) {
                        mainHandler.post { callback(false) }
                    }
                }
            }
    }

    fun addReview(review: Review, callback: EmptyCallback) {
        firebaseModel.addReview(review) {
            executor.execute {
                try {
                    reviewDao.insert(review)
                    mainHandler.post { callback(true) }
                } catch (e: Exception) {
                    mainHandler.post { callback(false) }
                }
            }
        }
    }

    fun addRental(rental: Rental, callback: EmptyCallback) {
        firebaseModel.addRental(rental) {
            executor.execute {
                try {
                    rentalDao.insert(rental)
                    mainHandler.post { callback(true) }
                } catch (e: Exception) {
                    mainHandler.post { callback(false) }
                }
            }
        }
    }

    fun deleteRental(rentalId: Int, callback: (Boolean) -> Unit) {
        firebaseModel.deleteRental(rentalId) { success ->
            if (success) {
                executor.execute {
                    try {
                        val rental = rentalDao.getRentalById(rentalId)
                        if (rental != null) {
                            rentalDao.delete(rental)
                            mainHandler.post { callback(true) }
                        } else {
                            mainHandler.post { callback(false) }
                        }
                    } catch (e: Exception) {
                        mainHandler.post { callback(false) }
                    }
                }
            } else {
                mainHandler.post { callback(false) }
            }
        }
    }
}