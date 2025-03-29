package com.example.nestswap

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: Int,
    val itemId: String,
    val itemImageUrl: String?,
    val itemName: String,
    val reviewer: String,
    val body: String,
    val rating: Int
)