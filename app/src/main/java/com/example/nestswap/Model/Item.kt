package com.example.nestswap

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val condition: String,
    val owner: String,
    val price: Double?,
    val imageUri: String? = null
) : Parcelable