package ir.fallahpoor.eks.libraries.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.theme.EksTheme

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
    TopAppBar(
        modifier = modifier,
        title = {
            var toolbarMode by rememberSaveable { mutableStateOf(ToolbarMode.Normal) }
            Box(contentAlignment = Alignment.BottomCenter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    SortOrderButton(
                        currentSortOrderProvider = sortOrderProvider,
                        onSortOrderChange = onSortOrderChange
                    )
                    SearchButton(onClick = { toolbarMode = ToolbarMode.Search })
                }
                AnimatedVisibility(visible = toolbarMode == ToolbarMode.Search) {
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        hint = stringResource(R.string.search),
                        query = searchQueryProvider(),
                        onQueryChange = {
                            onSearchQueryChange(it)
                        },
                        onQuerySubmit = onSearchQuerySubmit,
                        onClearClick = {
                            if (searchQueryProvider().isNotBlank()) {
                                onSearchQueryChange("")
                            }
                        },
                        onCloseClick = {
                            toolbarMode = ToolbarMode.Normal
                            if (searchQueryProvider().isNotBlank()) {
                                onSearchQueryChange("")
                            }
                        }
                    )
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