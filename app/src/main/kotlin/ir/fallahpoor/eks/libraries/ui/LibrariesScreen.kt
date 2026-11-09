package ir.fallahpoor.eks.libraries.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.libraries.viewmodel.LibrariesViewModel

object LibrariesScreenTags {
    const val TOOLBAR = "librariesScreenToolbar"
    const val CONTENT = "librariesScreenContent"
}

@Composable
fun LibrariesScreen(
    onLibraryClick: (Library) -> Unit,
    onLibraryVersionClick: (Version) -> Unit,
    modifier: Modifier = Modifier,
    librariesViewModel: LibrariesViewModel = viewModel()
) {
    val uiState: LibrariesScreenUiState by librariesViewModel.librariesScreenUiState
        .collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            val keyboardController = LocalSoftwareKeyboardController.current
            Toolbar(
                modifier = Modifier.testTag(LibrariesScreenTags.TOOLBAR),
                sortOrderProvider = { uiState.sortOrder },
                onSortOrderChange = { sortOrder: SortOrder ->
                    librariesViewModel.handleEvent(
                        LibrariesViewModel.Event.ChangeSortOrder(sortOrder)
                    )
                },
                searchQueryProvider = { uiState.searchQuery },
                onSearchQueryChange = { searchQuery: String ->
                    librariesViewModel.handleEvent(
                        LibrariesViewModel.Event.ChangeSearchQuery(searchQuery)
                    )
                },
                onSearchQuerySubmit = { searchQuery: String ->
                    keyboardController?.hide()
                    librariesViewModel.handleEvent(
                        LibrariesViewModel.Event.ChangeSearchQuery(searchQuery)
                    )
                }
            )
        }
    ) { innerPadding ->
        LibrariesContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(LibrariesScreenTags.CONTENT),
            librariesState = uiState.librariesState,
            refreshDate = uiState.refreshDate,
            onLibraryClick = onLibraryClick,
            onLibraryVersionClick = onLibraryVersionClick,
            onLibraryPinClick = { library: Library, pin: Boolean ->
                librariesViewModel.handleEvent(
                    LibrariesViewModel.Event.PinLibrary(library, pin)
                )
            },
            onTryAgainClick = { librariesViewModel.handleEvent(LibrariesViewModel.Event.GetLibraries) }
        )
    }
}