package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActivityClassification
import com.example.ui.theme.AuroraBgPrimary
import com.example.ui.theme.AuroraBgSecondary
import com.example.ui.theme.AuroraBgTertiary
import com.example.ui.theme.AuroraBorder
import com.example.ui.theme.AuroraBorderHighlight
import com.example.ui.theme.AuroraElectricBlue
import com.example.ui.theme.AuroraPinkHighlight
import com.example.ui.theme.AuroraPrimary
import com.example.ui.theme.AuroraSecondary
import com.example.ui.theme.AuroraSoftCyan
import com.example.ui.theme.AuroraSurfaceCard
import com.example.ui.theme.AuroraTextMuted
import com.example.ui.theme.AuroraTextPrimary
import com.example.ui.theme.AuroraTextSecondary
import com.example.ui.theme.AuroraVioletGlow
import com.example.ui.theme.DaylogMotion
import com.example.ui.theme.NeutralAurora
import com.example.ui.theme.NeutralAuroraBg
import com.example.ui.theme.PrimaryAuroraBrush
import com.example.ui.theme.ProductiveAurora
import com.example.ui.theme.ProductiveAuroraBg
import com.example.ui.theme.UnproductiveAurora
import com.example.ui.theme.UnproductiveAuroraBg
import com.example.ui.theme.UntrackedAurora
import com.example.ui.theme.UntrackedAuroraBg
import kotlin.math.cos
import kotlin.math.sin

// ==========================================
// Aurora Atmospheric Background (PRD §60, 61, 103)
// 3 non-synchronized, organic drifting light fields
// Isolated background layer prevents any child recompositions
// ==========================================
@Composable
fun AuroraAtmosphericCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_ambient_master")

    // Drift Field 1 (Violet-Indigo core, 8s loop)
    val phase1State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = DaylogMotion.OrganicAtmosphere),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    // Drift Field 2 (Electric Cyan-Blue secondary, 11s loop)
    val phase2State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = DaylogMotion.OrganicAtmosphere),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    // Drift Field 3 (Soft Pink-Magenta ambient pool, 14s loop)
    val phase3State = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = DaylogMotion.OrganicAtmosphere),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    // Breathing luminance pulse (4.5s cycle)
    val pulseState = infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = DaylogMotion.OrganicAtmosphere),
            repeatMode = RepeatMode.Reverse
        ),
        label = "luminous_pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val w = size.width
        val h = size.height
        val phase1 = phase1State.value
        val phase2 = phase2State.value
        val phase3 = phase3State.value
        val pulse = pulseState.value

        // Orb 1: Upper left orbital drift
        val x1 = w * (0.22f + 0.08f * cos(phase1.toDouble()).toFloat())
        val y1 = h * (0.15f + 0.06f * sin(phase1.toDouble()).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraPrimary.copy(alpha = 0.20f * pulse),
                    AuroraBgSecondary.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                center = Offset(x1, y1),
                radius = w * 0.72f * pulse
            ),
            radius = w * 0.72f * pulse,
            center = Offset(x1, y1)
        )

        // Orb 2: Center right Lissajous drift
        val x2 = w * (0.82f + 0.07f * sin(phase2.toDouble()).toFloat())
        val y2 = h * (0.42f + 0.09f * cos(phase2.toDouble() * 1.5).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraElectricBlue.copy(alpha = 0.14f * pulse),
                    AuroraVioletGlow.copy(alpha = 0.06f),
                    Color.Transparent
                ),
                center = Offset(x2, y2),
                radius = w * 0.68f * pulse
            ),
            radius = w * 0.68f * pulse,
            center = Offset(x2, y2)
        )

        // Orb 3: Bottom ambient pool
        val x3 = w * (0.35f + 0.06f * cos(phase3.toDouble()).toFloat())
        val y3 = h * (0.86f + 0.05f * sin(phase3.toDouble()).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AuroraSecondary.copy(alpha = 0.12f),
                    AuroraPinkHighlight.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(x3, y3),
                radius = w * 0.65f
            ),
            radius = w * 0.65f,
            center = Offset(x3, y3)
        )
    }
}

@Composable
fun AuroraAtmosphericBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AuroraBgPrimary)
    ) {
        AuroraAtmosphericCanvas()
        content()
    }
}

// ==========================================
// Aurora Luminous Orb Graphic (PRD §14, 94)
// ==========================================
@Composable
fun AuroraOrb(
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    sizeDp: Dp = 100.dp,
    coreColor: Color = AuroraPrimary,
    glowColor: Color = AuroraSecondary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_glow")
    val scale by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.94f else 0.98f,
        targetValue = if (isActive) 1.06f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 3500 else 6000, easing = DaylogMotion.OrganicAtmosphere),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = if (isActive) 0.28f else 0.12f,
        targetValue = if (isActive) 0.52f else 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isActive) 3500 else 6000, easing = DaylogMotion.OrganicAtmosphere),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = modifier.size(sizeDp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    glowColor.copy(alpha = alphaGlow),
                    coreColor.copy(alpha = alphaGlow * 0.4f),
                    Color.Transparent
                ),
                center = center,
                radius = radius * scale
            ),
            radius = radius * scale,
            center = center
        )

        // Core soft glowing sphere
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (isActive) 0.45f else 0.20f),
                    coreColor.copy(alpha = 0.88f),
                    AuroraBgTertiary.copy(alpha = 0.95f)
                ),
                center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                radius = radius * 0.80f
            ),
            radius = radius * 0.72f,
            center = center
        )

        // Thin luminous rim
        drawCircle(
            color = Color.White.copy(alpha = if (isActive) 0.40f else 0.20f),
            radius = radius * 0.72f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
        )
    }
}

// ==========================================
// Aurora Card (Glass & Translucent Surfaces)
// PRD §64-66: Tactile response on press
// ==========================================
@Composable
fun BrutalistCard(
    modifier: Modifier = Modifier,
    borderColor: Color = AuroraBorder,
    backgroundColor: Color = AuroraSurfaceCard,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(22.dp),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val resolvedBorderColor = if (isHighlighted) AuroraBorderHighlight else borderColor
    val resolvedBgColor = if (backgroundColor == MaterialTheme.colorScheme.surface || backgroundColor == AuroraSurfaceCard) {
        Color(0xDD101026) // Deep translucent indigo glass
    } else {
        backgroundColor
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.985f else 1.0f,
        animationSpec = DaylogMotion.microTween(),
        label = "card_press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(shape)
            .background(resolvedBgColor, shape)
            .border(BorderStroke(borderWidth, resolvedBorderColor), shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(contentPadding),
        content = content
    )
}

// ==========================================
// Aurora Primary & Secondary Buttons
// PRD §10-12: Instant Press (scale 1 -> 0.97) + Snappy Release (90-130ms)
// ==========================================
@Composable
fun BrutalistButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isDestructive: Boolean = false,
    isSecondary: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1.0f,
        animationSpec = DaylogMotion.microTween(DaylogMotion.SnappyInteraction),
        label = "button_scale"
    )

    if (isDestructive) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = UnproductiveAurora.copy(alpha = 0.2f),
                contentColor = UnproductiveAurora,
                disabledContainerColor = UnproductiveAurora.copy(alpha = 0.1f),
                disabledContentColor = UnproductiveAurora.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, UnproductiveAurora.copy(alpha = 0.4f)),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            modifier = modifier
                .height(48.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else if (isPrimary) {
        Box(
            modifier = modifier
                .height(48.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (enabled) {
                        if (isPressed) {
                            Brush.linearGradient(
                                listOf(
                                    AuroraPrimary.copy(alpha = 0.95f),
                                    AuroraElectricBlue.copy(alpha = 0.95f)
                                )
                            )
                        } else PrimaryAuroraBrush
                    } else Brush.linearGradient(listOf(Color(0xFF33334E), Color(0xFF33334E)))
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (isPressed && enabled) AuroraBorderHighlight else Color.Transparent
                    ),
                    RoundedCornerShape(16.dp)
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else AuroraTextMuted
            )
        }
    } else {
        // Secondary translucent glass button
        Box(
            modifier = modifier
                .height(48.dp)
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .clip(RoundedCornerShape(16.dp))
                .background(
                    if (isPressed && enabled) Color(0x28FFFFFF) else Color(0x18FFFFFF)
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (isPressed && enabled) AuroraBorderHighlight else AuroraBorder
                    ),
                    RoundedCornerShape(16.dp)
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 18.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) AuroraTextPrimary else AuroraTextMuted
            )
        }
    }
}

// ==========================================
// Aurora Chip Button (PRD §20)
// Press scale 0.96 with quick highlight response
// ==========================================
@Composable
fun BrutalistChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    accentColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = DaylogMotion.microTween(DaylogMotion.SnappyInteraction),
        label = "chip_scale"
    )

    val targetBorderColor = if (isSelected) {
        accentColor ?: AuroraBorderHighlight
    } else {
        if (isPressed) AuroraBorderHighlight.copy(alpha = 0.6f) else AuroraBorder
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = DaylogMotion.fastTween(),
        label = "chip_border_color"
    )

    val selectedBrush = if (accentColor != null) {
        Brush.linearGradient(listOf(accentColor.copy(alpha = 0.35f), accentColor.copy(alpha = 0.20f)))
    } else {
        Brush.linearGradient(listOf(Color(0x407C5CFF), Color(0x30A66CFF)))
    }

    val textColor = if (isSelected) {
        accentColor ?: AuroraTextPrimary
    } else {
        AuroraTextSecondary
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .clip(RoundedCornerShape(14.dp))
            .then(
                if (isSelected) Modifier.background(selectedBrush) else Modifier.background(Color(0x0DFFFFFF))
            )
            .border(BorderStroke(1.dp, animatedBorderColor), RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

// ==========================================
// Aurora Classification Badge (PRD §28)
// Smooth semantic color interpolation
// ==========================================
@Composable
fun BrutalistClassificationBadge(
    classification: ActivityClassification,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val (bgCol, textCol, label) = when (classification) {
        ActivityClassification.PRODUCTIVE -> Triple(ProductiveAuroraBg, ProductiveAurora, "Productive")
        ActivityClassification.NEUTRAL -> Triple(NeutralAuroraBg, NeutralAurora, "Neutral")
        ActivityClassification.UNPRODUCTIVE -> Triple(UnproductiveAuroraBg, UnproductiveAurora, "Unproductive")
        ActivityClassification.UNKNOWN -> Triple(UntrackedAuroraBg, UntrackedAurora, "Unknown")
    }

    val animatedBg by animateColorAsState(
        targetValue = bgCol,
        animationSpec = DaylogMotion.fastTween(),
        label = "badge_bg"
    )
    val animatedTextCol by animateColorAsState(
        targetValue = textCol,
        animationSpec = DaylogMotion.fastTween(),
        label = "badge_text"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg, RoundedCornerShape(10.dp))
            .border(BorderStroke(1.dp, animatedTextCol.copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(animatedTextCol, CircleShape)
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = animatedTextCol,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ==========================================
// Aurora Progress Bar (PRD §21-23)
// Fluid animated morph (180-280ms) with luminous pulse
// ==========================================
@Composable
fun BrutalistProgressBar(
    progressFraction: Float,
    modifier: Modifier = Modifier,
    filledBrush: Brush = PrimaryAuroraBrush,
    filledColor: Color = AuroraPrimary,
    unfilledColor: Color = Color(0x18FFFFFF)
) {
    val clamped = progressFraction.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clamped,
        animationSpec = DaylogMotion.standardTween(DaylogMotion.CrispEntrance),
        label = "progress_morph"
    )

    Box(
        modifier = modifier
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(unfilledColor, RoundedCornerShape(50))
            .border(BorderStroke(0.5.dp, Color(0x10FFFFFF)), RoundedCornerShape(50))
    ) {
        if (animatedProgress > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(filledBrush, RoundedCornerShape(50))
            )
        }
    }
}

// ==========================================
// Aurora Text Field (PRD §50, 51)
// Focus transition (100-140ms) & restrained validation
// ==========================================
@Composable
fun BrutalistTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        isError = isError,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isError) UnproductiveAurora else AuroraTextSecondary
            )
        },
        placeholder = {
            if (placeholder.isNotBlank()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuroraTextMuted
                )
            }
        },
        singleLine = singleLine,
        maxLines = maxLines,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0x14FFFFFF),
            unfocusedContainerColor = Color(0x0AFFFFFF),
            focusedBorderColor = AuroraPrimary,
            unfocusedBorderColor = AuroraBorder,
            errorBorderColor = UnproductiveAurora,
            errorLabelColor = UnproductiveAurora,
            cursorColor = AuroraPrimary,
            focusedTextColor = AuroraTextPrimary,
            unfocusedTextColor = AuroraTextPrimary,
            focusedLabelColor = AuroraPrimary,
            unfocusedLabelColor = AuroraTextSecondary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ==========================================
// Aurora Segmented Control (PRD §39)
// Shared Traveling Pill Indicator
// ==========================================
@Composable
fun <T> AuroraSegmentedControl(
    items: List<Pair<T, String>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x12FFFFFF))
            .border(BorderStroke(1.dp, AuroraBorder), RoundedCornerShape(18.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (itemKey, label) ->
            val isSelected = itemKey == selectedItem
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()

            val targetScale = if (isPressed) 0.97f else 1.0f
            val animatedScale by animateFloatAsState(
                targetValue = targetScale,
                animationSpec = DaylogMotion.microTween(),
                label = "seg_pill_scale"
            )

            val animatedBgColor by animateColorAsState(
                targetValue = if (isSelected) AuroraPrimary.copy(alpha = 0.90f) else Color.Transparent,
                animationSpec = DaylogMotion.fastTween(),
                label = "seg_pill_bg"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .clip(RoundedCornerShape(14.dp))
                    .background(animatedBgColor)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onItemSelected(itemKey) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else AuroraTextSecondary
                )
            }
        }
    }
}

// ==========================================
// Motion Animated Number (PRD §15, 24, 25)
// Compact vertical roll / slot animation without jitter
// ==========================================
@Composable
fun MotionTabularNumber(
    number: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
    color: Color = AuroraTextPrimary,
    fontWeight: FontWeight = FontWeight.Bold
) {
    AnimatedContent(
        targetState = number,
        transitionSpec = {
            (slideInVertically(
                animationSpec = DaylogMotion.fastTween(DaylogMotion.CrispEntrance),
                initialOffsetY = { -it / 2 }
            ) + fadeIn(animationSpec = DaylogMotion.fastTween())) togetherWith
                    (slideOutVertically(
                        animationSpec = DaylogMotion.exitTween(),
                        targetOffsetY = { it / 2 }
                    ) + fadeOut(animationSpec = DaylogMotion.exitTween()))
        },
        label = "motion_number_roll",
        modifier = modifier
    ) { targetNum ->
        Text(
            text = targetNum,
            style = style.copy(fontFeatureSettings = "tnum"),
            fontWeight = fontWeight,
            color = color,
            fontFamily = com.example.ui.theme.SoraFontFamily
        )
    }
}
