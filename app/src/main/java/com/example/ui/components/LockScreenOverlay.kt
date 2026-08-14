package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkOutlineVariant
import com.example.ui.theme.DarkSurfaceContainerLow
import com.example.ui.theme.DarkTextMuted
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.TrueBlackBackground

@Composable
fun LockScreenOverlay(
    isLocked: Boolean,
    onTriggerUnlock: () -> Unit,
    errorMessage: String? = null,
    onBypassUnlock: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isLocked,
        enter = fadeIn(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(250)),
        modifier = modifier
    ) {
        // Auto trigger unlock prompt once shown
        LaunchedEffect(isLocked) {
            if (isLocked) {
                onTriggerUnlock()
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TrueBlackBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // Consume clicks so underlying UI cannot be interacted with
                }
                .testTag("biometric_lock_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Placeholder (Spacing / Shield)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Shield",
                        tint = EmeraldLight.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "meeCrebit Vault",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextSecondary
                    )
                }

                // Center Biometric Core matching Screenshot 7 & HTML prototype
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Biometric Icon / Glowing Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(128.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldPrimary.copy(alpha = 0.22f),
                                        Color(0xFF0E1511),
                                        Color.Transparent
                                    )
                                )
                            )
                            .clickable { onTriggerUnlock() }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = DarkSurfaceContainerLow,
                            border = BorderStroke(1.2.dp, DarkOutlineVariant),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(108.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                // Subtle inner glow layer
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldPrimary.copy(alpha = 0.12f))
                                )
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Fingerprint Sensor",
                                    tint = EmeraldLight,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "meeCrebit Vault Locked",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Authenticate to access your secure financial overview.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            color = ExpenseRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color(0xFFFCA5A5),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Primary Action Button: "Unlock with Biometrics"
                    Button(
                        onClick = onTriggerUnlock,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color(0xFF002113)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = EmeraldPrimary)
                            .testTag("biometric_unlock_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Unlock",
                                tint = Color(0xFF002113),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Unlock with Biometrics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF002113)
                            )
                        }
                    }

                    if (errorMessage != null && onBypassUnlock != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = onBypassUnlock,
                            modifier = Modifier.testTag("emergency_unlock_button")
                        ) {
                            Text(
                                text = "Bypass to Ledger (Sensor Fallback)",
                                color = EmeraldLight,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Footer Privacy Notice matching Stitch Prototype
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Encryption Badge",
                        tint = DarkTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "END-TO-END ENCRYPTED",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextMuted,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}
