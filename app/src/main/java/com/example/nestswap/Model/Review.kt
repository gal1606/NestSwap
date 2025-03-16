package com.example.nestswap

data class Review(
    val itemId: Int,
    val itemImageResId: Int,
    val itemName: String,
    val reviewer: String,
    val body: String,
    val rating: Int
)