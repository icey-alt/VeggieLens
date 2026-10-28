package com.example.veggielens.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.veggielens.R
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.description.DescriptionSource
import com.example.veggielens.description.LocalVegetableDescriptions
import com.example.veggielens.ui.designsystem.TagTone
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.result.DescriptionCard
import com.example.veggielens.ui.result.LifeTipsCard
import com.example.veggielens.ui.result.MicronutrientCard
import com.example.veggielens.ui.result.NutritionCard
import com.example.veggielens.ui.result.ResultHeroCard
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.viewmodel.GuideEntry
import com.example.veggielens.viewmodel.GuideViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 图鉴详情页：与结果页同一套卡片（主图、科普、营养、维生素矿物质、选购烹饪），
 * 另加一张“我的识别”卡片，显示识别次数和最近一次识别。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VegetableDetailScreen(
    viewModel: GuideViewModel,
    vegetableName: String,
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    onGoScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries by viewModel.entries.collectAsState()
    val history by viewModel.history.collectAsState()
    val key = VegetableCatalog.normalize(vegetableName)
    val entry = entries.firstOrNull { key in it.aliases }
    val records = remember(entry, history) {
        if (entry == null) {
            emptyList()
        } else {
            history.filter { VegetableCatalog.normalize(it.vegetableName) in entry.aliases }
        }
    }
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(entry?.vegetable?.chineseName ?: stringResource(R.string.detail)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        if (entry == null) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            VegetableDetailContent(
                entry = entry,
                records = records,
                onGoScan = onGoScan,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun VegetableDetailContent(
    entry: GuideEntry,
    records: List<ScanHistory>,
    onGoScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vegetable = entry.vegetable
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen)
            .padding(top = Spacing.xxs, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ResultHeroCard(
            vegetable = vegetable,
            badgeText = stringResource(
                if (entry.discovered) R.string.guide_discovered else R.string.guide_undiscovered
            ),
            badgeTone = if (entry.discovered) TagTone.Primary else TagTone.Neutral,
        )
        MyScansCard(records = records, onGoScan = onGoScan)
        DescriptionCard(
            description = LocalVegetableDescriptions.forName(vegetable.name),
            source = DescriptionSource.LOCAL,
        )
        NutritionCard(vegetable = vegetable)
        MicronutrientCard(vegetable = vegetable)
        LifeTipsCard(vegetable = vegetable)
    }
}

/** “我的识别”：识别过几次、最近一次的时间和匹配度；没识别过时引导去拍。 */
@Composable
private fun MyScansCard(
    records: List<ScanHistory>,
    onGoScan: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val latest = records.maxByOrNull { it.timestamp }
    val latestTime = remember(latest?.timestamp) {
        latest?.let { SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(Date(it.timestamp)) }
    }

    VlCard {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (latest != null) Icons.Outlined.Schedule else Icons.Outlined.CenterFocusWeak,
                    contentDescription = null,
                    tint = colors.onPrimaryContainer,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                if (latest != null && latestTime != null) {
                    Text(
                        text = stringResource(R.string.detail_scanned_count, records.size),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.detail_scanned_latest, latestTime, latest.confidence),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.detail_not_scanned),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.detail_not_scanned_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
            if (latest == null) {
                Spacer(Modifier.width(Spacing.xs))
                FilledTonalButton(
                    onClick = onGoScan,
                    contentPadding = PaddingValues(horizontal = Spacing.md),
                ) {
                    Text(stringResource(R.string.detail_go_scan))
                }
            }
        }
    }
}
