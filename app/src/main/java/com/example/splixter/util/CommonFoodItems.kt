package com.example.splixter.util

object CommonFoodItems {
    val DISHES: List<String> = listOf(
        // Indian & Imported Beers
        "Kingfisher Premium", "Kingfisher Ultra", "Kingfisher Ultra Maxx", "Kingfisher Storm", "Kingfisher Strong",
        "Bira 91 White", "Bira 91 Blonde", "Bira 91 Boom", "Bira 91 Gold", "Bira 91 Malabar", "Corona Extra", 
        "Heineken", "Budweiser", "Budweiser Magnum", "Budweiser Light", "Tuborg Green", "Tuborg Strong", 
        "Carlsberg Elephant", "Carlsberg Smooth", "Hoegaarden", "Simba Stout", "Simba Wit", 
        "White Owl Spark", "BroCode", "Godfather Super Strong", "Haywards 5000", "Knock Out", 
        "Lone Wolf Strong", "Miller High Life", "Stella Artois", "Breezer Cranberry", "Breezer Blackberry",
        "Draft Beer Glass", "Draft Beer Pitcher", "Draft Beer Tower",

        // Indian IMFL Whiskies
        "Blenders Pride", "Blenders Pride Reserve", "Royal Stag", "Royal Stag Barrel Select", 
        "Imperial Blue", "McDowell's No. 1", "Officer's Choice", "Antiquity Blue", "Antiquity Rare", 
        "Signature Rare", "Signature Premier", "Director's Special", "All Seasons", "Bagpiper", "8PM Whiskey",

        // Scotch & Imported Whiskies
        "Jameson Irish Whiskey", "Jack Daniel's Old No. 7", "Johnnie Walker Red Label", 
        "Johnnie Walker Black Label", "Johnnie Walker Double Black", "Johnnie Walker Gold Label", "Johnnie Walker Blue Label",
        "Ballantine's Finest", "Chivas Regal 12", "Chivas Regal 18", "Glenfiddich 12", "Glenfiddich 15",
        "Glenfiddich 18", "The Glenlivet 12", "The Glenlivet 15", "Glenmorangie Original", "Talisker 10", 
        "Laphroaig 10", "Monkey Shoulder", "Teachers Highland Cream", "Teachers 50", 
        "100 Pipers", "Black & White", "Dewar's White Label", "VAT 69",

        // Indian Premium Single Malts
        "Amrut Fusion Single Malt", "Amrut Indian Single Malt", "Indri Trini Single Malt", 
        "Rampur Indian Single Malt", "Paul John Brilliance", "Paul John Edited", "Kamet Single Malt",

        // Rum Brands
        "Old Monk Rum", "Old Monk Supreme", "Bacardi Superior White Rum", "Bacardi Black Rum", 
        "Bacardi Limón", "Bacardi Gold", "Bacardi Añejo", "Captain Morgan Spiced Rum", "Malibu Coconut Rum", "Havana Club 7",

        // Vodka Brands
        "Smirnoff Triple Distilled", "Smirnoff Green Apple", "Smirnoff Orange", "Absolut Blue Vodka", 
        "Absolut Citron", "Absolut Raspberry", "Absolut Mandarin", "Magic Moments Vodka", "Magic Moments Remix", 
        "Grey Goose Vodka", "Ketel One Vodka", "Belvedere Vodka", "Cîroc Vodka", "Romanov Vodka",

        // Gin Brands
        "Greater Than Gin", "Stranger & Sons Gin", "Jaisalmer Indian Craft Gin", "Terai India Dry Gin", 
        "Bombay Sapphire Gin", "Tanqueray London Dry Gin", "Beefeater Gin", "Hendrick's Gin", 
        "Monkey 47 Gin", "Gordon's London Dry Gin",

        // Tequila & Specialty Liqueurs
        "Jose Cuervo Especial Tequila", "Sierra Tequila Silver", "Don Julio Blanco Tequila", 
        "Patrón Silver Tequila", "Baileys Irish Cream", "Jägermeister", "Kahlúa Coffee Liqueur", 
        "Cointreau", "Sambuca", "Triple Sec", "Campari", "Fireball Cinnamon Whiskey",

        // Popular Cocktails & Shots
        "Long Island Iced Tea (LIIT)", "Classic Mojito", "Whiskey Sour", "Old Fashioned", 
        "Cosmopolitan", "Margarita", "Bloody Mary", "Sex on the Beach", "Piña Colada", 
        "Daiquiri", "Manhattan", "Espresso Martini", "Sangria Red Pitcher", "Sangria White Pitcher", 
        "Jäger Bomb Shot", "Tequila Shot with Salt & Lemon", "Kamikaze Shot", "B52 Layered Shot",

        // Wines & Champagne
        "Sula Shiraz Red Wine", "Sula Sauvignon Blanc", "Sula Chenin Blanc", "Sula Zinfandel Rosé", 
        "Fratelli Classic Red Wine", "Fratelli Sette", "Grover Zampa La Réserve", 
        "Jacob's Creek Shiraz Cabernet", "Yellow Tail Shiraz", "Barefoot Moscato", "Chandon Brut Sparkling", "Moët & Chandon Champagne",

        // South Indian Delicacies
        "Masala Dosa", "Plain Dosa", "Onion Rava Dosa", "Mysore Masala Dosa", "Ghee Roast Dosa", 
        "Cheese Burst Dosa", "Paper Masala Dosa", "Schezwan Dosa", "Idli Sambhar", "Vada Sambhar", 
        "Medu Vada", "Plain Uttapam", "Onion Tomato Uttapam", "Mix Veg Uttapam", "Appam with Stew", 
        "Malabar Parotta", "Lemon Rice", "Curd Rice", "Tamarind Rice (Puliogare)", "Hyderabadi Upma", "Ven Pongal", "Pesarattu",

        // Indian Street Food & Chaat
        "Pani Puri / Golgappe", "Sev Puri", "Bhel Puri", "Dahi Puri", "Aloo Tikki Chaat", 
        "Samosa Chaat", "Papdi Chaat", "Raj Kachori", "Pav Bhaji", "Cheese Pav Bhaji", 
        "Vada Pav", "Misal Pav", "Chole Bhature", "Chole Kulche", "Kachori Sabzi", "Veg Kathi Roll", 
        "Paneer Roll", "Chicken Kathi Roll", "Egg Roll", "Double Egg Chicken Roll",

        // Starters & Kebabs - Vegetarian
        "Paneer Tikka", "Paneer Malai Tikka", "Paneer Achari Tikka", "Paneer Hariyali Tikka", "Paneer Pahadi Tikka", 
        "Paneer Lasooni Tikka", "Paneer Angara Tikka", "Paneer Kurkure", "Paneer 65", "Paneer Majestic", "Paneer Finger", "Chili Paneer", 
        "Tandoori Mushroom", "Mushroom Tikka", "Malai Mushroom Tikka", "Bharwan Mushroom", "Mushroom Kurkure", 
        "Tandoori Soya Chaap", "Malai Soya Chaap", "Afghani Soya Chaap", "Achari Soya Chaap", "Masala Soya Chaap Tikka", "Kurkure Soya Chaap", 
        "Hara Bhara Kebab", "Dahi Ke Kebab", "Dahi Ke Sholey", "Veg Seekh Kebab", "Veg Galouti Kebab", "Veg Shami Kebab", "Veg Cutlet", 
        "Corn Cheese Kebab", "Corn Tikki", "Aloo Tikki", "Tandoori Aloo", "Bharwan Aloo Tikka", 
        "Crispy Corn", "Crispy Baby Corn", "Sesame Fried Baby Corn", "Baby Corn Cigars", "Veg Cheese Cigars", "Veg Crispy", 
        "Veg Gold Coins", "Veg Bullets", "Gobi 65", "Tandoori Gobi", "Crispy Lotus Stem", "Onion Rings",

        // Starters & Kebabs - Non-Vegetarian (Chicken, Mutton, Seafood & Egg)
        "Tandoori Chicken (Half)", "Tandoori Chicken (Full)", "Tandoori Chicken Quarter", "Tangdi Kebab", 
        "Chicken Tikka", "Chicken Malai Tikka", "Chicken Achari Tikka", "Chicken Hariyali Tikka", "Chicken Pahadi Tikka", 
        "Chicken Lasooni Tikka", "Chicken Angara Tikka", "Chicken Kasturi Tikka", "Chicken Banjara Tikka", 
        "Chicken Seekh Kebab", "Chicken Reshmi Kebab", "Chicken Kalmi Kebab", "Chicken Rozali Kebab", "Chicken Shami Kebab", "Galouti Kebab", "Kakori Kebab", 
        "Chicken 65", "Chicken Majestic", "Chicken Wings", "BBQ Chicken Wings", "Peri Peri Chicken Wings", "Crispy Chicken Wings", 
        "Chicken Popcorn", "Crispy Chicken Strips", "Chicken Nuggets", "Chicken Ghee Roast", "Chicken Sukka", "Chicken Kanti", "Chicken Pepper Fry", 
        "Mutton Seekh Kebab", "Mutton Galouti Kebab", "Mutton Kakori Kebab", "Mutton Shami Kebab", "Mutton Boti Kebab", 
        "Tandoori Mutton Chaap", "Mutton Chaap Fry", "Mutton Ghee Roast", "Mutton Sukka", "Mutton Pepper Fry", "Mutton Cutlet", 
        "Fish Amritsari", "Fish Tikka", "Fish Malai Tikka", "Fish Finger", "Fish Fry", "Rava Fish Fry", "Tandoori Pomfret", "Apollo Fish", "Fish 65", 
        "Prawns Koliwada", "Prawns Fry", "Tandoori Prawns", "Prawns Tikka", "Butter Garlic Prawns", "Golden Fried Prawns", "Prawns 65", "Loose Prawns", "Crab Fry", 
        "Egg 65", "Chili Egg Dry", "Egg Manchurian Dry", "Boiled Egg Fry", "Egg Cutlet", "Deviled Eggs",

        // Finger Food, Fries, Momos & Dimsums
        "French Fries", "Peri Peri Fries", "Cheesy Fries", "Loaded Veg Fries", "Potato Wedges", "Cheese Garlic Bread", "Stuffed Garlic Bread", "Cheese Balls", "Cheese Corn Balls", "Cheesy Jalapeño Poppers", 
        "Veg Momos", "Chicken Momos", "Fried Veg Momos", "Fried Chicken Momos", "Steamed Veg Momos", "Steamed Chicken Momos", "Kurkure Momos", "Kurkure Veg Momos", "Kurkure Chicken Momos", "Cheese Corn Momos", "Steam Dimsums", "Chicken Dimsums",

        // Indo-Chinese Specialties
        "Veg Manchurian Dry", "Veg Manchurian Gravy", "Chicken Manchurian Dry", "Chicken Manchurian Gravy", 
        "Chili Paneer Dry", "Chili Paneer Gravy", "Chili Chicken Dry", "Chili Chicken Gravy", 
        "Chili Mushroom Dry", "Chili Baby Corn Dry", "Chili Fish Dry", "Chili Prawns Dry", 
        "Honey Chili Potato", "Gobi Manchurian", "Chili Mushroom", "Chili Baby Corn", 
        "Drums of Heaven", "Chicken Lollipop", "Chicken Lollipop Oil Fry", "Chicken Lollipop Masala Dry", "Dragon Chicken", "Thread Chicken", 
        "Crispy Chili Garlic Prawns", "Veg Spring Rolls", "Chicken Spring Rolls", 
        "Veg Manchow Soup", "Chicken Manchow Soup", "Hot and Sour Soup", "Veg Sweet Corn Soup", 
        "Chicken Sweet Corn Soup", "Lemon Coriander Soup", "Wonton Soup",

        // Noodles, Chowmein & Chop Suey
        "Veg Hakka Noodles", "Chicken Hakka Noodles", "Egg Hakka Noodles", "Schezwan Noodles", 
        "Chili Garlic Noodles", "Singapuri Noodles", "Veg American Chop Suey", "Chicken Chop Suey",

        // Fried Rice Specialties
        "Veg Fried Rice", "Egg Fried Rice", "Chicken Fried Rice", "Schezwan Fried Rice", 
        "Burnt Garlic Fried Rice", "Triple Schezwan Rice", "Mixed Fried Rice",

        // Main Course - Indian Veg
        "Paneer Butter Masala", "Kadhai Paneer", "Shahi Paneer", "Matar Paneer", "Palak Paneer", 
        "Paneer Do Pyaza", "Handi Paneer", "Paneer Lababdar", "Paneer Pasanda", "Dal Makhani", "Dal Tadka", "Dal Fry", "Chana Masala", 
        "Rajma Masala", "Malai Kofta", "Mix Vegetable", "Dum Aloo", "Bhindi Masala", 
        "Baingan Bharta", "Veg Kolhapuri", "Mushroom Masala", "Kaju Curry", "Methi Matar Malai", "Aloo Gobi", "Sarson Ka Saag", "Corn Palak",

        // Main Course - Indian Non-Veg & Mughlai
        "Butter Chicken", "Chicken Tikka Masala", "Kadhai Chicken", "Chicken Curry", "Chicken Kolhapuri", 
        "Chicken Chettinad", "Chicken Handi", "Chicken Mughlai", "Mutton Rogan Josh", "Mutton Curry", "Mutton Korma", "Mutton Do Pyaza",
        "Nalli Nihari", "Fish Curry", "Fish Tikka Masala", "Prawns Masala", "Prawns Curry", "Egg Curry", "Egg Bhurji", "Egg Masala",

        // Rice, Biryani & Pulao
        "Hyderabadi Chicken Biryani", "Hyderabadi Mutton Biryani", "Veg Dum Biryani", "Paneer Biryani", 
        "Egg Biryani", "Lucknowi Chicken Biryani", "Kolkata Biryani", "Steam Rice", "Jeera Rice", "Veg Pulao", "Matar Pulao", "Kashmiri Pulao",

        // Indian Breads
        "Tandoori Roti", "Butter Roti", "Plain Naan", "Butter Naan", "Garlic Naan", 
        "Cheese Garlic Naan", "Laccha Paratha", "Pudina Paratha", "Missi Roti", "Roomali Roti", 
        "Amritsari Stuffed Kulcha", "Aloo Paratha", "Paneer Paratha", "Gobi Paratha",

        // Cafe, Italian & Mexican
        "Margherita Pizza", "Farmhouse Pizza", "Peppy Paneer Pizza", "Pepperoni Pizza", "BBQ Chicken Pizza", "Exotic Veggie Pizza",
        "White Sauce Pasta (Alfredo)", "Red Sauce Pasta (Arrabbiata)", "Pink Sauce Pasta", "Pesto Pasta", 
        "Veg Club Sandwich", "Grilled Cheese Sandwich", "Chicken Tikka Sandwich", "Veg Burger", 
        "Chicken Burger", "Crispy Zinger Burger", "Cheesy Loaded Nachos", "Veg Tacos", "Chicken Tacos", "Veg Quesadilla",

        // Beverages, Shakes & Cafe Drinks
        "Fresh Lime Soda", "Fresh Lime Water", "Masala Lemonade", "Virgin Mojito", "Blue Lagoon", 
        "Iced Tea", "Peach Iced Tea", "Cold Coffee", "Cold Coffee with Ice Cream", "Hot Coffee", 
        "Masala Chai", "Filter Coffee", "Green Tea", "Earl Grey Tea", "Sweet Lassi", "Salted Lassi", "Mango Lassi", 
        "Chocolate Milkshake", "Oreo Milkshake", "KitKat Milkshake", "Strawberry Milkshake", "Mango Milkshake",
        "Cold Drink (Coke/Pepsi/Sprite)", "Mineral Water", "Red Bull Energy Drink",

        // Desserts & Sweets
        "Gulab Jamun", "Rasgulla", "Rasmalai", "Jalebi with Rabri", 
        "Sizzling Brownie with Ice Cream", "Vanilla Ice Cream", "Chocolate Ice Cream", "Butterscotch Ice Cream", "Mango Ice Cream", "Matka Kulfi", 
        "Falooda", "Gajar Ka Halwa", "Moong Dal Halwa", "Phirni", "Shahi Tukda", "Tiramisu", "New York Cheesecake", "Choco Lava Cake"
    )

    fun getSuggestions(query: String, limit: Int = 8): List<String> {
        if (query.trim().length < 2) return emptyList()
        val cleanQuery = query.trim().lowercase()
        return DISHES.filter { it.lowercase().contains(cleanQuery) }.take(limit)
    }
}
