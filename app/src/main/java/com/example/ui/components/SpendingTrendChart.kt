package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.MonthlyCategorySpend
import com.example.data.model.MonthlyOutflowDataPoint
import com.example.data.model.SixMonthSpendingTrend
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceContainer
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

enum class ChartViewType(val label: String, val icon: ImageVector) {
    STACKED_BAR("Stacked", Icons.Default.BarChart),
    GROUPED_BAR("Grouped", Icons.Default.Splitscreen),
    TREND_LINE("Area Curve", Icons.Default.ShowChart)
}

/**
 * Recharts-inspired Visual Monthly Spending Trend Chart
 * Compares total outflows and category breakdowns across the last six months.
 */
@Composable
fun MonthlySpendingTrendChartCard(
    trendData: SixMonthSpendingTrend,
    onNavigateToAnalytics: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedChartType by remember { mutableStateOf(ChartViewType.STACKED_BAR) }
    var selectedMonthIndex by remember(trendData) {
        // Default to the last month (most recent)
        mutableStateOf(if (trendData.months.isNotEmpty()) trendData.months.size - 1 else 0)
    }
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }

    val activeMonth = trendData.months.getOrNull(selectedMonthIndex)
    val textMeasurer = rememberTextMeasurer()

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, DarkOutlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_spending_trend_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Title, Subtitle, and Chart Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "6-Month Outflow Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF064E3B),
                            border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Recharts",
                                color = EmeraldLight,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (selectedCategoryFilter != null)
                            "Filtered by ${selectedCategoryFilter?.title} over 6 months"
                        else
                            "Comparing multi-category outflows over last 6 months",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        fontSize = 11.sp
                    )
                }

                // Chart View Mode Selector Pills
                Surface(
                    color = DarkSurfaceContainer,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkOutlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        ChartViewType.values().forEach { mode ->
                            val isSelected = selectedChartType == mode
                            Surface(
                                onClick = { selectedChartType = mode },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EmeraldPrimary else Color.Transparent,
                                modifier = Modifier.testTag("chart_mode_${mode.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = mode.icon,
                                        contentDescription = mode.label,
                                        tint = if (isSelected) Color(0xFF04201A) else Slate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Summary Stats Strip (Total 6-Mo Outflow, Average, Peak)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceContainerLow)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "6-MO TOTAL OUTFLOW",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        fontSize = 9.sp
                    )
                    Text(
                        text = formatCurrency(trendData.totalSixMonthOutflow),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Slate700)
                )

                Column {
                    Text(
                        text = "MONTHLY AVG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        fontSize = 9.sp
                    )
                    Text(
                        text = formatCurrency(trendData.monthlyAverageOutflow),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Slate700)
                )

                Column {
                    Text(
                        text = "TOP CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        fontSize = 9.sp
                    )
                    Text(
                        text = trendData.topSpendingCategoryOverall?.title ?: "N/A",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (trendData.topSpendingCategoryOverall != null)
                            Color(trendData.topSpendingCategoryOverall.hexColor)
                        else Slate200,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Interactive Chart Canvas Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF090D16))
                    .border(BorderStroke(1.dp, Color(0xFF1E293B)), RoundedCornerShape(12.dp))
                    .padding(top = 16.dp, bottom = 8.dp, start = 8.dp, end = 12.dp)
            ) {
                RechartsTrendCanvas(
                    trendData = trendData,
                    chartType = selectedChartType,
                    selectedMonthIndex = selectedMonthIndex,
                    selectedCategory = selectedCategoryFilter,
                    onSelectMonth = { selectedMonthIndex = it },
                    textMeasurer = textMeasurer,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Interactive Tooltip Card (Reveals Breakdown for Selected Month)
            AnimatedContent(
                targetState = activeMonth,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                label = "tooltip_transition"
            ) { monthData ->
                if (monthData != null) {
                    RechartsInteractiveTooltip(
                        monthData = monthData,
                        monthlyAverage = trendData.monthlyAverageOutflow,
                        selectedCategory = selectedCategoryFilter,
                        onCategoryClick = { cat ->
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        }
                    )
                }
            }

            // Category Legend & Filter Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FILTER CATEGORY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                    if (selectedCategoryFilter != null) {
                        Text(
                            text = "Reset to All",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldLight,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { selectedCategoryFilter = null }
                                .padding(4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // All Categories Pill
                    val isAllSelected = selectedCategoryFilter == null
                    Surface(
                        onClick = { selectedCategoryFilter = null },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isAllSelected) EmeraldPrimary else DarkSurfaceContainer,
                        border = BorderStroke(1.dp, if (isAllSelected) EmeraldLight else DarkOutlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAllSelected) Color(0xFF04201A) else EmeraldLight)
                            )
                            Text(
                                text = "All Categories",
                                color = if (isAllSelected) Color(0xFF04201A) else Slate200,
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    // Individual Categories from 6-Month Data
                    trendData.activeCategories.forEach { category ->
                        val isSelected = selectedCategoryFilter == category
                        val catColor = Color(category.hexColor)
                        Surface(
                            onClick = {
                                selectedCategoryFilter = if (isSelected) null else category
                            },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) catColor.copy(alpha = 0.25f) else DarkSurfaceContainer,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) catColor else DarkOutlineVariant
                            ),
                            modifier = Modifier.testTag("chart_legend_${category.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Text(
                                    text = category.title,
                                    color = if (isSelected) Color.White else Slate300,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Native Jetpack Compose Canvas rendering Stacked Bars, Grouped Bars, and Spline Curves
 * with Recharts-grade reference lines, horizontal gridlines, and interactive column selection.
 */
@Composable
private fun RechartsTrendCanvas(
    trendData: SixMonthSpendingTrend,
    chartType: ChartViewType,
    selectedMonthIndex: Int,
    selectedCategory: ExpenseCategory?,
    onSelectMonth: (Int) -> Unit,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier
) {
    val months = trendData.months
    if (months.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No transactions found in last 6 months", color = Slate400, fontSize = 12.sp)
        }
        return
    }

    // Determine the max spend ceiling for the Y-Axis
    val rawMaxSpend = if (selectedCategory != null) {
        months.maxOfOrNull { it.categoryOutflows[selectedCategory] ?: 0.0 } ?: 100.0
    } else {
        months.maxOfOrNull { it.totalOutflow } ?: 100.0
    }
    val maxSpend = max(rawMaxSpend * 1.15, 100.0) // 15% headroom for aesthetic spacing

    // Recharts Palette
    val gridColor = Color(0xFF1E293B)
    val axisTextColor = Color(0xFF64748B)
    val highlightColColor = Color(0xFF1E293B).copy(alpha = 0.6f)
    val referenceLineColor = EmeraldLight.copy(alpha = 0.5f)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(months, selectedCategory) {
                detectTapGestures { offset ->
                    val totalWidth = size.width
                    val leftPadding = 48.dp.toPx()
                    val rightPadding = 12.dp.toPx()
                    val chartWidth = totalWidth - leftPadding - rightPadding
                    val columnWidth = chartWidth / months.size

                    if (offset.x >= leftPadding && offset.x <= totalWidth - rightPadding) {
                        val index = ((offset.x - leftPadding) / columnWidth).toInt()
                            .coerceIn(0, months.size - 1)
                        onSelectMonth(index)
                    }
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val leftAxisMargin = 46.dp.toPx()
        val rightMargin = 8.dp.toPx()
        val bottomMargin = 22.dp.toPx()
        val topMargin = 12.dp.toPx()

        val plotWidth = canvasWidth - leftAxisMargin - rightMargin
        val plotHeight = canvasHeight - topMargin - bottomMargin
        val columnStep = plotWidth / months.size

        // 1. Draw Horizontal Dashed Grid Lines and Y-Axis Labels
        val gridSteps = 4
        for (i in 0..gridSteps) {
            val ratio = i.toFloat() / gridSteps.toFloat()
            val yPos = topMargin + (plotHeight * (1f - ratio))
            val valueAtStep = maxSpend * ratio

            // Dashed Gridline
            drawLine(
                color = gridColor,
                start = Offset(leftAxisMargin, yPos),
                end = Offset(canvasWidth - rightMargin, yPos),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Y-Axis Currency Label
            val labelText = formatCompactCurrency(valueAtStep)
            val measuredText = textMeasurer.measure(
                text = labelText,
                style = TextStyle(color = axisTextColor, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            )
            drawText(
                textLayoutResult = measuredText,
                topLeft = Offset(
                    x = leftAxisMargin - measuredText.size.width - 6.dp.toPx(),
                    y = yPos - (measuredText.size.height / 2f)
                )
            )
        }

        // 2. Draw Recharts-like Benchmark Reference Line (6-Month Average Outflow)
        if (selectedCategory == null && trendData.monthlyAverageOutflow > 0) {
            val avgRatio = (trendData.monthlyAverageOutflow / maxSpend).toFloat().coerceIn(0f, 1f)
            val avgY = topMargin + (plotHeight * (1f - avgRatio))

            drawLine(
                color = referenceLineColor,
                start = Offset(leftAxisMargin, avgY),
                end = Offset(canvasWidth - rightMargin, avgY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )

            // Inline Reference Badge
            val avgBadgeText = "Avg: ${formatCompactCurrency(trendData.monthlyAverageOutflow)}"
            val measuredAvg = textMeasurer.measure(
                text = avgBadgeText,
                style = TextStyle(color = EmeraldLight, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            )
            drawText(
                textLayoutResult = measuredAvg,
                topLeft = Offset(
                    x = canvasWidth - rightMargin - measuredAvg.size.width - 4.dp.toPx(),
                    y = avgY - measuredAvg.size.height - 2.dp.toPx()
                )
            )
        }

        // 3. Highlight Column Background for the selected month
        if (selectedMonthIndex in months.indices) {
            val highlightX = leftAxisMargin + (selectedMonthIndex * columnStep)
            drawRoundRect(
                color = highlightColColor,
                topLeft = Offset(highlightX + 4.dp.toPx(), topMargin),
                size = Size(columnStep - 8.dp.toPx(), plotHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
        }

        // 4. Render Data Visualizations by Chart Mode
        when (chartType) {
            ChartViewType.STACKED_BAR -> {
                drawStackedBars(
                    months = months,
                    selectedCategory = selectedCategory,
                    maxSpend = maxSpend,
                    leftAxisMargin = leftAxisMargin,
                    topMargin = topMargin,
                    plotHeight = plotHeight,
                    columnStep = columnStep,
                    selectedMonthIndex = selectedMonthIndex
                )
            }
            ChartViewType.GROUPED_BAR -> {
                drawGroupedBars(
                    months = months,
                    activeCategories = trendData.activeCategories,
                    selectedCategory = selectedCategory,
                    maxSpend = maxSpend,
                    leftAxisMargin = leftAxisMargin,
                    topMargin = topMargin,
                    plotHeight = plotHeight,
                    columnStep = columnStep
                )
            }
            ChartViewType.TREND_LINE -> {
                drawAreaSplineTrend(
                    months = months,
                    selectedCategory = selectedCategory,
                    activeCategories = trendData.activeCategories,
                    maxSpend = maxSpend,
                    leftAxisMargin = leftAxisMargin,
                    topMargin = topMargin,
                    plotHeight = plotHeight,
                    columnStep = columnStep,
                    selectedMonthIndex = selectedMonthIndex
                )
            }
        }

        // 5. Draw X-Axis Month Labels
        months.forEachIndexed { index, monthData ->
            val isSelected = index == selectedMonthIndex
            val colCenterX = leftAxisMargin + (index * columnStep) + (columnStep / 2f)

            val monthLabel = monthData.shortLabel
            val measuredLabel = textMeasurer.measure(
                text = monthLabel,
                style = TextStyle(
                    color = if (isSelected) EmeraldLight else Slate400,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            )

            drawText(
                textLayoutResult = measuredLabel,
                topLeft = Offset(
                    x = colCenterX - (measuredLabel.size.width / 2f),
                    y = topMargin + plotHeight + 6.dp.toPx()
                )
            )
        }
    }
}

/**
 * Draws Recharts-grade Stacked Bar Chart with rounded tops and category color slices
 */
private fun DrawScope.drawStackedBars(
    months: List<MonthlyOutflowDataPoint>,
    selectedCategory: ExpenseCategory?,
    maxSpend: Double,
    leftAxisMargin: Float,
    topMargin: Float,
    plotHeight: Float,
    columnStep: Float,
    selectedMonthIndex: Int
) {
    val barWidth = (columnStep * 0.52f).coerceIn(16.dp.toPx(), 44.dp.toPx())

    months.forEachIndexed { index, monthData ->
        val isSelected = index == selectedMonthIndex
        val colCenterX = leftAxisMargin + (index * columnStep) + (columnStep / 2f)
        val barLeft = colCenterX - (barWidth / 2f)

        if (selectedCategory != null) {
            // Single Category Isolated Bar
            val catSpend = monthData.categoryOutflows[selectedCategory] ?: 0.0
            val barHeight = ((catSpend / maxSpend).toFloat() * plotHeight).coerceAtLeast(0f)
            val barTop = topMargin + plotHeight - barHeight

            if (barHeight > 0f) {
                val catColor = Color(selectedCategory.hexColor)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(catColor, catColor.copy(alpha = 0.7f))
                    ),
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        } else {
            // Full Stacked Bar (all categories stacked vertically from bottom up)
            val totalMonthOutflow = monthData.totalOutflow
            if (totalMonthOutflow > 0) {
                var currentAccumulatedHeight = 0f

                // Draw slices in order of categories
                val sortedBreakdown = monthData.categoryBreakdownList.filter { it.amount > 0 }
                val totalSlices = sortedBreakdown.size

                sortedBreakdown.forEachIndexed { sliceIndex, slice ->
                    val sliceHeight = ((slice.amount / maxSpend).toFloat() * plotHeight)
                    val sliceTop = topMargin + plotHeight - currentAccumulatedHeight - sliceHeight
                    val isTopSlice = sliceIndex == 0 // largest on top

                    val sliceColor = Color(slice.category.hexColor)

                    if (isTopSlice && totalSlices == 1) {
                        drawRoundRect(
                            color = sliceColor,
                            topLeft = Offset(barLeft, sliceTop),
                            size = Size(barWidth, sliceHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    } else if (isTopSlice) {
                        // Rounded only at top
                        drawRoundRect(
                            color = sliceColor,
                            topLeft = Offset(barLeft, sliceTop),
                            size = Size(barWidth, sliceHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    } else {
                        // Middle/bottom flat slices
                        drawRect(
                            color = sliceColor,
                            topLeft = Offset(barLeft, sliceTop),
                            size = Size(barWidth, sliceHeight)
                        )
                    }

                    currentAccumulatedHeight += sliceHeight
                }
            } else {
                // Empty month zero-line indicator
                drawRect(
                    color = Slate700,
                    topLeft = Offset(barLeft, topMargin + plotHeight - 2.dp.toPx()),
                    size = Size(barWidth, 2.dp.toPx())
                )
            }
        }
    }
}

/**
 * Draws Grouped/Clustered Bar Chart for multi-category side-by-side comparison
 */
private fun DrawScope.drawGroupedBars(
    months: List<MonthlyOutflowDataPoint>,
    activeCategories: List<ExpenseCategory>,
    selectedCategory: ExpenseCategory?,
    maxSpend: Double,
    leftAxisMargin: Float,
    topMargin: Float,
    plotHeight: Float,
    columnStep: Float
) {
    val categoriesToShow = if (selectedCategory != null) {
        listOf(selectedCategory)
    } else {
        activeCategories.take(4) // top 4 active categories for clean grouped layout
    }

    if (categoriesToShow.isEmpty()) return

    val totalGroupWidth = columnStep * 0.75f
    val singleBarWidth = (totalGroupWidth / categoriesToShow.size).coerceIn(4.dp.toPx(), 14.dp.toPx())
    val groupMargin = (totalGroupWidth - (singleBarWidth * categoriesToShow.size)) / 2f

    months.forEachIndexed { monthIndex, monthData ->
        val colLeft = leftAxisMargin + (monthIndex * columnStep) + ((columnStep - totalGroupWidth) / 2f)

        categoriesToShow.forEachIndexed { catIndex, cat ->
            val catSpend = monthData.categoryOutflows[cat] ?: 0.0
            val barHeight = ((catSpend / maxSpend).toFloat() * plotHeight).coerceAtLeast(0f)
            val barTop = topMargin + plotHeight - barHeight
            val barLeft = colLeft + (catIndex * singleBarWidth)

            if (barHeight > 0f) {
                drawRoundRect(
                    color = Color(cat.hexColor),
                    topLeft = Offset(barLeft, barTop),
                    size = Size(singleBarWidth - 2.dp.toPx(), barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}

/**
 * Draws Smooth Bézier Spline Curves with Gradient Fill under the curve and vertex nodes
 */
private fun DrawScope.drawAreaSplineTrend(
    months: List<MonthlyOutflowDataPoint>,
    selectedCategory: ExpenseCategory?,
    activeCategories: List<ExpenseCategory>,
    maxSpend: Double,
    leftAxisMargin: Float,
    topMargin: Float,
    plotHeight: Float,
    columnStep: Float,
    selectedMonthIndex: Int
) {
    val points = months.mapIndexed { index, monthData ->
        val spend = if (selectedCategory != null) {
            monthData.categoryOutflows[selectedCategory] ?: 0.0
        } else {
            monthData.totalOutflow
        }
        val x = leftAxisMargin + (index * columnStep) + (columnStep / 2f)
        val y = topMargin + plotHeight - ((spend / maxSpend).toFloat() * plotHeight)
        Offset(x, y)
    }

    if (points.isEmpty()) return

    val primaryColor = if (selectedCategory != null) {
        Color(selectedCategory.hexColor)
    } else {
        EmeraldPrimary
    }

    // 1. Build smooth cubic Bézier spline
    val linePath = Path()
    val areaPath = Path()

    linePath.moveTo(points.first().x, points.first().y)
    areaPath.moveTo(points.first().x, topMargin + plotHeight)
    areaPath.lineTo(points.first().x, points.first().y)

    for (i in 0 until points.size - 1) {
        val current = points[i]
        val next = points[i + 1]
        val controlPoint1 = Offset(current.x + (next.x - current.x) / 2f, current.y)
        val controlPoint2 = Offset(current.x + (next.x - current.x) / 2f, next.y)

        linePath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            next.x, next.y
        )
        areaPath.cubicTo(
            controlPoint1.x, controlPoint1.y,
            controlPoint2.x, controlPoint2.y,
            next.x, next.y
        )
    }

    areaPath.lineTo(points.last().x, topMargin + plotHeight)
    areaPath.close()

    // 2. Draw Area Gradient
    drawPath(
        path = areaPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.35f),
                primaryColor.copy(alpha = 0.03f)
            ),
            startY = topMargin,
            endY = topMargin + plotHeight
        )
    )

    // 3. Draw Spline Line
    drawPath(
        path = linePath,
        color = primaryColor,
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // 4. Draw Vertex Nodes
    points.forEachIndexed { index, pt ->
        val isSelected = index == selectedMonthIndex
        val radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()

        // Outer glow/ring if selected
        if (isSelected) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                radius = radius + 4.dp.toPx(),
                center = pt
            )
        }

        drawCircle(
            color = Color(0xFF090D16),
            radius = radius,
            center = pt
        )
        drawCircle(
            color = primaryColor,
            radius = radius - 1.5.dp.toPx(),
            center = pt
        )
    }
}

/**
 * Recharts Floating Tooltip Card displaying detailed categorical breakdown and benchmark comparison
 */
@Composable
private fun RechartsInteractiveTooltip(
    monthData: MonthlyOutflowDataPoint,
    monthlyAverage: Double,
    selectedCategory: ExpenseCategory?,
    onCategoryClick: (ExpenseCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0B132B),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tooltip Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = monthData.fullLabel.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldLight,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = formatCurrency(monthData.totalOutflow),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Variance vs 6-Mo Avg Badge
                if (monthlyAverage > 0) {
                    val delta = monthData.totalOutflow - monthlyAverage
                    val deltaPct = (delta / monthlyAverage) * 100.0
                    val isHigher = delta > 0

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isHigher) Color(0xFF3B1219) else Color(0xFF052E16),
                        border = BorderStroke(1.dp, if (isHigher) ExpenseRed.copy(alpha = 0.4f) else IncomeGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isHigher) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isHigher) ExpenseRed else IncomeGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            val sign = if (deltaPct > 0) "+" else ""
                            Text(
                                text = "$sign${String.format(Locale.getDefault(), "%.1f", deltaPct)}% vs avg",
                                color = if (isHigher) ExpenseRed else IncomeGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

            // Category Slices in this month
            if (monthData.categoryBreakdownList.isEmpty() || monthData.totalOutflow <= 0) {
                Text(
                    text = "No category expenditures recorded for this month.",
                    color = Slate400,
                    fontSize = 11.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    monthData.categoryBreakdownList.filter { it.amount > 0 }.forEach { catSpend ->
                        val isHighlighted = selectedCategory == null || selectedCategory == catSpend.category
                        val catColor = Color(catSpend.category.hexColor)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoryClick(catSpend.category) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Text(
                                    text = catSpend.category.title,
                                    color = if (isHighlighted) Color.White else Slate500,
                                    fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "${(catSpend.percentageOfMonthlyTotal * 100).roundToInt()}%",
                                    color = Slate400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatCurrency(catSpend.amount),
                                    color = if (isHighlighted) EmeraldLight else Slate400,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact currency formatter for Axis labels (e.g. $1.5k, $500, $0)
 */
private fun formatCompactCurrency(amount: Double): String {
    return when {
        amount >= 1_000_000 -> "$${String.format(Locale.getDefault(), "%.1f", amount / 1_000_000)}M"
        amount >= 1_000 -> "$${String.format(Locale.getDefault(), "%.1f", amount / 1_000)}k"
        else -> "$${amount.toInt()}"
    }
}
