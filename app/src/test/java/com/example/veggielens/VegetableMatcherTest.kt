package com.example.veggielens

import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.viewmodel.ClassificationCandidate
import com.example.veggielens.viewmodel.VegetableMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * 模型标签 → 蔬菜的匹配规则。
 * 标签取自模型自带的 labels.txt（如 "Onion Red"、"Sweet Potato"），
 * 蔬菜名取自数据库（如 "Onion_Red"、"Sweet_Potato"）。
 */
class VegetableMatcherTest {

    private val names = listOf(
        "Bean", "Bitter_Gourd", "Bottle_Gourd", "Brinjal", "Broccoli", "Cabbage", "Capsicum",
        "Carrot", "Cauliflower", "Corn", "Cucumber", "Eggplant", "Galangal", "Garlic", "Ginger",
        "Lettuce", "Onion", "Onion_Red", "Papaya", "Potato", "Pumpkin", "Radish", "Spinach",
        "Sweet_Potato", "Tomato",
    )
    private val vegetables = names.map { veg(it) }

    private fun veg(name: String) = VegetableEntity(
        name = name,
        chineseName = name,
        scientificName = "",
        calories = "",
        protein = "",
        carbs = "",
        fiber = "",
        assetImagePath = "vegetables/$name.jpg",
    )

    private fun match(label: String) = VegetableMatcher.matchLabel(label, vegetables)?.name

    @Test
    fun redOnionIsNotMatchedAsOnion() {
        assertEquals("Onion_Red", match("Onion Red"))
        assertEquals("Onion", match("Onion"))
    }

    @Test
    fun sweetPotatoIsNotMatchedAsPotato() {
        assertEquals("Sweet_Potato", match("Sweet Potato"))
        assertEquals("Potato", match("Potato"))
    }

    @Test
    fun everyModelLabelMapsToItself() {
        val modelLabels = listOf(
            "Bean", "Bitter_Gourd", "Bottle_Gourd", "Brinjal", "Broccoli", "Cabbage", "Capsicum",
            "Carrot", "Cauliflower", "Corn", "Cucumber", "Eggplant", "Galangal", "Garlic", "Ginger",
            "Lettuce", "Onion", "Onion Red", "Papaya", "Potato", "Pumpkin", "Radish", "Spinach",
            "Sweet Potato", "Tomato",
        )
        modelLabels.forEach { label ->
            assertEquals("label=$label", label.replace(' ', '_'), match(label))
        }
    }

    @Test
    fun otherClassMatchesNothing() {
        assertNull(match("Other"))
        assertNull(match(""))
    }

    @Test
    fun bestMatchRespectsThresholdAndOrder() {
        val candidates = listOf(
            ClassificationCandidate("Potato", 0.30f),
            ClassificationCandidate("Sweet Potato", 0.65f),
        )
        val best = VegetableMatcher.findBestMatch(candidates, vegetables, minimumConfidence = 40)
        assertEquals("Sweet_Potato", best?.vegetable?.name)
        assertEquals(65, best?.confidence)

        assertNull(VegetableMatcher.findBestMatch(candidates, vegetables, minimumConfidence = 70))
    }
}
