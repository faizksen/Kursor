package dev.faizksen.kursor.domain

import dev.faizksen.kursor.data.WatchedPairsRepository
import dev.faizksen.kursor.model.CurrencyPair
import kotlinx.coroutines.flow.Flow

fun interface DeletePairUseCase {
    suspend operator fun invoke(id: Long): CurrencyPair?
}

fun interface UndoDeletedPairUseCase {
    suspend operator fun invoke(): Boolean
}

fun interface ForgetDeletedPairUseCase {
    operator fun invoke()
}

fun interface ObserveDeletedPairUseCase {
    operator fun invoke(): Flow<CurrencyPair?>
}

internal class DeletePairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): DeletePairUseCase {
    override suspend fun invoke(id: Long): CurrencyPair? = watchedPairsRepository.delete(id)
}

internal class UndoDeletedPairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): UndoDeletedPairUseCase {
    override suspend fun invoke(): Boolean = watchedPairsRepository.undoDelete()
}

internal class ForgetDeletedPairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): ForgetDeletedPairUseCase {
    override fun invoke() = watchedPairsRepository.forgetDeleted()
}

internal class ObserveDeletedPairUseCaseImpl(
    private val watchedPairsRepository: WatchedPairsRepository,
): ObserveDeletedPairUseCase {
    override fun invoke(): Flow<CurrencyPair?> = watchedPairsRepository.observeDeleted()
}
