package dev.faizksen.kursor.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

internal val KursorShapes = Shapes(
    extraSmall = RoundedCornerShape(KursorRadius.deltaBadge),
    small = RoundedCornerShape(KursorRadius.periodChip),
    medium = RoundedCornerShape(KursorRadius.card),
    large = RoundedCornerShape(KursorRadius.container),
    extraLarge = RoundedCornerShape(KursorRadius.bottomSheetTop),
)
