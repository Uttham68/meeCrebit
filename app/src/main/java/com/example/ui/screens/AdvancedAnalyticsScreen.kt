package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdvancedAnalyticsState
import com.example.data.model.CategoryDeltaComparison
import com.example.data.model.CategoryForecast
import com.example.data.model.DeltaTrendStatus
import com.example.data.model.ExpenseCategory
import com.example.data.model.MonthOverMonthCategoryComparison
import com.example.data.model.RecurringSubscription
import com.example.data.model.SixMonthSpendingTrend
import com.example.data.model.SpendingForecast
import com.example.data.model.SubscriptionFrequency
import com.example.data.model.SubscriptionStatus
import com.example.data.model.SubscriptionsSummary
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.MeeCrebitBrandHeader
import com.example.ui.components.MonthlySpendingTrendChartCard
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
import com.example.ui.theme.Slate800
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class AnalyticsSubTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    SIX_MONTH_TRENDS("6-Mo Outflow", Icons.Default.BarChart),
    FORECAST("Forecast", Icons.Default.AutoGraph),
    SUBSCRIPTIONS("Subscriptions", Icons.Default.Repeat),
    MOM_TRENDS("MoM Comparison", Icons.Default.CompareArrows)
}

@Composable
fun AdvancedAnalyticsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val analyticsState by viewModel.advancedAnalyticsState.collectAsStateWithLifecycle()
    val selectedMonth by viewModel.selectedMonthYear.collectAsStateWithLifecycle()

    var selectedSubTab by remember { mutableStateOf(AnalyticsSubTab.FORECAST) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Branding & Security Header
        Surface(
            color = DarkSurfaceContainerLow,
            border = BorderStroke(1.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MeeCrebitBrandHeader(tagline = "Advanced AI & Spending Insights")

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF04201A),
                        border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% On-Device",
                                color = EmeraldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceContainer,
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AnalyticsSubTab.values().forEach { tab ->
                            val isSelected = selectedSubTab == tab
                            Surface(
                                onClick = { selectedSubTab = tab },
                                shape = RoundedCornerShape(9.dp),
                                color = if (isSelected) EmeraldPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("analytics_tab_${tab.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFF04201A) else Slate400,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) Color(0xFF04201A) else Slate400,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SubTab Content
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = selectedSubTab,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(140)) },
                label = "analytics_sub_tab"
            ) { targetTab ->
                when (targetTab) {
                    AnalyticsSubTab.SIX_MONTH_TRENDS -> SixMonthTrendSection(
                        trend = analyticsState.sixMonthTrend,
                        modifier = Modifier.fillMaxSize()
                    )
                    AnalyticsSubTab.FORECAST -> SpendingForecastSection(
                        forecast = analyticsState.spendingForecast,
                        modifier = Modifier.fillMaxSize()
                    )
                    AnalyticsSubTab.SUBSCRIPTIONS -> SubscriptionsSection(
                        summary = analyticsState.subscriptionsSummary,
                        modifier = Modifier.fillMaxSize()
                    )
                    AnalyticsSubTab.MOM_TRENDS -> MonthOverMonthSection(
                        momComparison = analyticsState.momComparison,
                        onSelectMonth = { /* Month is controlled via unified month context */ },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 1: SPENDING FORECAST
// -----------------------------------------------------------------------------
@Composable
fun SpendingForecastSection(
    forecast: SpendingForecast,
    modifier: Modifier = Modifier
) {
    var simulatedSavingsSlider by remember { mutableFloatStateOf(0f) }
    val simulatedSavingsAmount = (forecast.discretionaryProjected * (simulatedSavingsSlider / 100f))
    val simulatedNewTotal = (forecast.projectedTotal - simulatedSavingsAmount).coerceAtLeast(forecast.fixedRecurringBaseline)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Forecast Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceContainerLow,
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("spending_forecast_hero_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoGraph,
                                        contentDescription = null,
                                        tint = EmeraldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "NEXT MONTH FORECAST",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = forecast.nextMonthFormatted,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                            }
                        }

                        // Confidence Tag
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SlateSurfaceVariant,
                            border = BorderStroke(1.dp, SlateBorder)
                        ) {
                            Text(
                                text = "${forecast.confidencePercent}% Confidence",
                                color = EmeraldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Projected Amount Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = formatCurrency(forecast.projectedTotal),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Text(
                                text = "Expected Range: ${formatCurrency(forecast.projectedLowerBound)} – ${formatCurrency(forecast.projectedUpperBound)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Trend Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (forecast.momTrendSlopePercent > 0) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                if (forecast.momTrendSlopePercent > 0) ExpenseRed.copy(alpha = 0.4f) else IncomeGreen.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (forecast.momTrendSlopePercent > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (forecast.momTrendSlopePercent > 0) ExpenseRed else IncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (forecast.momTrendSlopePercent > 0) "+" else ""}${forecast.momTrendSlopePercent.roundToInt()}% pace",
                                    color = if (forecast.momTrendSlopePercent > 0) ExpenseRed else IncomeGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = SlateBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Fixed vs Discretionary Split
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fixed Baseline (Subscriptions/Bills)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCurrency(forecast.fixedRecurringBaseline),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Discretionary (Variable)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatCurrency(forecast.discretionaryProjected),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual Progress Split Bar
                    val fixedRatio = if (forecast.projectedTotal > 0) (forecast.fixedRecurringBaseline / forecast.projectedTotal).toFloat() else 0f
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        val width = size.width
                        val height = size.height
                        val fixedWidth = width * fixedRatio

                        drawRoundRect(
                            color = Color(0xFF3B82F6),
                            topLeft = Offset(0f, 0f),
                            size = Size(fixedWidth, height),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                        drawRoundRect(
                            color = AccentGold,
                            topLeft = Offset(fixedWidth, 0f),
                            size = Size(width - fixedWidth, height),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }
        }

        // Smart Predictive Drivers & Insights
        if (forecast.keyDrivers.isNotEmpty() || forecast.savingsInsights.isNotEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceContainer,
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Forecast Drivers & Advice",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        forecast.keyDrivers.forEach { driver ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = EmeraldLight, fontWeight = FontWeight.Bold)
                                Text(
                                    text = driver,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        forecast.savingsInsights.forEach { saving ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("💡 ", fontSize = 12.sp)
                                Text(
                                    text = saving,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive "What-If" Savings Goal Simulator
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceContainerLow,
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WHAT-IF SAVINGS SIMULATOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                        if (simulatedSavingsSlider > 0) {
                            Text(
                                text = "-${simulatedSavingsSlider.roundToInt()}% Discretionary",
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Slide to simulate discretionary trimming on your forecast:",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextMuted
                    )

                    Slider(
                        value = simulatedSavingsSlider,
                        onValueChange = { simulatedSavingsSlider = it },
                        valueRange = 0f..35f,
                        steps = 6,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldLight,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Simulated Spend",
                                style = MaterialTheme.typography.labelSmall,
                                color = Slate400
                            )
                            Text(
                                text = formatCurrency(simulatedNewTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (simulatedSavingsAmount > 0) EmeraldLight else DarkTextPrimary
                            )
                        }

                        if (simulatedSavingsAmount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EmeraldPrimary.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "Bank +${formatCurrency(simulatedSavingsAmount)}!",
                                    color = EmeraldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category-by-Category Forecast Breakdown
        item {
            Text(
                text = "PROJECTED SPENDING BY CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(forecast.categoryForecasts) { catForecast ->
            CategoryForecastCard(
                forecast = catForecast,
                totalProjected = forecast.projectedTotal
            )
        }
    }
}

@Composable
fun CategoryForecastCard(
    forecast: CategoryForecast,
    totalProjected: Double
) {
    val progress = if (totalProjected > 0) (forecast.projectedAmount / totalProjected).toFloat() else 0f

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceContainerLow,
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBox(category = forecast.category, size = 32)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = forecast.category.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Text(
                            text = "Hist. avg: ${formatCurrency(forecast.historicalAverage)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(forecast.projectedAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    if (forecast.momTrendPercent != 0.0) {
                        Text(
                            text = "${if (forecast.isIncreasing) "▲ +" else "▼ "}${forecast.momTrendPercent.roundToInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (forecast.isIncreasing) ExpenseRed else IncomeGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(forecast.category.hexColor),
                trackColor = SlateSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = forecast.rationale,
                style = MaterialTheme.typography.labelSmall,
                color = Slate400,
                fontSize = 10.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 2: RECURRING SUBSCRIPTIONS
// -----------------------------------------------------------------------------
@Composable
fun SubscriptionsSection(
    summary: SubscriptionsSummary,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }

    val filteredSubscriptions = remember(summary.subscriptions, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) summary.subscriptions
        else summary.subscriptions.filter { it.category == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Header Card
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceContainerLow,
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("subscriptions_summary_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECURRING SUBSCRIPTION BURDEN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF04201A),
                            border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${summary.activeCount} Detected",
                                color = EmeraldLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = formatCurrency(summary.totalMonthlyBurden),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldLight
                            )
                            Text(
                                text = "Monthly Recurring Total",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatCurrency(summary.totalAnnualCost),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                            Text(
                                text = "Annual Commitment",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextMuted
                            )
                        }
                    }

                    if (summary.dueSoonCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ExpenseRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${summary.dueSoonCount} subscription(s) renewing within the next 7 days.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${summary.activeCount})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color(0xFF04201A),
                            containerColor = SlateSurfaceVariant,
                            labelColor = Slate400
                        )
                    )
                }

                items(summary.categoryBreakdown.keys.toList()) { category ->
                    FilterChip(
                        selected = selectedCategoryFilter == category,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == category) null else category
                        },
                        label = { Text(category.title) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            selectedLabelColor = Color(0xFF04201A),
                            containerColor = SlateSurfaceVariant,
                            labelColor = Slate400
                        )
                    )
                }
            }
        }

        // Subscriptions List
        if (filteredSubscriptions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceContainer,
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Subscriptions Detected Yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "As SMS debit alerts from Netflix, Airtel, Spotify, SIPs, or recurring utilities arrive, meeCrebit will automatically identify their cadences locally.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredSubscriptions) { sub ->
                SubscriptionItemCard(subscription = sub)
            }
        }
    }
}

@Composable
fun SubscriptionItemCard(subscription: RecurringSubscription) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val formattedNextDate = remember(subscription.nextExpectedBillingDate) {
        sdf.format(Date(subscription.nextExpectedBillingDate))
    }
    val formattedLastDate = remember(subscription.lastBilledDate) {
        sdf.format(Date(subscription.lastBilledDate))
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceContainerLow,
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subscription_card_${subscription.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBox(category = subscription.category, size = 34)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = subscription.merchant,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            if (subscription.isKnownService) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = EmeraldPrimary.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "VERIFIED",
                                        color = EmeraldLight,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${subscription.frequency.displayName} • ${subscription.occurrences} billing cycles detected",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatCurrency(subscription.averageAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "${formatCurrency(subscription.annualizedCost)}/yr",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.6f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (subscription.status) {
                        SubscriptionStatus.DUE_SOON -> ExpenseRed
                        SubscriptionStatus.OVERDUE -> AccentGold
                        else -> EmeraldLight
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Next Renewal: $formattedNextDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Last: $formattedLastDate",
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkTextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 3: MONTH-OVER-MONTH CATEGORY COMPARISON
// -----------------------------------------------------------------------------
@Composable
fun MonthOverMonthSection(
    momComparison: MonthOverMonthCategoryComparison,
    onSelectMonth: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Comparison Banner
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = DarkSurfaceContainerLow,
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mom_comparison_hero_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MONTH-OVER-MONTH COMPARISON",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SlateSurfaceVariant,
                            border = BorderStroke(1.dp, SlateBorder)
                        ) {
                            Text(
                                text = "${momComparison.previousMonthFormatted} ➔ ${momComparison.currentMonthFormatted}",
                                color = DarkTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${if (momComparison.totalDeltaAmount > 0) "+" else ""}${formatCurrency(momComparison.totalDeltaAmount)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (momComparison.totalDeltaAmount > 0) ExpenseRed else IncomeGreen
                            )
                            Text(
                                text = "${momComparison.currentMonthFormatted}: ${formatCurrency(momComparison.currentTotalSpent)} vs ${momComparison.previousMonthFormatted}: ${formatCurrency(momComparison.previousTotalSpent)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DarkTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (momComparison.totalDeltaAmount > 0) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f),
                            border = BorderStroke(
                                1.dp,
                                if (momComparison.totalDeltaAmount > 0) ExpenseRed.copy(alpha = 0.4f) else IncomeGreen.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "${if (momComparison.totalDeltaPercent > 0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", momComparison.totalDeltaPercent)}%",
                                color = if (momComparison.totalDeltaAmount > 0) ExpenseRed else IncomeGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = SlateBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = momComparison.primaryDriverText,
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Highlights Strip: Top Surges vs Top Savings
        if (momComparison.topSurges.isNotEmpty() || momComparison.topSavings.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (momComparison.topSurges.isNotEmpty()) {
                        val topSurge = momComparison.topSurges.first()
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceContainer,
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = ExpenseRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Top Surge",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = topSurge.category.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                                Text(
                                    text = "+${formatCurrency(topSurge.deltaAmount)} (+${topSurge.deltaPercent.roundToInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (momComparison.topSavings.isNotEmpty()) {
                        val topSaving = momComparison.topSavings.first()
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceContainer,
                            border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = IncomeGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Top Savings",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = IncomeGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = topSaving.category.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkTextPrimary
                                )
                                Text(
                                    text = "-${formatCurrency(abs(topSaving.deltaAmount))} (${abs(topSaving.deltaPercent).roundToInt()}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Delta List Header
        item {
            Text(
                text = "CATEGORY BREAKDOWN (${momComparison.previousMonthFormatted} vs ${momComparison.currentMonthFormatted})",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        // Category Comparative Cards
        items(momComparison.categories) { catDelta ->
            CategoryDeltaCard(catDelta = catDelta)
        }
    }
}

@Composable
fun CategoryDeltaCard(catDelta: CategoryDeltaComparison) {
    val maxSpend = kotlin.math.max(catDelta.currentSpent, catDelta.previousSpent).coerceAtLeast(1.0)
    val currentRatio = (catDelta.currentSpent / maxSpend).toFloat()
    val previousRatio = (catDelta.previousSpent / maxSpend).toFloat()

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceContainerLow,
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBox(category = catDelta.category, size = 30)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = catDelta.category.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                }

                // Delta Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = catDelta.status.badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, catDelta.status.badgeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (catDelta.deltaAmount != 0.0) {
                            "${if (catDelta.deltaAmount > 0) "+" else ""}${formatCurrency(catDelta.deltaAmount)} (${if (catDelta.deltaPercent > 0) "+" else ""}${catDelta.deltaPercent.roundToInt()}%)"
                        } else "0% (Equal)",
                        color = catDelta.status.badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comparative Dual Bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Current Month Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp,
                        modifier = Modifier.width(64.dp)
                    )
                    LinearProgressIndicator(
                        progress = { currentRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = EmeraldLight,
                        trackColor = SlateSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCurrency(catDelta.currentSpent),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary,
                        fontSize = 11.sp,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                }

                // Previous Month Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Month",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400,
                        fontSize = 10.sp,
                        modifier = Modifier.width(64.dp)
                    )
                    LinearProgressIndicator(
                        progress = { previousRatio },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF64748B),
                        trackColor = SlateSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatCurrency(catDelta.previousSpent),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Slate400,
                        fontSize = 11.sp,
                        modifier = Modifier.width(60.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// SECTION 4: 6-MONTH SPENDING TRENDS (RECHARTS VISUAL OUTFLOW)
// -----------------------------------------------------------------------------
@Composable
fun SixMonthTrendSection(
    trend: SixMonthSpendingTrend,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Spotlight Visual Trend Chart
        item {
            MonthlySpendingTrendChartCard(
                trendData = trend
            )
        }

        // Summary Insights Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Peak Month Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainerLow),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "HIGHEST SPEND MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            fontSize = 9.sp
                        )
                        Text(
                            text = trend.highestSpendMonth?.fullLabel ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = formatCurrency(trend.highestSpendMonth?.totalOutflow ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ExpenseRed
                        )
                    }
                }

                // Lowest Spend Month Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainerLow),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LOWEST SPEND MONTH",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate400,
                            fontSize = 9.sp
                        )
                        Text(
                            text = trend.lowestSpendMonth?.fullLabel ?: "N/A",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = formatCurrency(trend.lowestSpendMonth?.totalOutflow ?: 0.0),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = IncomeGreen
                        )
                    }
                }
            }
        }

        // Active Category Multi-Month Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "6-MONTH CATEGORY CUMULATIVE VOLUME",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )

                    trend.activeCategories.forEach { category ->
                        val catTotal = trend.months.sumOf { it.categoryOutflows[category] ?: 0.0 }
                        val pctOfTotal = if (trend.totalSixMonthOutflow > 0)
                            (catTotal / trend.totalSixMonthOutflow).toFloat()
                        else 0f

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(category.hexColor))
                                    )
                                    Text(
                                        text = category.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${(pctOfTotal * 100).roundToInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                    Text(
                                        text = formatCurrency(catTotal),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldLight
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { pctOfTotal },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(category.hexColor),
                                trackColor = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }
    }
}
