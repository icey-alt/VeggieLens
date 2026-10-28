package com.example.veggielens.viewmodel

/**
 * 识别相关的全部可调参数，集中在这里。
 *
 * 换模型时：把新的 .tflite 放进 app/src/main/assets/，改 [MODEL_ASSET]，
 * 再用训练笔记本（ml/train_veggielens.ipynb）最后一步给出的建议值更新下面的阈值。
 * 阈值单位都是“匹配度百分比”（模型分数 × 100）。
 */
object RecognitionConfig {
    /** assets 里的模型文件名 */
    const val MODEL_ASSET = "veggielens_classifier_v1.tflite"

    /** 模型最多返回几个候选（主结果 + “也可能是”） */
    const val MAX_RESULTS = 3

    /** 自动识别：匹配度达到这个值才自动出结果（宁缺毋滥） */
    const val AUTO_SCAN_MIN_CONFIDENCE = 70

    /** 手动拍照 / 相册识别：低于这个值提示“未能可靠识别” */
    const val MANUAL_SCAN_MIN_CONFIDENCE = 40

    /** 取景时显示“像是 xx”的最低匹配度 */
    const val HINT_MIN_CONFIDENCE = 25

    /** 结果页“也可能是”候选的最低匹配度 */
    const val ALTERNATIVE_MIN_CONFIDENCE = 15

    /** 交给模型前，从相机画面中心裁出的正方形边长占短边的比例（略大于取景框） */
    const val CAMERA_CROP_FRACTION = 0.8f

    /** 自动识别的最短间隔，毫秒 */
    const val AUTO_SCAN_INTERVAL_MS = 1200L
}
