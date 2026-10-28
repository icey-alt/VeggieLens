package com.example.veggielens.ui.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** 根据英文名得到 assets 里的蔬菜图片地址（与 VegetableEntity.assetImagePath 规则一致）。 */
fun vegetableImageModel(englishName: String): String =
    "file:///android_asset/vegetables/${englishName.trim().replace(" ", "_")}.jpg"

/** assets 相对路径（如 "vegetables/Carrot.jpg"）转为 Coil 可用的地址。 */
fun assetImageModel(assetPath: String): String = "file:///android_asset/$assetPath"

/**
 * 蔬菜实拍图，统一占位底色与裁切。
 * @param grayscale 去色显示（图鉴中尚未发现的蔬菜）
 */
@Composable
fun VegetableImage(
    model: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    grayscale: Boolean = false,
) {
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        colorFilter = if (grayscale) GrayscaleFilter else null,
        alpha = if (grayscale) 0.75f else 1f,
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}

/** 去色滤镜，用于图鉴中尚未发现的蔬菜。 */
private val GrayscaleFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/** 固定尺寸缩略图。 */
@Composable
fun VegetableThumbnail(
    englishName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    VegetableImage(
        model = vegetableImageModel(englishName),
        contentDescription = contentDescription,
        modifier = modifier.size(size),
    )
}
