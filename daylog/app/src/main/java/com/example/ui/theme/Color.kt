package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// Aurora Glass UI Color Tokens (DaylogTokens)
// ==========================================

val Night = DaylogTokens.Night
val NightDeep = DaylogTokens.NightDeep
val AuroraViolet = DaylogTokens.AuroraViolet
val AuroraCyan = DaylogTokens.AuroraCyan
val AuroraMagenta = DaylogTokens.AuroraMagenta
val Productive = DaylogTokens.Productive
val Neutral = DaylogTokens.Neutral
val Distraction = DaylogTokens.Distraction
val TextPrimary = DaylogTokens.TextPrimary
val TextSecondary = DaylogTokens.TextSecondary
val TextTertiary = DaylogTokens.TextTertiary

val GlassFill = DaylogTokens.GlassFill
val GlassStroke = DaylogTokens.GlassStroke
val PrimaryButtonGradient = DaylogTokens.PrimaryButtonGradient

// Compatibility Aliases with new tokens
val AuroraBgPrimary = Night
val AuroraBgSecondary = NightDeep
val AuroraBgTertiary = Color(0xFF141324)
val AuroraSurfaceGlass = GlassFill
val AuroraSurfaceGlassElevated = Color(0x1FFFFFFF)
val AuroraSurfaceCard = Color(0xFF110F20)
val AuroraBorder = Color(0x24FFFFFF)
val AuroraBorderHighlight = AuroraViolet.copy(alpha = 0.5f)

val AuroraPrimary = AuroraViolet
val AuroraSecondary = AuroraViolet
val AuroraElectricBlue = AuroraCyan
val AuroraSoftCyan = AuroraCyan
val AuroraVioletGlow = AuroraViolet
val AuroraPinkHighlight = AuroraMagenta

val ProductiveAurora = Productive
val ProductiveAuroraBg = Productive.copy(alpha = 0.15f)
val NeutralAurora = Neutral
val NeutralAuroraBg = Neutral.copy(alpha = 0.15f)
val UnproductiveAurora = Distraction
val UnproductiveAuroraBg = Distraction.copy(alpha = 0.15f)
val UntrackedAurora = TextTertiary
val UntrackedAuroraBg = TextTertiary.copy(alpha = 0.15f)

val AuroraTextPrimary = TextPrimary
val AuroraTextSecondary = TextSecondary
val AuroraTextMuted = TextTertiary

val PrimaryAuroraGradient = listOf(AuroraViolet, AuroraCyan)
val BlueAuroraGradient = listOf(AuroraCyan, AuroraViolet)
val PinkAuroraGradient = listOf(AuroraViolet, AuroraMagenta)
val GreenAuroraGradient = listOf(Productive, AuroraCyan)

val PrimaryAuroraBrush = Brush.linearGradient(PrimaryAuroraGradient)
val BlueAuroraBrush = Brush.linearGradient(BlueAuroraGradient)
val PinkAuroraBrush = Brush.linearGradient(PinkAuroraGradient)
val ProductiveBrush = Brush.linearGradient(GreenAuroraGradient)

val NaturalBg = Night
val NaturalTextPrimary = TextPrimary
val NaturalTextSecondary = TextSecondary
val NaturalCardSurface = AuroraSurfaceCard
val NaturalBorder = AuroraBorder
val NaturalSurfaceVariant = AuroraBgTertiary
val NaturalHeroBg = AuroraViolet.copy(alpha = 0.2f)
val NaturalHeroText = TextPrimary
val NaturalTerracotta = AuroraViolet
val NaturalTerracottaDark = AuroraViolet
val NaturalTerracottaLight = AuroraViolet.copy(alpha = 0.2f)
val NaturalSageBg = ProductiveAuroraBg
val NaturalSageText = Productive
val NaturalSagePrimary = Productive
val NaturalAmberBg = NeutralAuroraBg
val NaturalAmberText = Neutral
val NaturalAmber = Neutral
val NaturalNeutralBg = NeutralAuroraBg
val NaturalNeutralText = Neutral
val NaturalAlertBg = UnproductiveAuroraBg
val NaturalAlertText = Distraction
val NaturalAlert = Distraction
val NaturalUntracked = UntrackedAurora
val NaturalUntrackedLight = UntrackedAurora
val ProductiveGreen = Productive
val ProductiveGreenDark = Productive
val NeutralYellow = Neutral
val NeutralYellowDark = Neutral
val UnproductiveRed = Distraction
val UnproductiveRedDark = Distraction
val UntrackedGray = UntrackedAurora
val UntrackedGrayLight = UntrackedAurora
