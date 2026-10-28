package com.example.veggielens.ui.result

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.ImageSearch
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.veggielens.R
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.description.DescriptionSource
import com.example.veggielens.ui.designsystem.EmptyState
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme
import com.example.veggielens.viewmodel.ScanState
import com.example.veggielens.viewmodel.ScanViewModel
import com.example.veggielens.viewmodel.VegetableMatch

/** 结果页：居中标题栏（返回 / 分享）+ 主图卡 + 科普卡 + 营养亮点卡，全部为白底描边卡片。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ScanViewModel,
    contentPadding: PaddingValues,
    onNavigateBack: () -> Unit,
    onScanAgain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestResult by viewModel.latestResult.collectAsState()
    val isLoading by viewModel.isLoadingResult.collectAsState()
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val result = latestResult

    Scaffold(
        modifier = modifier.padding(bottom = contentPadding.calculateBottomPadding()),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.result)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    if (result != null) {
                        IconButton(onClick = { context.shareResult(result) }) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = stringResource(R.string.cd_share),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        when {
            result != null -> ResultContent(
                result = result,
                onSelectAlternative = viewModel::selectAlternative,
                modifier = Modifier.padding(innerPadding),
            )
            isLoading -> Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            else -> EmptyState(
                icon = Icons.Rounded.ImageSearch,
                title = stringResource(R.string.result_empty_title),
                message = stringResource(R.string.result_empty_message),
                actionLabel = stringResource(R.string.result_go_scan),
                onAction = onScanAgain,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
fun ResultContent(
    result: ScanState.Success,
    onSelectAlternative: (VegetableMatch) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screen)
            .padding(top = Spacing.xxs, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ResultHeroCard(
            vegetable = result.vegetable,
            badgeText = stringResource(R.string.result_match_badge, result.confidence),
            showLowConfidenceHint = result.confidence < LowConfidenceThreshold,
            alternatives = result.alternatives,
            onSelectAlternative = onSelectAlternative,
        )
        DescriptionCard(
            description = result.description,
            source = result.descriptionSource,
        )
        NutritionCard(vegetable = result.vegetable)
        MicronutrientCard(vegetable = result.vegetable)
        LifeTipsCard(vegetable = result.vegetable)
    }
}

/** 低于这个匹配度时提示“不太确定” */
private const val LowConfidenceThreshold = 60

/** 用系统分享面板分享识别结果（纯文本）。 */
private fun Context.shareResult(result: ScanState.Success) {
    val vegetable = result.vegetable
    val text = getString(
        R.string.result_share_text,
        vegetable.chineseName,
        vegetable.name,
        result.confidence,
        vegetable.calories,
        vegetable.protein,
        vegetable.carbs,
        vegetable.fiber,
        result.description,
    )
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, getString(R.string.result_share_title)))
}

@PreviewLightDark
@Composable
private fun ResultContentPreview() {
    VeggieLensTheme {
        ResultContent(
            result = ScanState.Success(
                vegetable = PreviewVegetable,
                confidence = 95,
                description = "西兰花富含维生素 C、维生素 K 和膳食纤维，口感脆嫩，适合清炒或焯水凉拌。烹饪时间不宜过长，以保留营养和翠绿的颜色。",
                descriptionSource = DescriptionSource.DEEPSEEK,
                alternatives = listOf(VegetableMatch(PreviewVegetable.copy(name = "Cauliflower", chineseName = "花椰菜"), 31)),
            ),
            onSelectAlternative = {},
        )
    }
}

internal val PreviewVegetable = VegetableEntity(
    name = "Broccoli",
    chineseName = "西兰花",
    scientificName = "Brassica oleracea var. italica",
    calories = "34kcal",
    protein = "2.8g",
    carbs = "6.6g",
    fiber = "2.6g",
    assetImagePath = "vegetables/Broccoli.jpg",
)
