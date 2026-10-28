package com.example.veggielens.data.model

/**
 * 蔬菜的扩展资料：微量营养素 + 时令 / 挑选 / 保存 / 推荐做法。
 *
 * 只读的静态资料，放在代码里而不是 Room 表中，避免数据库结构变更和迁移。
 *
 * 营养素数值：参考 USDA FoodData Central（SR Legacy）生食可食部分，每 100 克，
 * 为近似值；没有可靠数据的蔬菜填 null，界面显示“暂无数据”。
 */
data class MicroNutrients(
    /** 维生素 C，mg */
    val vitaminC: Float,
    /** 维生素 A，μg RAE */
    val vitaminA: Float,
    /** 钾，mg */
    val potassium: Float,
    /** 钙，mg */
    val calcium: Float,
    /** 铁，mg */
    val iron: Float,
    /** 叶酸，μg */
    val folate: Float,
)

data class VegetableGuideInfo(
    val nutrients: MicroNutrients?,
    /** 时令 */
    val season: String,
    /** 挑选要点 */
    val selection: String,
    /** 保存方法 */
    val storage: String,
    /** 推荐做法（菜名） */
    val dishes: List<String>,
)

/** 营养素种类，含单位和营养素参考值（NRV，GB 28050-2011）。 */
enum class Nutrient(val unit: String, val nrv: Float) {
    VitaminC("mg", 100f),
    VitaminA("μg", 800f),
    Potassium("mg", 2000f),
    Calcium("mg", 800f),
    Iron("mg", 15f),
    Folate("μg", 400f);

    fun amountIn(n: MicroNutrients): Float = when (this) {
        VitaminC -> n.vitaminC
        VitaminA -> n.vitaminA
        Potassium -> n.potassium
        Calcium -> n.calcium
        Iron -> n.iron
        Folate -> n.folate
    }

    /** 每 100 克占营养素参考值的百分比 */
    fun percentOfNrv(n: MicroNutrients): Int = (amountIn(n) / nrv * 100).toInt()

    companion object {
        /** 每 100 克达到 NRV 的 30% 及以上，可称为“富含”（参照 GB 28050 的含量声称条件）。 */
        const val RICH_THRESHOLD_PERCENT = 30
    }
}

object VegetableGuide {

    fun forName(englishName: String): VegetableGuideInfo? =
        guide[VegetableCatalog.normalize(englishName)]

    /** 达到“富含”标准的营养素，按占比从高到低。 */
    fun richNutrients(n: MicroNutrients): List<Nutrient> {
        return Nutrient.entries
            .filter { it.percentOfNrv(n) >= Nutrient.RICH_THRESHOLD_PERCENT }
            .sortedByDescending { it.percentOfNrv(n) }
    }

    private fun n(c: Float, a: Float, k: Float, ca: Float, fe: Float, fol: Float) =
        MicroNutrients(c, a, k, ca, fe, fol)

    private val eggplant = VegetableGuideInfo(
        nutrients = n(2.2f, 1f, 229f, 9f, 0.23f, 22f),
        season = "夏秋（6–9 月）",
        selection = "表皮紫黑发亮、无伤斑，手握有弹性；萼片新鲜挺拔的较嫩。",
        storage = "不宜久放，用纸包好放阴凉处或冷藏，2–3 天内吃完；切开后易氧化变黑。",
        dishes = listOf("鱼香茄子", "蒜蓉蒸茄子", "地三鲜"),
    )

    private val onion = VegetableGuideInfo(
        nutrients = n(7.4f, 0f, 146f, 23f, 0.21f, 19f),
        season = "春夏收获，耐储存，全年可买",
        selection = "外皮干燥完整有光泽，球体坚实、颈部紧闭，没有发芽和软烂。",
        storage = "放在阴凉通风干燥处，避免和土豆放一起；切开后包好冷藏。",
        dishes = listOf("洋葱炒蛋", "洋葱炒牛肉", "凉拌洋葱"),
    )

    private val guide: Map<String, VegetableGuideInfo> = mapOf(
        "bean" to VegetableGuideInfo(
            nutrients = n(12.2f, 35f, 211f, 37f, 1.03f, 33f),
            season = "夏秋（6–9 月）",
            selection = "豆荚饱满、颜色翠绿，折断时清脆有汁；豆粒不过分鼓起的更嫩。",
            storage = "装保鲜袋冷藏 3–5 天；也可焯水后分装冷冻。",
            dishes = listOf("干煸豆角", "豆角焖面", "蒜蓉豆角"),
        ),
        "bitter gourd" to VegetableGuideInfo(
            nutrients = n(84f, 24f, 296f, 19f, 0.43f, 72f),
            season = "夏季（6–8 月）",
            selection = "表面颗粒饱满、颜色青绿有光泽；颗粒越大越饱满，通常苦味越淡。",
            storage = "用纸包好放冷藏，3–4 天内吃完；变黄说明已过熟。",
            dishes = listOf("苦瓜炒蛋", "凉拌苦瓜", "苦瓜酿肉"),
        ),
        "bottle gourd" to VegetableGuideInfo(
            nutrients = n(10.1f, 1f, 150f, 26f, 0.2f, 6f),
            season = "夏季（6–8 月）",
            selection = "表皮嫩绿带细绒毛，指甲能轻松掐入的较嫩；手感沉、无伤痕。",
            storage = "整只放阴凉处可存放一周左右；切开后包好冷藏尽快食用。",
            dishes = listOf("清炒瓠瓜", "瓠瓜虾仁汤", "瓠瓜鸡蛋饺子"),
        ),
        "brinjal" to eggplant,
        "eggplant" to eggplant,
        "broccoli" to VegetableGuideInfo(
            nutrients = n(89.2f, 31f, 316f, 47f, 0.73f, 63f),
            season = "秋冬至早春（10 月–次年 4 月）",
            selection = "花球紧密、颜色深绿均匀，没有发黄开花；茎部切口新鲜不空心。",
            storage = "装保鲜袋冷藏，3–5 天内吃完；花球发黄说明不新鲜。",
            dishes = listOf("蒜蓉西兰花", "西兰花炒虾仁", "白灼西兰花"),
        ),
        "cabbage" to VegetableGuideInfo(
            nutrients = n(36.6f, 5f, 170f, 40f, 0.47f, 43f),
            season = "全年可买，冬春最佳",
            selection = "叶球紧实、拿起来压手，外叶翠绿有光泽，切口新鲜。",
            storage = "整颗用保鲜膜包好冷藏可放 1–2 周；吃时从外层逐片剥取。",
            dishes = listOf("手撕包菜", "醋溜白菜", "卷心菜炒粉丝"),
        ),
        "capsicum" to VegetableGuideInfo(
            nutrients = n(127.7f, 157f, 211f, 7f, 0.43f, 46f),
            season = "夏秋（6–10 月）",
            selection = "果形周正、表皮光亮紧绷，果蒂新鲜翠绿，拿起来有分量。",
            storage = "不洗直接装袋冷藏，可放约一周；切开后尽快食用。",
            dishes = listOf("彩椒炒肉", "虎皮青椒", "甜椒沙拉"),
        ),
        "carrot" to VegetableGuideInfo(
            nutrients = n(5.9f, 835f, 320f, 33f, 0.3f, 19f),
            season = "秋冬（10 月–次年 2 月）",
            selection = "根形顺直、表皮光滑、颜色橙红均匀，芯细的口感更甜。",
            storage = "切掉缨子后装袋冷藏，可放 2–3 周。",
            dishes = listOf("胡萝卜炒蛋", "胡萝卜炖牛腩", "胡萝卜丝饼"),
        ),
        "cauliflower" to VegetableGuideInfo(
            nutrients = n(48.2f, 0f, 299f, 22f, 0.42f, 57f),
            season = "秋冬（10 月–次年 3 月）",
            selection = "花球洁白或乳白、紧实无黑斑，外叶鲜绿包裹。",
            storage = "花球朝下装袋冷藏，约一周内吃完。",
            dishes = listOf("干锅花菜", "番茄炒花菜", "花菜炒肉片"),
        ),
        "corn" to VegetableGuideInfo(
            nutrients = n(6.8f, 9f, 270f, 2f, 0.52f, 42f),
            season = "夏秋（7–9 月）",
            selection = "苞叶翠绿紧包，玉米须湿润呈褐色；籽粒饱满整齐，掐开有浆。",
            storage = "带苞叶冷藏，1–2 天内吃最甜；也可煮熟后冷冻。",
            dishes = listOf("清水煮玉米", "松仁玉米", "玉米排骨汤"),
        ),
        "cucumber" to VegetableGuideInfo(
            nutrients = n(2.8f, 5f, 147f, 16f, 0.28f, 7f),
            season = "春夏（4–8 月）",
            selection = "颜色深绿、带刺有小疙瘩，顶花还在的更新鲜；粗细均匀、手感硬挺。",
            storage = "用纸包好放冷藏 3–5 天，避免和番茄、苹果放一起。",
            dishes = listOf("拍黄瓜", "黄瓜炒蛋", "黄瓜蘸酱"),
        ),
        "galangal" to VegetableGuideInfo(
            nutrients = null,
            season = "秋冬采收",
            selection = "根茎饱满坚实，表皮红棕有光泽，断面淡黄、香气浓。",
            storage = "放阴凉干燥处可存放较久；切片晾干后密封保存。",
            dishes = listOf("冬阴功汤", "南洋咖喱", "炖卤调味"),
        ),
        "garlic" to VegetableGuideInfo(
            nutrients = n(31.2f, 0f, 401f, 181f, 1.7f, 3f),
            season = "新蒜 5–6 月，干蒜全年可买",
            selection = "蒜头大而饱满、外皮干燥完整，按压坚实，没有发芽。",
            storage = "放网袋挂在阴凉通风处；剥好的蒜瓣密封冷藏，尽快用完。",
            dishes = listOf("蒜蓉粉丝蒸扇贝", "糖醋蒜", "蒜香排骨"),
        ),
        "ginger" to VegetableGuideInfo(
            nutrients = n(5f, 0f, 415f, 16f, 0.6f, 11f),
            season = "嫩姜夏秋，老姜全年",
            selection = "姜块饱满坚实、表皮完整，没有软烂和霉斑。",
            storage = "用纸包好装袋冷藏，或埋在干燥沙土里；腐烂的姜不要食用。",
            dishes = listOf("姜撞奶", "姜丝炒肉", "姜汁调味"),
        ),
        "lettuce" to VegetableGuideInfo(
            nutrients = n(9.2f, 370f, 194f, 36f, 0.86f, 38f),
            season = "春秋（3–5 月、9–11 月）",
            selection = "叶片鲜嫩舒展、颜色鲜亮，切口白净不发红，没有烂叶。",
            storage = "用厨房纸包好装袋冷藏，2–3 天内吃完。",
            dishes = listOf("蚝油生菜", "生菜沙拉", "生菜包肉"),
        ),
        "onion" to onion,
        "onion red" to onion,
        "papaya" to VegetableGuideInfo(
            nutrients = n(60.9f, 47f, 182f, 20f, 0.25f, 37f),
            season = "夏秋（7–10 月）",
            selection = "生食选表皮大部分转黄、按压微软的；做菜选青木瓜。",
            storage = "未熟放常温催熟，熟后冷藏 2–3 天内吃完。",
            dishes = listOf("木瓜炖奶", "凉拌青木瓜", "木瓜排骨汤"),
        ),
        "potato" to VegetableGuideInfo(
            nutrients = n(19.7f, 0f, 425f, 12f, 0.81f, 15f),
            season = "全年可买，春秋两季收获",
            selection = "表皮光滑、个头均匀坚实，没有发芽、发绿和破损。",
            storage = "放阴凉避光通风处；发芽或表皮发绿的部分含较多龙葵素，应挖掉或丢弃。",
            dishes = listOf("酸辣土豆丝", "土豆炖牛肉", "土豆泥"),
        ),
        "pumpkin" to VegetableGuideInfo(
            nutrients = n(9f, 426f, 340f, 21f, 0.8f, 16f),
            season = "夏秋（7–10 月），耐储存",
            selection = "外皮坚硬、纹路清晰，拿起来沉；瓜蒂干燥坚硬的更成熟。",
            storage = "整只放阴凉处可存数周；切开后去瓤包好冷藏。",
            dishes = listOf("南瓜粥", "蒸南瓜", "南瓜饼"),
        ),
        "radish" to VegetableGuideInfo(
            nutrients = n(14.8f, 0f, 233f, 25f, 0.34f, 25f),
            season = "秋冬（10 月–次年 1 月）",
            selection = "表皮光滑、拿起来沉，敲击声音清脆的不空心。",
            storage = "去掉缨子后装袋冷藏，可放 1–2 周。",
            dishes = listOf("萝卜炖排骨", "凉拌萝卜丝", "萝卜丝饼"),
        ),
        "spinach" to VegetableGuideInfo(
            nutrients = n(28.1f, 469f, 558f, 99f, 2.71f, 194f),
            season = "秋冬春（10 月–次年 4 月）",
            selection = "叶片深绿肥厚、挺拔不蔫，根部带红色的较新鲜。",
            storage = "用纸包好竖放冷藏，2–3 天内吃完；做菜前焯水可去掉部分草酸。",
            dishes = listOf("蒜蓉菠菜", "菠菜猪肝汤", "凉拌菠菜"),
        ),
        "sweet potato" to VegetableGuideInfo(
            nutrients = n(2.4f, 709f, 337f, 30f, 0.61f, 11f),
            season = "秋冬（9 月–次年 1 月）",
            selection = "外形完整、表皮光滑，没有黑斑和发芽。",
            storage = "放阴凉通风处，不要冷藏；出现黑斑或苦味的不要食用。",
            dishes = listOf("烤红薯", "红薯粥", "拔丝红薯"),
        ),
        "tomato" to VegetableGuideInfo(
            nutrients = n(13.7f, 42f, 237f, 10f, 0.27f, 15f),
            season = "夏秋（6–9 月）",
            selection = "颜色红润均匀、果蒂新鲜，捏起来有弹性；避免青肩或有裂口的。",
            storage = "成熟的放冷藏 2–3 天；未熟透的放常温，蒂朝下摆放。",
            dishes = listOf("番茄炒蛋", "番茄牛腩", "番茄蛋汤"),
        ),
    )
}
