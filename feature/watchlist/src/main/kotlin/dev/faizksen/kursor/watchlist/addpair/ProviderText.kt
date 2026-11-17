package dev.faizksen.kursor.watchlist.addpair

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.watchlist.R

@Composable
internal fun cadenceText(cadence: PublishCadence): String = stringResource(cadence.labelRes)

private val PublishCadence.labelRes: Int
    get() = when (this) {
        PublishCadence.DAILY -> R.string.add_pair_cadence_daily
        PublishCadence.WEEKLY -> R.string.add_pair_cadence_weekly
        PublishCadence.MONTHLY -> R.string.add_pair_cadence_monthly
        PublishCadence.QUARTERLY -> R.string.add_pair_cadence_quarterly
        PublishCadence.UNKNOWN -> R.string.add_pair_cadence_unknown
    }
