package com.example.nestswap.Model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.nestswap.Model.dao.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getAllItems(): List<Item>

    @Query("SELECT * FROM items WHERE id = :id")
    fun getItemById(id: String): Item?

    @Query("SELECT * FROM items WHERE owner = :ownerId")
    fun getItemsByOwner(ownerId: String): List<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Item)

    @Update
    fun update(item: Item)

    @Delete
    fun delete(item: Item)
}