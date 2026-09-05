package dev.faizksen.kursor.network

import dev.faizksen.kursor.network.dto.FrankfurterRateDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterfaceFrankfurter {

    @GET("rates")
    suspend fun rates(
        @Query("base") base: String,
        @Query("quotes") quotes: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("group") group: String? = null
    ): List<FrankfurterRateDto>
}