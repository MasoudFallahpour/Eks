package ir.fallahpoor.eks.libraries.ui

import ir.fallahpoor.eks.data.Constants
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.data.repository.model.Library
import kotlinx.collections.immutable.ImmutableList

data class LibrariesScreenUiState(
    val sortOrder: SortOrder = SortOrder.A_TO_Z,
    val searchQuery: String = "",
    val refreshDate: String = Constants.NOT_AVAILABLE,
    val librariesState: LibrariesState = LibrariesState.Loading
)

sealed interface LibrariesState {
    object Loading : LibrariesState
    data class Success(val libraries: ImmutableList<Library>) : LibrariesState
    data class Error(val message: String) : LibrariesState
}