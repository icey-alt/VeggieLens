package com.example.veggielens.viewmodel

import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.data.model.VegetableEntity

data class ClassificationCandidate(val label: String, val score: Float)

data class VegetableMatch(val vegetable: VegetableEntity, val confidence: Int)

object VegetableMatcher {
    fun findBestMatch(
        candidates: List<ClassificationCandidate>,
        vegetables: List<VegetableEntity>,
        minimumConfidence: Int
    ): VegetableMatch? {
        return candidates
            .sortedByDescending { it.score }
            .firstNotNullOfOrNull { candidate ->
                val vegetable = matchLabel(candidate.label, vegetables)
                val confidence = (candidate.score * 100).toInt()
                if (vegetable != null && confidence >= minimumConfidence) {
                    VegetableMatch(vegetable, confidence)
                } else {
                    null
                }
            }
    }

    /** 所有达到阈值的匹配，按匹配度降序、每种蔬菜只保留一次。 */
    fun findMatches(
        candidates: List<ClassificationCandidate>,
        vegetables: List<VegetableEntity>,
        minimumConfidence: Int
    ): List<VegetableMatch> {
        return candidates
            .sortedByDescending { it.score }
            .mapNotNull { candidate ->
                findBestMatch(listOf(candidate), vegetables, minimumConfidence)
            }
            .distinctBy { it.vegetable.name }
    }

    /**
     * 把模型标签对应到蔬菜。
     *
     * 依次尝试：本名完全相同 → 别名完全相同 → “包含”匹配。
     * 顺序很重要：如果直接用“包含”并按列表顺序取第一个，
     * “onion red”会先匹配到“onion”、“sweet potato”会先匹配到“potato”，
     * 导致红洋葱永远被认成洋葱、红薯永远被认成土豆。
     */
    internal fun matchLabel(label: String, vegetables: List<VegetableEntity>): VegetableEntity? {
        val normalized = VegetableCatalog.normalize(label)
        if (normalized.isEmpty()) return null

        // 1. 与蔬菜本名完全相同（"Eggplant" 应匹配 Eggplant，而不是把它当别名的 Brinjal）
        vegetables.firstOrNull { item -> VegetableCatalog.normalize(item.name) == normalized }
            ?.let { return it }

        // 2. 与某个别名完全相同
        vegetables.firstOrNull { item ->
            VegetableCatalog.aliases(item.name).any { alias -> alias == normalized }
        }?.let { return it }

        // 3. 退回到“包含”匹配

        if (normalized.length < 3) return null
        return vegetables
            .mapNotNull { item ->
                // 有多个“包含”命中时，取别名最长（最具体）的那个
                VegetableCatalog.aliases(item.name)
                    .filter { alias -> normalized.contains(alias) || alias.contains(normalized) }
                    .maxOfOrNull { it.length }
                    ?.let { item to it }
            }
            .maxByOrNull { it.second }
            ?.first
    }
}
