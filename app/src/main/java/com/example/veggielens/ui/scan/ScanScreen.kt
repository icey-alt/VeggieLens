package com.example.veggielens.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.rounded.AutoMode
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.NoPhotography
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.veggielens.R
import com.example.veggielens.ui.designsystem.EmptyState
import com.example.veggielens.ui.theme.ScanColors
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.viewmodel.ScanState
import com.example.veggielens.viewmodel.ScanViewModel
import kotlinx.coroutines.launch

/**
 * 扫描页：白色居中标题栏（左：闪光灯，右：使用帮助）+ 圆角取景卡片。
 * 相册 / 快门 / 自动识别三个按钮压在取景卡片底部；识别中在取景框内显示动效，
 * 出错用 Snackbar 提示，不打断取景。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel,
    contentPadding: PaddingValues,
    onNavigateToResult: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanState by viewModel.scanState.collectAsState()
    val autoScanEnabled by viewModel.isAutoScanEnabled.collectAsState()
    val liveHint by viewModel.liveHint.collectAsState()

    var isFlashEnabled by rememberSaveable { mutableStateOf(false) }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var hasCameraPermission by remember { mutableStateOf(context.hasCameraPermission()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val colors = MaterialTheme.colorScheme

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        permissionDenied = !isGranted
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasCameraPermission = context.hasCameraPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val readImageError = stringResource(R.string.scan_error_read_image)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = context.contentResolver.decodeGalleryBitmap(it)
                viewModel.scanFromGallery(bitmap)
            } catch (e: Exception) {
                viewModel.reportError(readImageError)
            }
        }
    }

    LaunchedEffect(scanState) {
        when (val state = scanState) {
            is ScanState.Success -> {
                onNavigateToResult()
                viewModel.resetState()
            }
            is ScanState.Error -> {
                viewModel.resetState()
                scope.launch {
                    snackbarHostState.showSnackbar(message = state.message, withDismissAction = true)
                }
            }
            else -> Unit
        }
    }

    Scaffold(
        modifier = modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.scan)) },
                navigationIcon = {
                    if (hasCameraPermission) {
                        IconToggleButton(
                            checked = isFlashEnabled,
                            onCheckedChange = { isFlashEnabled = it },
                        ) {
                            Icon(
                                imageVector = if (isFlashEnabled) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                                contentDescription = stringResource(R.string.cd_flash),
                                tint = if (isFlashEnabled) colors.primary else colors.onSurface,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showHelp = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.cd_help),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (hasCameraPermission) {
                CameraCard(
                    isProcessing = scanState is ScanState.Loading,
                    autoScanEnabled = autoScanEnabled,
                    liveHint = liveHint,
                    snackbarHostState = snackbarHostState,
                    cameraPreview = {
                        CameraPreview(
                            modifier = Modifier.fillMaxSize(),
                            imageAnalyzer = viewModel.analyzer,
                            isFlashEnabled = isFlashEnabled,
                        )
                    },
                    onToggleAutoScan = viewModel::toggleAutoScan,
                    onShutter = viewModel::triggerScan,
                    onPickFromGallery = { galleryLauncher.launch("image/*") },
                )
            } else {
                EmptyState(
                    icon = Icons.Rounded.NoPhotography,
                    title = stringResource(R.string.permission_title),
                    message = stringResource(R.string.permission_message),
                    actionLabel = stringResource(
                        if (permissionDenied) R.string.permission_open_settings else R.string.permission_grant
                    ),
                    onAction = {
                        if (permissionDenied) {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    "package:${context.packageName}".toUri()
                                )
                            )
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    secondaryActionLabel = stringResource(R.string.permission_pick_gallery),
                    onSecondaryAction = { galleryLauncher.launch("image/*") },
                )
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(Spacing.md),
                )
            }
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(stringResource(R.string.scan_help_title)) },
            text = { Text(stringResource(R.string.scan_help_message)) },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
        )
    }
}

@Composable
private fun CameraCard(
    isProcessing: Boolean,
    autoScanEnabled: Boolean,
    liveHint: String?,
    snackbarHostState: SnackbarHostState,
    cameraPreview: @Composable () -> Unit,
    onToggleAutoScan: () -> Unit,
    onShutter: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    val hint = when {
        isProcessing -> stringResource(R.string.scan_hint_processing)
        autoScanEnabled && liveHint != null -> stringResource(R.string.scan_hint_live, liveHint)
        autoScanEnabled -> stringResource(R.string.scan_hint_auto)
        else -> stringResource(R.string.scan_hint_manual)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = Spacing.sm, end = Spacing.sm, bottom = Spacing.sm)
            .clip(MaterialTheme.shapes.large)
            .background(ScanColors.Background),
    ) {
        cameraPreview()

        ScanOverlay(
            hint = hint,
            isProcessing = isProcessing,
            isConfident = liveHint != null,
            bottomReserved = ControlsAreaHeight,
            modifier = Modifier.fillMaxSize(),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(horizontal = Spacing.sm),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SideButton(
                    icon = Icons.Rounded.PhotoLibrary,
                    contentDescription = stringResource(R.string.cd_gallery),
                    onClick = onPickFromGallery,
                )
                ShutterButton(isProcessing = isProcessing, onClick = onShutter)
                SideToggleButton(
                    icon = Icons.Rounded.AutoMode,
                    contentDescription = stringResource(R.string.scan_auto),
                    checked = autoScanEnabled,
                    onToggle = onToggleAutoScan,
                )
            }
        }
    }
}

/** 取景卡片底部控制区（按钮 + 上下留白）的高度，取景框会避开它。 */
private val ControlsAreaHeight = 124.dp

/** 快门：白色外环 + 白色内圆 + 主色相机图标。 */
@Composable
private fun ShutterButton(
    isProcessing: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, label = "shutterScale")
    val description = stringResource(R.string.cd_shutter)

    Box(
        modifier = Modifier
            .size(76.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(4.dp, ScanColors.ShutterRing, CircleShape)
            .padding(7.dp)
            .clip(CircleShape)
            .background(ScanColors.ShutterFill)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = !isProcessing,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(26.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun SideButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(ScanColors.SideButton)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = ScanColors.OnSideButton, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun SideToggleButton(
    icon: ImageVector,
    contentDescription: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (checked) colors.primary else ScanColors.SideButton,
        label = "sideToggle",
    )
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(container)
            .toggleable(value = checked, role = Role.Switch, onValueChange = { onToggle() })
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) colors.onPrimary else ScanColors.OnSideButton,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
