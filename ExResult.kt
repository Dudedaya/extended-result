package xyz.dudedayaworks.result

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class ExResult<out DATA, out ERROR : ResultError> {
    data class Success<out DATA, out ERROR : ResultError>(val data: DATA) :
        ExResult<DATA, ERROR>()

    data class Failure<out DATA, out ERROR : ResultError>(val error: ERROR) :
        ExResult<DATA, ERROR>()

    fun getOrNull(): DATA? {
        return if (this is Success) data else null
    }

    fun errorOrNull(): ERROR? {
        return if (this is Failure) error else null
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <DATA, ERROR : ResultError> ExResult<DATA, ERROR>.onSuccess(action: (DATA) -> Unit): ExResult<DATA, ERROR> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (isSuccess()) {
        action(data)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
inline fun <DATA, ERROR : ResultError> ExResult<DATA, ERROR>.onFailure(action: (ERROR) -> Unit): ExResult<DATA, ERROR> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }
    if (isFailure()) {
        action(error)
    }
    return this
}

@OptIn(ExperimentalContracts::class)
fun <DATA, ERROR : ResultError> ExResult<DATA, ERROR>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is ExResult.Success)
        returns(false) implies (this@isSuccess is ExResult.Failure)
    }
    return this is ExResult.Success
}

@OptIn(ExperimentalContracts::class)
fun <DATA, ERROR : ResultError> ExResult<DATA, ERROR>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is ExResult.Failure)
        returns(false) implies (this@isFailure is ExResult.Success)
    }
    return this is ExResult.Failure
}

fun <DATA, ERROR : ResultError> ExResult<DATA, ERROR>.getOrDefault(default: DATA): DATA {
    return if (isFailure()) default else data
}