package com.example.veggielens.network

import android.icu.lang.UProperty
import com.google.gson.annotations.SerializedName

data class DeepSeekRequest(
    @SerializedName("model") val model: String = "deepseek-v4-flash",
    @SerializedName("messages") val messages: List<DeepSeekMessage>,
    @SerializedName("temperature") val temperature: Double = 0.5,
    @SerializedName("max_tokens") val maxTokens: Int = 150
)

data class DeepSeekMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
)

data class DeepSeekResponse(
    @SerializedName("choices") val choice: List<DeepSeekChoice>
)

data class DeepSeekChoice(
    @SerializedName("message") val message: DeepSeekMessage
)