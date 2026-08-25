package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.security.BiometricAuthManager
import com.example.ui.components.BudgetsWalletIcon
import com.example.ui.components.HomeHouseIcon
import com.example.ui.components.LockScreenOverlay
import com.example.ui.components.ReportsBoxIcon
import com.example.ui.components.SettingsGearIcon
import com.example.ui.components.TerminalPromptIcon
import com.example.ui.screens.BillRemindersScreen
import com.example.ui.screens.BudgetScreen
import com.example.ui.screens.CustomReportsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavingsGoalsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SettingsSubPage
import com.example.ui.screens.SmsStudioScreen
import com.example.ui.screens.SplitExpensesScreen
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.MeeCrebitTheme
import com.example.viewmodel.FinanceViewModel

enum class MeeCrebitNavTab(
    val title: String,
    val testTag: String
) {
    HOME("Home", "nav_home"),
    BUDGET("Budget", "nav_budget"),
    REPORTS("Reports", "nav_reports"),
    SETTINGS("Settings", "nav_settings")
}

enum class ActiveOverlayScreen {
    NONE,
    SAVINGS_GOALS,
    SPLIT_EXPENSES,
    BILL_REMINDERS
}

class MainActivity : FragmentActivity() {
    private var dynamicSmsReceiver: com.example.receiver.SmsBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register dynamic SMS receiver and ContentObserver for real-time automatic detection
        registerDynamicSmsReceiver()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            com.example.engine.SmsInboxObserver.startObserving(this)
        }

        setContent {
            MeeCrebitTheme {
                val viewModel: FinanceViewModel = viewModel()
                MeeCrebitAppRoot(
                    viewModel = viewModel,
                    onTriggerUnlock = {
                        BiometricAuthManager.getInstance(this).authenticate(
                            activity = this,
                            title = "Unlock meeCrebit Ledger",
                            subtitle = "Verify biometric or device credential to view accounts",
                            onSuccess = {
                                viewModel.unlockApp()
                            },
                            onError = { errorCode, errString ->
                                viewModel.setBiometricErrorMessage(errString.toString())
                            },
                            onFailed = {
                                viewModel.setBiometricErrorMessage("Authentication failed. Please try again.")
                            }
                        )
                    }
                )
            }
        }
    }

    private fun registerDynamicSmsReceiver() {
        try {
            if (dynamicSmsReceiver == null) {
                dynamicSmsReceiver = com.example.receiver.SmsBroadcastReceiver()
                val filter = android.content.IntentFilter(android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION).apply {
                    priority = 999
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(dynamicSmsReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
                } else {
                    registerReceiver(dynamicSmsReceiver, filter)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to register dynamic SMS receiver: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            dynamicSmsReceiver?.let {
                unregisterReceiver(it)
                dynamicSmsReceiver = null
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun MeeCrebitAppRoot(
    viewModel: FinanceViewModel,
    onTriggerUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MeeCrebitNavTab.HOME) }
    var activeOverlay by remember { mutableStateOf(ActiveOverlayScreen.NONE) }
    var targetSettingsSubPage by remember { mutableStateOf(SettingsSubPage.MAIN) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val biometricErrorMessage by viewModel.biometricErrorMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Notifications permitted
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.recordAppPaused()
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.checkAppResumeLock()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 16.dp),
                    color = Color(0xFF111418),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MeeCrebitNavTab.values().forEach { tab ->
                            val isSelected = activeOverlay == ActiveOverlayScreen.NONE && selectedTab == tab

                            // Lively Spring bounce animation when active
                            val iconScale by animateFloatAsState(
                                targetValue = if (isSelected) 1.12f else 1.0f,
                                animationSpec = spring(
                                    dampingRatio = 0.55f,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "tab_scale_${tab.name}"
                            )

                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) EmeraldLight else Color(0xFF8E9BAE),
                                animationSpec = tween(durationMillis = 250),
                                label = "tab_color_${tab.name}"
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        activeOverlay = ActiveOverlayScreen.NONE
                                        selectedTab = tab
                                    }
                                    .padding(vertical = 4.dp)
                                    .testTag(tab.testTag)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .scale(iconScale)
                                ) {
                                    when (tab) {
                                        MeeCrebitNavTab.HOME -> HomeHouseIcon(
                                            tint = contentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        MeeCrebitNavTab.BUDGET -> BudgetsWalletIcon(
                                            tint = contentColor,
                                            modifier = Modifier.size(23.dp)
                                        )
                                        MeeCrebitNavTab.REPORTS -> ReportsBoxIcon(
                                            tint = contentColor,
                                            modifier = Modifier.size(23.dp)
                                        )
                                        MeeCrebitNavTab.SETTINGS -> SettingsGearIcon(
                                            tint = contentColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = tab.title,
                                    color = contentColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = activeOverlay,
                    transitionSpec = {
                        if (targetState != ActiveOverlayScreen.NONE) {
                            // Slide in overlay screen smoothly from bottom
                            (slideInVertically(
                                animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                                initialOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() }
                            ) + fadeIn(tween(220, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.96f, animationSpec = tween(220, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    fadeOut(tween(160, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.96f, animationSpec = tween(160, easing = FastOutSlowInEasing))
                                )
                        } else {
                            // Dismiss back to main screen
                            (fadeIn(tween(200, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.98f, animationSpec = tween(200, easing = FastOutSlowInEasing)))
                                .togetherWith(
                                    slideOutVertically(
                                        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
                                        targetOffsetY = { fullHeight -> (fullHeight * 0.12f).toInt() }
                                    ) + fadeOut(tween(180, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.96f, animationSpec = tween(180, easing = FastOutSlowInEasing))
                                )
                        }
                    },
                    label = "overlay_screen_transition"
                ) { overlay ->
                    when (overlay) {
                        ActiveOverlayScreen.SAVINGS_GOALS -> {
                            SavingsGoalsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { activeOverlay = ActiveOverlayScreen.NONE }
                            )
                        }
                        ActiveOverlayScreen.SPLIT_EXPENSES -> {
                            SplitExpensesScreen(
                                viewModel = viewModel,
                                onNavigateBack = { activeOverlay = ActiveOverlayScreen.NONE }
                            )
                        }
                        ActiveOverlayScreen.BILL_REMINDERS -> {
                            BillRemindersScreen(
                                viewModel = viewModel,
                                onNavigateBack = { activeOverlay = ActiveOverlayScreen.NONE }
                            )
                        }
                        ActiveOverlayScreen.NONE -> {
                            AnimatedContent(
                                targetState = selectedTab,
                                transitionSpec = {
                                    val isForward = targetState.ordinal > initialState.ordinal
                                    val slideOffset = 0.08f
                                    if (isForward) {
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                            initialOffsetX = { fullWidth -> (fullWidth * slideOffset).toInt() }
                                        ) + fadeIn(tween(durationMillis = 180, easing = FastOutSlowInEasing)))
                                            .togetherWith(
                                                slideOutHorizontally(
                                                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                                    targetOffsetX = { fullWidth -> -(fullWidth * slideOffset).toInt() }
                                                ) + fadeOut(tween(durationMillis = 140, easing = FastOutSlowInEasing))
                                            )
                                    } else {
                                        (slideInHorizontally(
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                            initialOffsetX = { fullWidth -> -(fullWidth * slideOffset).toInt() }
                                        ) + fadeIn(tween(durationMillis = 180, easing = FastOutSlowInEasing)))
                                            .togetherWith(
                                                slideOutHorizontally(
                                                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                                    targetOffsetX = { fullWidth -> (fullWidth * slideOffset).toInt() }
                                                ) + fadeOut(tween(durationMillis = 140, easing = FastOutSlowInEasing))
                                            )
                                    }.using(SizeTransform(clip = false))
                                },
                                label = "tab_navigation"
                            ) { targetTab ->
                                when (targetTab) {
                                    MeeCrebitNavTab.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToSmsStudio = {
                                            targetSettingsSubPage = SettingsSubPage.SMS_STUDIO
                                            selectedTab = MeeCrebitNavTab.SETTINGS
                                        },
                                        onNavigateToBudgets = { selectedTab = MeeCrebitNavTab.BUDGET },
                                        onNavigateToReports = { selectedTab = MeeCrebitNavTab.REPORTS },
                                        onNavigateToSettings = {
                                            targetSettingsSubPage = SettingsSubPage.MAIN
                                            selectedTab = MeeCrebitNavTab.SETTINGS
                                        },
                                        onNavigateToSavings = {
                                            activeOverlay = ActiveOverlayScreen.SAVINGS_GOALS
                                        },
                                        onNavigateToSplits = {
                                            activeOverlay = ActiveOverlayScreen.SPLIT_EXPENSES
                                        },
                                        onNavigateToBills = {
                                            activeOverlay = ActiveOverlayScreen.BILL_REMINDERS
                                        }
                                    )
                                    MeeCrebitNavTab.BUDGET -> BudgetScreen(
                                        viewModel = viewModel
                                    )
                                    MeeCrebitNavTab.REPORTS -> CustomReportsScreen(
                                        viewModel = viewModel
                                    )
                                    MeeCrebitNavTab.SETTINGS -> SettingsScreen(
                                        viewModel = viewModel,
                                        initialSubPage = targetSettingsSubPage,
                                        onNavigateBack = { selectedTab = MeeCrebitNavTab.HOME }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Biometric Security Lock Overlay (Covers the UI completely when app is locked)
        LockScreenOverlay(
            isLocked = isAppLocked,
            onTriggerUnlock = onTriggerUnlock,
            errorMessage = biometricErrorMessage
        )
    }
}

