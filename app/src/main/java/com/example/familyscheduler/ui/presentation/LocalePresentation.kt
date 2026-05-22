package com.example.familyscheduler.ui.presentation

import java.util.Locale

object LocalePresentation {

    fun formatHours(hours: Double): String {
        return String.format(Locale.JAPAN, "%.1f時間", hours)
    }
}