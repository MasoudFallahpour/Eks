package ir.fallahpoor.eks.libraries.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.theme.ReleaseTrackerTheme
import ir.fallahpoor.eks.theme.spacing

object SearchBarTags {
    const val CLOSE_BUTTON = "searchBarCloseButton"
    const val CLEAR_BUTTON = "searchBarClearButton"
    const val QUERY_TEXT_FIELD = "searchBarQueryTextField"
    const val SEARCH_BAR = "searchBar"
}

@Composable
fun SearchBar(
    hint: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onQuerySubmit: (String) -> Unit,
    onClearClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    elevation: Dp = MaterialTheme.spacing.small
) {
    Surface(
        modifier = modifier.testTag(SearchBarTags.SEARCH_BAR),
        shape = shape,
        elevation = elevation
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CloseButton(onCloseClick = onCloseClick)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (query.isBlank()) {
                    HintText(hint = hint)
                }
                SearchTextField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onQuerySubmit = onQuerySubmit
                )
            }
            ClearButton(onClearClick = onClearClick)
        }
    }
}

@Composable
private fun CloseButton(onCloseClick: () -> Unit) {
    IconButton(
        modifier = Modifier.testTag(SearchBarTags.CLOSE_BUTTON),
        onClick = onCloseClick
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(R.string.close_search_bar)
        )
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onQuerySubmit: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    BasicTextField(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag(SearchBarTags.QUERY_TEXT_FIELD),
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onQuerySubmit(query)
            }
        ),
        textStyle = MaterialTheme.typography.body1.copy(color = MaterialTheme.colors.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colors.onSurface)
    )
    DisposableEffect(Unit) {
        focusRequester.requestFocus()
        onDispose { }
    }
}

@Composable
private fun HintText(hint: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = hint
        )
    }
}

@Composable
private fun ClearButton(onClearClick: () -> Unit) {
    IconButton(
        modifier = Modifier.testTag(SearchBarTags.CLEAR_BUTTON),
        onClick = onClearClick
    ) {
        Icon(
            imageVector = Icons.Filled.Clear,
            contentDescription = stringResource(R.string.clear_search_bar)
        )
    }
}

@Composable
@Preview
private fun SearchBarPreview() {
    ReleaseTrackerTheme(darkTheme = false) {
        Surface {
            SearchBar(
                shape = MaterialTheme.shapes.small,
                elevation = 8.dp,
                hint = "Search",
                query = "Coil",
                onQueryChange = {},
                onClearClick = {},
                onCloseClick = {},
                onQuerySubmit = {}
            )
        }
    }
}