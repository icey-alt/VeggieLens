package com.example.veggielens.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.data.model.VeggieDatabase
import com.example.veggielens.data.repository.VegetableRepository
import com.example.veggielens.description.DefaultDescriptionProvider
import com.example.veggielens.description.DescriptionProvider
import com.example.veggielens.description.DescriptionSource
import com.example.veggielens.network.DeepSeekClient
import com.example.veggielens.security.ApiKeyStore
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier.ImageClassifierOptions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import com.google.mediapipe.tasks.vision.core.RunningMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

sealed class ScanState {
    object Idle : ScanState()
    object Loading : ScanState()
    data class Success(
        val vegetable: VegetableEntity,
        val confidence: Int,
        val description: String,
        val descriptionSource: DescriptionSource,
        /** 其他可能的结果（不含 vegetable 本身），最多 2 个，供结果页“也可能是”使用 */
        val alternatives: List<VegetableMatch> = emptyList()
    ) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VegetableRepository

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)

    val scanState: StateFlow<ScanState> = _scanState

    private val _latestResult = MutableStateFlow<ScanState.Success?>(null)

    val latestResult: StateFlow<ScanState.Success?> = _latestResult

    private val _isLoadingResult = MutableStateFlow(false)

    /** 从历史记录打开结果页时，结果正在加载 */
    val isLoadingResult: StateFlow<Boolean> = _isLoadingResult

    /** 最近一帧相机画面（原始方向）及其旋转角度 */
    @Volatile
    private var latestFrame: CameraFrame? = null

    private var imageClassifier: ImageClassifier? = null

    private val classifierReady = CompletableDeferred<ImageClassifier>()

    private val descriptionProvider: DescriptionProvider = DefaultDescriptionProvider(
        api = DeepSeekClient.api,
        apiKeyStore = ApiKeyStore(application)
    )

    private val autoScanInProgress = AtomicBoolean(false)

    private val _isAutoScanEnabled = MutableStateFlow(true)

    val isAutoScanEnabled: StateFlow<Boolean> = _isAutoScanEnabled

    /** 自动扫描时画面里“像是”什么蔬菜（中文名），用于取景页的实时提示 */
    private val _liveHint = MutableStateFlow<String?>(null)

    val liveHint: StateFlow<String?> = _liveHint

    private var lastAnalysisTime = 0L

    fun toggleAutoScan() {
        _isAutoScanEnabled.value = !_isAutoScanEnabled.value
        _liveHint.value = null
    }

    init {
        val database = VeggieDatabase.getDatabase(application)
        repository = VegetableRepository(database.vegetableDao(), database.scanHistoryDao())
        initMediaPipe()
    }

    private fun initMediaPipe() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val options = ImageClassifierOptions.builder()
                    .setBaseOptions(
                        BaseOptions.builder()
                            .setModelAssetPath(RecognitionConfig.MODEL_ASSET)
                            .build()
                    )
                    .setRunningMode(RunningMode.IMAGE)
                    .setMaxResults(RecognitionConfig.MAX_RESULTS)
                    // 与“也可能是”的阈值一致，否则低分候选在这里就被过滤掉了
                    .setScoreThreshold(RecognitionConfig.ALTERNATIVE_MIN_CONFIDENCE / 100f)
                    .build()
                val classifier = ImageClassifier.createFromOptions(getApplication(), options)
                imageClassifier =classifier
                classifierReady.complete(classifier)
            } catch (e: Throwable) {
                Log.e(TAG, "Unable to initialize image classifier", e)
                classifierReady.completeExceptionally(e)
            }
        }
    }

    val analyzer = ImageAnalysis.Analyzer { imageProxy ->
        try {
            val frame = CameraFrame(
                bitmap = imageProxy.toBitmap(),
                rotationDegrees = imageProxy.imageInfo.rotationDegrees
            )
            latestFrame = frame

            val currentTime = System.currentTimeMillis()
            if (_isAutoScanEnabled.value &&
                _scanState.value is ScanState.Idle &&
                currentTime - lastAnalysisTime >= RecognitionConfig.AUTO_SCAN_INTERVAL_MS &&
                autoScanInProgress.compareAndSet(false, true)
                ) {
                lastAnalysisTime = currentTime
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        performAutoScan(frame.toModelInput())
                    } finally {
                        autoScanInProgress.set(false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to read camera frame", e)
        } finally {
            imageProxy.close()
        }
    }

    fun triggerScan() {
        val frame = latestFrame
        if (frame == null) {
            _scanState.value = ScanState.Error("未检测到相机画面")
            return
        }
        processImage(frame.toModelInput())
    }

    fun scanFromGallery(bitmap: Bitmap) {
        processImage(bitmap)
    }

    fun reportError(message: String) {
        _scanState.value = ScanState.Error(message)
    }

    private fun processImage(bitmap: Bitmap) {
        _scanState.value = ScanState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val classifier = awaitClassifier() ?: return@launch

                val mpImage = BitmapImageBuilder(bitmap).build()
                val results = classifier.classify(mpImage)

                val presetList = repository.getAllVegetables()
                val candidates = results.toCandidates()
                val match = VegetableMatcher.findBestMatch(
                    candidates = candidates,
                    vegetables = presetList,
                    minimumConfidence = MANUAL_SCAN_MIN_CONFIDENCE
                )

                if (match != null) {
                    val matchedVeggie = match.vegetable
                    val description = descriptionProvider.describe(
                        matchedVeggie.name,
                        matchedVeggie.chineseName
                    )
                    val history = ScanHistory(
                        vegetableName = matchedVeggie.name,
                        confidence = match.confidence,
                        description = description.text,
                        descriptionSource = description.source.name,
                        timestamp = System.currentTimeMillis()
                    )
                    repository.insertScanHistory(history)

                    val success = ScanState.Success(
                        vegetable = matchedVeggie,
                        confidence = match.confidence,
                        description = description.text,
                        descriptionSource = description.source,
                        alternatives = alternativesFor(candidates, presetList, matchedVeggie)
                    )
                    _latestResult.value = success
                    _scanState.value = success
                } else {
                    _scanState.value = ScanState.Error("未能可靠识别，请将蔬菜置于画面中央并重试")
                }
            } catch (e: Throwable) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Manual image classification failed", e)
                _scanState.value = ScanState.Error("识别失败，请稍后重试或更换一张清晰图片")
            }
        }
    }

    fun loadResultFromHistory(history: ScanHistory) {
        // 先清空，避免结果页短暂显示上一次的结果
        _latestResult.value = null
        _isLoadingResult.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val vegetable = repository.getVegetableByName(history.vegetableName)
                if (vegetable != null) {
                    _latestResult.value = ScanState.Success(
                        vegetable = vegetable,
                        confidence = history.confidence,
                        description = history.description.orEmpty(),
                        descriptionSource = DescriptionSource.fromStorage(history.descriptionSource)
                    )
                }
            } finally {
                _isLoadingResult.value = false
            }
        }
    }

    private suspend fun performAutoScan(bitmap: Bitmap) {
        val classifier = imageClassifier ?: return
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = classifier.classify(mpImage)

            val presetList = repository.getAllVegetables()
            val candidates = result.toCandidates()
            // 候选按分数降序匹配，所以“提示”与“达到自动识别阈值”的结果是同一个蔬菜
            val hint = VegetableMatcher.findBestMatch(
                candidates = candidates,
                vegetables = presetList,
                minimumConfidence = HINT_MIN_CONFIDENCE
            )
            _liveHint.value = hint?.vegetable?.chineseName
            val match = hint?.takeIf { it.confidence >= AUTO_SCAN_MIN_CONFIDENCE }

            if (match != null) {
                _scanState.value = ScanState.Loading
                _liveHint.value = null

                val matchedVeggie = match.vegetable
                val description = descriptionProvider.describe(
                    matchedVeggie.name,
                    matchedVeggie.chineseName
                )
                val history = ScanHistory(
                    vegetableName = matchedVeggie.name,
                    confidence = match.confidence,
                    description = description.text,
                    descriptionSource = description.source.name,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertScanHistory(history)

                val success = ScanState.Success(
                    vegetable = matchedVeggie,
                    confidence = match.confidence,
                    description = description.text,
                    descriptionSource = description.source,
                    alternatives = alternativesFor(candidates, presetList, matchedVeggie)
                )
                _latestResult.value = success
                _scanState.value = success
            }
        } catch (e: Exception) {
            Log.e(TAG, "Automatic image classification failed", e)
        }
    }

    fun resetState() {
        _scanState.value = ScanState.Idle
    }

    /** 结果页点击“也可能是”中的候选：切换为该蔬菜，原结果变为候选之一 */
    fun selectAlternative(alternative: VegetableMatch) {
        val current = _latestResult.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val vegetable = alternative.vegetable
            val description = descriptionProvider.describe(vegetable.name, vegetable.chineseName)
            val previous = VegetableMatch(current.vegetable, current.confidence)
            _latestResult.value = ScanState.Success(
                vegetable = vegetable,
                confidence = alternative.confidence,
                description = description.text,
                descriptionSource = description.source,
                alternatives = (listOf(previous) + current.alternatives)
                    .filter { it.vegetable.name != vegetable.name }
                    .sortedByDescending { it.confidence }
                    .take(MAX_ALTERNATIVES)
            )
        }
    }

    private fun alternativesFor(
        candidates: List<ClassificationCandidate>,
        vegetables: List<VegetableEntity>,
        chosen: VegetableEntity
    ): List<VegetableMatch> =
        VegetableMatcher.findMatches(candidates, vegetables, ALTERNATIVE_MIN_CONFIDENCE)
            .filter { it.vegetable.name != chosen.name }
            .take(MAX_ALTERNATIVES)

    private suspend fun awaitClassifier(): ImageClassifier? {
        return try {
            classifierReady.await()
        } catch (e: Throwable) {
            if (e is CancellationException) throw e
            val reason = e.localizedMessage?.takeIf { it.isNotBlank() } ?: "未知错误"
            _scanState.value = ScanState.Error("离线模型初始化: $reason，请重新打开应用")
            null
        }
    }

    override fun onCleared() {
        imageClassifier?.close()
        imageClassifier = null
        super.onCleared()
    }

    private fun com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifierResult.toCandidates(): List<ClassificationCandidate> =
        classificationResult().classifications().firstOrNull()?.categories().orEmpty().map {
            ClassificationCandidate(label = it.categoryName(), score = it.score())
        }

    /** 相机帧：原始 Bitmap + CameraX 给出的顺时针旋转角度 */
    private class CameraFrame(val bitmap: Bitmap, val rotationDegrees: Int)

    /** 转正并裁出取景框区域，作为模型输入 */
    private fun CameraFrame.toModelInput(): Bitmap =
        FramePreprocessor.uprightCenterCrop(bitmap, rotationDegrees, RecognitionConfig.CAMERA_CROP_FRACTION)

    private companion object {
        const val TAG = "ScanViewModel"
        const val MANUAL_SCAN_MIN_CONFIDENCE = RecognitionConfig.MANUAL_SCAN_MIN_CONFIDENCE
        const val AUTO_SCAN_MIN_CONFIDENCE = RecognitionConfig.AUTO_SCAN_MIN_CONFIDENCE
        const val HINT_MIN_CONFIDENCE = RecognitionConfig.HINT_MIN_CONFIDENCE
        const val ALTERNATIVE_MIN_CONFIDENCE = RecognitionConfig.ALTERNATIVE_MIN_CONFIDENCE
        const val MAX_ALTERNATIVES = RecognitionConfig.MAX_RESULTS - 1
    }
}