package com.example.veggielens.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.veggielens.R
import com.example.veggielens.ui.designsystem.MetricTile
import com.example.veggielens.ui.designsystem.SectionHeader
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.designsystem.VlTag
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme
import com.example.veggielens.viewmodel.HistoryViewModel
import com.example.veggielens.viewmodel.ProfileStats
import com.example.veggielens.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    historyViewModel: HistoryViewModel,
    contentPadding: PaddingValues,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stats by profileViewModel.stats.collectAsState()
    val apiKey by profileViewModel.apiKey.collectAsState()
    val totalVegetables by profileViewModel.totalVegetables.collectAsState()

    ProfileContent(
        stats = stats,
        totalVegetables = totalVegetables,
        apiKeyConfigured = apiKey.isNotBlank(),
        currentApiKey = apiKey,
        contentPadding = contentPadding,
        onSaveApiKey = profileViewModel::updateApiKey,
        onClearHistory = historyViewModel::clearAllHistory,
        onOpenGuide = onOpenGuide,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    stats: ProfileStats,
    totalVegetables: Int,
    apiKeyConfigured: Boolean,
    currentApiKey: String,
    contentPadding: PaddingValues,
    onSaveApiKey: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showKeySheet by rememberSaveable { mutableStateOf(false) }
    var showClearDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colors = MaterialTheme.colorScheme

    val appVersion = rememberAppVersion()
    val keySavedMessage = stringResource(R.string.profile_key_saved)
    val historyClearedMessage = stringResource(R.string.profile_history_cleared)

    Scaffold(
        modifier = modifier
            .padding(bottom = contentPadding.calculateBottomPadding())
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screen)
                .padding(bottom = Spacing.xl),
        ) {
            CollectionHero(stats = stats, totalVegetables = totalVegetables, onClick = onOpenGuide)

            Spacer(Modifier.height(Spacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                MetricTile(
                    value = stats.monthlyScans.toString(),
                    unit = stringResource(R.string.unit_times),
                    label = stringResource(R.string.profile_monthly),
                    icon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.weight(1f),
                )
                MetricTile(
                    value = stats.streakDays.toString(),
                    unit = stringResource(R.string.unit_days),
                    label = stringResource(R.string.profile_streak),
                    icon = Icons.Rounded.Whatshot,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.xl))
            SectionHeader(title = stringResource(R.string.profile_section_ai))
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Rounded.Key,
                    title = stringResource(R.string.profile_api_key),
                    subtitle = stringResource(
                        if (apiKeyConfigured) R.string.profile_api_key_on else R.string.profile_api_key_off
                    ),
                    onClick = { showKeySheet = true },
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            SectionHeader(title = stringResource(R.string.profile_section_data))
            SettingsGroup {
                SettingsItem(
                    icon = Icons.Rounded.DeleteOutline,
                    title = stringResource(R.string.profile_clear_history),
                    subtitle = stringResource(R.string.profile_clear_history_desc),
                    destructive = true,
                    onClick = { showClearDialog = true },
                )
                HorizontalDivider(
                    color = colors.outlineVariant,
                    modifier = Modifier.padding(start = 72.dp),
                )
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.profile_about),
                    subtitle = stringResource(R.string.profile_version, appVersion),
                    onClick = { showAboutDialog = true },
                )
            }

            Spacer(Modifier.height(Spacing.lg))
            VlTag(
                text = stringResource(R.string.profile_privacy_badge),
                icon = Icons.Rounded.Shield,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }

    if (showKeySheet) {
        ApiKeySheet(
            initialKey = currentApiKey,
            onDismiss = { showKeySheet = false },
            onSave = { key ->
                onSaveApiKey(key)
                showKeySheet = false
                scope.launch { snackbarHostState.showSnackbar(keySavedMessage) }
            },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
            title = { Text(stringResource(R.string.profile_clear_dialog_title)) },
            text = { Text(stringResource(R.string.profile_clear_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                        scope.launch { snackbarHostState.showSnackbar(historyClearedMessage) }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.onError,
                    ),
                ) {
                    Text(stringResource(R.string.profile_clear_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.profile_about)) },
            text = {
                Text(stringResource(R.string.profile_about_message, appVersion))
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
        )
    }
}

/** 顶部统计：累计识别次数（主角数字）+ 图鉴收集进度。 */
@Composable
private fun CollectionHero(
    stats: ProfileStats,
    totalVegetables: Int,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val progress = if (totalVegetables > 0) {
        (stats.uniqueVegetables.toFloat() / totalVegetables).coerceIn(0f, 1f)
    } else {
        0f
    }
    val collectionText = stringResource(
        R.string.profile_collection,
        stats.uniqueVegetables,
        totalVegetables,
    )
    val totalLabel = stringResource(R.string.profile_total_scans)
    val timesUnit = stringResource(R.string.unit_times)

    Surface(
        onClick = onClick,
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(text = totalLabel, style = MaterialTheme.typography.labelLarge)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stats.totalScans.toString(),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    text = timesUnit,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = Spacing.xxs, bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(Spacing.md))
            Text(text = collectionText, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(colors.onPrimaryContainer.copy(alpha = 0.12f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.profile_open_guide),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.primary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    VlCard { content() }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val accent = if (destructive) colors.error else colors.primary
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (destructive) colors.errorContainer else colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (destructive) colors.onErrorContainer else colors.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
        },
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (destructive) accent else colors.onSurface,
            )
        },
        supportingContent = {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApiKeySheet(
    initialKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var key by rememberSaveable { mutableStateOf(initialKey) }
    var visible by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.lg)
                .imePadding(),
        ) {
            Text(
                text = stringResource(R.string.profile_key_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.profile_key_sheet_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.md))
            OutlinedTextField(
                value = key,
                onValueChange = { key = it.trim() },
                label = { Text(stringResource(R.string.profile_key_label)) },
                placeholder = { Text(stringResource(R.string.profile_key_placeholder)) },
                singleLine = true,
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = stringResource(
                                if (visible) R.string.cd_hide_key else R.string.cd_show_key
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(Spacing.lg))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs, Alignment.End),
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(onClick = { onSave(key) }) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}

/** 读取 versionName（不依赖 BuildConfig，AGP 8+ 默认不生成它）。 */
@Composable
private fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
}

@PreviewLightDark
@Composable
private fun ProfileContentPreview() {
    VeggieLensTheme {
        ProfileContent(
            stats = ProfileStats(totalScans = 42, uniqueVegetables = 11, monthlyScans = 9, streakDays = 3),
            totalVegetables = 25,
            apiKeyConfigured = false,
            currentApiKey = "",
            contentPadding = PaddingValues(),
            onSaveApiKey = {},
            onClearHistory = {},
            onOpenGuide = {},
        )
    }
}
