package ir.fallahpoor.eks.libraries.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.theme.EksTheme

object SearchBarTags {
    const val CLOSE_BUTTON = "searchBarCloseButton"
    const val CLEAR_BUTTON = "searchBarClearButton"
    const val QUERY_TEXT_FIELD = "searchBarQueryTextField"
    const val SEARCH_BAR = "searchBar"
}

@Composable
fun SearchBar(
    hint: String,
    state: TextFieldState,
    onQuerySubmit: (String) -> Unit,
    onClearClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small
) {
    Surface(
        modifier = modifier.testTag(SearchBarTags.SEARCH_BAR),
        shape = shape
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CloseButton(onCloseClick = onCloseClick)
            SearchTextField(
                modifier = Modifier.weight(1f),
                hint = hint,
                state = state,
                onQuerySubmit = onQuerySubmit
            )
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
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.close_search_bar)
        )
    }
}

@Composable
private fun SearchTextField(
    hint: String,
    state: TextFieldState,
    onQuerySubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag(SearchBarTags.QUERY_TEXT_FIELD),
        state = state,
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { onQuerySubmit(state.text.toString()) },
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorator = { innerTextField ->
            Box {
                if (state.text.isBlank()) {
                    HintText(hint = hint)
                }
                innerTextField()
            }
        }
    )
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun HintText(hint: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = hint,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ClearButton(onClearClick: () -> Unit) {
    IconButton(
        modifier = Modifier.testTag(SearchBarTags.CLEAR_BUTTON),
        onClick = onClearClick
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.clear_search_bar)
        )
    }
}

@Composable
@PreviewLightDark
private fun SearchBarNonEmptyQueryPreview() {
    SearchBarPreview("Coil")
}

@Composable
@PreviewLightDark
private fun SearchBarEmptyQueryPreview() {
    SearchBarPreview("")
}

@Composable
private fun SearchBarPreview(query: String) {
    EksTheme {
        Surface {
            SearchBar(
                shape = MaterialTheme.shapes.small,
                hint = "Search",
                state = rememberTextFieldState(query),
                onClearClick = {},
                onCloseClick = {},
                onQuerySubmit = {}
            )
        }
    }
}