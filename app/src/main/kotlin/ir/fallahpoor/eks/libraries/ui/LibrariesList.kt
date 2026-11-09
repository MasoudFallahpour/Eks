package ir.fallahpoor.eks.libraries.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.theme.EksTheme
import ir.fallahpoor.eks.theme.spacing
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

object LibrariesListTags {
    const val LIBRARIES_LIST = "librariesListLibrariesList"
    const val NO_LIBRARY_TEXT = "librariesListNoLibraryText"
    const val SCROLL_TO_TOP_BUTTON = "librariesListScrollToTopButton"
}

@Composable
fun LibrariesList(
    libraries: ImmutableList<Library>,
    onLibraryClick: (Library) -> Unit,
    onLibraryVersionClick: (Version) -> Unit,
    onLibraryPinClick: (Library, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = libraries.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            NoLibraryText()
        }
        AnimatedVisibility(
            visible = libraries.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                val listState = rememberLazyListState()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(LibrariesListTags.LIBRARIES_LIST),
                    state = listState
                ) {
                    items(
                        items = libraries,
                        key = { library: Library -> library.name }
                    ) { library: Library ->
                        LibraryItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            library = library,
                            onLibraryClick = onLibraryClick,
                            onLibraryVersionClick = onLibraryVersionClick,
                            onLibraryPinClick = onLibraryPinClick
                        )
                        HorizontalDivider()
                    }
                }
                val coroutineScope = rememberCoroutineScope()
                val showScrollToTopButton by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 0
                    }
                }
                ScrollToTopButton(show = showScrollToTopButton) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoLibraryText() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(LibrariesListTags.NO_LIBRARY_TEXT),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(MaterialTheme.spacing.normal),
            text = stringResource(R.string.no_library)
        )
    }
}

@Composable
private fun ScrollToTopButton(show: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        FloatingActionButton(
            modifier = Modifier
                .padding(MaterialTheme.spacing.normal)
                .testTag(LibrariesListTags.SCROLL_TO_TOP_BUTTON),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_up),
                contentDescription = stringResource(R.string.scroll_to_top)
            )
        }
    }
}

@Composable
@Preview(name = "List of libraries is not empty")
private fun LibrariesListNonEmptyListPreview() {
    LibrariesListPreview(
        libraries = listOf(
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
        ).toImmutableList()
    )
}

@Composable
@Preview(name = "List of libraries is empty")
private fun LibrariesListEmptyListPreview() {
    LibrariesListPreview(libraries = emptyList<Library>().toImmutableList())
}

@Composable
private fun LibrariesListPreview(libraries: ImmutableList<Library>) {
    EksTheme {
        Surface {
            LibrariesList(
                libraries = libraries,
                onLibraryClick = {},
                onLibraryVersionClick = {},
                onLibraryPinClick = { _, _ -> },
            )
        }
    }
}