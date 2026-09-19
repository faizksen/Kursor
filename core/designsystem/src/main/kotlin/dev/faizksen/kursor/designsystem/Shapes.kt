package dev.faizksen.kursor.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

internal val KursorShapes = Shapes(
    extraSmall = RoundedCornerShape(KursorRadius.deltaBadge),
    small = RoundedCornerShape(KursorRadius.periodChip),
    medium = RoundedCornerShape(KursorRadius.listCard),
    large = RoundedCornerShape(KursorRadius.chartCard),
    extraLarge = RoundedCornerShape(KursorRadius.bottomSheetTop),
)
