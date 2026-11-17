package dev.faizksen.kursor.watchlist.addpair

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.faizksen.kursor.model.Provider
import dev.faizksen.kursor.model.PublishCadence
import dev.faizksen.kursor.watchlist.R

@Composable
internal fun providerMetaText(provider: Provider): String {
    val cadence = stringResource(provider.cadence.labelRes)

    return provider.rateType?.let { rateType ->
        stringResource(R.string.add_pair_provider_meta, rateType, cadence)
    } ?: cadence
}

private val PublishCadence.labelRes: Int
    get() = when (this) {
        PublishCadence.DAILY -> R.string.add_pair_cadence_daily
        PublishCadence.WEEKLY -> R.string.add_pair_cadence_weekly
        PublishCadence.MONTHLY -> R.string.add_pair_cadence_monthly
        PublishCadence.QUARTERLY -> R.string.add_pair_cadence_quarterly
        PublishCadence.UNKNOWN -> R.string.add_pair_cadence_unknown
    }
