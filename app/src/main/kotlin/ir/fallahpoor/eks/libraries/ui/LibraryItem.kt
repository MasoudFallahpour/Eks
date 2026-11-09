package ir.fallahpoor.eks.libraries.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.Constants
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.theme.EksTheme
import ir.fallahpoor.eks.theme.spacing

object LibraryItemTags {
    const val ITEM = "libraryItem_"
    const val PIN_BUTTON = "pinLibraryButton_"
}

@Composable
fun LibraryItem(
    library: Library,
    onLibraryClick: (Library) -> Unit,
    onLibraryVersionClick: (Version) -> Unit,
    onLibraryPinClick: (Library, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = { onLibraryClick(library) })
            .padding(MaterialTheme.spacing.small)
            .testTag(LibraryItemTags.ITEM + library.name)
    ) {
        LibraryPinButton(
            modifier = Modifier.testTag(LibraryItemTags.PIN_BUTTON + library.name),
            isPinned = library.isPinned,
            onPinChange = { onLibraryPinClick(library, it) }
        )
        LibraryInformation(
            library = library,
            onLibraryVersionClick = onLibraryVersionClick
        )
    }
}

@Composable
private fun LibraryPinButton(
    isPinned: Boolean,
    onPinChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    IconToggleButton(
        modifier = modifier,
        checked = isPinned,
        onCheckedChange = onPinChange
    ) {
        val pinImage: Painter
        val contentDescription: String
        if (isPinned) {
            pinImage = painterResource(R.drawable.ic_pin_filled)
            contentDescription = stringResource(R.string.unpin_library)
        } else {
            pinImage = painterResource(R.drawable.ic_pin_outline)
            contentDescription = stringResource(R.string.pin_library)
        }
        Icon(
            painter = pinImage,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun LibraryInformation(
    library: Library,
    onLibraryVersionClick: (Version) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        LibraryName(name = library.name)
        LibraryDescription(description = library.description)
        LibraryVersions(
            library = library,
            onLibraryVersionClick = onLibraryVersionClick
        )
    }
}

@Composable
private fun LibraryName(name: String) {
    Surface(
        color = MaterialTheme.colorScheme.onBackground,
        contentColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            modifier = Modifier
                .padding(MaterialTheme.spacing.small),
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun LibraryDescription(description: String) {
    var isDescriptionExpanded by rememberSaveable { mutableStateOf(false) }
    var isDescriptionOverflowing by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.small)
                .animateContentSize(),
            text = description,
            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            onTextLayout = { textLayoutResult ->
                isDescriptionOverflowing = textLayoutResult.hasVisualOverflow
            }
        )
        if (isDescriptionOverflowing || isDescriptionExpanded) {
            ExpandCollapseButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                isExpanded = isDescriptionExpanded,
                onClick = { isDescriptionExpanded = !isDescriptionExpanded }
            )
        }
    }
}

@Composable
private fun ExpandCollapseButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        if (isExpanded) {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_up),
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = stringResource(R.string.collapse_library_description)
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                tint = MaterialTheme.colorScheme.secondary,
                contentDescription = stringResource(R.string.expand_library_description)
            )
        }
    }
}

@Composable
private fun LibraryVersions(
    library: Library,
    onLibraryVersionClick: (Version) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            LibraryVersion(
                text = stringResource(id = R.string.version_stable, library.stableVersion.name),
                version = library.stableVersion,
                onLibraryVersionClick = onLibraryVersionClick
            )
            LibraryVersion(
                text = stringResource(id = R.string.version_beta, library.betaVersion.name),
                version = library.betaVersion,
                onLibraryVersionClick = onLibraryVersionClick
            )
        }
        Row {
            LibraryVersion(
                text = stringResource(id = R.string.version_rc, library.rcVersion.name),
                version = library.rcVersion,
                onLibraryVersionClick = onLibraryVersionClick
            )
            LibraryVersion(
                text = stringResource(id = R.string.version_alpha, library.alphaVersion.name),
                version = library.alphaVersion,
                onLibraryVersionClick = onLibraryVersionClick
            )
        }
    }
}

@Composable
private fun RowScope.LibraryVersion(
    version: Version,
    text: String,
    onLibraryVersionClick: (Version) -> Unit
) {
    Text(
        modifier = Modifier
            .weight(1f)
            .clickable(
                onClick = {
                    if (version.name != Constants.NOT_AVAILABLE) {
                        onLibraryVersionClick(version)
                    }
                }
            )
            .padding(vertical = MaterialTheme.spacing.small),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        text = text,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
@PreviewLightDark
private fun LibraryItemPreview() {
    EksTheme {
        Surface {
            LibraryItem(
                library = Library(
                    name = "Room",
                    description = "Create, store, and manage persistent data backed by a SQLite database. Create, store, and manage persistent data backed by a SQLite database.",
                    stableVersion = Version("1.3.1"),
                    rcVersion = Version(),
                    betaVersion = Version("1.5.0-beta05"),
                    alphaVersion = Version("1.6.0-alpha02")
                ),
                onLibraryClick = {},
                onLibraryVersionClick = {},
                onLibraryPinClick = { _, _ -> }
            )
        }
    }
}