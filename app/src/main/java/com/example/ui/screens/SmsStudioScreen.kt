package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ModelTraining
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType
import com.example.ui.components.CategoryIconBox
import com.example.ui.components.PrivacyShieldBadge
import com.example.ui.components.formatCurrency
import com.example.ui.theme.AccentGold
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SmsPreset(
    val title: String,
    val sender: String,
    val body: String,
    val tag: String = "Common"
)

val PRESET_SMS_TEMPLATES = listOf(
    SmsPreset(
        title = "HDFC Card (Swiggy)",
        sender = "HDFCBK",
        body = "Rs 420.00 spent on HDFC Bank Card ending 8492 at SWIGGY BANGALORE on 14-AUG-26. Avl bal: Rs 42,500.00.",
        tag = "Food & Dining"
    ),
    SmsPreset(
        title = "SBI UPI (Zepto Grocery)",
        sender = "SBIBNK",
        body = "Dear UPI user, A/C 4829 debited by Rs 780.00 on 14-Aug-26 transfer to ZEPTO QUICK COMMERCE. Ref 392819. Avail Bal Rs 18,250.00.",
        tag = "Groceries"
    ),
    SmsPreset(
        title = "ICICI Debit (Fuel)",
        sender = "ICICIB",
        body = "Your ICICI Bank Acct XX9102 debited for INR 2,500.00 on 14-Aug-26 towards INDIAN OIL PETROL PUMP. Available balance INR 35,210.00.",
        tag = "Transport"
    ),
    SmsPreset(
        title = "Axis Bank (Amazon India)",
        sender = "AXISBK",
        body = "Axis Bank: Alert! Rs 3,499.00 spent on your Credit Card XX1104 at AMAZON INDIA on 14-Aug-26. Avail Limit: Rs 95,000.00.",
        tag = "Shopping"
    ),
    SmsPreset(
        title = "Kotak Card (Cult.fit Gym)",
        sender = "KOTAKB",
        body = "Transaction alert: Rs 4,999.00 spent on Kotak Debit Card 4002 at CULT FIT GYM & FITNESS on 14-Aug-2026. Avl Bal: Rs 28,100.00.",
        tag = "Health & Fitness"
    ),
    SmsPreset(
        title = "Salary Deposit (Infosys)",
        sender = "HDFCBK",
        body = "Salary Credited! Rs 85,000.00 deposited into your HDFC Bank A/C XX9123 on 01-Aug-26 from INFOSYS TECHNOLOGIES. Total Bal: Rs 1,12,450.00.",
        tag = "Income"
    ),
    SmsPreset(
        title = "Paytm UPI (Zomato)",
        sender = "PAYTMB",
        body = "Paid Rs 349.00 using Paytm UPI on 14-Aug-26 to ZOMATO ONLINE. Txn ID: 8392019. Balance: Rs 5,420.00.",
        tag = "Food & Dining"
    ),
    SmsPreset(
        title = "Electricity Bill (BESCOM)",
        sender = "SBIBNK",
        body = "Transaction Alert: Rs 1,850.00 debited from A/C XX4829 for BESCOM ELECTRICITY BILL on 10-Aug-26.",
        tag = "Bills & Utilities"
    )
)

@Composable
fun SmsStudioScreen(
    viewModel: FinanceViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val parsedResult by viewModel.testSmsResult.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanningInbox.collectAsStateWithLifecycle()
    val modelMetrics by viewModel.modelMetrics.collectAsStateWithLifecycle()
    val isTrainingModel by viewModel.isTrainingModel.collectAsStateWithLifecycle()

    var inputSmsText by remember { mutableStateOf(PRESET_SMS_TEMPLATES[0].body) }
    var inputSender by remember { mutableStateOf(PRESET_SMS_TEMPLATES[0].sender) }
    var showReinforceDialog by remember { mutableStateOf(false) }
    var showModelDetailsDialog by remember { mutableStateOf(false) }

    // Check system permission status
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasSmsPermission = (perms[Manifest.permission.RECEIVE_SMS] == true || perms[Manifest.permission.READ_SMS] == true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
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
                        modifier = Modifier.testTag("sms_studio_back_btn")
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
                        text = "SMS Interceptor & ML Studio",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "On-device NLP machine learning parser & laboratory",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldLight
                    )
                }
            }
        }

        // On-Device Machine Learning Model Diagnostics Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ml_model_diagnostics_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "On-Device ML",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "On-Device ML Classifier",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Multinomial Naive Bayes • 100% Local",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldLight
                                )
                            }
                        }

                        Surface(
                            color = EmeraldDark,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${String.format(Locale.getDefault(), "%.1f", modelMetrics.accuracy)}% ACC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate800)
                    Spacer(modifier = Modifier.height(12.dp))

                    // ML Metrics 3-column stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Training Corpus", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                "${modelMetrics.totalTrainedSamples} samples",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Vocabulary Size", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                "${modelMetrics.vocabularySize} features",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Learned Merchants", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                "${modelMetrics.learnedMerchantsCount} patterns",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Train Model Button
                    Button(
                        onClick = { viewModel.trainLocalMlModel() },
                        enabled = !isTrainingModel,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("train_local_ml_button")
                    ) {
                        if (isTrainingModel) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF04201A),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Training on Device...", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = Icons.Default.ModelTraining,
                                contentDescription = null,
                                tint = Color(0xFF04201A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retrain & Reinforce ML Model", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Permission Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hasSmsPermission) Color(0xFF0F172A) else Color(0xFF1F1D1B)
                ),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(
                    1.dp,
                    if (hasSmsPermission) Color(0xFF10B981).copy(alpha = 0.4f) else AccentGold.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sms_permission_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasSmsPermission) Color(0xFF10B981).copy(alpha = 0.15f) else AccentGold.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (hasSmsPermission) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = "Security Status",
                                tint = if (hasSmsPermission) EmeraldLight else AccentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (hasSmsPermission) "Background SMS Interceptor Active" else "SMS Permissions Not Granted",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (hasSmsPermission) "All incoming bank SMS are auto-parsed offline." else "Tap to allow background transaction parsing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (hasSmsPermission) EmeraldLight else TextSecondary
                            )
                        }
                    }

                    if (!hasSmsPermission) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.READ_SMS
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("grant_sms_permission_button")
                        ) {
                            Text("Grant", color = Color(0xFF321200), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Preset Templates Carousel
        item {
            Column {
                Text(
                    text = "Try Real-World Bank SMS & Novel Merchants",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(PRESET_SMS_TEMPLATES) { preset ->
                        Surface(
                            modifier = Modifier
                                .clickable {
                                    inputSender = preset.sender
                                    inputSmsText = preset.body
                                    viewModel.testParseSms(preset.body, preset.sender)
                                }
                                .testTag("preset_${preset.title.replace(" ", "_")}"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (inputSmsText == preset.body) EmeraldPrimary.copy(alpha = 0.2f) else SlateSurfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (inputSmsText == preset.body) EmeraldPrimary else SlateBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inputSmsText == preset.body) EmeraldLight else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = preset.tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = if (preset.tag == "Novel Merchant") AccentGold else TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Parsing Testbed
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sms_testbed_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Interactive Parser & Heuristic Tester",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = inputSender,
                        onValueChange = {
                            inputSender = it
                            viewModel.testParseSms(inputSmsText, it)
                        },
                        label = { Text("SMS Sender ID") },
                        placeholder = { Text("e.g. HDFCBK, ICICIB, CHASE") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sms_sender_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = inputSmsText,
                        onValueChange = {
                            inputSmsText = it
                            viewModel.testParseSms(it, inputSender)
                        },
                        label = { Text("Raw SMS Text Body") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("sms_body_input"),
                        maxLines = 4
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testParseSms(inputSmsText, inputSender) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("run_parse_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Parse",
                                tint = Color(0xFF04201A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Classify with Local ML", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                inputSmsText = ""
                                viewModel.testParseSms("", "")
                            },
                            modifier = Modifier.weight(0.6f)
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }
        }

        // Extracted Structured Output Card with ML Confidence & Novel Merchant Discovery
        parsedResult?.let { result ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.isValidTransaction) Color(0xFF061A14) else Color(0xFF1E1515)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (result.isValidTransaction) EmeraldPrimary.copy(alpha = 0.5f) else ExpenseRed.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("parsed_output_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIconBox(category = result.category, size = 36)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = result.merchant,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = result.category.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = EmeraldLight
                                        )
                                        if (result.isNovelMerchant) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = AccentGold.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "✨ Novel Merchant",
                                                    fontSize = 10.sp,
                                                    color = AccentGold,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                text = if (result.type == TransactionType.DEBIT) "-${formatCurrency(result.amount)}" else "+${formatCurrency(result.amount)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (result.type == TransactionType.DEBIT) ExpenseRed else IncomeGreen
                            )
                        }

                        // ML Confidence & Feature Extraction Indicators
                        if (result.isValidTransaction) {
                            Surface(
                                color = Slate800,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("ML Prediction Confidence", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                        Text(
                                            "${String.format(Locale.getDefault(), "%.1f", result.mlConfidence * 100)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldLight
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { result.mlConfidence },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = EmeraldPrimary,
                                        trackColor = Slate900,
                                        strokeCap = StrokeCap.Round
                                    )

                                    if (result.matchedFeatures.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text("Features: ", style = MaterialTheme.typography.labelSmall, color = Slate400, fontSize = 10.sp)
                                            result.matchedFeatures.forEach { feat ->
                                                Surface(
                                                    color = Slate900,
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        feat,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFFA7F3D0),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Grid attributes extracted
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Bank Detected", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(result.bankName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Account / Card", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(result.accountNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Available Balance", style = MaterialTheme.typography.labelSmall, color = Slate400)
                                Text(
                                    if (result.balanceAfter != null) formatCurrency(result.balanceAfter) else "N/A",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldLight
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (result.isValidTransaction) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.saveParsedSmsToDb(result) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_parsed_sms_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = "Save",
                                        tint = Color(0xFF04201A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Commit & Train (+15 Zen)", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showReinforceDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("teach_ml_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Correct",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Teach ML")
                                }
                            }
                        } else {
                            Text(
                                text = "Could not extract standard bank debit/credit pattern. Adjust SMS template above.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Scan Historical Inbox
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(EmeraldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Scan Inbox",
                                tint = EmeraldLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Scan Device SMS Inbox",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Retroactively train ML on past receipts",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.scanExistingInbox() },
                        enabled = !isScanning,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("scan_inbox_button")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Scan", color = Color(0xFF04201A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Teach / Reinforce ML Dialog
    if (showReinforceDialog && parsedResult != null) {
        val result = parsedResult!!
        AlertDialog(
            onDismissRequest = { showReinforceDialog = false },
            title = { Text("Reinforce Local ML Model") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Teach the on-device model the correct category for \"${result.merchant}\". The model adjusts its TF-IDF keyword weights immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val catScroll = rememberScrollState()
                    Column(
                        modifier = Modifier.horizontalScroll(catScroll),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExpenseCategory.values().forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (result.category == cat) Color(cat.hexColor).copy(alpha = 0.2f) else SlateSurfaceVariant,
                                border = BorderStroke(1.dp, if (result.category == cat) Color(cat.hexColor) else SlateBorder),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.reinforceMlFeedback(result.rawBody, cat, result.merchant)
                                        // Re-parse with updated model
                                        viewModel.testParseSms(inputSmsText, inputSender)
                                        showReinforceDialog = false
                                    }
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CategoryIconBox(category = cat, size = 24)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReinforceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
