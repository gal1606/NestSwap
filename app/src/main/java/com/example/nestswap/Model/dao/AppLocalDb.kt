package com.example.nestswap.Model.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nestswap.Model.dao.Item
import com.example.nestswap.Model.dao.Rental
import com.example.nestswap.Review
import com.example.nestswap.base.MyApplication

@Database(entities = [Item::class, Review::class, Rental::class], version = 2, exportSchema = false)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun reviewDao(): ReviewDao
    abstract fun rentalDao(): RentalDao
}

object AppLocalDb {

    val database: AppLocalDbRepository by lazy {
        val context = MyApplication.Globals.context
            ?: throw IllegalStateException("Application context is missing")

        Room.databaseBuilder(
            context = context,
            klass = AppLocalDbRepository::class.java,
            name = "nestswap_database.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("""
                CREATE TABLE reviews_new (
                    id INTEGER PRIMARY KEY NOT NULL,
                    itemId TEXT NOT NULL,
                    itemImageUrl TEXT,  -- New field for image URL
                    itemName TEXT NOT NULL,
                    reviewer TEXT NOT NULL,
                    body TEXT NOT NULL,
                    rating INTEGER NOT NULL
                )
            """)
            database.execSQL("""
                INSERT INTO reviews_new (id, itemId, itemName, reviewer, body, rating)
                SELECT id, itemId, itemName, reviewer, body, rating FROM reviews
            """)
            // Drop the old table and rename the new one
            database.execSQL("DROP TABLE reviews")
            database.execSQL("ALTER TABLE reviews_new RENAME TO reviews")
        }
    }
}