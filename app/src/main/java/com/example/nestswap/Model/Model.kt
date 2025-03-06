package com.example.nestswap.Model

import com.example.nestswap.Item


class Model private constructor() {

    val items: List<Item> = listOf(
        Item(1, "Laptop", "A barely used gaming laptop with high specs.", "Electronics", "Used", "Alice", 500),
        Item(2, "Vintage Jacket", "Leather jacket from the 80s, great condition.", "Clothing", "Used", "Bob", 80),
        Item(3, "Coffee Maker", "Brand new espresso machine, unopened.", "Appliances", "New", "Charlie", 150),
        Item(4, "Bookshelf", "Wooden bookshelf, slightly scratched.", "Furniture", "Used", "Diana", 120),
        Item(5, "Smartphone", "Latest model, still in box.", "Electronics", "New", "Eve", 800),
        Item(6, "Guitar", "Acoustic guitar with a few strum marks.", "Musical Instruments", "Used", "Frank", 200),
        Item(7, "Desk Lamp", "Modern LED lamp, perfect for studying.", "Furniture", "New", "Grace", 50),
        Item(8, "Running Shoes", "Worn a few times, size 9.", "Clothing", "Used", "Hank", 40),
        Item(9, "Blender", "High-power blender, used once.", "Appliances", "Used", "Ivy", 90),
        Item(10, "Board Game", "Complete set, played twice.", "Games", "Used", "Jack", 30),
        Item(11, "Headphones", "Noise-canceling headphones, brand new.", "Electronics", "New", "Kelly", 250),
        Item(12, "Dining Table", "Solid oak table, minor wear.", "Furniture", "Used", "Liam", 300),
        Item(13, "Camera", "DSLR camera with lens, excellent condition.", "Electronics", "Used", "Mia", 600),
        Item(14, "Sweater", "Hand-knitted wool sweater, like new.", "Clothing", "New", "Noah", 60),
        Item(15, "Microwave", "Compact microwave, works perfectly.", "Appliances", "Used", "Olivia", 100),
        Item(16, "Chess Set", "Wooden chess set, missing one pawn.", "Games", "Used", "Paul", 25),
        Item(17, "Monitor", "27-inch 4K monitor, unopened.", "Electronics", "New", "Quinn", 400),
        Item(18, "Couch", "Comfy sofa, some stains.", "Furniture", "Used", "Rose", 350),
        Item(19, "Skateboard", "Barely used, great for tricks.", "Sports", "Used", "Sam", 70),
        Item(20, "Watch", "Luxury wristwatch, brand new.", "Accessories", "New", "Tina", 900)
    )

    companion object {
        val instance: Model by lazy { Model() }
    }
}