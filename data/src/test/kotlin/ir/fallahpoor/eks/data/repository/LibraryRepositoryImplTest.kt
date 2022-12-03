package ir.fallahpoor.eks.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import ir.fallahpoor.eks.commontest.MainDispatcherRule
import ir.fallahpoor.eks.data.SortOrder
import ir.fallahpoor.eks.data.TestData
import ir.fallahpoor.eks.data.fakes.FakeLibraryDao
import ir.fallahpoor.eks.data.fakes.FakeStorage
import ir.fallahpoor.eks.data.network.LibrariesFetcher
import ir.fallahpoor.eks.data.network.dto.LibraryDto
import ir.fallahpoor.eks.data.network.dto.toLibraryEntity
import ir.fallahpoor.eks.data.repository.model.Library
import ir.fallahpoor.eks.data.repository.model.toLibraryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LibraryRepositoryImplTest {

    companion object {
        private const val REFRESH_DATE = "September 11, 09:10"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var librariesFetcher: LibrariesFetcher

    @Mock
    private lateinit var dateProvider: DateProvider

    private lateinit var libraryRepository: LibraryRepositoryImpl
    private lateinit var libraryDao: FakeLibraryDao
    private lateinit var storage: FakeStorage

    @Before
    fun runBeforeEachTest() {
        storage = FakeStorage()
        libraryDao = FakeLibraryDao()
        libraryRepository = LibraryRepositoryImpl(
            storage = storage,
            libraryDao = libraryDao,
            librariesFetcher = librariesFetcher,
            dateProvider = dateProvider
        )
        Mockito.`when`(dateProvider.getCurrentDate()).thenReturn(REFRESH_DATE)
    }

    @Test
    fun `list of libraries is fetched from the Internet when cache is empty`() = runTest {

        // Given
        val expectedLibraries = getOldLibraries()
        Mockito.`when`(librariesFetcher.fetchLibraries()).thenReturn(expectedLibraries)

        // When
        val actualLibraries = libraryRepository.getLibraries()

        // Then
        Truth.assertThat(actualLibraries.map(Library::toLibraryEntity))
            .isEqualTo(expectedLibraries.map(LibraryDto::toLibraryEntity))
        Mockito.verify(librariesFetcher).fetchLibraries()

    }

    private fun getOldLibraries() = listOf(
        TestData.activity, TestData.biometric, TestData.core, TestData.room
    )

    @Test
    fun `list of libraries is returned from cache when cache is not empty`() = runTest {

        // Given
        val expectedLibraries = getOldLibraries().map(LibraryDto::toLibraryEntity)
        libraryDao.insertLibraries(expectedLibraries)

        // When
        val actualLibraries = libraryRepository.getLibraries()

        // Then
        Truth.assertThat(actualLibraries.map(Library::toLibraryEntity)).isEqualTo(expectedLibraries)
        Mockito.verifyNoInteractions(librariesFetcher)

    }

    @Test
    fun `list of libraries is returned sorted by given sort order`() = runTest {

        // Given
        Mockito.`when`(librariesFetcher.fetchLibraries()).thenReturn(getOldLibraries())

        // When
        val actualLibraries = libraryRepository.getLibraries(sortOrder = SortOrder.Z_TO_A)

        // Then
        val expectedLibraries = listOf(
            TestData.room, TestData.core, TestData.biometric, TestData.activity
        )
        Truth.assertThat(actualLibraries.map(Library::toLibraryEntity))
            .isEqualTo(expectedLibraries.map(LibraryDto::toLibraryEntity))
        Mockito.verify(librariesFetcher).fetchLibraries()

    }

    @Test
    fun `list of libraries is filtered by given search query`() = runTest {

        // Given
        libraryDao.insertLibraries(getOldLibraries().map(LibraryDto::toLibraryEntity))

        // When
        val actualLibraries = libraryRepository.getLibraries(searchQuery = "ro")

        // Then
        Truth.assertThat(actualLibraries.map(Library::toLibraryEntity))
            .isEqualTo(listOf(TestData.room).map(LibraryDto::toLibraryEntity))

    }

    @Test
    fun `library is pinned`() = runTest {

        // Given
        val library = TestData.activity
        libraryDao.insertLibraries(listOf(library).map(LibraryDto::toLibraryEntity))

        // When
        val libraryToPin: Library =
            libraryRepository.getLibraries().first { it.name == library.name }
        libraryRepository.pinLibrary(libraryToPin, true)

        // Then
        val pinnedLibrary = libraryRepository.getLibraries().first { it.name == library.name }
        Truth.assertThat(pinnedLibrary.isPinned).isTrue()

    }

    @Test
    fun `library is unpinned`() = runTest {

        // Given
        val library = TestData.core
        libraryDao.insertLibraries(listOf(library).map { it.toLibraryEntity(true) })

        // When
        val libraryToUnpin: Library =
            libraryRepository.getLibraries().first { it.name == library.name }
        libraryRepository.pinLibrary(libraryToUnpin, false)

        // Then
        val unpinnedLibrary: Library =
            libraryRepository.getLibraries().first { it.name == library.name }
        Truth.assertThat(unpinnedLibrary.isPinned).isFalse()

    }

    @Test
    fun `refreshing libraries replaces old libraries with the latest ones`() = runTest {

        // Given
        libraryDao.insertLibraries(getOldLibraries().map(LibraryDto::toLibraryEntity))
        val expectedLibraries = listOf(
            TestData.core, TestData.preference, TestData.room
        )
        Mockito.`when`(librariesFetcher.fetchLibraries()).thenReturn(expectedLibraries)

        // When
        libraryRepository.refreshLibraries()

        // Then
        Truth.assertThat(libraryDao.getLibraries())
            .isEqualTo(expectedLibraries.map(LibraryDto::toLibraryEntity))

    }

    @Test
    fun `refreshing libraries preserves the pin state of libraries`() = runTest {

        // Given
        val libraryToPin = TestData.core
        libraryDao.insertLibraries(getOldLibraries().map {
            it.toLibraryEntity(it.name.equals(libraryToPin.name, ignoreCase = true))
        })
        val refreshedLibraries = listOf(
            TestData.core, TestData.preference, TestData.room
        )
        Mockito.`when`(librariesFetcher.fetchLibraries()).thenReturn(refreshedLibraries)

        // When
        libraryRepository.refreshLibraries()

        // Then
        val actualPinStates = libraryDao.getLibraries().map { it.pinned == 1 }
        Truth.assertThat(actualPinStates).isEqualTo(listOf(true, false, false))

    }

    @Test
    fun `refresh date is saved when libraries are refreshed`() = runTest {

        // Given
        Mockito.`when`(librariesFetcher.fetchLibraries()).thenReturn(emptyList())

        // When
        libraryRepository.refreshLibraries()

        // Then
        Truth.assertThat(storage.getRefreshDateAsFlow().first()).isEqualTo(REFRESH_DATE)

    }

    @Test
    fun `correct refresh date is returned`() = runTest {

        // Given
        storage.setRefreshDate(REFRESH_DATE)

        // When
        val actualRefreshDate: String = libraryRepository.getRefreshDate().first()

        // Then
        Truth.assertThat(actualRefreshDate).isEqualTo(REFRESH_DATE)

    }

    @Test
    fun `save sort order`() = runTest {

        // Given

        // When
        libraryRepository.saveSortOrder(SortOrder.PINNED_FIRST)

        // Then
        Truth.assertThat(storage.getSortOrder()).isEqualTo(SortOrder.PINNED_FIRST)

    }

    @Test
    fun `correct sort order is returned`() = runTest {

        // Given
        storage.setSortOrder(SortOrder.Z_TO_A)

        // When
        val actualSortOrder: SortOrder = libraryRepository.getSortOrder()

        // Then
        Truth.assertThat(actualSortOrder).isEqualTo(SortOrder.Z_TO_A)

    }

    // TODO write a test to verify that libraryRepository.getSortOrderAsFlow() returns the collect sequence
    //  of values

}