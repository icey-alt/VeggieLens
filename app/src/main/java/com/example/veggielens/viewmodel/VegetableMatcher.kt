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
                val label = VegetableCatalog.normalize(candidate.label)
                val vegetable = vegetables.firstOrNull { item ->
                    VegetableCatalog.aliases(item.name).any { alias ->
                        label == alias ||
                                (label.length >= 3 && (label.contains(alias) || alias.contains(label)))
                    }
                }
                val confidence = (candidate.score * 100).toInt()
                if (vegetable != null && confidence >= minimumConfidence) {
                    VegetableMatch(vegetable, confidence)
                } else {
                    null
                }
            }
    }
}