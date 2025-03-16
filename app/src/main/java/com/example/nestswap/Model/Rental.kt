package com.example.nestswap.Model

data class Rental(
    val itemId: Int,
    val renter: String,
    val owner: String,
    val startDate: String,
    val endDate: String
)