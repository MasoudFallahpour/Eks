package ir.fallahpoor.eks.libraries.ui

import android.content.Context
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import ir.fallahpoor.eks.commontest.TestData
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.Version
import ir.fallahpoor.eks.libraries.ui.robots.LibraryItemRobot
import org.junit.Rule
import org.junit.Test

class LibraryItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val libraryItemRobot = LibraryItemRobot(context, composeTestRule)

    @Test
    fun library_information_is_displayed_correctly() {
        // Given
        libraryItemRobot.composeLibraryItem(library = TestData.core)

        // Then
        libraryItemRobot.assertAllInformationAreDisplayedCorrectly()
    }

    @Test
    fun when_a_library_is_clicked_correct_callback_is_called() {
        // Given
        val library: Library = TestData.room
        var callbackCalled = false
        var clickedLibrary: Library? = null
        val onLibraryClick: (Library) -> Unit = { library ->
            callbackCalled = true
            clickedLibrary = library
        }
        libraryItemRobot.composeLibraryItem(library = library, onLibraryClick = onLibraryClick)

        // When
        libraryItemRobot.clickOnLibrary()

        // Then
        Truth.assertThat(callbackCalled).isTrue()
        Truth.assertThat(clickedLibrary).isEqualTo(library)
    }

    @Test
    fun when_a_library_version_is_clicked_correct_callback_is_called() {
        // Given
        val library: Library = TestData.room
        var callbackCalled = false
        var clickedVersion: Version? = null
        val onLibraryVersionClick: (Version) -> Unit = { version ->
            callbackCalled = true
            clickedVersion = version
        }
        libraryItemRobot.composeLibraryItem(
            library = library,
            onLibraryVersionClick = onLibraryVersionClick
        )

        // When
        libraryItemRobot.clickOnStableVersion()

        // Then
        Truth.assertThat(callbackCalled).isTrue()
        Truth.assertThat(clickedVersion).isEqualTo(library.stableVersion)
    }

    @Test
    fun when_a_library_version_is_clicked_and_library_version_is_not_available_no_callback_is_called() {
        // Given
        var callbackCalled = false
        val onLibraryVersionClick: (Version) -> Unit = { version ->
            callbackCalled = true
        }
        libraryItemRobot.composeLibraryItem(
            library = TestData.room,
            onLibraryVersionClick = onLibraryVersionClick
        )

        // When
        libraryItemRobot.clickOnRcVersion()

        // Then
        Truth.assertThat(callbackCalled).isFalse()
        libraryItemRobot.assertRcVersionIsDisabled()
    }

    @Test
    fun when_a_library_is_pinned_correct_callback_is_called() {
        // Given
        val library: Library = TestData.room
        var callbackCalled = false
        var isPinned = false
        var pinnedLibrary: Library? = null
        val onLibraryPinClick: (Library, Boolean) -> Unit = { library, pinned ->
            callbackCalled = true
            isPinned = pinned
            pinnedLibrary = library
        }
        libraryItemRobot.composeLibraryItem(
            library = library,
            onLibraryPinClick = onLibraryPinClick
        )

        // When
        libraryItemRobot.clickOnPin()

        // Then
        Truth.assertThat(callbackCalled).isTrue()
        Truth.assertThat(pinnedLibrary).isEqualTo(library)
        Truth.assertThat(isPinned).isTrue()
    }

    @Test
    fun when_a_library_is_unpinned_correct_callback_is_called() {
        // Given
        val library: Library = TestData.core
        var callbackCalled = false
        var isPinned = false
        var unpinnedLibrary: Library? = null
        val onLibraryPinClick: (Library, Boolean) -> Unit = { library, pinned ->
            callbackCalled = true
            isPinned = pinned
            unpinnedLibrary = library
        }
        libraryItemRobot.composeLibraryItem(
            library = library,
            onLibraryPinClick = onLibraryPinClick
        )

        // When
        libraryItemRobot.clickOnPin()

        // Then
        Truth.assertThat(callbackCalled).isTrue()
        Truth.assertThat(unpinnedLibrary).isEqualTo(library)
        Truth.assertThat(isPinned).isFalse()
    }
}