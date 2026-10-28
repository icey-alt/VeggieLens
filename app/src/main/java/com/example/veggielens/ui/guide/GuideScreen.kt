package com.example.veggielens.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.example.veggielens.R
import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.ui.designsystem.CategorySelectionSaver
import com.example.veggielens.data.model.VegetableEntity
import com.example.veggielens.ui.designsystem.TagTone
import com.example.veggielens.ui.designsystem.VegetableImage
import com.example.veggielens.ui.designsystem.VlCard
import com.example.veggielens.ui.designsystem.VlTag
import com.example.veggielens.ui.designsystem.assetImageModel
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme
import com.example.veggielens.viewmodel.GuideEntry
import com.example.veggielens.viewmodel.GuideViewModel

/** 蔬菜图鉴：收集进度 + 分类筛选 + 两列卡片网格；未识别过的蔬菜去色显示。 */
@Composable
fun GuideScreen(
    viewModel: GuideViewModel,
    contentPadding: PaddingValues,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries by viewModel.entries.collectAsState()
    var selectedCategories by rememberSaveable(stateSaver = CategorySelectionSaver) {
        mutableStateOf(emptySet<String>())
    }

    GuideContent(
        entries = entries,
        selectedCategories = selectedCategories,
        contentPadding = contentPadding,
        onCategoryToggled = { selectedCategories = VegetableCatalog.toggleCategory(selectedCategories, it) },
        onOpenDetail = onOpenDetail,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideContent(
    entries: List<GuideEntry>,
    selectedCategories: Set<String>,
    contentPadding: PaddingValues,
    onCategoryToggled: (String) -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(entries, selectedCategories) {
        entries.filter { VegetableCatalog.matchesCategories(selectedCategories, it.vegetable.name) }
    }
    val discoveredCount = entries.count { it.discovered }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier
            .padding(bottom = contentPadding.calculateBottomPadding())
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.guide)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(
                start = Spacing.screen,
                end = Spacing.screen,
                bottom = Spacing.lg,
            ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            item(key = "progress", span = { GridItemSpan(maxLineSpan) }) {
                CollectionProgressCard(discovered = discoveredCount, total = entries.size)
            }
            item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
                CategoryChips(
                    categories = VegetableCatalog.categories,
                    selected = selectedCategories,
                    onToggle = onCategoryToggled,
                )
            }
            items(filtered, key = { it.vegetable.name }) { entry ->
                GuideCard(entry = entry, onClick = { onOpenDetail(entry.vegetable.name) })
            }
        }
    }
}

@Composable
private fun CollectionProgressCard(
    discovered: Int,
    total: Int,
) {
    val colors = MaterialTheme.colorScheme
    val progress = if (total > 0) discovered.toFloat() / total else 0f

    VlCard {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(
                text = stringResource(R.string.guide_progress, discovered, total),
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
            )
            Spacer(Modifier.height(Spacing.xs))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceContainerHigh),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = stringResource(R.string.guide_progress_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        items(categories) { category ->
            val isSelected = VegetableCatalog.isCategorySelected(selected, category)
            FilterChip(
                selected = isSelected,
                onClick = { onToggle(category) },
                label = { Text(category) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    }
                } else {
                    null
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.primaryContainer,
                    selectedLabelColor = colors.onPrimaryContainer,
                    selectedLeadingIconColor = colors.onPrimaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun GuideCard(
    entry: GuideEntry,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val vegetable = entry.vegetable
    val status = stringResource(if (entry.discovered) R.string.guide_discovered else R.string.guide_undiscovered)
    val spoken = "${vegetable.chineseName}，$status"

    VlCard(
        onClick = onClick,
        modifier = Modifier.clearAndSetSemantics { contentDescription = spoken },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(Spacing.xs),
        ) {
            VegetableImage(
                model = assetImageModel(vegetable.assetImagePath),
                contentDescription = null,
                grayscale = !entry.discovered,
                modifier = Modifier.fillMaxSize(),
            )
            if (entry.discovered) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.xs)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                VlTag(
                    text = status,
                    tone = TagTone.Neutral,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(Spacing.xs),
                )
            }
        }
        Column(
            modifier = Modifier.padding(
                start = Spacing.sm,
                end = Spacing.sm,
                bottom = Spacing.sm,
            ),
        ) {
            Text(
                text = vegetable.chineseName,
                style = MaterialTheme.typography.titleSmall,
                color = if (entry.discovered) colors.onSurface else colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = vegetable.name.replace('_', ' '),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GuideContentPreview() {
    fun veg(name: String, cn: String) = VegetableEntity(
        name = name,
        chineseName = cn,
        scientificName = "",
        calories = "",
        protein = "",
        carbs = "",
        fiber = "",
        assetImagePath = "vegetables/$name.jpg",
    )
    VeggieLensTheme {
        GuideContent(
            entries = listOf(
                GuideEntry(veg("Broccoli", "西兰花"), setOf("broccoli"), discovered = true),
                GuideEntry(veg("Carrot", "胡萝卜"), setOf("carrot"), discovered = false),
                GuideEntry(veg("Tomato", "番茄"), setOf("tomato"), discovered = true),
            ),
            selectedCategories = emptySet(),
            contentPadding = PaddingValues(),
            onCategoryToggled = {},
            onOpenDetail = {},
        )
    }
}
