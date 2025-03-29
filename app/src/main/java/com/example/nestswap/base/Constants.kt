package com.example.nestswap.base

import com.example.nestswap.Model.dao.Item
import com.example.nestswap.Model.dao.Rental
import com.example.nestswap.Review

typealias ItemsCallback = (List<Item>, Boolean) -> Unit
typealias RentalsCallback = (List<Rental>, Boolean) -> Unit
typealias ReviewsCallback = (List<Review>) -> Unit
typealias EmptyCallback = (Boolean) -> Unit

object Constants {
    object COLLECTIONS {
        const val ITEMS = "items"
        const val REVIEWS = "reviews"
        const val RENTALS = "rentals"
    }
}