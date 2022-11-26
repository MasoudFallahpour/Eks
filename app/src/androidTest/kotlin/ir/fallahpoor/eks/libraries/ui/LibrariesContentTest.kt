package ir.fallahpoor.eks.libraries.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import ir.fallahpoor.eks.commontest.TestData
import ir.fallahpoor.eks.data.entity.Library
import ir.fallahpoor.eks.data.entity.Version
import ir.fallahpoor.eks.libraries.ui.robots.LibrariesContentRobot
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class LibrariesContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val librariesContentRobot = LibrariesContentRobot(context, composeRule)

    @Test
    fun when_state_is_LOADING_progress_indicator_is_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Loading)

        // Then
        librariesContentRobot.progressIndicatorIsDisplayed()

    }

    @Test
    fun when_state_is_LOADING_refresh_date_is_not_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Loading)

        // Then
        librariesContentRobot.refreshDateIsNotDisplayed()

    }

    @Test
    fun when_state_is_LOADING_libraries_are_not_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Loading)

        // Then
        librariesContentRobot.librariesListIsNotDisplayed()

    }

    @Test
    fun when_state_is_LOADING_try_again_is_not_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Loading)

        // Then
        librariesContentRobot.tryAgainIsNotDisplayed()

    }

    @Test
    fun when_state_is_SUCCESS_libraries_are_displayed() {

        // Given
        val libraries = listOf(
            TestData.Activity.old,
            TestData.Biometric.old,
            TestData.core, TestData.preference
        )
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(
                libraries
            )
        )

        // Then
        librariesContentRobot.librariesListIsDisplayed()

    }

    @Test
    fun when_state_is_SUCCESS_refresh_date_is_displayed() {

        // Given
        val libraries = listOf(
            TestData.Activity.old,
            TestData.Biometric.old,
            TestData.core,
            TestData.preference
        )
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(
                libraries
            )
        )

        // Then
        librariesContentRobot.refreshDateIsDisplayed()

    }

    @Test
    fun when_state_is_SUCCESS_progress_indicator_is_not_displayed() {

        // Given
        val libraries = listOf(
            TestData.Activity.old,
            TestData.Biometric.old,
            TestData.core,
            TestData.preference
        )
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(
                libraries
            )
        )

        // Then
        librariesContentRobot.progressIndicatorIsNotDisplayed()

    }

    @Test
    fun when_state_is_SUCCESS_try_again_is_not_displayed() {

        // Given
        val libraries = listOf(
            TestData.Activity.old,
            TestData.Biometric.old,
            TestData.core,
            TestData.preference
        )
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(
                libraries
            )
        )

        // Then
        librariesContentRobot.tryAgainIsNotDisplayed()

    }

    @Test
    fun when_state_is_ERROR_try_again_is_displayed() {

        // Given
        val errorMessage = "An error occurred."
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Error(
                errorMessage
            )
        )

        // Then
        librariesContentRobot.tryAgainIsDisplayed()
        librariesContentRobot.errorMessageIsDisplayed(errorMessage)

    }

    @Test
    fun when_state_is_ERROR_libraries_are_not_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Error("An error occurred."))

        // Then
        librariesContentRobot.librariesListIsNotDisplayed()

    }

    @Test
    fun when_state_is_ERROR_refresh_date_is_not_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Error("An error occurred."))

        // Then
        librariesContentRobot.refreshDateIsNotDisplayed()

    }

    @Test
    fun when_state_is_ERROR_progress_indicator_is_not_displayed() {

        // Given
        librariesContentRobot.composeLibrariesContent(librariesState = LibrariesState.Error("An error occurred."))

        // Then
        librariesContentRobot.progressIndicatorIsNotDisplayed()

    }

    @Test
    fun when_a_library_is_clicked_correct_callback_is_called() {

        // Given
        val library: Library = TestData.room
        val onLibraryClick: (Library) -> Unit = mock()
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(listOf(library)),
            onLibraryClick = onLibraryClick
        )

        // When
        librariesContentRobot.clickOnLibrary(library)

        // Then
        Mockito.verify(onLibraryClick).invoke(library)

    }

    @Test
    fun when_a_library_version_is_clicked_correct_callback_is_called() {

        // Given
        val library: Library = TestData.room
        val onLibraryVersionClick: (Version) -> Unit = mock()
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(listOf(library)),
            onLibraryVersionClick = onLibraryVersionClick
        )

        // When
        librariesContentRobot.clickOnLibraryStableVersion(library)

        // Then
        Mockito.verify(onLibraryVersionClick).invoke(library.stableVersion)

    }

    @Test
    fun when_a_library_is_pinned_correct_callback_is_called() {

        // Given
        val library: Library = TestData.room
        val onLibraryPinClick: (Library, Boolean) -> Unit = mock()
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(listOf(library)),
            onLibraryPinClick = onLibraryPinClick
        )

        // When
        librariesContentRobot.clickOnPin(library)

        // Then
        Mockito.verify(onLibraryPinClick).invoke(library, true)

    }

    @Test
    fun when_a_library_is_unpinned_correct_callback_is_called() {

        // Given
        val library: Library = TestData.core
        val onLibraryPinClick: (Library, Boolean) -> Unit = mock()
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Success(listOf(library)),
            onLibraryPinClick = onLibraryPinClick
        )

        // When
        librariesContentRobot.clickOnPin(library)

        // Then
        Mockito.verify(onLibraryPinClick).invoke(library, false)

    }

    @Test
    fun when_try_again_button_is_clicked_correct_callback_is_called() {

        // Given
        val onTryAgainClick: () -> Unit = mock()
        librariesContentRobot.composeLibrariesContent(
            librariesState = LibrariesState.Error("something went wrong"),
            onTryAgainClick = onTryAgainClick
        )

        // When
        librariesContentRobot.clickOnTryAgain()

        // Then
        Mockito.verify(onTryAgainClick).invoke()

    }

}