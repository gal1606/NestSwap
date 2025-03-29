package com.example.nestswap.Model.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class Rental(
    @PrimaryKey val id: Int,
    val itemName: String,
    val owner: String,
    val renter: String
)