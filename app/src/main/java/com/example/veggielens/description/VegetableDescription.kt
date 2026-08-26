package com.example.veggielens.description

import com.example.veggielens.network.DeepSeekApi
import com.example.veggielens.network.DeepSeekMessage
import com.example.veggielens.network.DeepSeekRequest
import com.example.veggielens.security.ApiKeyStore
import kotlinx.coroutines.CancellationException

enum class DescriptionSource {
    DEEPSEEK,
    LOCAL;

    companion object {
        fun fromStorage(value: String?): DescriptionSource =
            entries.firstOrNull { it.name == value } ?: LOCAL
    }
}

data class VegetableDescription(
    val text: String,
    val source: DescriptionSource
)

interface DescriptionProvider {
    suspend fun describe(englishName: String, chineseName: String): VegetableDescription
}

class DefaultDescriptionProvider(
    private val api: DeepSeekApi,
    private val apiKeyStore: ApiKeyStore
) : DescriptionProvider {
    override suspend fun describe(englishName: String, chineseName: String): VegetableDescription {
        val localDescription = VegetableDescription(
            text = LocalVegetableDescriptions.forName(englishName),
            source = DescriptionSource.LOCAL
        )
        val apiKey = apiKeyStore.read()
        if (apiKey.isBlank()) return localDescription

        return try {
            val response = api.getChatCompletion(
                authorization = "Bearer $apiKey",
                request = DeepSeekRequest(
                    messages = listOf(
                        DeepSeekMessage(
                            role = "system",
                            content = """
                                你是食品营养科普助手。请用中文介绍蔬菜的常见营养成分和日常食用方式，
                                控制在80字以内。不要声称能够治疗、预防疾病或替代医疗建议；
                                如果信息不确定，使用“通常”“可作为”等审慎表达。
                            """.trimIndent()
                        ),
                        DeepSeekMessage(
                            role = "user",
                            content = "介绍蔬菜：$chineseName ($englishName)"
                        )
                    )
                )
            )
            val content = response.choice.firstOrNull()?.message?.content?.trim()
            if (content.isNullOrBlank()) {
                localDescription
            } else {
                VegetableDescription(content, DescriptionSource.DEEPSEEK)
            }
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            localDescription
        }
    }
}

object LocalVegetableDescriptions {
    fun forName(vegetableName: String): String =
        descriptions[normalize(vegetableName)] ?: DEFAULT_DESCRIPTION

    private fun normalize(value: String): String = value
        .lowercase()
        .replace('_', ' ')
        .trim()
        .replace(Regex("\\s+"), " ")

    private val descriptions = mapOf(
        "bean" to "豆角含有膳食纤维、维生素C和植物蛋白，适合充分加热后作为日常蔬菜食用。",
        "bitter gourd" to "苦瓜含有维生素C和膳食纤维，味道清苦，可通过焯水或搭配其他食材改善口感。",
        "bottle gourd" to "瓠瓜水分较多、能量较低，可炒食或煮汤；食用前应确认没有异常苦味。",
        "brinjal" to "茄子含有膳食纤维和多种植物化合物，烹饪时控制用油量更有利于均衡饮食。",
        "eggplant" to "茄子含有膳食纤维和多种植物化合物，烹饪时控制用油量更有利于均衡饮食。",
        "broccoli" to "西兰花含有维生素C、维生素K、叶酸和膳食纤维，可通过蒸、炒等方式食用。",
        "cabbage" to "卷心菜含有维生素C、维生素K和膳食纤维，可用于凉拌、清炒或炖煮。",
        "capsicum" to "甜椒含有维生素C和类胡萝卜素，口感清脆，可生食或与其他蔬菜搭配烹饪。",
        "carrot" to "胡萝卜富含β-胡萝卜素和膳食纤维，与少量含脂肪食物搭配有利于类胡萝卜素吸收。",
        "cauliflower" to "花椰菜含有维生素C、叶酸和膳食纤维，适合蒸煮或短时间翻炒。",
        "corn" to "玉米提供碳水化合物、膳食纤维和类胡萝卜素，可作为主食的一部分食用。",
        "cucumber" to "黄瓜水分含量较高、口感清爽，可凉拌或直接食用，注意清洗干净。",
        "galangal" to "高良姜具有辛香风味，通常作为调味食材少量使用，可用于汤、咖喱等菜肴。",
        "garlic" to "大蒜含有含硫化合物，常用作调味食材；肠胃敏感人群可根据耐受程度控制用量。",
        "ginger" to "生姜具有辛辣香气，常用于去腥和调味，通常以少量加入菜肴或饮品。",
        "lettuce" to "生菜含有叶酸、维生素K和膳食纤维，生食时应充分清洗，也可快速炒熟。",
        "onion" to "洋葱含有膳食纤维和多种植物化合物，可生食、炒制或用于汤和酱汁。",
        "onion red" to "红洋葱含有膳食纤维和花青素类物质，可用于沙拉、炒菜或腌渍。",
        "papaya" to "木瓜含有维生素C、类胡萝卜素和膳食纤维，成熟后可直接食用。",
        "potato" to "土豆主要提供碳水化合物，也含钾和维生素C，可作为主食的一部分，避免食用发芽土豆。",
        "pumpkin" to "南瓜含有β-胡萝卜素、膳食纤维和碳水化合物，可蒸煮、烘烤或用于煮粥。",
        "radish" to "萝卜含有维生素C和膳食纤维，可凉拌、炒食或煮汤，辛辣程度因品种而异。",
        "spinach" to "菠菜含有叶酸、维生素K和类胡萝卜素，焯水后食用可减少部分草酸。",
        "sweet potato" to "红薯提供碳水化合物、膳食纤维和β-胡萝卜素，可作为主食的一部分。",
        "tomato" to "番茄含有维生素C和番茄红素，可生食或熟食；加热并搭配少量油脂有利于番茄红素利用。"
    )

    private const val DEFAULT_DESCRIPTION =
        "蔬菜通常能提供维生素、矿物质和膳食纤维，建议搭配不同种类食物保持饮食多样化。"
}