package com.example.veggielens.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.veggielens.data.local.ScanHistoryDao
import com.example.veggielens.data.local.VegetableDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [VegetableEntity::class, ScanHistory::class],
    version = 1,
    exportSchema = false
)
abstract class VeggieDatabase : RoomDatabase() {
    abstract fun vegetableDao(): VegetableDao
    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: VeggieDatabase? = null

        fun getDatabase(context: Context): VeggieDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VeggieDatabase::class.java,
                    "veggie_database"
                )
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    database.vegetableDao().insertAll(getPresetVegetables())
                }
            }
        }
    }
}
private fun getPresetVegetables(): List<VegetableEntity> = listOf(
    VegetableEntity(
        name = "Bean",
        chineseName = "豆角",
        scientificName = "Phaseolus vulgaris",
        calories = "31 kcal",
        protein = "1.8g",
        carbs = "7g",
        fiber = "3.4g",
        assetImagePath = "vegetables/Bean.jpg"
    ),
    VegetableEntity(
        name = "Bitter_Gourd",
        chineseName = "苦瓜",
        scientificName = "Momordica charantia",
        calories = "17 kcal",
        protein = "1g",
        carbs = "3.7g",
        fiber = "2.8g",
        assetImagePath = "vegetables/Bitter_Gourd.jpg"
    ),
    VegetableEntity(
        name = "Bottle_Gourd",
        chineseName = "瓠瓜",
        scientificName = "Lagenaria siceraria",
        calories = "15 kcal",
        protein = "0.6g",
        carbs = "3.5g",
        fiber = "1.2g",
        assetImagePath = "vegetables/Bottle_Gourd.jpg"
    ),
    VegetableEntity(
        name = "Brinjal",
        chineseName = "茄子",
        scientificName = "Solanum melongena",
        calories = "25 kcal",
        protein = "1g",
        carbs = "6g",
        fiber = "3g",
        assetImagePath = "vegetables/Brinjal.jpg"
    ),
    VegetableEntity(
        name = "Broccoli",
        chineseName = "西兰花",
        scientificName = "Brassica oleracea",
        calories = "34 kcal",
        protein = "2.8g",
        carbs = "7g",
        fiber = "2.6g",
        assetImagePath = "vegetables/Broccoli.jpg"
    ),
    VegetableEntity(
        name = "Cabbage",
        chineseName = "卷心菜",
        scientificName = "Brassica oleracea var. capitata",
        calories = "25 kcal",
        protein = "1.3g",
        carbs = "5.8g",
        fiber = "2.5g",
        assetImagePath = "vegetables/Cabbage.jpg"
    ),
    VegetableEntity(
        name = "Capsicum",
        chineseName = "甜椒",
        scientificName = "Capsicum annuum",
        calories = "31 kcal",
        protein = "1g",
        carbs = "6g",
        fiber = "2.1g",
        assetImagePath = "vegetables/Capsicum.jpg"
    ),
    VegetableEntity(
        name = "Carrot",
        chineseName = "胡萝卜",
        scientificName = "Daucus carota",
        calories = "41 kcal",
        protein = "0.9g",
        carbs = "10g",
        fiber = "2.8g",
        assetImagePath = "vegetables/Carrot.jpg"
    ),
    VegetableEntity(
        name = "Cauliflower",
        chineseName = "花椰菜",
        scientificName = "Brassica oleracea var. botrytis",
        calories = "25 kcal",
        protein = "1.9g",
        carbs = "5g",
        fiber = "2g",
        assetImagePath = "vegetables/Cauliflower.jpg"
    ),
    VegetableEntity(
        name = "Corn",
        chineseName = "玉米",
        scientificName = "Zea mays",
        calories = "86 kcal",
        protein = "3.2g",
        carbs = "19g",
        fiber = "2.7g",
        assetImagePath = "vegetables/Corn.jpg"
    ),
    VegetableEntity(
        name = "Cucumber",
        chineseName = "黄瓜",
        scientificName = "Cucumis sativus",
        calories = "15 kcal",
        protein = "0.7g",
        carbs = "3.6g",
        fiber = "0.5g",
        assetImagePath = "vegetables/Cucumber.jpg"
    ),
    VegetableEntity(
        name = "Eggplant",
        chineseName = "茄子",
        scientificName = "Solanum melongena",
        calories = "25 kcal",
        protein = "1g",
        carbs = "6g",
        fiber = "3g",
        assetImagePath = "vegetables/Eggplant.jpg"
    ),
    VegetableEntity(
        name = "Galangal",
        chineseName = "高良姜",
        scientificName = "Alpinia galanga",
        calories = "46 kcal",
        protein = "1.5g",
        carbs = "10g",
        fiber = "2.4g",
        assetImagePath = "vegetables/Galangal.jpg"
    ),
    VegetableEntity(
        name = "Garlic",
        chineseName = "大蒜",
        scientificName = "Allium sativum",
        calories = "149 kcal",
        protein = "6.4g",
        carbs = "33g",
        fiber = "2.1g",
        assetImagePath = "vegetables/Garlic.jpg"
    ),
    VegetableEntity(
        name = "Ginger",
        chineseName = "生姜",
        scientificName = "Zingiber officinale",
        calories = "80 kcal",
        protein = "1.8g",
        carbs = "18g",
        fiber = "2g",
        assetImagePath = "vegetables/Ginger.jpg"
    ),
    VegetableEntity(
        name = "Lettuce",
        chineseName = "生菜",
        scientificName = "Lactuca sativa",
        calories = "15 kcal",
        protein = "1.4g",
        carbs = "2.9g",
        fiber = "1.3g",
        assetImagePath = "vegetables/Lettuce.jpg"
    ),
    VegetableEntity(
        name = "Onion",
        chineseName = "洋葱",
        scientificName = "Allium cepa",
        calories = "40 kcal",
        protein = "1.1g",
        carbs = "9.3g",
        fiber = "1.7g",
        assetImagePath = "vegetables/Onion.jpg"
    ),
    VegetableEntity(
        name = "Onion_Red",
        chineseName = "红洋葱",
        scientificName = "Allium cepa",
        calories = "40 kcal",
        protein = "1.1g",
        carbs = "9.3g",
        fiber = "1.7g",
        assetImagePath = "vegetables/Onion_Red.jpg"
    ),
    VegetableEntity(
        name = "Papaya",
        chineseName = "木瓜",
        scientificName = "Carica papaya",
        calories = "43 kcal",
        protein = "0.5g",
        carbs = "11g",
        fiber = "1.7g",
        assetImagePath = "vegetables/Papaya.jpg"
    ),
    VegetableEntity(
        name = "Potato",
        chineseName = "土豆",
        scientificName = "Solanum tuberosum",
        calories = "77 kcal",
        protein = "2g",
        carbs = "17g",
        fiber = "2.2g",
        assetImagePath = "vegetables/Potato.jpg"
    ),
    VegetableEntity(
        name = "Pumpkin",
        chineseName = "南瓜",
        scientificName = "Cucurbita maxima",
        calories = "26 kcal",
        protein = "1g",
        carbs = "6.5g",
        fiber = "0.5g",
        assetImagePath = "vegetables/Pumpkin.jpg"
    ),
    VegetableEntity(
        name = "Radish",
        chineseName = "萝卜",
        scientificName = "Raphanus sativus",
        calories = "16 kcal",
        protein = "0.7g",
        carbs = "3.4g",
        fiber = "1.6g",
        assetImagePath = "vegetables/Radish.jpg"
    ),
    VegetableEntity(
        name = "Spinach",
        chineseName = "菠菜",
        scientificName = "Spinacia oleracea",
        calories = "23 kcal",
        protein = "2.9g",
        carbs = "3.6g",
        fiber = "2.2g",
        assetImagePath = "vegetables/Spinach.jpg"
    ),
    VegetableEntity(
        name = "Sweet_Potato",
        chineseName = "红薯",
        scientificName = "Ipomoea batatas",
        calories = "86 kcal",
        protein = "1.6g",
        carbs = "20g",
        fiber = "3g",
        assetImagePath = "vegetables/Sweet_Potato.jpg"
    ),
    VegetableEntity(
        name = "Tomato",
        chineseName = "番茄",
        scientificName = "Solanum lycopersicum",
        calories = "18 kcal",
        protein = "0.9g",
        carbs = "3.9g",
        fiber = "1.2g",
        assetImagePath = "vegetables/Tomato.jpg"
    )
)