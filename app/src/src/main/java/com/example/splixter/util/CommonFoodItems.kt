package com.example.splixter.util

object CommonFoodItems {
    val DISHES: List<String> = listOf(
        // South Indian Delicacies
        "Masala Dosa", "Plain Dosa", "Onion Rava Dosa", "Mysore Masala Dosa", "Ghee Roast Dosa", 
        "Cheese Burst Dosa", "Paper Masala Dosa", "Idli Sambhar (2 Pcs)", "Vada Sambhar (2 Pcs)", 
        "Medu Vada", "Plain Uttapam", "Onion Tomato Uttapam", "Mix Veg Uttapam", "Appam with Stew", 
        "Malabar Parotta", "Lemon Rice", "Curd Rice", "Hyderabadi Upma",

        // Indian Street Food & Chaat
        "Pani Puri / Golgappe", "Sev Puri", "Bhel Puri", "Dahi Puri", "Aloo Tikki Chaat", 
        "Samosa Chaat", "Papdi Chaat", "Raj Kachori", "Pav Bhaji", "Cheese Pav Bhaji", 
        "Vada Pav", "Misal Pav", "Chole Bhature", "Chole Kulche", "Veg Kathi Roll", 
        "Paneer Roll", "Chicken Kathi Roll", "Egg Roll", "Double Egg Chicken Roll",

        // Starters & Kebabs (Veg & Non-Veg)
        "Paneer Tikka", "Paneer Malai Tikka", "Paneer Achari Tikka", "Paneer 65", "Chili Paneer", 
        "Crispy Corn", "Hara Bhara Kebab", "Veg Cutlet", "Tandoori Chicken (Half)", "Tandoori Chicken (Full)", 
        "Chicken Tikka", "Chicken Malai Tikka", "Chicken Achari Tikka", "Chicken 65", "Chicken Seekh Kebab", 
        "Mutton Seekh Kebab", "Galouti Kebab", "Kakori Kebab", "Tangdi Kebab", "Fish Amritsari", 
        "French Fries", "Peri Peri Fries", "Cheesy Fries", "Potato Wedges", "Cheese Garlic Bread", 
        "Veg Momos", "Chicken Momos", "Fried Veg Momos", "Steam Dimsums",

        // Indo-Chinese Specialties
        "Veg Manchurian Dry", "Veg Manchurian Gravy", "Chicken Manchurian Dry", "Chicken Manchurian Gravy", 
        "Chili Paneer Dry", "Chili Paneer Gravy", "Chili Chicken Dry", "Chili Chicken Gravy", 
        "Honey Chili Potato", "Gobi Manchurian", "Chili Mushroom", "Chili Baby Corn", 
        "Drums of Heaven", "Chicken Lollipop", "Dragon Chicken", "Thread Chicken", 
        "Crispy Chili Garlic Prawns", "Veg Spring Rolls", "Chicken Spring Rolls", 
        "Veg Manchow Soup", "Chicken Manchow Soup", "Hot and Sour Soup", "Veg Sweet Corn Soup", 
        "Chicken Sweet Corn Soup", "Lemon Coriander Soup",

        // Noodles, Chowmein & Chop Suey
        "Veg Hakka Noodles", "Chicken Hakka Noodles", "Egg Hakka Noodles", "Schezwan Noodles", 
        "Chili Garlic Noodles", "Singapuri Noodles", "Veg American Chop Suey", "Chicken Chop Suey",

        // Fried Rice Specialties
        "Veg Fried Rice", "Egg Fried Rice", "Chicken Fried Rice", "Schezwan Fried Rice", 
        "Burnt Garlic Fried Rice", "Triple Schezwan Rice", "Mixed Fried Rice",

        // Main Course - Indian Veg
        "Paneer Butter Masala", "Kadhai Paneer", "Shahi Paneer", "Matar Paneer", "Palak Paneer", 
        "Paneer Do Pyaza", "Handi Paneer", "Dal Makhani", "Dal Tadka", "Dal Fry", "Chana Masala", 
        "Rajma Masala", "Malai Kofta", "Mix Vegetable", "Dum Aloo", "Bhindi Masala", 
        "Baingan Bharta", "Veg Kolhapuri", "Mushroom Masala", "Kaju Curry", "Methi Matar Malai",

        // Main Course - Indian Non-Veg & Mughlai
        "Butter Chicken", "Chicken Tikka Masala", "Kadhai Chicken", "Chicken Curry", "Chicken Kolhapuri", 
        "Chicken Chettinad", "Chicken Handi", "Mutton Rogan Josh", "Mutton Curry", "Mutton Korma", 
        "Nalli Nihari", "Fish Curry", "Fish Tikka Masala", "Prawns Masala", "Egg Curry", "Egg Bhurji",

        // Rice, Biryani & Pulao
        "Hyderabadi Chicken Biryani", "Hyderabadi Mutton Biryani", "Veg Dum Biryani", "Paneer Biryani", 
        "Egg Biryani", "Lucknowi Chicken Biryani", "Steam Rice", "Jeera Rice", "Veg Pulao", "Matar Pulao",

        // Indian Breads
        "Tandoori Roti", "Butter Roti", "Plain Naan", "Butter Naan", "Garlic Naan", 
        "Cheese Garlic Naan", "Laccha Paratha", "Pudina Paratha", "Missi Roti", "Roomali Roti", 
        "Amritsari Stuffed Kulcha", "Aloo Paratha", "Paneer Paratha",

        // Cafe, Italian & Mexican
        "Margherita Pizza", "Farmhouse Pizza", "Peppy Paneer Pizza", "Pepperoni Pizza", "BBQ Chicken Pizza", 
        "White Sauce Pasta (Alfredo)", "Red Sauce Pasta (Arrabbiata)", "Pink Sauce Pasta", "Pesto Pasta", 
        "Veg Club Sandwich", "Grilled Cheese Sandwich", "Chicken Tikka Sandwich", "Veg Burger", 
        "Chicken Burger", "Crispy Zinger Burger", "Cheesy Loaded Nachos", "Veg Tacos", "Chicken Tacos", "Veg Quesadilla",

        // Beers & Draught
        "Kingfisher Premium", "Kingfisher Ultra", "Bira 91 White", "Bira 91 Blonde", "Corona Extra", 
        "Heineken", "Budweiser", "Budweiser Magnum", "Tuborg Green", "Carlsberg Elephant", 
        "Hoegaarden", "Simba Stout", "Draft Beer Pitcher", "Draft Beer Mug",

        // Spirits & Liquors (Whisky, Rum, Vodka, Gin, Tequila)
        "Old Monk Rum", "Bacardi White Rum", "Bacardi Black", "Captain Morgan", 
        "Smirnoff Vodka", "Absolut Vodka", "Magic Moments", "Grey Goose", 
        "Jameson Irish Whiskey", "Jack Daniel's", "Johnnie Walker Red Label", "Johnnie Walker Black Label", 
        "Ballantine's", "Chivas Regal 12", "Glenfiddich 12", "Blenders Pride", "Royal Stag", 
        "Imperial Blue", "Teachers Highland Cream", "100 Pipers", "Antiquity Blue", "Amrut Single Malt", 
        "Greater Than Gin", "Bombay Sapphire Gin", "Tanqueray Gin", "Jose Cuervo Tequila", 
        "Tequila Shot", "Baileys Irish Cream", "Jägermeister", "Jäger Bomb Shot",

        // Cocktails & Wines
        "Long Island Iced Tea (LIIT)", "Classic Mojito", "Whiskey Sour", "Old Fashioned", 
        "Cosmopolitan", "Margarita", "Bloody Mary", "Kamikaze Shot", "Sangria Pitcher", 
        "Sula Shiraz Red Wine", "Sula Sauvignon White Wine", "Jacob's Creek Wine",

        // Beverages, Shakes & Cafe Drinks
        "Fresh Lime Soda", "Fresh Lime Water", "Masala Lemonade", "Virgin Mojito", "Blue Lagoon", 
        "Iced Tea", "Peach Iced Tea", "Cold Coffee", "Cold Coffee with Ice Cream", "Hot Coffee", 
        "Masala Chai", "Filter Coffee", "Green Tea", "Sweet Lassi", "Salted Lassi", "Mango Lassi", 
        "Chocolate Milkshake", "Oreo Milkshake", "KitKat Milkshake", "Strawberry Milkshake", 
        "Cold Drink (Coke/Pepsi)", "Mineral Water",

        // Desserts & Sweets
        "Gulab Jamun (2 Pcs)", "Rasgulla (2 Pcs)", "Rasmalai (2 Pcs)", "Jalebi with Rabri", 
        "Sizzling Brownie with Ice Cream", "Vanilla Ice Cream", "Chocolate Ice Cream", "Matka Kulfi", 
        "Gajar Ka Halwa", "Moong Dal Halwa", "Tiramisu", "New York Cheesecake"
    )

    fun getSuggestions(query: String, limit: Int = 7): List<String> {
        if (query.trim().length < 2) return emptyList()
        val cleanQuery = query.trim().lowercase()
        return DISHES.filter { it.lowercase().contains(cleanQuery) }.take(limit)
    }
}
