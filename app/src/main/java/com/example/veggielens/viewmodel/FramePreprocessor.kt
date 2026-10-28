package com.example.veggielens.viewmodel

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 相机帧送进模型前的预处理。
 *
 * 1. 旋转校正：CameraX 给出的帧是传感器方向，竖屏时通常横着，需要按 rotationDegrees 顺时针转正；
 *    训练图片都是正向的，不转正会明显拉低准确率。
 * 2. 中心裁剪：训练图片是主体占满画面的正方形，这里只取画面中心的正方形区域（对应取景框），
 *    去掉框外背景的干扰。中心正方形与旋转无关，所以先裁剪再旋转，一次完成。
 */
internal object FramePreprocessor {

    fun uprightCenterCrop(source: Bitmap, rotationDegrees: Int, cropFraction: Float): Bitmap {
        val shortSide = min(source.width, source.height)
        val side = (shortSide * cropFraction.coerceIn(0.1f, 1f)).roundToInt().coerceAtLeast(1)
        val left = (source.width - side) / 2
        val top = (source.height - side) / 2
        val matrix = Matrix().apply {
            if (rotationDegrees % 360 != 0) postRotate(rotationDegrees.toFloat())
        }
        return Bitmap.createBitmap(source, left, top, side, side, matrix, true)
    }
}
