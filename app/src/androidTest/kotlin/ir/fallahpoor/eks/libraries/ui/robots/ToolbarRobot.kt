package ir.fallahpoor.eks.libraries.ui.robots

import android.content.Context
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.libraries.ui.SearchBarTags
import ir.fallahpoor.eks.libraries.ui.Toolbar
import ir.fallahpoor.eks.libraries.ui.assertDoesNotExistNodeWithTag
import ir.fallahpoor.eks.libraries.ui.assertIsDisplayedNodeWithContentDescription
import ir.fallahpoor.eks.libraries.ui.assertTextIsDisplayed
import ir.fallahpoor.eks.libraries.ui.clickOnNodeWithContentDescription
import ir.fallahpoor.eks.libraries.ui.clickOnNodeWithTag
import ir.fallahpoor.eks.libraries.ui.clickOnNodeWithText

class ToolbarRobot(
    private val context: Context,
    private val composeTestRule: ComposeContentTestRule
) {

    private val sortText = context.getString(R.string.sort)
    private val searchText = context.getString(R.string.search)

    fun assertElementsAreDisplayedCorrectly() {
        with(composeTestRule) {
            assertTextIsDisplayed(context.getString(R.string.app_name))
            assertIsDisplayedNodeWithContentDescription(sortText)
            assertIsDisplayedNodeWithContentDescription(searchText)
            assertDoesNotExistNodeWithTag(SearchBarTags.SEARCH_BAR)
        }
    }

    fun composeToolbar(
        sortOrder: SortOrder = SortOrder.A_TO_Z,
        onSortOrderChange: (SortOrder) -> Unit = {},
        searchQuery: String = "",
        onSearchQueryChange: (String) -> Unit = {},
        onSearchQuerySubmit: (String) -> Unit = {}
    ) {
        composeTestRule.setContent {
            Toolbar(
                sortOrderProvider = { sortOrder },
                onSortOrderChange = onSortOrderChange,
                searchQueryProvider = { searchQuery },
                onSearchQueryChange = onSearchQueryChange,
                onSearchQuerySubmit = onSearchQuerySubmit
            )
        }
    }

    fun clearSearchQuery() {
        clickOnSearchButton()
        composeTestRule.clickOnNodeWithTag(SearchBarTags.CLEAR_BUTTON)
        composeTestRule.waitForIdle()
    }

    fun closeSearchBar() {
        composeTestRule.clickOnNodeWithContentDescription(searchText)
        composeTestRule.clickOnNodeWithTag(SearchBarTags.CLOSE_BUTTON)
    }

    fun clickOnSearchButton() {
        composeTestRule.clickOnNodeWithContentDescription(searchText)
    }

    fun selectSortOrder(sortOrder: SortOrder) {
        composeTestRule.clickOnNodeWithContentDescription(sortText)
        composeTestRule.clickOnNodeWithText(context.getString(sortOrder.stringResId))
    }

    fun enterSearchQuery(searchQuery: String) {
        clickOnSearchButton()
        composeTestRule.onNodeWithTag(SearchBarTags.QUERY_TEXT_FIELD)
            .performTextInput(searchQuery)
        composeTestRule.waitForIdle()
    }

}