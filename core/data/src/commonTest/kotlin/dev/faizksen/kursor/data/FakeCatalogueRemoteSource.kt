package dev.faizksen.kursor.data

import dev.faizksen.kursor.model.Catalogue
import dev.faizksen.kursor.network.CatalogueRemoteSource

internal class FakeCatalogueRemoteSource(
    private var catalogue: Catalogue,
) : CatalogueRemoteSource {

    var fetchCount: Int = 0
        private set

    fun serve(next: Catalogue) {
        catalogue = next
    }

    override suspend fun fetch(): Catalogue {
        fetchCount++
        return catalogue
    }
}
