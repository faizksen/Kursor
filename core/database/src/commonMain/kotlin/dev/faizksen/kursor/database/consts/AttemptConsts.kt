package dev.faizksen.kursor.database.consts

internal const val ATTEMPT_ANSWERED = "answered"
internal const val ATTEMPT_EMPTY = "empty"
internal const val ATTEMPT_FAILED = "failed"

sealed interface AttemptResultColumn {
    val columnValue: String
    data object Answered: AttemptResultColumn { override val columnValue = ATTEMPT_ANSWERED }
    data object Empty: AttemptResultColumn { override val columnValue = ATTEMPT_EMPTY }
    data object Failed: AttemptResultColumn { override val columnValue = ATTEMPT_FAILED }
    data class Unrecognized(override val columnValue: String): AttemptResultColumn
}
