package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary

val MeeCrebitTeal = Color(0xFF2DD4BF)
val MeeCrebitGreen = Color(0xFF00D09C)

@Composable
fun MeeCrebitLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showContainer: Boolean = true,
    cornerShape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    if (showContainer) {
        Surface(
            shape = cornerShape,
            color = Color(0xFF000000),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = modifier.size(size)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                MeeCrebitStackedText(
                    meeFontSize = (size.value * 0.22f).sp,
                    crebitFontSize = (size.value * 0.36f).sp
                )
            }
        }
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
        ) {
            MeeCrebitStackedText(
                meeFontSize = (size.value * 0.22f).sp,
                crebitFontSize = (size.value * 0.36f).sp
            )
        }
    }
}

@Composable
fun MeeCrebitStackedText(
    meeFontSize: TextUnit = 14.sp,
    crebitFontSize: TextUnit = 24.sp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = "mee",
            color = Color(0xFFFFFFFF),
            fontWeight = FontWeight.Medium,
            fontSize = meeFontSize,
            letterSpacing = 0.5.sp,
            lineHeight = meeFontSize
        )
        Text(
            text = "Crebit",
            color = Color(0xFF00D09C),
            fontWeight = FontWeight.Bold,
            fontSize = crebitFontSize,
            letterSpacing = (-0.5).sp,
            lineHeight = crebitFontSize
        )
    }
}

@Composable
fun MeeCrebitBrandHeader(
    modifier: Modifier = Modifier,
    tagline: String? = "100% On-Device SMS Ledger"
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        MeeCrebitLogo(size = 40.dp, cornerShape = RoundedCornerShape(10.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "mee",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp
                )
                Text(
                    text = "Crebit",
                    color = Color(0xFF00D09C),
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp
                )
            }
            if (tagline != null) {
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }
    }
}
