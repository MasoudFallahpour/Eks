package ir.fallahpoor.eks.libraries.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.libraries.viewmodel.LibrariesViewModel
import ir.fallahpoor.eks.theme.EksTheme
import kotlinx.collections.immutable.persistentListOf

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

    LibrariesScreen(
        modifier = modifier,
        uiState = uiState,
        onEvent = librariesViewModel::handleEvent,
        onLibraryClick = onLibraryClick,
        onLibraryVersionClick = onLibraryVersionClick
    )
}

@Composable
private fun LibrariesScreen(
    uiState: LibrariesScreenUiState,
    onEvent: (LibrariesViewModel.Event) -> Unit,
    onLibraryClick: (Library) -> Unit,
    onLibraryVersionClick: (Version) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            val keyboardController = LocalSoftwareKeyboardController.current
            Toolbar(
                modifier = Modifier.testTag(LibrariesScreenTags.TOOLBAR),
                sortOrderProvider = { uiState.sortOrder },
                onSortOrderChange = { sortOrder: SortOrder ->
                    onEvent(LibrariesViewModel.Event.ChangeSortOrder(sortOrder))
                },
                searchQueryProvider = { uiState.searchQuery },
                onSearchQueryChange = { searchQuery: String ->
                    onEvent(LibrariesViewModel.Event.ChangeSearchQuery(searchQuery))
                },
                onSearchQuerySubmit = { searchQuery: String ->
                    keyboardController?.hide()
                    onEvent(LibrariesViewModel.Event.ChangeSearchQuery(searchQuery))
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
                onEvent(LibrariesViewModel.Event.PinLibrary(library, pin))
            },
            onTryAgainClick = { onEvent(LibrariesViewModel.Event.GetLibraries) }
        )
    }
}

@Composable
@Preview(name = "Success - Light")
@Preview(name = "Success - Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
private fun LibrariesScreenSuccessStatePreview() {
    LibrariesScreenPreview(
        librariesState = LibrariesState.Success(
            libraries = persistentListOf(
                Library(
                    name = "Library 1",
                    description = "Description 1",
                ),
                Library(
                    name = "Library 2",
                    description = "Description 2",
                    isPinned = true,
                    stableVersion = Version("1.0.0"),
                    betaVersion = Version("2.0.0"),
                    rcVersion = Version("3.0.0"),
                    alphaVersion = Version("4.0.0")
                )
            )
        )
    )
}

@Composable
@Preview(name = "Error - Light")
@Preview(name = "Error - Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
private fun LibrariesScreenErrorStatePreview() {
    LibrariesScreenPreview(
        librariesState = LibrariesState.Error("An error has occurred.")
    )
}

@Composable
@Preview(name = "Loading - Light")
@Preview(name = "Loading - Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
private fun LibrariesScreenLoadingStatePreview() {
    LibrariesScreenPreview(
        librariesState = LibrariesState.Loading
    )
}

@Composable
private fun LibrariesScreenPreview(librariesState: LibrariesState) {
    EksTheme {
        LibrariesScreen(
            uiState = LibrariesScreenUiState(
                refreshDate = "December 31, 2021",
                librariesState = librariesState
            ),
            onEvent = {},
            onLibraryClick = {},
            onLibraryVersionClick = {}
        )
    }
}
