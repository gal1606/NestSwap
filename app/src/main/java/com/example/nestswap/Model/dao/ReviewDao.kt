package com.example.nestswap.Model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.nestswap.Review

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews")
    fun getAllReviews(): List<Review>

    @Query("SELECT * FROM reviews WHERE itemId = :itemId")
    fun getReviewsByItem(itemId: String): List<Review>

    @Insert
    fun insert(review: Review)

    @Update
    fun update(review: Review)

    @Delete
    fun delete(review: Review)
}