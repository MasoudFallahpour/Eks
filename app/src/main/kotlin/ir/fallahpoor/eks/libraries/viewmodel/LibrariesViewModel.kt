package ir.fallahpoor.eks.libraries.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.data.entity.Library
import ir.fallahpoor.eks.data.repository.LibraryRepository
import ir.fallahpoor.eks.libraries.ui.LibrariesScreenUiState
import ir.fallahpoor.eks.libraries.ui.LibrariesState
import ir.fallahpoor.eks.libraries.viewmodel.exceptionparser.ExceptionParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LibrariesViewModel
@Inject constructor(
    private val libraryRepository: LibraryRepository, private val exceptionParser: ExceptionParser
) : ViewModel() {

    sealed class Event {
        object GetLibraries : Event()
        data class PinLibrary(val library: Library, val pin: Boolean) : Event()
        data class ChangeSortOrder(val sortOrder: SortOrder) : Event()
        data class ChangeSearchQuery(val searchQuery: String) : Event()
    }

    private var currentJob: Job? = null
    private val _librariesScreenUiState = MutableStateFlow(
        LibrariesScreenUiState(sortOrder = libraryRepository.getSortOrder())
    )
    val librariesScreenUiState: StateFlow<LibrariesScreenUiState> = combine(
        _librariesScreenUiState, libraryRepository.getRefreshDate()
    ) { libraries, refreshDate ->
        libraries.copy(refreshDate = refreshDate)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = LibrariesScreenUiState(sortOrder = libraryRepository.getSortOrder())
    )

    fun handleEvent(event: Event) {
        when (event) {
            is Event.GetLibraries -> getLibraries()
            is Event.PinLibrary -> pinLibrary(event.library, event.pin)
            is Event.ChangeSortOrder -> changeSortOrder(event.sortOrder)
            is Event.ChangeSearchQuery -> changeSearchQuery(event.searchQuery)
        }
    }

    private fun getLibraries() {
        performActionAndGetLibraries()
    }

    private fun pinLibrary(library: Library, pin: Boolean) {
        performActionAndGetLibraries {
            libraryRepository.pinLibrary(library = library, pinned = pin)
        }
    }

    private fun performActionAndGetLibraries(action: suspend () -> Unit = {}) {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            kotlin.runCatching {
                action()
                libraryRepository.getLibraries(
                    sortOrder = _librariesScreenUiState.value.sortOrder,
                    searchQuery = _librariesScreenUiState.value.searchQuery
                )
            }.onSuccess { libraries ->
                val librariesState = LibrariesState.Success(libraries)
                _librariesScreenUiState.update {
                    it.copy(librariesState = librariesState)
                }
            }.onFailure { throwable ->
                Timber.e(throwable)
                val librariesState = LibrariesState.Error(exceptionParser.getMessage(throwable))
                _librariesScreenUiState.update {
                    it.copy(librariesState = librariesState)
                }
            }
        }
    }

    private fun changeSortOrder(sortOrder: SortOrder) {
        viewModelScope.launch {
            kotlin.runCatching {
                libraryRepository.saveSortOrder(sortOrder)
            }.onSuccess {
                _librariesScreenUiState.update {
                    it.copy(
                        librariesState = LibrariesState.Loading,
                        sortOrder = libraryRepository.getSortOrder()
                    )
                }
                getLibraries()
            }.onFailure { throwable ->
                Timber.e(throwable)
            }
        }
    }

    private fun changeSearchQuery(searchQuery: String) {
        _librariesScreenUiState.update {
            it.copy(
                librariesState = LibrariesState.Loading, searchQuery = searchQuery
            )
        }
        getLibraries()
    }

}