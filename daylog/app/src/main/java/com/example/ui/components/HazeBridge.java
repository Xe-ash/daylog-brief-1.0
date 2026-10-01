package com.example.ui.components;

import androidx.compose.ui.Modifier;
import dev.chrisbanes.haze.HazeChildKt;
import dev.chrisbanes.haze.HazeKt;
import dev.chrisbanes.haze.HazeState;
import dev.chrisbanes.haze.HazeStyle;

public final class HazeBridge {
    private HazeBridge() {}

    public static Modifier applyHazeSource(Modifier modifier, HazeState hazeState) {
        return HazeKt.haze(modifier, hazeState);
    }

    public static Modifier applyHazeEffect(Modifier modifier, HazeState hazeState, HazeStyle style) {
        return HazeChildKt.hazeChild(modifier, hazeState, style, null);
    }
}
