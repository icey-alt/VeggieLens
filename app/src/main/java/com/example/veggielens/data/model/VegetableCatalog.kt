package com.example.veggielens.data.model

object VegetableCatalog {
    const val ALL_CATEGORY = "全部"

    val categories = listOf(ALL_CATEGORY, "十字花科", "茄科", "瓜类", "葱蒜类", "叶菜类", "其他")

    private val chineseNames = mapOf(
        "bean" to "豆角",
        "bitter gourd" to "苦瓜",
        "bottle gourd" to "瓠瓜",
        "brinjal" to "茄子",
        "eggplant" to "茄子",
        "broccoli" to "西兰花",
        "cabbage" to "卷心菜",
        "capsicum" to "甜椒",
        "carrot" to "胡萝卜",
        "cauliflower" to "花椰菜",
        "corn" to "玉米",
        "cucumber" to "黄瓜",
        "galangal" to "高良姜",
        "garlic" to "大蒜",
        "ginger" to "生姜",
        "lettuce" to "生菜",
        "onion" to "洋葱",
        "onion red" to "红洋葱",
        "papaya" to "木瓜",
        "potato" to "土豆",
        "pumpkin" to "南瓜",
        "radish" to "萝卜",
        "spinach" to "菠菜",
        "sweet potato" to "红薯",
        "tomato" to "番茄"
    )

    fun chineseName(englishName: String): String =
        chineseNames[normalize(englishName)] ?: englishName

    fun category(englishName: String): String = when (normalize(englishName)) {
        "broccoli", "cabbage", "cauliflower", "radish" -> "十字花科"
        "tomato", "eggplant", "brinjal", "capsicum", "potato" -> "茄科"
        "cucumber", "pumpkin", "bitter gourd", "bottle gourd" -> "瓜类"
        "onion", "onion red", "garlic" -> "葱蒜类"
        "spinach", "lettuce" -> "叶菜类"
        else -> "其他"
    }

    fun aliases(englishName: String): Set<String> {
        val normalizedName = normalize(englishName)
        val extraAliases = when (normalizedName) {
            "bean" -> setOf("green bean", "snap bean", "string bean")
            "bitter gourd" -> setOf("bitter melon")
            "bottle gourd" -> setOf("calabash")
            "brinjal" -> setOf("eggplant", "aubergine")
            "cabbage" -> setOf("head cabbage")
            "capsicum" -> setOf("bell pepper", "sweet pepper")
            "corn" -> setOf("maize", "ear of corn")
            "onion red" -> setOf("red onion")
            "papaya" -> setOf("pawpaw")
            "sweet potato" -> setOf("yam")
            else -> emptySet()
        }
        return extraAliases + normalizedName
    }

    fun normalize(value: String): String = value
        .lowercase()
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .replace(Regex("\\s+"), " ")

}