package ir.fallahpoor.eks.libraries.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.theme.EksTheme
import kotlinx.coroutines.flow.filter

private enum class ToolbarMode {
    Normal,
    Search
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Toolbar(
    sortOrderProvider: () -> SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    searchQueryProvider: () -> String,
    onSearchQueryChange: (String) -> Unit,
    onSearchQuerySubmit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchFieldState = rememberTextFieldState(searchQueryProvider())
    val currentSearchQueryProvider by rememberUpdatedState(searchQueryProvider)
    val currentOnSearchQueryChange by rememberUpdatedState(onSearchQueryChange)
    LaunchedEffect(searchFieldState) {
        snapshotFlow { searchFieldState.text.toString() }
            .filter { it != currentSearchQueryProvider() }
            .collect { currentOnSearchQueryChange(it) }
    }
    var toolbarMode by rememberSaveable { mutableStateOf(ToolbarMode.Normal) }
    TopAppBar(
        modifier = modifier,
        title = {
            Box(contentAlignment = Alignment.CenterStart) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium
                )
                AnimatedVisibility(visible = toolbarMode == ToolbarMode.Search) {
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        hint = stringResource(R.string.search),
                        state = searchFieldState,
                        onQuerySubmit = onSearchQuerySubmit,
                        onClearClick = { searchFieldState.clearText() },
                        onCloseClick = {
                            toolbarMode = ToolbarMode.Normal
                            searchFieldState.clearText()
                        }
                    )
                }
            }
        },
        actions = {
            AnimatedVisibility(visible = toolbarMode == ToolbarMode.Normal) {
                Row {
                    SortOrderButton(
                        currentSortOrderProvider = sortOrderProvider,
                        onSortOrderChange = onSortOrderChange
                    )
                    SearchButton(onClick = { toolbarMode = ToolbarMode.Search })
                }
            }
        }
    )
}

@Composable
private fun SortOrderButton(
    currentSortOrderProvider: () -> SortOrder,
    onSortOrderChange: (SortOrder) -> Unit
) {
    var showSortOrderDialog by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { showSortOrderDialog = true }) {
        Icon(
            painter = painterResource(R.drawable.ic_sort),
            contentDescription = stringResource(R.string.sort)
        )
    }
    if (showSortOrderDialog) {
        SortOrderDialog(
            sortOrderProvider = currentSortOrderProvider,
            onSortOrderClick = { sortOrder: SortOrder ->
                showSortOrderDialog = false
                onSortOrderChange(sortOrder)
            },
            onDismiss = { showSortOrderDialog = false }
        )
    }
}

@Composable
private fun SearchButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = stringResource(R.string.search)
        )
    }
}

@Composable
@PreviewLightDark
private fun ToolbarPreview() {
    EksTheme {
        Surface {
            Toolbar(
                sortOrderProvider = { SortOrder.A_TO_Z },
                onSortOrderChange = {},
                searchQueryProvider = { "" },
                onSearchQueryChange = {},
                onSearchQuerySubmit = {}
            )
        }
    }
}