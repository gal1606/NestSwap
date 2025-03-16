package com.example.nestswap.Model

import com.example.nestswap.Item
import com.example.nestswap.Review
import com.example.nestswap.R

import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class Model private constructor() {

    var items: MutableList<Item> = mutableListOf(
        Item(1, "Laptop", "A barely used gaming laptop with high specs.", "Electronics", "Used", "Alice", 500.0),
        Item(2, "Vintage Jacket", "Leather jacket from the 80s, great condition.", "Clothing", "Used", "Bob", 80.0),
        Item(3, "Coffee Maker", "Brand new espresso machine, unopened.", "Appliances", "New", "Charlie", 150.0),
        Item(4, "Bookshelf", "Wooden bookshelf, slightly scratched.", "Furniture", "Used", "logged_in_user_id", 120.0),
        Item(5, "Smartphone", "Latest model, still in box.", "Electronics", "New", "Eve", 800.0),
        Item(6, "Guitar", "Acoustic guitar with a few strum marks.", "Musical Instruments", "Used", "Frank", 200.0),
        Item(7, "Desk Lamp", "Modern LED lamp, perfect for studying.", "Furniture", "New", "Grace", 50.0),
        Item(8, "Running Shoes", "Worn a few times, size 9.", "Clothing", "Used", "Hank", 40.0),
        Item(9, "Blender", "High-power blender, used once.", "Appliances", "Used", "Ivy", 90.0),
        Item(10, "Board Game", "Complete set, played twice.", "Games", "Used", "Jack", 30.0),
        Item(11, "Headphones", "Noise-canceling headphones, brand new.", "Electronics", "New", "Kelly", 250.0),
        Item(12, "Dining Table", "Solid oak table, minor wear.", "Furniture", "Used", "Liam", 300.0),
        Item(13, "Camera", "DSLR camera with lens, excellent condition.", "Electronics", "Used", "Mia", 600.0),
        Item(14, "Sweater", "Hand-knitted wool sweater, like new.", "Clothing", "New", "Noah", 60.0),
        Item(15, "Microwave", "Compact microwave, works perfectly.", "Appliances", "Used", "Olivia", 100.0),
        Item(16, "Chess Set", "Wooden chess set, missing one pawn.", "Games", "Used", "Paul", 25.0),
        Item(17, "Monitor", "27-inch 4K monitor, unopened.", "Electronics", "New", "Quinn", 400.0),
        Item(18, "Couch", "Comfy sofa, some stains.", "Furniture", "Used", "Rose", 350.0),
        Item(19, "Skateboard", "Barely used, great for tricks.", "Sports", "Used", "Sam", 70.0),
        Item(20, "Watch", "Luxury wristwatch, brand new.", "Accessories", "New", "Tina", 900.0)
    )

    val reviews: List<Review> = listOf(
        Review(
            itemId = 1,
            itemImageResId = R.drawable.projector,
            itemName = "Laptop",
            reviewer = "John Doe",
            body = "This laptop was perfect for my gaming needs! The performance was excellent, and the battery lasted all day. Highly recommend renting from Alice.",
            rating = 5
        ),
        Review(
            itemId = 3,
            itemImageResId = R.drawable.projector,
            itemName = "Coffee Maker",
            reviewer = "Jane Smith",
            body = "The coffee maker was brand new and worked like a charm. Made great espresso, though the instructions were a bit unclear. Thanks, Charlie!",
            rating = 4
        ),
        Review(
            itemId = 6,
            itemImageResId = R.drawable.projector,
            itemName = "Guitar",
            reviewer = "Bob Johnson",
            body = "The guitar was in good shape despite the strum marks. It sounded great, and Frank was easy to deal with during the rental.",
            rating = 4
        ),
        Review(
            itemId = 9,
            itemImageResId = R.drawable.projector,
            itemName = "Blender",
            reviewer = "Alice Brown",
            body = "This blender was powerful and easy to clean. Only used it once, but it was worth the rental. Ivy was a great host!",
            rating = 4
        ),
        Review(
            itemId = 13,
            itemImageResId = R.drawable.projector,
            itemName = "Camera",
            reviewer = "Charlie Davis",
            body = "The DSLR camera was in excellent condition. The lens was a bonus, and the photos turned out amazing. Mia was very responsive.",
            rating = 5
        ),
        Review(
            itemId = 20,
            itemImageResId = R.drawable.projector,
            itemName = "Watch",
            reviewer = "Emily Wilson",
            body = "This luxury watch was stunning and brand new. Kept perfect time, and Tina made the rental process seamless.",
            rating = 5
        )
    )

    // Changed to MutableList to allow adding new rentals
    var rentals: MutableList<Rental> = mutableListOf(
        Rental(1, "logged_in_user_id", "Alice", "2025-03-01", "2025-03-10"),
        Rental(2, "logged_in_user_id", "Bob", "2025-03-05", "2025-03-15")
    )

    companion object {
        val instance: Model by lazy { Model() }
    }

    fun addItem(item: Item, bitmap: Bitmap?, callback: () -> Unit) {
        if (bitmap != null) {
            // Save the Bitmap to a file (simulating storage like Cloudinary)
            val imageUri = saveBitmapToFile(bitmap)
            val updatedItem = item.copy(imageUri = imageUri)
            items = (items + updatedItem).toMutableList()
        } else {
            items = (items + item).toMutableList()
        }
        // Simulate async operation (e.g., uploading to Cloudinary)
        Thread {
            Thread.sleep(1000) // Simulate network delay
            callback()
        }.start()
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String? {
        return try {
            val fileName = "item_${UUID.randomUUID()}.jpg"
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}