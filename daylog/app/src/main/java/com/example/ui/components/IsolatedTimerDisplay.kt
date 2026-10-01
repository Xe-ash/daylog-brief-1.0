package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AuroraTextPrimary
import com.example.util.DateTimeUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Isolated Realtime Timer Display Component (PRD §4, 5, 6, 7, 8, 9, 10, 14, 15, 91, 92, 97, 98)
 *
 * Core architecture:
 * 1. Calculates elapsed duration strictly from wall-clock timestamps (`System.currentTimeMillis() - startedAt`).
 * 2. Manages an internal precision ticker that aligns updates to exact second boundaries (`1000L - (now % 1000L)`).
 * 3. Scopes recompositions EXCLUSIVELY to this text rendering composable. Parent cards, screens,
 *    metrics, goals, and background gradients never recompose on second ticks.
 * 4. On app resume / tab switch / device unlock, immediately evaluates `System.currentTimeMillis()`
 *    with zero catch-up lag, drift, or drift compensation overhead.
 */
@Composable
fun IsolatedTimerDisplay(
    startedAt: Long,
    baseDurationSeconds: Long = 0L,
    isRunning: Boolean = true,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
    color: Color = AuroraTextPrimary
) {
    var currentElapsedSeconds by remember(startedAt, baseDurationSeconds, isRunning) {
        val initialElapsed = if (isRunning) {
            baseDurationSeconds + ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L)
        } else {
            baseDurationSeconds
        }
        mutableLongStateOf(initialElapsed)
    }

    if (isRunning) {
        LaunchedEffect(startedAt, baseDurationSeconds) {
            while (isActive) {
                val now = System.currentTimeMillis()
                val liveElapsed = baseDurationSeconds + ((now - startedAt) / 1000L).coerceAtLeast(0L)
                currentElapsedSeconds = liveElapsed

                // Synchronize with wall-clock millisecond boundary for zero drift
                val delayUntilNextSecond = (1000L - (now % 1000L)).coerceIn(50L, 1000L)
                delay(delayUntilNextSecond)
            }
        }
    }

    Text(
        text = DateTimeUtils.formatDurationHhMmSs(currentElapsedSeconds),
        style = style,
        fontWeight = FontWeight.Bold,
        color = color,
        letterSpacing = (-0.5).sp,
        modifier = modifier
    )
}
