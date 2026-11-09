package ir.fallahpoor.eks.libraries.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.google.common.truth.Truth
import ir.fallahpoor.eks.commontest.FakeLibraryRepository
import ir.fallahpoor.eks.commontest.FakeStorageRepository
import ir.fallahpoor.eks.commontest.MainDispatcherRule
import ir.fallahpoor.eks.commontest.TestData
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.libraries.ui.LibrariesScreenUiState
import ir.fallahpoor.eks.libraries.ui.LibrariesState
import ir.fallahpoor.eks.libraries.viewmodel.exceptionparser.FakeExceptionParser
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LibrariesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var librariesViewModel: LibrariesViewModel
    private lateinit var libraryRepository: FakeLibraryRepository
    private lateinit var storageRepository: FakeStorageRepository

    @Before
    fun runBeforeEachTest() {
        libraryRepository = FakeLibraryRepository()
        storageRepository = FakeStorageRepository()
        librariesViewModel = LibrariesViewModel(
            libraryRepository = libraryRepository,
            storageRepository = storageRepository,
            exceptionParser = FakeExceptionParser()
        )
    }

    @Test
    fun `Libraries are loaded when the state is first collected`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            val actualState = awaitItem()
            val expectedState = loadedState()
            Truth.assertThat(actualState).isEqualTo(expectedState)
        }
    }

    @Test
    fun `Error state is shown when loading the libraries on first collection fails`() = runTest {
        libraryRepository.throwException = true
        librariesViewModel.librariesScreenUiState.test {
            val actualState = awaitItem()
            val expectedState = errorState()
            Truth.assertThat(actualState).isEqualTo(expectedState)
        }
    }

    @Test
    fun `Libraries are not reloaded when the state is collected again`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
        }
        libraryRepository.throwException = true

        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
        }
    }

    @Test
    fun `Sequence of states is correct when getting the list of libraries succeeds`() = runTest {
        libraryRepository.throwException = true
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(errorState())
            libraryRepository.throwException = false

            // When
            librariesViewModel.handleEvent(LibrariesViewModel.Event.GetLibraries)

            Truth.assertThat(awaitItem()).isEqualTo(LibrariesScreenUiState())
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
        }
    }

    @Test
    fun `Sequence of states is correct when getting the list of libraries fails`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
            libraryRepository.throwException = true

            // When
            librariesViewModel.handleEvent(LibrariesViewModel.Event.GetLibraries)

            Truth.assertThat(awaitItem()).isEqualTo(LibrariesScreenUiState())
            Truth.assertThat(awaitItem()).isEqualTo(errorState())
        }
    }

    @Test
    fun `Sequence of states is correct when changing the sort order succeeds`() = runTest {
        val expectedSortOrder = SortOrder.Z_TO_A
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())

            // When
            librariesViewModel.handleEvent(
                LibrariesViewModel.Event.ChangeSortOrder(
                    sortOrder = expectedSortOrder
                )
            )

            Truth.assertThat(awaitItem()).isEqualTo(LibrariesScreenUiState(sortOrder = expectedSortOrder))
            Truth.assertThat(awaitItem()).isEqualTo(loadedState(sortOrder = expectedSortOrder))
        }
    }

    @Test
    fun `Sequence of states is correct when changing the sort order fails`() = runTest {
        val expectedSortOrder = SortOrder.Z_TO_A
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
            libraryRepository.throwException = true

            // When
            librariesViewModel.handleEvent(
                LibrariesViewModel.Event.ChangeSortOrder(
                    sortOrder = expectedSortOrder
                )
            )

            Truth.assertThat(awaitItem()).isEqualTo(LibrariesScreenUiState(sortOrder = expectedSortOrder))
            Truth.assertThat(awaitItem()).isEqualTo(errorState().copy(sortOrder = expectedSortOrder))
        }
        // TODO assert that sort order is not persisted

        // Then
        Truth.assertThat(storageRepository.getSortOrder()).isEqualTo(expectedSortOrder)
    }

    @Test
    fun `Sort order is persisted when changing the sort order succeeds`() {

        // Given
        val expectedSortOrder = SortOrder.Z_TO_A

        // When
        librariesViewModel.handleEvent(
            LibrariesViewModel.Event.ChangeSortOrder(
                sortOrder = expectedSortOrder
            )
        )

        // Then
        Truth.assertThat(storageRepository.getSortOrder()).isEqualTo(expectedSortOrder)

    }

    @Test
    fun `Sequence of states is correct when changing the search query succeeds`() = runTest {
        val searchQuery = "ko"
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())

            // When
            librariesViewModel.handleEvent(LibrariesViewModel.Event.ChangeSearchQuery(searchQuery))

            Truth.assertThat(awaitItem()).isEqualTo(LibrariesScreenUiState(searchQuery = searchQuery))
            Truth.assertThat(awaitItem()).isEqualTo(loadedState(searchQuery = searchQuery))
        }
    }

    @Test
    fun `Sequence of states is correct when changing the search query fails`() = runTest {
        val searchQuery = "ko"
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
            libraryRepository.throwException = true

            // When
            librariesViewModel.handleEvent(LibrariesViewModel.Event.ChangeSearchQuery(searchQuery))

            Truth.assertThat(awaitItem()).isEqualTo(LibrariesScreenUiState(searchQuery = searchQuery))
            Truth.assertThat(awaitItem()).isEqualTo(errorState().copy(searchQuery = searchQuery))
        }
    }

    @Test
    fun `Sequence of states is correct when pinning a library succeeds`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())

            // When
            librariesViewModel.handleEvent(
                LibrariesViewModel.Event.PinLibrary(library = TestData.preference, pin = true)
            )

            val pinnedLibrary = awaitItem().librariesState.libraries()
                .single { it.name.equals(TestData.preference.name, ignoreCase = true) }
            Truth.assertThat(pinnedLibrary.isPinned).isTrue()
        }
    }

    @Test
    fun `Sequence of states is correct when pinning a library fails`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
            libraryRepository.throwException = true

            // When
            librariesViewModel.handleEvent(
                LibrariesViewModel.Event.PinLibrary(library = TestData.preference, pin = true)
            )

            Truth.assertThat(awaitItem()).isEqualTo(errorState())
        }
    }

    @Test
    fun `Sequence of states is correct when unpinning a library succeeds`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())

            // When
            librariesViewModel.handleEvent(
                LibrariesViewModel.Event.PinLibrary(library = TestData.core, pin = false)
            )

            val unpinnedLibrary = awaitItem().librariesState.libraries()
                .single { it.name.equals(TestData.core.name, ignoreCase = true) }
            Truth.assertThat(unpinnedLibrary.isPinned).isFalse()
        }
    }

    @Test
    fun `Sequence of states is correct when unpinning a library fails`() = runTest {
        librariesViewModel.librariesScreenUiState.test {
            Truth.assertThat(awaitItem()).isEqualTo(loadedState())
            libraryRepository.throwException = true

            // When
            librariesViewModel.handleEvent(
                LibrariesViewModel.Event.PinLibrary(library = TestData.core, pin = false)
            )

            Truth.assertThat(awaitItem()).isEqualTo(errorState())
        }
    }

    private suspend fun loadedState(
        sortOrder: SortOrder = SortOrder.A_TO_Z,
        searchQuery: String = ""
    ): LibrariesScreenUiState {
        val throwException = libraryRepository.throwException
        libraryRepository.throwException = false
        val libraries = libraryRepository.getLibraries(sortOrder = sortOrder, searchQuery = searchQuery)
        libraryRepository.throwException = throwException
        return LibrariesScreenUiState(
            sortOrder = sortOrder,
            searchQuery = searchQuery,
            librariesState = LibrariesState.Success(libraries)
        )
    }

    private fun errorState() = LibrariesScreenUiState(
        librariesState = LibrariesState.Error(FakeExceptionParser.ERROR_MESSAGE)
    )

    private fun LibrariesState.libraries(): List<Library> = (this as LibrariesState.Success).libraries

}
