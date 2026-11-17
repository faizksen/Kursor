package dev.faizksen.kursor.network

import dev.faizksen.kursor.model.Catalogue

interface CatalogueRemoteSource {
    suspend fun fetch(): Catalogue
}
