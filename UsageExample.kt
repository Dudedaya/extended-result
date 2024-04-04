package xyz.dudedayaworks.result

import android.content.res.Resources

interface FooRepository {
    suspend fun getFoo(id: Int): ExResult<Foo, FooError>
}

data class Foo(val value: Int = 42)

sealed class FooError : ResultError {
    data object NotFound : FooError()
    data object InvalidId : FooError()
    data class ServerError(val code: Int) : FooError()
    data class UnknownError(override val cause: Exception) : FooError()
}

sealed class FooUiState {
    data object Loading : FooUiState()
    data class DataLoaded(val data: Foo) : FooUiState()
    data class LoadError(val error: String) : FooUiState()
}

class FooViewModel(
    private val repository: FooRepository,
    private val errorUiMapper: FooErrorUiMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FooUiState>(FooUiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun onShowFoo(id: Int) {
        viewModelScope.launch {
            _uiState.emit(FooUiState.Loading)
            repository.getFoo(id)
                .onSuccess {
                    _uiState.emit(FooUiState.DataLoaded(it))
                }.onFailure {
                    val errorMessage = errorUiMapper.map(it)
                    _uiState.emit(FooUiState.LoadError(errorMessage))
                }
        }
    }

    fun onShowFooAlternative(id: Int) {
        viewModelScope.launch {
            _uiState.emit(FooUiState.Loading)
            val result = repository.getFoo(id)
            if (result.isSuccess()) {
                _uiState.emit(FooUiState.DataLoaded(result.data))
            } else {
                val errorMessage = errorUiMapper.map(result.error)
                _uiState.emit(FooUiState.LoadError(errorMessage))
            }
        }
    }
}

interface FooErrorUiMapper {
    fun map(error: FooError): String
}

@Suppress("IMPLICIT_CAST_TO_ANY")
class FooErrorUiMapperImpl(
    private val resources: Resources,
) : FooErrorUiMapper {
    override fun map(error: FooError): String {
        return when (error) {
            FooError.InvalidId -> Resource(R.string.foo_error_invalid_id)
            FooError.NotFound -> Resource(R.string.foo_error_not_found)
            is FooError.ServerError -> ResourceWithArgs(R.string.foo_error_server, error.code)
            is FooError.UnknownError -> ResourceWithArgs(
                R.string.foo_error_unknown,
                error.cause.message ?: "No cause"
            )
        }.let {
            when (val resource = it) {
                is Resource -> resources.getString(resource.id)
                is ResourceWithArgs -> resources.getString(resource.id, resource.args)
                else -> error("Not reachable")
            }
        }
    }

    @JvmInline
    private value class Resource(val id: Int)
    private class ResourceWithArgs(val id: Int, vararg val args: Any)
}
