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
        val descriptionSource: DescriptionSource
    ) : ScanState()
    data class Error(val message: String) : ScanState()
}

class ScanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VegetableRepository

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)

    val scanState: StateFlow<ScanState> = _scanState

    private val _latestResult = MutableStateFlow<ScanState.Success?>(null)

    val latestResult: StateFlow<ScanState.Success?> = _latestResult

    private var latestBitmap: Bitmap? = null

    private var imageClassifier: ImageClassifier? = null

    private val classifierReady = CompletableDeferred<ImageClassifier>()

    private val descriptionProvider: DescriptionProvider = DefaultDescriptionProvider(
        api = DeepSeekClient.api,
        apiKeyStore = ApiKeyStore(application)
    )

    private val autoScanInProgress = AtomicBoolean(false)

    private val _isAutoScanEnabled = MutableStateFlow(true)

    val isAutoScanEnabled: StateFlow<Boolean> = _isAutoScanEnabled

    private var lastAnalysisTime = 0L

    fun toggleAutoScan() {
        _isAutoScanEnabled.value = !_isAutoScanEnabled.value
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
                            .setModelAssetPath("efficientnet_lite0.tflite")
                            .build()
                    )
                    .setRunningMode(RunningMode.IMAGE)
                    .setMaxResults(3)
                    .setScoreThreshold(0.2f)
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
            val bitmap = imageProxy.toBitmap()
            latestBitmap = bitmap

            val currentTime = System.currentTimeMillis()
            if (_isAutoScanEnabled.value &&
                _scanState.value is ScanState.Idle &&
                currentTime - lastAnalysisTime >= 1200L &&
                autoScanInProgress.compareAndSet(false, true)
                ) {
                lastAnalysisTime = currentTime
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        performAutoScan(bitmap)
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
        val bitmap = latestBitmap
        if (bitmap == null) {
            _scanState.value = ScanState.Error("未检测到相机画面")
            return
        }
        processImage(bitmap)
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
                val match = VegetableMatcher.findBestMatch(
                    candidates = results.toCandidates(),
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
                        descriptionSource = description.source
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
        viewModelScope.launch(Dispatchers.IO) {
            val vegetable = repository.getVegetableByName(history.vegetableName)
            if (vegetable != null) {
                _latestResult.value = ScanState.Success(
                    vegetable = vegetable,
                    confidence = history.confidence,
                    description = history.description.orEmpty(),
                    descriptionSource = DescriptionSource.fromStorage(history.descriptionSource)
                )
            }
        }
    }

    private suspend fun performAutoScan(bitmap: Bitmap) {
        val classifier = imageClassifier ?: return
        try {
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = classifier.classify(mpImage)

            val presetList = repository.getAllVegetables()
            val match = VegetableMatcher.findBestMatch(
                candidates = result.toCandidates(),
                vegetables = presetList,
                minimumConfidence = AUTO_SCAN_MIN_CONFIDENCE
            )

            if (match != null) {
                _scanState.value = ScanState.Loading

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
                    descriptionSource = description.source
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

    private companion object {
        const val TAG = "ScanViewModel"
        const val MANUAL_SCAN_MIN_CONFIDENCE = 40
        const val AUTO_SCAN_MIN_CONFIDENCE = 70
    }
}