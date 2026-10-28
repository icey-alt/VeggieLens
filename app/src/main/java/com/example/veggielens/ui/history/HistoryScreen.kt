package com.example.veggielens.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CenterFocusWeak
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.example.veggielens.R
import com.example.veggielens.data.model.ScanHistory
import com.example.veggielens.data.model.VegetableCatalog
import com.example.veggielens.ui.designsystem.CategorySelectionSaver
import com.example.veggielens.ui.designsystem.EmptyState
import com.example.veggielens.ui.theme.Spacing
import com.example.veggielens.ui.theme.VeggieLensTheme
import com.example.veggielens.viewmodel.HistoryViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    contentPadding: PaddingValues,
    onNavigateToScan: () -> Unit,
    onNavigateToResult: (ScanHistory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val scientificNames by viewModel.scientificNames.collectAsState()
    var selectedCategories by rememberSaveable(stateSaver = CategorySelectionSaver) {
        mutableStateOf(emptySet<String>())
    }

    HistoryContent(
        searchQuery = searchQuery,
        historyList = historyList,
        scientificNames = scientificNames,
        selectedCategories = selectedCategories,
        contentPadding = contentPadding,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onCategoryToggled = { selectedCategories = VegetableCatalog.toggleCategory(selectedCategories, it) },
        onClearFilters = {
            viewModel.updateSearchQuery("")
            selectedCategories = emptySet()
        },
        onNavigateToScan = onNavigateToScan,
        onNavigateToResult = onNavigateToResult,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryContent(
    searchQuery: String,
    historyList: List<ScanHistory>,
    scientificNames: Map<String, String>,
    selectedCategories: Set<String>,
    contentPadding: PaddingValues,
    onSearchQueryChange: (String) -> Unit,
    onCategoryToggled: (String) -> Unit,
    onClearFilters: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToResult: (ScanHistory) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredHistory = remember(historyList, selectedCategories) {
        historyList.filter { VegetableCatalog.matchesCategories(selectedCategories, it.vegetableName) }
    }
    val today = stringResource(R.string.history_today)
    val yesterday = stringResource(R.string.history_yesterday)
    val sections = remember(filteredHistory, today, yesterday) {
        HistoryDateGrouping.group(filteredHistory, todayLabel = today, yesterdayLabel = yesterday)
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val colors = MaterialTheme.colorScheme
    var filtersExpanded by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .padding(bottom = contentPadding.calculateBottomPadding())
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history)) },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    scrolledContainerColor = colors.background,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                shape = CircleShape,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CenterFocusWeak,
                    contentDescription = stringResource(R.string.cd_start_scan),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            HistorySearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                filterActive = selectedCategories.isNotEmpty(),
                filtersExpanded = filtersExpanded,
                onToggleFilters = { filtersExpanded = !filtersExpanded },
            )
            AnimatedVisibility(
                visible = filtersExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                CategoryFilterRow(
                    categories = VegetableCatalog.categories,
                    selected = selectedCategories,
                    onToggle = onCategoryToggled,
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    filteredHistory.isNotEmpty() -> LazyColumn(
                        contentPadding = PaddingValues(
                            start = Spacing.screen,
                            end = Spacing.screen,
                            bottom = 88.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        sections.forEach { section ->
                            stickyHeader(key = "header-${section.key}") {
                                DayHeader(label = section.label)
                            }
                            items(section.items, key = { it.id }) { item ->
                                HistoryItem(
                                    history = item,
                                    scientificName = scientificNames[item.vegetableName],
                                    onClick = { onNavigateToResult(item) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                    searchQuery.isBlank() && selectedCategories.isEmpty() ->
                        EmptyState(
                            icon = Icons.Rounded.ShoppingBasket,
                            title = stringResource(R.string.history_empty_title),
                            message = stringResource(R.string.history_empty_message),
                            actionLabel = stringResource(R.string.history_go_scan),
                            onAction = onNavigateToScan,
                        )
                    else -> EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.history_no_match_title),
                        message = stringResource(R.string.history_no_match_message),
                        actionLabel = stringResource(R.string.history_clear_filters),
                        onAction = onClearFilters,
                    )
                }
            }
        }
    }
}

/**
 * 搜索栏：灰色胶囊搜索框 + 右侧独立的圆形筛选按钮。
 * 筛选按钮展开/收起分类 Chip；选中了任意具体分类时按钮高亮并带小圆点。
 */
@Composable
private fun HistorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    filterActive: Boolean,
    filtersExpanded: Boolean,
    onToggleFilters: () -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val colors = MaterialTheme.colorScheme
    val textStyle = MaterialTheme.typography.bodyLarge

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.screen, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(SearchBarHeight)
                .clip(CircleShape)
                .background(colors.surfaceContainerHigh)
                .padding(start = Spacing.md, end = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.history_search_hint),
                        style = textStyle,
                        color = colors.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = textStyle.copy(color = colors.onSurface),
                    cursorBrush = SolidColor(colors.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.cd_clear_search),
                        tint = colors.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.width(Spacing.sm))
            }
        }

        val filterDescription = stringResource(R.string.cd_filter_categories)
        val filterState = stringResource(
            if (filtersExpanded) R.string.state_expanded else R.string.state_collapsed
        )
        Box(
            modifier = Modifier
                .size(SearchBarHeight)
                .clip(CircleShape)
                .background(if (filterActive || filtersExpanded) colors.primaryContainer else colors.surfaceContainerHigh)
                .clickable(role = Role.Button, onClick = onToggleFilters)
                .semantics {
                    contentDescription = filterDescription
                    stateDescription = filterState
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.FilterList,
                contentDescription = null,
                tint = if (filterActive || filtersExpanded) colors.onPrimaryContainer else colors.onSurface,
            )
            if (filterActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 11.dp, end = 11.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.primary),
                )
            }
        }
    }
}

private val SearchBarHeight = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        contentPadding = PaddingValues(horizontal = Spacing.screen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.xs),
    ) {
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
private fun DayHeader(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = Spacing.sm, bottom = Spacing.xxs)
            .semantics { heading() },
    )
}

@PreviewLightDark
@Composable
private fun HistoryContentPreview() {
    val now = System.currentTimeMillis()
    VeggieLensTheme {
        HistoryContent(
            searchQuery = "",
            historyList = listOf(
                ScanHistory(1, "Broccoli", 87, "西兰花富含维生素 C 与膳食纤维。", "DEEPSEEK", now),
                ScanHistory(2, "Carrot", 74, "胡萝卜含丰富的 β-胡萝卜素。", "LOCAL", now - 3_600_000),
                ScanHistory(3, "Tomato", 92, "番茄富含番茄红素。", "LOCAL", now - 86_400_000 * 3L),
            ),
            scientificNames = mapOf("Broccoli" to "Brassica oleracea", "Carrot" to "Daucus carota"),
            selectedCategories = emptySet(),
            contentPadding = PaddingValues(),
            onSearchQueryChange = {},
            onCategoryToggled = {},
            onClearFilters = {},
            onNavigateToScan = {},
            onNavigateToResult = {},
        )
    }
}
