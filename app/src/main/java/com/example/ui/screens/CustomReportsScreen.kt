package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DateRangePreset
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.data.model.TransactionTypeFilter
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerHigh
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate400
import com.example.ui.theme.TrueBlackBackground
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportsViewMode(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    ADVANCED_ANALYTICS("Smart Analytics", Icons.Default.TrendingUp),
    CUSTOM_REPORTS("Ledger Reports & Export", Icons.Default.Assessment)
}

enum class ReportSortOption(val displayName: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    AMOUNT_DESC("Highest Amount"),
    AMOUNT_ASC("Lowest Amount"),
    MERCHANT_ASC("Merchant A-Z")
}

@Composable
fun CustomReportsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeViewMode by remember { mutableStateOf(ReportsViewMode.ADVANCED_ANALYTICS) }
    val insights by viewModel.reportInsights.collectAsStateWithLifecycle()
    val filter by viewModel.reportFilter.collectAsStateWithLifecycle()
    val savedPresets by viewModel.savedPresets.collectAsStateWithLifecycle()
    val allTransactions by viewModel.transactions.collectAsStateWithLifecycle()

    var isFilterPanelExpanded by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(ReportSortOption.DATE_DESC) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }
    var selectedTxForDetails by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedBarIndex by remember { mutableIntStateOf(-1) }

    // Distinct merchants from all transactions for quick filtering
    val distinctMerchants = remember(allTransactions) {
        allTransactions.map { it.merchant }.distinct().sorted()
    }

    val sdfDisplay = remember { SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()) }

    // Sort transactions based on active sort option
    val sortedTransactions = remember(insights.filteredTransactions, sortOption) {
        when (sortOption) {
            ReportSortOption.DATE_DESC -> insights.filteredTransactions.sortedByDescending { it.timestamp }
            ReportSortOption.DATE_ASC -> insights.filteredTransactions.sortedBy { it.timestamp }
            ReportSortOption.AMOUNT_DESC -> insights.filteredTransactions.sortedByDescending { it.amount }
            ReportSortOption.AMOUNT_ASC -> insights.filteredTransactions.sortedBy { it.amount }
            ReportSortOption.MERCHANT_ASC -> insights.filteredTransactions.sortedBy { it.merchant.lowercase(Locale.getDefault()) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlackBackground)
    ) {
        // Top Master Mode Switcher Bar
        Surface(
            color = DarkSurfaceContainerLow,
            border = BorderStroke(1.dp, DarkOutlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportsViewMode.values().forEach { mode ->
                    val isSelected = activeViewMode == mode
                    Surface(
                        onClick = { activeViewMode = mode },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) EmeraldPrimary else DarkSurfaceContainer,
                        border = BorderStroke(1.dp, if (isSelected) EmeraldLight else DarkOutlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("report_mode_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF04201A) else Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.title,
                                color = if (isSelected) Color(0xFF04201A) else Slate400,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        if (activeViewMode == ReportsViewMode.ADVANCED_ANALYTICS) {
            AdvancedAnalyticsScreen(
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
        // Top Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "Reports & Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Deep slice financial behavior analysis offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = EmeraldLight
                )
            }
        }

        // Action Toolbar (Export, Share, Bookmark)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.exportCustomReportCsv(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("export_report_csv_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Export CSV",
                        tint = Color(0xFF002113),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Export CSV",
                        color = Color(0xFF002113),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.shareCustomReportSummary(context) },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_report_summary_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = DarkTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share",
                        color = DarkTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    onClick = { showSavePresetDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = DarkSurfaceContainerHigh,
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("save_preset_icon_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = "Save Filter Preset",
                            tint = EmeraldLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Saved Presets Carousel (if any)
        if (savedPresets.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SAVED PRESETS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextMuted,
                        letterSpacing = 1.sp
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(savedPresets) { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceContainer,
                                border = BorderStroke(1.dp, DarkOutlineVariant),
                                modifier = Modifier
                                    .clickable { viewModel.applySavedPreset(preset) }
                                    .testTag("preset_${preset.name.replace(" ", "_")}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = preset.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkTextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteSavedPreset(preset.id) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete Preset",
                                            tint = DarkTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Scope Card (Spacious, clean with smooth accordion)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_filters_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isFilterPanelExpanded = !isFilterPanelExpanded },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = "Filter",
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Report Scope & Filters",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                                Text(
                                    text = "${filter.datePreset.displayName} • ${filter.typeFilter.displayName} • ${filter.selectedCategories.size} Categories",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight
                                )
                            }
                        }

                        IconButton(
                            onClick = { isFilterPanelExpanded = !isFilterPanelExpanded }
                        ) {
                            Icon(
                                imageVector = if (isFilterPanelExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Toggle Filters",
                                tint = DarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date range chips scroll
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateRangePreset.values().forEach { preset ->
                            val isSelected = filter.datePreset == preset
                            Surface(
                                onClick = { viewModel.setReportDatePreset(preset) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else DarkSurfaceContainerHigh,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else DarkOutlineVariant)
                            ) {
                                Text(
                                    text = preset.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldLight else DarkTextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    // Expanded Controls
                    AnimatedVisibility(
                        visible = isFilterPanelExpanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HorizontalDivider(color = DarkOutlineVariant)

                            // Flow Type Filter
                            Column {
                                Text(
                                    text = "FLOW TYPE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkTextMuted,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TransactionTypeFilter.values().forEach { tFilter ->
                                        val isSelected = filter.typeFilter == tFilter
                                        Surface(
                                            onClick = { viewModel.setReportTypeFilter(tFilter) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else DarkSurfaceContainerHigh,
                                            border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else DarkOutlineVariant)
                                        ) {
                                            Text(
                                                text = tFilter.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) EmeraldLight else DarkTextSecondary,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Category Multi-select Chips
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CATEGORIES (${filter.selectedCategories.size}/${ExpenseCategory.values().size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DarkTextMuted,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        TextButton(
                                            onClick = { viewModel.selectAllReportCategories(true) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Select All", fontSize = 11.sp, color = EmeraldLight)
                                        }
                                        TextButton(
                                            onClick = { viewModel.selectAllReportCategories(false) },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Clear", fontSize = 11.sp, color = DarkTextMuted)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                val catScroll = rememberScrollState()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(catScroll),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ExpenseCategory.values().forEach { cat ->
                                        val isSelected = cat in filter.selectedCategories
                                        Surface(
                                            onClick = { viewModel.toggleReportCategory(cat) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) Color(cat.hexColor).copy(alpha = 0.2f) else DarkSurfaceContainerHigh,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) Color(cat.hexColor) else DarkOutlineVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CategoryIconBox(category = cat, size = 18)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = cat.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color(cat.hexColor) else DarkTextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Search Query & Merchant Selector
                            Column {
                                Text(
                                    text = "SEARCH OR MERCHANT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkTextMuted,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = filter.searchQuery,
                                    onValueChange = { viewModel.setReportSearchQuery(it) },
                                    placeholder = {
                                        Text(
                                            "Filter by merchant, bank, or notes...",
                                            fontSize = 13.sp,
                                            color = DarkTextMuted
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = DarkTextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (filter.searchQuery.isNotBlank()) {
                                            IconButton(onClick = { viewModel.setReportSearchQuery("") }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Clear",
                                                    tint = DarkTextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = EmeraldPrimary,
                                        unfocusedBorderColor = DarkOutlineVariant,
                                        focusedContainerColor = DarkSurfaceContainerHigh,
                                        unfocusedContainerColor = DarkSurfaceContainerHigh,
                                        focusedTextColor = DarkTextPrimary,
                                        unfocusedTextColor = DarkTextPrimary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Executive Financial Summary Card (Hero Card)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_executive_summary_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CUSTOM REPORT SUMMARY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldLight,
                            letterSpacing = 1.5.sp
                        )
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${insights.transactionCount} records in scope",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (filter.typeFilter == TransactionTypeFilter.CREDIT_ONLY) "Total Inflow" else "Total Outflow",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextSecondary
                            )
                            val displaySum = if (filter.typeFilter == TransactionTypeFilter.CREDIT_ONLY) insights.totalCredits else insights.totalDebits
                            Text(
                                text = formatCurrency(displaySum),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (filter.typeFilter == TransactionTypeFilter.CREDIT_ONLY) IncomeGreen else DarkTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Daily Burn Rate",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextSecondary
                            )
                            Text(
                                text = "${formatCurrency(insights.dailyAverageBurnRate)}/day",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = DarkOutlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-column key stats grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Avg / Tx", style = MaterialTheme.typography.labelSmall, color = DarkTextMuted)
                            Text(
                                formatCurrency(insights.averageSpendPerTx),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Median Tx", style = MaterialTheme.typography.labelSmall, color = DarkTextMuted)
                            Text(
                                formatCurrency(insights.medianSpend),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Max Tx", style = MaterialTheme.typography.labelSmall, color = DarkTextMuted)
                            Text(
                                if (insights.highestTransaction != null) formatCurrency(insights.highestTransaction!!.amount) else "₹0",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekend vs Weekday Behavior Meter
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Weekday: ${String.format(Locale.getDefault(), "%.0f%%", insights.weekdaySpendPercent * 100)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextSecondary
                            )
                            Text(
                                text = "Weekend: ${String.format(Locale.getDefault(), "%.0f%%", insights.weekendSpendPercent * 100)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentGold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { insights.weekdaySpendPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = EmeraldPrimary,
                            trackColor = AccentGold,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }

        // Daily Spend Velocity Trend (Interactive Visual Chart with Tap-to-Inspect Tooltip)
        if (insights.dailyTrend.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_daily_trend_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Spend Velocity",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                                Text(
                                    text = "Tap on any day bar below to inspect exact totals",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Selected Day Tooltip Banner (Dynamic real insights on tap)
                        if (selectedBarIndex in insights.dailyTrend.indices) {
                            val activePoint = insights.dailyTrend[selectedBarIndex]
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = EmeraldPrimary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Selected: ${activePoint.label}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldLight
                                        )
                                        Text(
                                            text = "${activePoint.count} transaction${if (activePoint.count > 1) "s" else ""}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkTextSecondary
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(activePoint.totalSpend),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (activePoint.totalSpend > 0) EmeraldLight else DarkTextMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Interactive Bar Chart Canvas
                        val maxDaily = remember(insights.dailyTrend) {
                            insights.dailyTrend.maxOfOrNull { it.totalSpend }?.coerceAtLeast(10.0) ?: 100.0
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(insights.dailyTrend) {
                                        detectTapGestures { offset ->
                                            val count = insights.dailyTrend.size
                                            if (count > 0) {
                                                val step = size.width / count
                                                val tappedIdx = (offset.x / step).toInt().coerceIn(0, count - 1)
                                                selectedBarIndex = if (selectedBarIndex == tappedIdx) -1 else tappedIdx
                                            }
                                        }
                                    }
                            ) {
                                val count = insights.dailyTrend.size
                                if (count == 0) return@Canvas

                                val slotWidth = size.width / count
                                val barWidth = (slotWidth * 0.6f).coerceIn(6.dp.toPx(), 24.dp.toPx())

                                // Background guideline
                                drawLine(
                                    color = DarkOutlineVariant.copy(alpha = 0.5f),
                                    start = Offset(0f, size.height - 1f),
                                    end = Offset(size.width, size.height - 1f),
                                    strokeWidth = 1.dp.toPx()
                                )

                                insights.dailyTrend.forEachIndexed { index, point ->
                                    val isSelected = index == selectedBarIndex
                                    val barHeight = ((point.totalSpend / maxDaily).toFloat() * (size.height * 0.85f)).coerceAtLeast(4.dp.toPx())
                                    val centerX = (index * slotWidth) + (slotWidth / 2f)
                                    val x = centerX - (barWidth / 2f)
                                    val y = size.height - barHeight

                                    val barColor = when {
                                        isSelected -> EmeraldLight
                                        point.totalSpend == maxDaily && maxDaily > 0 -> AccentGold
                                        point.totalSpend > 0 -> EmeraldPrimary
                                        else -> DarkSurfaceContainerHigh
                                    }

                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(x, y),
                                        size = Size(barWidth, barHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val first = insights.dailyTrend.firstOrNull()?.label ?: ""
                            val last = insights.dailyTrend.lastOrNull()?.label ?: ""
                            Text(first, style = MaterialTheme.typography.labelSmall, color = DarkTextMuted)
                            Text(
                                "Peak: ${insights.peakSpendingDayLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(last, style = MaterialTheme.typography.labelSmall, color = DarkTextMuted)
                        }
                    }
                }
            }
        }

        // Visual Distribution Chart (Category Breakdown)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("report_category_breakdown_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Category Spending Distribution",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "Proportional share across active filters",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (insights.categoryBreakdown.isEmpty()) {
                        Text(
                            "No matching transactions in this range.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextMuted
                        )
                    } else {
                        // Multi-color segmented progress bar
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            var currentX = 0f
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            for (item in insights.categoryBreakdown) {
                                val segWidth = canvasWidth * item.percentage
                                if (segWidth > 0) {
                                    drawRect(
                                        color = Color(item.category.hexColor),
                                        topLeft = Offset(currentX, 0f),
                                        size = Size(segWidth, canvasHeight)
                                    )
                                    currentX += segWidth
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Category items list
                        insights.categoryBreakdown.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(item.category.hexColor))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    CategoryIconBox(category = item.category, size = 26)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            item.category.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            "${item.transactionCount} transactions",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkTextMuted
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        formatCurrency(item.totalAmount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkTextPrimary
                                    )
                                    Text(
                                        String.format(Locale.getDefault(), "%.1f%%", item.percentage * 100),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Top Merchants in Slice
        if (insights.topMerchants.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Top Merchants in Scope",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        insights.topMerchants.forEachIndexed { idx, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = DarkSurfaceContainerHigh,
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${idx + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldLight
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            item.merchant,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = DarkTextPrimary
                                        )
                                        Text(
                                            "${item.transactionCount} transactions",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = DarkTextMuted
                                        )
                                    }
                                }

                                Text(
                                    formatCurrency(item.totalAmount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Detailed Itemized Transactions List Header with Sorting
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Itemized Breakdown (${sortedTransactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )

                // Sort Dropdown Selector
                Surface(
                    color = DarkSurfaceContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                sortOption = when (sortOption) {
                                    ReportSortOption.DATE_DESC -> ReportSortOption.AMOUNT_DESC
                                    ReportSortOption.AMOUNT_DESC -> ReportSortOption.AMOUNT_ASC
                                    ReportSortOption.AMOUNT_ASC -> ReportSortOption.MERCHANT_ASC
                                    ReportSortOption.MERCHANT_ASC -> ReportSortOption.DATE_ASC
                                    ReportSortOption.DATE_ASC -> ReportSortOption.DATE_DESC
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            modifier = Modifier.size(14.dp),
                            tint = EmeraldLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            sortOption.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextPrimary
                        )
                    }
                }
            }
        }

        // Transaction items
        if (sortedTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = DarkTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No transactions match current filters",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try selecting 'All Time' or clearing category/merchant filters.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(sortedTransactions) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedTxForDetails = tx }
                        .testTag("report_tx_item_${tx.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CategoryIconBox(category = tx.category, size = 40)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = tx.merchant,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${sdfDisplay.format(Date(tx.timestamp))} • ${tx.bankName} (${tx.accountNumber})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkTextSecondary
                                )
                            }
                        }

                        Text(
                            text = if (tx.type == TransactionType.DEBIT) "-${formatCurrency(tx.amount)}" else "+${formatCurrency(tx.amount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (tx.type == TransactionType.DEBIT) ExpenseRed else IncomeGreen
                        )
                    }
                }
            }
        }
    }
}
}

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = {
                Text("Save Report Preset", fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            },
            text = {
                Column {
                    Text(
                        "Save this combination of date range, categories, and filters for quick access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        placeholder = { Text("e.g. Weekend Food & Dining", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPresetName.isNotBlank()) {
                            viewModel.saveCurrentFilterPreset(newPresetName)
                            newPresetName = ""
                            showSavePresetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text("Save", color = Color(0xFF002113), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurfaceContainer,
            titleContentColor = DarkTextPrimary,
            textContentColor = DarkTextSecondary
        )
    }

    // Transaction Details & ML Feedback Sheet/Dialog
    selectedTxForDetails?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTxForDetails = null },
            title = {
                Text(tx.merchant, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Amount: ${formatCurrency(tx.amount)} (${tx.type.displayName})",
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == TransactionType.DEBIT) ExpenseRed else IncomeGreen
                    )
                    Text("Category: ${tx.category.title}", color = DarkTextPrimary)
                    Text("Bank: ${tx.bankName} (${tx.accountNumber})", color = DarkTextSecondary)
                    Text("Date: ${sdfDisplay.format(Date(tx.timestamp))}", color = DarkTextSecondary)
                    if (tx.balanceAfter != null) {
                        Text("Balance After: ${formatCurrency(tx.balanceAfter)}", color = DarkTextSecondary)
                    }
                    if (!tx.rawSmsBody.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Raw SMS Content:", style = MaterialTheme.typography.labelSmall, color = DarkTextMuted)
                        Surface(
                            color = DarkSurfaceContainerHigh,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tx.rawSmsBody,
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedTxForDetails = null }) {
                    Text("Close", color = EmeraldLight, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceContainer,
            titleContentColor = DarkTextPrimary,
            textContentColor = DarkTextSecondary
        )
    }
}
