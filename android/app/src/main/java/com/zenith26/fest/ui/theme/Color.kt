package com.zenith26.fest.ui.theme

import androidx.compose.ui.graphics.Color

// Tailwind Slate Palette
val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)

// Brand Vibrant Accents
val Purple600 = Color(0xFF9333EA)
val Purple500 = Color(0xFFA855F7)
val Purple400 = Color(0xFFC084FC)
val Purple300 = Color(0xFFD8B4FE)
val Purple800 = Color(0xFF6B21A8)
val Purple900 = Color(0xFF581C87)
val Purple950 = Color(0xFF3B0764)

val Indigo600 = Color(0xFF4F46E5)
val Indigo500 = Color(0xFF6366F1)
val Pink400 = Color(0xFFF472B6)

// Technical Category (Blue)
val TechBg = Color(0x991E3A8A)
val TechText = Color(0xFF93C5FD)
val TechBorder = Color(0x991D4ED8)

// Cultural Category (Purple)
val CultBg = Color(0x99581C87)
val CultText = Color(0xFFD8B4FE)
val CultBorder = Color(0x997E22CE)

// Sports Category (Emerald)
val SportsBg = Color(0x99064E3B)
val SportsText = Color(0xFF6EE7B7)
val SportsBorder = Color(0x99047857)

// Gaming Category (Rose)
val GamingBg = Color(0x99881337)
val GamingText = Color(0xFFFDA4AF)
val GamingBorder = Color(0x99BE123C)

// Workshop Category (Amber)
val WorkshopBg = Color(0x9978350F)
val WorkshopText = Color(0xFFFCD34D)
val WorkshopBorder = Color(0x99B45309)

// Status & Warnings
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)
val EmeraldBg = Color(0x80022C22)
val EmeraldBorder = Color(0x80047857)

val Amber400 = Color(0xFFFBBF24)
val Amber500 = Color(0xFFF59E0B)
val AmberBg = Color(0x80451A03)
val AmberBorder = Color(0x8092400E)

val Rose400 = Color(0xFFFB7185)
val Rose500 = Color(0xFFF43F5E)
val Rose600 = Color(0xFFE11D48)
val RoseBg = Color(0x804C0519)
val RoseBorder = Color(0x809F1239)

data class CategoryStyle(
    val background: Color,
    val textColor: Color,
    val borderColor: Color
)

fun getCategoryStyle(category: String): CategoryStyle {
    return when (category.trim().lowercase()) {
        "technical" -> CategoryStyle(TechBg, TechText, TechBorder)
        "cultural" -> CategoryStyle(CultBg, CultText, CultBorder)
        "sports" -> CategoryStyle(SportsBg, SportsText, SportsBorder)
        "gaming" -> CategoryStyle(GamingBg, GamingText, GamingBorder)
        "workshop" -> CategoryStyle(WorkshopBg, WorkshopText, WorkshopBorder)
        else -> CategoryStyle(Slate800, Slate300, Slate700)
    }
}
