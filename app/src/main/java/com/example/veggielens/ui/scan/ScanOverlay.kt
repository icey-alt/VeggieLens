package com.example.veggielens.ui.scan

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScanOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanLine")
    val linePositionFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLinePosition"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val boxSize = size.minDimension * 0.75f
            val left = (width - boxSize) / 2
            val top = (height - boxSize) / 2
            val right = left + boxSize
            val bottom = top + boxSize

            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(0f, 0f),
                size = Size(width, top)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(0f, bottom),
                size = Size(width, height - bottom)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(0f, top),
                size = Size(left, boxSize)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(right, top),
                size = Size(width - right, boxSize)
            )

            val primaryColor = Color(0xFF4CAF50)
            val strokeWidth = 5.dp.toPx()
            val bracketLen = 30.dp.toPx()

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(left, top + bracketLen)
                    lineTo(left, top)
                    lineTo(left + bracketLen, top)
                },
                color = primaryColor,
                style = Stroke(width = strokeWidth)
            )

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(right - bracketLen, top)
                    lineTo(right, top)
                    lineTo(right, top + bracketLen)
                },
                color = primaryColor,
                style = Stroke(width = strokeWidth)
            )

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(left, bottom - bracketLen)
                    lineTo(left, bottom)
                    lineTo(left + bracketLen, bottom)
                },
                color = primaryColor,
                style = Stroke(width = strokeWidth)
            )

            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(right - bracketLen, bottom)
                    lineTo(right, bottom)
                    lineTo(right, bottom - bracketLen)
                },
                color = primaryColor,
                style = Stroke(width = strokeWidth)
            )

            val currentLineY = top + (boxSize * linePositionFraction)
            drawLine(
                color = primaryColor.copy(alpha = 0.7f),
                start = Offset(left + 10.dp.toPx(), currentLineY),
                end = Offset(right - 10.dp.toPx(), currentLineY),
                strokeWidth = 3.dp.toPx()
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "请将蔬菜放入框内",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}