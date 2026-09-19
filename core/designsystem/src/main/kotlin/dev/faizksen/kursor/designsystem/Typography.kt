package dev.faizksen.kursor.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private const val TabularFigures = "tnum"

internal val KursorTypography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(
            fontSize = 42.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = TabularFigures,
        ),
        headlineMedium = headlineMedium.copy(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = TabularFigures,
        ),
        titleMedium = titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Normal),
        bodyMedium = bodyMedium.copy(fontSize = 13.5.sp, fontWeight = FontWeight.Normal),
        labelLarge = labelLarge.copy(
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            fontFeatureSettings = TabularFigures,
        ),
        labelMedium = labelMedium.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Normal),
        labelSmall = labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Normal),
    )
}
