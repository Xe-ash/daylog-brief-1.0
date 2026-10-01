package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DaylogTheme
import com.example.ui.theme.DaylogTokens
import com.example.ui.theme.DaylogTypography

@Composable
fun ScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    dateLine: String? = null,
    headerAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Header - aligns at 20dp left edge, title at Display size (32sp Bold)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = DaylogTokens.ScreenHorizontalPadding,
                        end = DaylogTokens.ScreenHorizontalPadding,
                        top = 16.dp,
                        bottom = 12.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    if (dateLine != null) {
                        Text(
                            text = dateLine,
                            style = DaylogTypography.Label,
                            color = DaylogTokens.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = title,
                        style = DaylogTypography.Display,
                        color = DaylogTokens.TextPrimary
                    )
                }

                if (headerAction != null) {
                    headerAction()
                }
            }

            // Main screen content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                content()

                // 24dp gradient fade (transparent to Night at 60%) above bottom bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DaylogTokens.FadeHeight)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    DaylogTokens.Night.copy(alpha = 0.60f)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Preview(name = "ScreenScaffold Preview", widthDp = 360, heightDp = 800)
@Composable
fun ScreenScaffoldPreview() {
    DaylogTheme {
        ScreenScaffold(
            title = "Today",
            dateLine = "Thursday, 1 October"
        ) {
            Text("Sample Content", color = DaylogTokens.TextPrimary, modifier = Modifier.padding(20.dp))
        }
    }
}
