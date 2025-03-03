package com.example.nestswap.Model

import com.example.nestswap.Item

class Model private constructor() {

    val items: List<Item> = listOf(
        Item(1, "Laptop", "A barely used gaming laptop with high specs.", "Electronics", "Used", "Alice"),
        Item(2, "Vintage Jacket", "Leather jacket from the 80s, great condition.", "Clothing", "Used", "Bob"),
        Item(3, "Coffee Maker", "Brand new espresso machine, unopened.", "Appliances", "New", "Charlie"),
        Item(4, "Bookshelf", "Wooden bookshelf, slightly scratched.", "Furniture", "Used", "Diana"),
        Item(5, "Smartphone", "Latest model, still in box.", "Electronics", "New", "Eve"),
        Item(6, "Guitar", "Acoustic guitar with a few strum marks.", "Musical Instruments", "Used", "Frank"),
        Item(7, "Desk Lamp", "Modern LED lamp, perfect for studying.", "Furniture", "New", "Grace"),
        Item(8, "Running Shoes", "Worn a few times, size 9.", "Clothing", "Used", "Hank"),
        Item(9, "Blender", "High-power blender, used once.", "Appliances", "Used", "Ivy"),
        Item(10, "Board Game", "Complete set, played twice.", "Games", "Used", "Jack"),
        Item(11, "Headphones", "Noise-canceling headphones, brand new.", "Electronics", "New", "Kelly"),
        Item(12, "Dining Table", "Solid oak table, minor wear.", "Furniture", "Used", "Liam"),
        Item(13, "Camera", "DSLR camera with lens, excellent condition.", "Electronics", "Used", "Mia"),
        Item(14, "Sweater", "Hand-knitted wool sweater, like new.", "Clothing", "New", "Noah"),
        Item(15, "Microwave", "Compact microwave, works perfectly.", "Appliances", "Used", "Olivia"),
        Item(16, "Chess Set", "Wooden chess set, missing one pawn.", "Games", "Used", "Paul"),
        Item(17, "Monitor", "27-inch 4K monitor, unopened.", "Electronics", "New", "Quinn"),
        Item(18, "Couch", "Comfy sofa, some stains.", "Furniture", "Used", "Rose"),
        Item(19, "Skateboard", "Barely used, great for tricks.", "Sports", "Used", "Sam"),
        Item(20, "Watch", "Luxury wristwatch, brand new.", "Accessories", "New", "Tina")
    )

    companion object {
        val instance: Model = Model()
    }
}