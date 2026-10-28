package com.example.veggielens.ui.scan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.example.veggielens.ui.theme.ScanColors
import com.example.veggielens.ui.theme.Spacing

/**
 * 取景卡片上的覆盖层：顶部居中的深色提示气泡 + 四个白色圆角角标。
 * 识别中框内有呼吸动效；自动识别时“像是”某种蔬菜，角标变为浅绿色。
 *
 * @param bottomReserved 卡片底部被按钮占用的高度，取景框在剩余区域内居中
 */
@Composable
fun ScanOverlay(
    hint: String,
    isProcessing: Boolean,
    isConfident: Boolean,
    bottomReserved: Dp,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "scanOverlay")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val highlight = MaterialTheme.colorScheme.inversePrimary

    BoxWithConstraints(modifier = modifier) {
        val topReserved = 88.dp
        val availableHeight = maxHeight - topReserved - bottomReserved
        val frameSize = min(maxWidth * 0.72f, availableHeight * 0.92f)
        val frameLeft = (maxWidth - frameSize) / 2
        val frameTop = topReserved + (availableHeight - frameSize) / 2

        Canvas(modifier = Modifier.fillMaxSize()) {
            val left = frameLeft.toPx()
            val top = frameTop.toPx()
            val side = frameSize.toPx()
            val radius = 20.dp.toPx()
            val right = left + side
            val bottom = top + side

            // 轻微压暗框外区域，让角标更清楚
            val frame = RoundRect(Rect(left, top, right, bottom), CornerRadius(radius, radius))
            val outside = Path().apply {
                fillType = androidx.compose.ui.graphics.PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                addRoundRect(frame)
            }
            drawPath(outside, ScanColors.Scrim)

            if (isProcessing) {
                clipPath(Path().apply { addRoundRect(frame) }) {
                    drawRect(color = highlight.copy(alpha = 0.12f + 0.18f * pulse))
                }
            }

            val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            val arm = 34.dp.toPx()
            val color = if (isConfident || isProcessing) highlight else ScanColors.Bracket
            listOf(
                Path().apply {
                    moveTo(left, top + arm)
                    lineTo(left, top + radius)
                    quadraticTo(left, top, left + radius, top)
                    lineTo(left + arm, top)
                },
                Path().apply {
                    moveTo(right - arm, top)
                    lineTo(right - radius, top)
                    quadraticTo(right, top, right, top + radius)
                    lineTo(right, top + arm)
                },
                Path().apply {
                    moveTo(right, bottom - arm)
                    lineTo(right, bottom - radius)
                    quadraticTo(right, bottom, right - radius, bottom)
                    lineTo(right - arm, bottom)
                },
                Path().apply {
                    moveTo(left + arm, bottom)
                    lineTo(left + radius, bottom)
                    quadraticTo(left, bottom, left, bottom - radius)
                    lineTo(left, bottom - arm)
                },
            ).forEach { drawPath(it, color, style = stroke) }
        }

        Surface(
            color = ScanColors.HintContainer,
            contentColor = ScanColors.OnHint,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = Spacing.md)
                .widthIn(max = 240.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
        ) {
            AnimatedContent(
                targetState = hint,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "hint",
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
                )
            }
        }
    }
}
