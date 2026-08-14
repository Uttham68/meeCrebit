package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.theme.TrueBlackBackground
import com.example.viewmodel.FinanceViewModel

enum class SettingsSubPage {
    MAIN,
    SMS_STUDIO,
    SETUP_GUIDE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: (() -> Unit)? = null,
    initialSubPage: SettingsSubPage = SettingsSubPage.MAIN,
    modifier: Modifier = Modifier
) {
    var currentSubPage by remember(initialSubPage) { mutableStateOf(initialSubPage) }

    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "settings_subpage_anim"
    ) { subPage ->
        when (subPage) {
            SettingsSubPage.SMS_STUDIO -> {
                SmsStudioScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentSubPage = SettingsSubPage.MAIN },
                    modifier = modifier
                )
            }
            SettingsSubPage.SETUP_GUIDE -> {
                SetupGuideScreen(
                    onNavigateBack = { currentSubPage = SettingsSubPage.MAIN },
                    modifier = modifier
                )
            }
            SettingsSubPage.MAIN -> {
                SettingsMainView(
                    viewModel = viewModel,
                    onOpenSmsStudio = { currentSubPage = SettingsSubPage.SMS_STUDIO },
                    onOpenSetupGuide = { currentSubPage = SettingsSubPage.SETUP_GUIDE },
                    onNavigateBack = onNavigateBack,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun SettingsMainView(
    viewModel: FinanceViewModel,
    onOpenSmsStudio: () -> Unit,
    onOpenSetupGuide: () -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val lockTimeoutSec by viewModel.lockTimeoutSeconds.collectAsStateWithLifecycle()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var notificationPushEnabled by remember { mutableStateOf(true) }
    var budgetLimitPushEnabled by remember { mutableStateOf(true) }
    var autoScanOnLaunch by remember { mutableStateOf(true) }
    var mlConfidenceThreshold by remember { mutableFloatStateOf(92f) }
    var selectedCurrency by remember { mutableStateOf("₹ INR (India)") }

    val timeoutOptions = listOf(
        0L to "Immediately",
        30L to "30s",
        60L to "1 min",
        300L to "5 mins"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrueBlackBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Navigation Header
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
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = DarkTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary
                    )
                    Text(
                        text = "Vault security, diagnostics & device permissions",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldLight
                    )
                }
            }
        }

        // Dedicated Sub-page Hub: SMS Studio & ML Parser
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSmsStudio() }
                    .testTag("open_sms_studio_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "SMS & ML Studio",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "SMS Interceptor & ML Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "On-device NLP sandbox, custom SMS test parser & bank accuracy metrics",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = EmeraldLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Dedicated Sub-page Hub: Background Setup Guide
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSetupGuide() }
                    .testTag("open_setup_guide_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BatteryAlert,
                                    contentDescription = "Background Guide",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Android Setup & Battery Guide",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "5-step OEM checklist for 100% background intercept reliability",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Section 1: Security & Biometrics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_security_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vault Biometric Security",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Hardware-backed keystore lock & screen privacy",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Biometric Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require Biometric / PIN on Open",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Prompts fingerprint or device lock screen credentials",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextMuted
                            )
                        }
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF002113),
                                checkedTrackColor = EmeraldPrimary,
                                uncheckedThumbColor = DarkTextMuted,
                                uncheckedTrackColor = DarkSurfaceContainerHigh
                            ),
                            modifier = Modifier.testTag("settings_biometric_switch")
                        )
                    }

                    if (isBiometricEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Auto-Lock Timeout",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            timeoutOptions.forEach { (sec, label) ->
                                val isSelected = lockTimeoutSec == sec
                                Surface(
                                    onClick = { viewModel.setLockTimeout(sec) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) EmeraldPrimary else DarkSurfaceContainerHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) EmeraldLight else DarkOutlineVariant
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF002113) else DarkTextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.lockApp() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceContainerHigh),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmeraldLight.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = EmeraldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lock meeCrebit Vault Now",
                                color = EmeraldLight,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Real-time Notification Guard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_notifications_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Notification Guard",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "On-device transaction shade alerts & budget limit notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Instant Transaction Alerts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkTextPrimary
                        )
                        Switch(
                            checked = notificationPushEnabled,
                            onCheckedChange = { notificationPushEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF002113),
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Monthly Budget Over-limit Warning",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkTextPrimary
                        )
                        Switch(
                            checked = budgetLimitPushEnabled,
                            onCheckedChange = { budgetLimitPushEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF002113),
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testSendTransactionNotification() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, DarkOutlineVariant),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Txn Alert", fontSize = 11.sp, color = EmeraldLight)
                        }
                        OutlinedButton(
                            onClick = { viewModel.testSendBudgetExceededNotification() },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test Limit Alert", fontSize = 11.sp, color = ExpenseRed)
                        }
                    }
                }
            }
        }

        // Section 3: Engine Config
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Local ML Classifier Threshold",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Naive Bayes minimum confidence: ${mlConfidenceThreshold.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Slider(
                        value = mlConfidenceThreshold,
                        onValueChange = { mlConfidenceThreshold = it },
                        valueRange = 75f..99f,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldPrimary,
                            activeTrackColor = EmeraldPrimary,
                            inactiveTrackColor = DarkSurfaceContainerHigh
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-scan Inbox on Launch",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DarkTextPrimary
                        )
                        Switch(
                            checked = autoScanOnLaunch,
                            onCheckedChange = { autoScanOnLaunch = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF002113),
                                checkedTrackColor = EmeraldPrimary
                            )
                        )
                    }
                }
            }
        }

        // Section 4: Currency & Formats
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmeraldPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = null,
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Currency & Locale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Standard denomination formatting",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val currencies = listOf("₹ INR (India)", "$ USD (United States)", "€ EUR (Europe)", "£ GBP (UK)")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currencies.take(2).forEach { curr ->
                            val isSelected = selectedCurrency == curr
                            Surface(
                                onClick = { selectedCurrency = curr },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else DarkSurfaceContainerHigh,
                                border = BorderStroke(1.dp, if (isSelected) EmeraldPrimary else DarkOutlineVariant),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = curr,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) EmeraldLight else DarkTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 5: Data & Privacy Danger Zone
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceContainer),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, DarkOutlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ExpenseRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vault Storage & Danger Zone",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "100% on-device SQLite database actions",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wipe All Transactions & Reset Ledger",
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text("Confirm Ledger Reset", fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            },
            text = {
                Text(
                    "Are you sure you want to delete all transactions and reset your budget limits? This operation is permanent and cannot be undone.",
                    color = DarkTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete All Data", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurfaceContainer,
            titleContentColor = DarkTextPrimary,
            textContentColor = DarkTextSecondary
        )
    }
}
