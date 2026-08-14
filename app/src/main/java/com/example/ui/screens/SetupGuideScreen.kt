package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PrivacyShieldBadge
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

data class SetupStepItem(
    val number: Int,
    val title: String,
    val summary: String,
    val detail: String,
    val icon: ImageVector,
    val actionLabel: String,
    val settingIntent: String
)

val SYSTEM_SETUP_STEPS = listOf(
    SetupStepItem(
        number = 1,
        title = "Grant SMS Permissions",
        summary = "RECEIVE_SMS & READ_SMS",
        detail = "Allows meeCrebit's BroadcastReceiver to intercept transactional receipts from your bank in real-time.",
        icon = Icons.Default.Sms,
        actionLabel = "Open App Permissions",
        settingIntent = "APP_SETTINGS"
    ),
    SetupStepItem(
        number = 2,
        title = "Disable Battery Optimization (Doze)",
        summary = "Unrestricted Battery Mode",
        detail = "Prevents Android Doze Mode from killing the background receiver when your screen is locked during transactions.",
        icon = Icons.Default.BatteryAlert,
        actionLabel = "Exempt Battery Saver",
        settingIntent = "BATTERY_SETTINGS"
    ),
    SetupStepItem(
        number = 3,
        title = "Enable Autostart / Background Launch",
        summary = "OEM Specific (MIUI / OneUI / OxygenOS)",
        detail = "Aggressive OEM battery managers (Xiaomi, Samsung, OnePlus, Realme) kill background receivers on device reboot without Autostart enabled.",
        icon = Icons.Default.PowerSettingsNew,
        actionLabel = "Open Device Settings",
        settingIntent = "APP_SETTINGS"
    ),
    SetupStepItem(
        number = 4,
        title = "Notification Channel Priority",
        summary = "Instant Spend Alerts",
        detail = "Enables meeCrebit to show an instant heads-up card whenever a debit occurs so you can catch unauthorized charges.",
        icon = Icons.Default.NotificationsActive,
        actionLabel = "Configure Notifications",
        settingIntent = "NOTIFICATION_SETTINGS"
    ),
    SetupStepItem(
        number = 5,
        title = "100% Offline Privacy Guarantee",
        summary = "Zero Network Calls & Internet Off",
        detail = "meeCrebit has ZERO internet permissions declared in AndroidManifest. It works completely offline and will never transmit telemetry.",
        icon = Icons.Default.Shield,
        actionLabel = "Verified On-Device",
        settingIntent = "NONE"
    )
)

@Composable
fun SetupGuideScreen(
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedOemTab by remember { mutableStateOf("Samsung") }

    val oemGuides = mapOf(
        "Samsung" to "Settings → Apps → meeCrebit → Battery → Select 'Unrestricted'. Also ensure 'Put unused apps to sleep' is toggled off.",
        "Xiaomi / Poco" to "Settings → Apps → Manage Apps → meeCrebit → Enable 'Autostart' toggle + set Battery Saver to 'No restrictions'.",
        "OnePlus" to "Settings → Battery → Battery Optimization → meeCrebit → Choose 'Don't Optimize'.",
        "Stock / Pixel" to "Settings → Apps → All Apps → meeCrebit → App Battery Usage → Set to 'Unrestricted'."
    )

    fun openSetting(intentType: String) {
        try {
            when (intentType) {
                "APP_SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                "BATTERY_SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                "NOTIFICATION_SETTINGS" -> {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("guide_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back to Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Android Setup Guide",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "5 steps for 100% background intercept reliability",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldLight
                    )
                }
            }
        }

        // OEM Instructions Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Manufacturer Battery Killer Workarounds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        oemGuides.keys.forEach { oem ->
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedOemTab = oem }
                                    .testTag("oem_tab_$oem"),
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedOemTab == oem) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedOemTab == oem) EmeraldPrimary else SlateBorder
                                )
                            ) {
                                Text(
                                    text = oem,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedOemTab == oem) EmeraldLight else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = Color(0xFF061410),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = oemGuides[selectedOemTab] ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldLight,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 5 Core Steps
        items(SYSTEM_SETUP_STEPS) { step ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("setup_step_${step.number}")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = EmeraldPrimary.copy(alpha = 0.15f),
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${step.number}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldLight
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = step.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = step.summary,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldLight
                                )
                            }
                        }

                        Icon(
                            imageVector = step.icon,
                            contentDescription = step.title,
                            tint = EmeraldLight,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = step.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    if (step.settingIntent != "NONE") {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { openSetting(step.settingIntent) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open",
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.actionLabel,
                                color = EmeraldLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
