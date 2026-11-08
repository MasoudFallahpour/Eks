package ir.fallahpoor.eks.libraries.ui.robots

import android.content.Context
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.libraries.ui.SortOrderDialog
import ir.fallahpoor.eks.libraries.ui.assertTextIsDisplayed
import ir.fallahpoor.eks.libraries.ui.clickOnNodeWithText

class SortOrderDialogRobot(
    private val context: Context,
    private val composeTestRule: ComposeContentTestRule
) {

    fun composeSortOrderDialog(
        sortOrder: SortOrder = SortOrder.A_TO_Z,
        onSortOrderClick: (SortOrder) -> Unit = {},
        onDismiss: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            SortOrderDialog(
                sortOrderProvider = { sortOrder },
                onSortOrderClick = onSortOrderClick,
                onDismiss = onDismiss
            )
        }
    }

    fun assertElementsAreCorrectlyDisplayedInitially(selectedSortOrder: SortOrder) {
        with(composeTestRule) {
            assertTextIsDisplayed(context.getString(R.string.select_sort_order))
            SortOrder.values().forEach { sortOrder ->
                assertTextIsDisplayed(context.getString(sortOrder.stringResId))
                if (sortOrder == selectedSortOrder) {
                    onNodeWithTag(context.getString(sortOrder.stringResId))
                        .assertIsSelected()
                } else {
                    onNodeWithTag(context.getString(sortOrder.stringResId))
                        .assertIsNotSelected()
                }
            }
        }
    }

    fun selectSortOrder(sortOrder: SortOrder) {
        composeTestRule.clickOnNodeWithText(context.getString(sortOrder.stringResId))
    }

    fun dismissDialog() {
        Espresso.onView(isRoot()).inRoot(isDialog()).perform(pressBack())
    }

}