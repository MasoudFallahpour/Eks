package ir.fallahpoor.eks.libraries.ui.robots

import android.content.Context
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import ir.fallahpoor.eks.R
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.libraries.ui.LibraryItem
import ir.fallahpoor.eks.libraries.ui.LibraryItemTags
import ir.fallahpoor.eks.libraries.ui.assertTextIsDisplayed
import ir.fallahpoor.eks.libraries.ui.clickOnNodeWithTag
import ir.fallahpoor.eks.libraries.ui.clickOnNodeWithText

class LibraryItemRobot(
    private val context: Context,
    private val composeTestRule: ComposeContentTestRule
) {

    private lateinit var library: Library

    fun composeLibraryItem(
        library: Library,
        onLibraryClick: (Library) -> Unit = {},
        onLibraryVersionClick: (Version) -> Unit = {},
        onLibraryPinClick: (Library, Boolean) -> Unit = { _, _ -> }
    ): LibraryItemRobot {
        composeTestRule.setContent {
            LibraryItem(
                library = library,
                onLibraryClick = onLibraryClick,
                onLibraryVersionClick = onLibraryVersionClick,
                onLibraryPinClick = onLibraryPinClick
            )
        }
        this.library = library
        return this
    }

    fun clickOnLibrary() {
        composeTestRule.clickOnNodeWithText(library.name)
    }

    fun clickOnStableVersion() {
        composeTestRule.clickOnNodeWithText(
            context.getString(
                R.string.version_stable, library.stableVersion.name
            )
        )
    }

    fun clickOnRcVersion() {
        composeTestRule.clickOnNodeWithText(
            context.getString(
                R.string.version_rc, library.rcVersion.name
            )
        )
    }

    fun assertRcVersionIsDisabled() {
        composeTestRule.onNodeWithText(
            context.getString(
                R.string.version_rc, library.rcVersion.name
            )
        ).assertIsNotEnabled()
    }

    fun clickOnPin() {
        composeTestRule.clickOnNodeWithTag(LibraryItemTags.PIN_BUTTON + library.name)
    }

    fun assertAllInformationAreDisplayedCorrectly() {
        with(composeTestRule) {
            assertTextIsDisplayed(library.name)
            assertTextIsDisplayed(library.description)
            assertTextIsDisplayed(
                context.getString(
                    R.string.version_stable, library.stableVersion.name
                )
            )
            assertTextIsDisplayed(
                context.getString(R.string.version_rc, library.rcVersion.name)
            )
            assertTextIsDisplayed(
                context.getString(R.string.version_beta, library.betaVersion.name)
            )
            assertTextIsDisplayed(
                context.getString(R.string.version_alpha, library.alphaVersion.name)
            )
            if (library.isPinned) {
                onNodeWithTag(LibraryItemTags.PIN_BUTTON + library.name).assertIsOn()
            } else {
                onNodeWithTag(LibraryItemTags.PIN_BUTTON + library.name).assertIsOff()
            }
        }
    }

}