package com.example.nestswap.Model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.nestswap.Model.dao.Rental

@Dao
interface RentalDao {
    @Query("SELECT * FROM rentals")
    fun getAllRentals(): List<Rental>

    @Query("SELECT * FROM rentals WHERE renter = :renterId")
    fun getRentalsByRenter(renterId: String): List<Rental>

    @Query("SELECT * FROM rentals WHERE id = :id")
    fun getRentalById(id: Int): Rental?

    @Insert
    fun insert(rental: Rental)

    @Update
    fun update(rental: Rental)

    @Delete
    fun delete(rental: Rental)

    @Query("DELETE FROM rentals WHERE id NOT IN (:ids)")
    fun deleteRentalsNotIn(ids: List<Int>)
}