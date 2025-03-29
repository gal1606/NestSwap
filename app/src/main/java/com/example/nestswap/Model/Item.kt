package com.example.nestswap.Model.dao

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "items")
data class Item(
    @PrimaryKey val id: Int = UUID.randomUUID().hashCode(),
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val condition: String = "",
    val owner: String = "",
    val price: Double = 0.0,
    val imageUrl: String? = null,
    val imagePublicId: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        name = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        category = parcel.readString() ?: "",
        condition = parcel.readString() ?: "",
        owner = parcel.readString() ?: "",
        price = parcel.readDouble(),
        imageUrl = parcel.readString(),
        imagePublicId = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(condition)
        parcel.writeString(owner)
        parcel.writeDouble(price)
        parcel.writeString(imageUrl)
        parcel.writeString(imagePublicId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Item> {
        override fun createFromParcel(parcel: Parcel): Item = Item(parcel)
        override fun newArray(size: Int): Array<Item?> = arrayOfNulls(size)
    }
}