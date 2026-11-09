package ir.fallahpoor.eks.libraries.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.theme.EksTheme
import ir.fallahpoor.eks.theme.spacing
import kotlinx.collections.immutable.persistentListOf

object LibrariesContentTags {
    const val PROGRESS_INDICATOR = "librariesContentProgressIndicator"
    const val LIBRARIES_LIST = "librariesContentLibrariesList"
    const val REFRESH_DATE = "librariesContentRefreshDate"
    const val TRY_AGAIN_LAYOUT = "librariesContentTryAgainLayout"
    const val TRY_AGAIN_BUTTON = "librariesContentTryAgainButton"
}

@Composable
fun LibrariesContent(
    librariesState: LibrariesState,
    refreshDate: String,
    onLibraryClick: (Library) -> Unit,
    onLibraryVersionClick: (Version) -> Unit,
    onLibraryPinClick: (Library, Boolean) -> Unit,
    onTryAgainClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (librariesState) {
            is LibrariesState.Loading -> ProgressIndicator()
            is LibrariesState.Success -> {
                RefreshDate(refreshDate)
                HorizontalDivider()
                LibrariesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(LibrariesContentTags.LIBRARIES_LIST),
                    libraries = librariesState.libraries,
                    onLibraryClick = onLibraryClick,
                    onLibraryVersionClick = onLibraryVersionClick,
                    onLibraryPinClick = onLibraryPinClick
                )
            }

            is LibrariesState.Error -> {
                TryAgain(
                    errorMessage = librariesState.message,
                    onTryAgainClick = onTryAgainClick
                )
            }
        }
    }
}

@Composable
private fun ProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(LibrariesContentTags.PROGRESS_INDICATOR),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun RefreshDate(refreshDate: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.normal)
            .testTag(LibrariesContentTags.REFRESH_DATE),
        text = stringResource(R.string.refresh_date, refreshDate)
    )
}

@Composable
private fun TryAgain(errorMessage: String, onTryAgainClick: () -> Unit) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .testTag(LibrariesContentTags.TRY_AGAIN_LAYOUT)
    ) {
        val guideline = createGuidelineFromTop(0.5f)
        val (tryAgainButton, errorMessageText) = createRefs()
        Text(
            modifier = Modifier
                .padding(MaterialTheme.spacing.normal)
                .constrainAs(errorMessageText) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(guideline)
                },
            text = errorMessage,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
        Button(
            modifier = Modifier
                .constrainAs(tryAgainButton) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(guideline)
                }
                .testTag(LibrariesContentTags.TRY_AGAIN_BUTTON),
            onClick = onTryAgainClick
        ) {
            Text(text = stringResource(R.string.try_again))
        }
    }
}

@Composable
private fun LibrariesContentPreview(
    librariesState: LibrariesState,
    refreshDate: String = "December 31, 2021",
    onLibraryClick: (Library) -> Unit = {},
    onLibraryVersionClick: (Version) -> Unit = {},
    onLibraryPinClick: (Library, Boolean) -> Unit = { _, _ -> },
    onTryAgainClick: () -> Unit = {}
) {
    EksTheme {
        Surface {
            LibrariesContent(
                librariesState = librariesState,
                refreshDate = refreshDate,
                onLibraryClick = onLibraryClick,
                onLibraryVersionClick = onLibraryVersionClick,
                onLibraryPinClick = onLibraryPinClick,
                onTryAgainClick = onTryAgainClick
            )
        }
    }
}

@Composable
@Preview(name = "Success State")
private fun LibrariesContentSuccessStatePreview() {
    LibrariesContentPreview(
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
@Preview(name = "Error State")
private fun LibrariesContentErrorStatePreview() {
    LibrariesContentPreview(
        librariesState = LibrariesState.Error("An error has occurred.")
    )
}

@Composable
@Preview(name = "Loading State")
private fun LibrariesContentLoadingStatePreview() {
    LibrariesContentPreview(
        librariesState = LibrariesState.Loading
    )
}