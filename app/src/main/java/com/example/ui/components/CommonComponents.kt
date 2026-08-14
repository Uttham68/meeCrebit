package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeHouseIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.16f)
            lineTo(w * 0.86f, h * 0.46f)
            lineTo(w * 0.74f, h * 0.46f)
            lineTo(w * 0.74f, h * 0.84f)
            lineTo(w * 0.26f, h * 0.84f)
            lineTo(w * 0.26f, h * 0.46f)
            lineTo(w * 0.14f, h * 0.46f)
            close()
        }
        drawPath(
            path = path,
            color = tint
        )
    }
}

@Composable
fun TerminalPromptIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        val cornerRadius = 4.dp.toPx()

        // Outer box
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2, stroke / 2 + h * 0.05f),
            size = Size(w - stroke, h * 0.90f - stroke),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = stroke)
        )

        // Chevron '>'
        val path = Path().apply {
            moveTo(w * 0.26f, h * 0.36f)
            lineTo(w * 0.44f, h * 0.50f)
            lineTo(w * 0.26f, h * 0.64f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Underscore '_'
        drawLine(
            color = tint,
            start = Offset(w * 0.52f, h * 0.64f),
            end = Offset(w * 0.74f, h * 0.64f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun BudgetsWalletIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        val cornerRadius = 4.dp.toPx()

        // Outer wallet box
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2, stroke / 2 + h * 0.08f),
            size = Size(w - stroke, h * 0.84f - stroke),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = stroke)
        )

        // Wallet snap flap on right
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.62f, h * 0.36f),
            size = Size(w * 0.38f - stroke / 2, h * 0.28f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Small circle dot in flap
        drawCircle(
            color = tint,
            radius = 1.2.dp.toPx(),
            center = Offset(w * 0.74f, h * 0.50f)
        )
    }
}

@Composable
fun ReportsBoxIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.8.dp.toPx()
        val cornerRadius = 4.dp.toPx()

        // Outer box
        drawRoundRect(
            color = tint,
            topLeft = Offset(stroke / 2, stroke / 2 + h * 0.05f),
            size = Size(w - stroke, h * 0.90f - stroke),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = stroke)
        )

        // 3 Vertical Bars
        val barWidth = 2.dp.toPx()
        val bottomY = h * 0.72f

        // Bar 1 (medium)
        drawLine(
            color = tint,
            start = Offset(w * 0.33f, bottomY),
            end = Offset(w * 0.33f, h * 0.48f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )

        // Bar 2 (short)
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, bottomY),
            end = Offset(w * 0.50f, h * 0.58f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )

        // Bar 3 (tall)
        drawLine(
            color = tint,
            start = Offset(w * 0.67f, bottomY),
            end = Offset(w * 0.67f, h * 0.34f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun MerchantAvatar(
    merchantName: String,
    category: ExpenseCategory? = null,
    size: Int = 46,
    modifier: Modifier = Modifier
) {
    val initial = remember(merchantName) {
        val trimmed = merchantName.trim()
        if (trimmed.isNotEmpty()) {
            trimmed.first().uppercase()
        } else "T"
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E2228)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.40).sp
        )
    }
}

fun formatRelativeTimestamp(timestamp: Long): String {
    val nowCal = Calendar.getInstance()
    val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))

    val isSameDay = nowCal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR)

    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterdayCal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) &&
            yesterdayCal.get(Calendar.DAY_OF_YEAR) == txCal.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameDay -> "Today, $timeFormat"
        isYesterday -> "Yesterday, $timeFormat"
        nowCal.get(Calendar.YEAR) == txCal.get(Calendar.YEAR) -> {
            val dateFormat = SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(timestamp))
            dateFormat
        }
        else -> {
            val dateFormat = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault()).format(Date(timestamp))
            dateFormat
        }
    }
}

@Composable
fun PrivacyShieldBadge(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Surface(
        color = Color(0xFFD1FAE5),
        shape = RoundedCornerShape(50.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFFA7F3D0)
        ),
        modifier = modifier.testTag("privacy_shield_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981))
                    .alpha(alphaAnim)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "LOCAL ONLY",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF047857),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun CategoryIconBox(
    category: ExpenseCategory,
    size: Int = 42
) {
    val catColor = Color(category.hexColor)
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(catColor.copy(alpha = 0.12f))
            .border(1.dp, catColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.getIcon(),
            contentDescription = category.title,
            tint = catColor,
            modifier = Modifier.size((size * 0.52).dp)
        )
    }
}

@Composable
fun SleekProgressBar(
    progress: Float,
    isOverBudget: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    val progressColor = when {
        isOverBudget -> ExpenseRed
        progress >= 0.8f -> AccentGold
        else -> EmeraldPrimary
    }

    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
        color = progressColor,
        trackColor = Slate100,
        strokeCap = StrokeCap.Round
    )
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatCurrency(amount: Double): String {
    val isNegative = amount < 0
    val absAmount = kotlin.math.abs(amount)
    val formatted = String.format(Locale("en", "IN"), "₹%,.2f", absAmount)
    return if (isNegative) "-$formatted" else formatted
}

