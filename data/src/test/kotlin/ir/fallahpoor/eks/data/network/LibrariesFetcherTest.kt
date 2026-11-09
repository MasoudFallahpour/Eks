package ir.fallahpoor.eks.data.network

import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import ir.fallahpoor.eks.data.Constants
import ir.fallahpoor.eks.data.network.dto.LibraryDto
import ir.fallahpoor.eks.data.network.dto.VersionDto
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class LibrariesFetcherTest {

    private val librariesFetcher = LibrariesFetcher()

    @After
    fun runAfterEachTest() {
        unmockkStatic(Jsoup::class)
    }

    @Test
    fun `fetchLibraries downloads the versions page and parses it`() = runTest {
        // Given
        stubVersionsPage(page(row()))

        // When
        val libraries = librariesFetcher.fetchLibraries()

        // Then
        verify { Jsoup.connect("https://developer.android.com/jetpack/androidx/versions") }
        Truth.assertThat(libraries.map { it.name }).containsExactly("sample")
    }

    @Test
    fun `fetchLibraries propagates network errors`() {
        // Given
        val connection = mockk<Connection>()
        every { connection.execute() } throws IOException()
        mockkStatic(Jsoup::class)
        every { Jsoup.connect(any<String>()) } returns connection

        // When / Then
        assertThrows(IOException::class.java) {
            runBlocking { librariesFetcher.fetchLibraries() }
        }
    }

    @Test
    fun `page without tables yields no libraries`() {
        // When
        val libraries = librariesFetcher.parseLibraries("<html><body><p>Nothing here</p></body></html>")

        // Then
        Truth.assertThat(libraries).isEmpty()
    }

    @Test
    fun `library with all versions is parsed correctly`() {
        // Given
        val html = page(
            row(
                info = """<td><a href="/jetpack/androidx/releases/activity" data-title="Access composable APIs built on top of Activity.">activity</a></td>""",
                releaseDate = "<td>October 22, 2025</td>",
                stable = version("activity", "1.13.0"),
                rc = version("activity", "1.14.0-rc01"),
                beta = version("activity", "1.14.0-beta02"),
                alpha = version("activity", "1.15.0-alpha03")
            )
        )

        // When
        val libraries = librariesFetcher.parseLibraries(html)

        // Then
        Truth.assertThat(libraries).containsExactly(
            LibraryDto(
                name = "activity",
                description = "Access composable APIs built on top of Activity.",
                url = "https://developer.android.com/jetpack/androidx/releases/activity",
                releaseDate = "October 22, 2025",
                stableVersion = VersionDto(
                    name = "1.13.0",
                    releaseNotesUrl = "https://developer.android.com/jetpack/androidx/releases/activity#1.13.0"
                ),
                rcVersion = VersionDto(
                    name = "1.14.0-rc01",
                    releaseNotesUrl = "https://developer.android.com/jetpack/androidx/releases/activity#1.14.0-rc01"
                ),
                betaVersion = VersionDto(
                    name = "1.14.0-beta02",
                    releaseNotesUrl = "https://developer.android.com/jetpack/androidx/releases/activity#1.14.0-beta02"
                ),
                alphaVersion = VersionDto(
                    name = "1.15.0-alpha03",
                    releaseNotesUrl = "https://developer.android.com/jetpack/androidx/releases/activity#1.15.0-alpha03"
                )
            )
        )
    }

    @Test
    fun `libraries are returned in page order`() {
        // Given
        val html = page(
            row(info = info(name = "core")),
            row(info = info(name = "activity")),
            row(info = info(name = "room"))
        )

        // When
        val libraries = librariesFetcher.parseLibraries(html)

        // Then
        Truth.assertThat(libraries.map { it.name }).containsExactly("core", "activity", "room").inOrder()
    }

    @Test
    fun `rows without exactly six cells are ignored`() {
        // Given
        val html = page(
            "<tr><th>Library</th><th>Updated</th><th>Stable</th><th>RC</th><th>Beta</th><th>Alpha</th></tr>",
            "<tr><td>one</td><td>two</td><td>three</td><td>four</td><td>five</td></tr>",
            "<tr><td>1</td><td>2</td><td>3</td><td>4</td><td>5</td><td>6</td><td>7</td></tr>",
            row(info = info(name = "core"))
        )

        // When
        val libraries = librariesFetcher.parseLibraries(html)

        // Then
        Truth.assertThat(libraries.map { it.name }).containsExactly("core")
    }

    @Test
    fun `asterisk suffix is removed from the library name`() {
        // Given
        val html = page(row(info = info(name = "locationbutton (*)")))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("locationbutton")
    }

    @Test
    fun `info cell without a link yields not available name, description and url`() {
        // Given
        val html = page(row(info = "<td>sample</td>"))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo(Constants.NOT_AVAILABLE)
        Truth.assertThat(library.description).isEqualTo(Constants.NOT_AVAILABLE)
        Truth.assertThat(library.url).isEqualTo(Constants.NOT_AVAILABLE)
    }

    @Test
    fun `link without href yields a not available url`() {
        // Given
        val html = page(row(info = """<td><a data-title="Sample description.">sample</a></td>"""))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("sample")
        Truth.assertThat(library.url).isEqualTo(Constants.NOT_AVAILABLE)
    }

    @Test
    fun `link with a blank href yields a not available url`() {
        // Given
        val html = page(row(info = """<td><a href=" " data-title="Sample description.">sample</a></td>"""))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.url).isEqualTo(Constants.NOT_AVAILABLE)
    }

    @Test
    fun `link without data-title yields a not available description`() {
        // Given
        val html = page(row(info = """<td><a href="/jetpack/androidx/releases/sample">sample</a></td>"""))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo(Constants.NOT_AVAILABLE)
    }

    @Test
    fun `empty description yields not available`() {
        // Given
        val html = page(row(info = info(description = "")))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo(Constants.NOT_AVAILABLE)
    }

    @Test
    fun `whitespace-only description yields not available`() {
        // Given
        val html = page(row(info = info(description = " \n  \n ")))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo(Constants.NOT_AVAILABLE)
    }

    @Test
    fun `unescaped HTML in a description is reduced to plain text`() {
        // Given
        val html = page(
            row(
                info = """<td><a href="/jetpack/androidx/releases/a2ui" data-title="The Jetpack Compose Agent-to-UI (A2UI) renderer provides an implementation of the <a href="https://a2ui.org/">A2UI protocol</a>, enabling AI agents to generate rich, interactive user interfaces that render native Compose components—without executing arbitrary code.">a2ui</a></td>"""
            )
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("a2ui")
        Truth.assertThat(library.description).isEqualTo(
            "The Jetpack Compose Agent-to-UI (A2UI) renderer provides an implementation of the A2UI protocol, enabling AI agents to generate rich, interactive user interfaces that render native Compose components—without executing arbitrary code."
        )
        Truth.assertThat(library.url).isEqualTo("https://developer.android.com/jetpack/androidx/releases/a2ui")
    }

    @Test
    fun `unescaped HTML in a description does not affect the following row`() {
        // Given
        val html = page(
            row(info = info(name = "a2ui", description = """See the <a href="https://a2ui.org/">A2UI protocol</a>.""")),
            row(info = info(name = "activity", description = "Access composable APIs built on top of Activity."))
        )

        // When
        val libraries = librariesFetcher.parseLibraries(html)

        // Then
        Truth.assertThat(libraries.map { it.name to it.description }).containsExactly(
            "a2ui" to "See the A2UI protocol.",
            "activity" to "Access composable APIs built on top of Activity."
        ).inOrder()
    }

    @Test
    fun `cell that does not end right after the link is left to the HTML parser`() {
        // Given
        val html = page(
            row(info = """<td><a href="/jetpack/androidx/releases/first" data-title="First description.">first</a> (deprecated)</td>"""),
            row(info = info(name = "second", description = """Second <b>description</b>."""))
        )

        // When
        val libraries = librariesFetcher.parseLibraries(html)

        // Then
        Truth.assertThat(libraries.map { it.name to it.description }).containsExactly(
            "first" to "First description.",
            "second" to "Second description."
        ).inOrder()
    }

    @Test
    fun `HTML in a description is cleaned when the link is on its own line`() {
        // Given
        val html = page(
            row(
                info = """
                    <td>
                      <a href="/jetpack/androidx/releases/sample" data-title="Uses <code>Modifier</code> APIs.">sample</a>
                    </td>
                """.trimIndent()
            )
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("sample")
        Truth.assertThat(library.description).isEqualTo("Uses Modifier APIs.")
    }

    @Test
    fun `whitespace around a multi-line description is removed`() {
        // Given
        val html = page(
            row(
                info = """
                    <td><a href="/jetpack/androidx/releases/navigation3" data-title="
                    Navigation 3 is a new navigation library designed to work with Compose.
                    ">navigation3</a></td>
                """.trimIndent()
            )
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("navigation3")
        Truth.assertThat(library.description)
            .isEqualTo("Navigation 3 is a new navigation library designed to work with Compose.")
    }

    @Test
    fun `line breaks inside a description are collapsed into spaces`() {
        // Given
        val html = page(row(info = info(description = "Write Jetpack Compose applications\nfor Wear OS devices.")))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo("Write Jetpack Compose applications for Wear OS devices.")
    }

    @Test
    fun `quotes and ampersands in a description are preserved`() {
        // Given
        val html = page(row(info = info(description = "Use &quot;Compose&quot; &amp; Views together.")))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo("Use \"Compose\" & Views together.")
    }

    @Test
    fun `angle brackets written as entities in a description are preserved`() {
        // Given
        val html = page(row(info = info(description = "Supports List&lt;T&gt; and Map&lt;K, V&gt;.")))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo("Supports List<T> and Map<K, V>.")
    }

    @Test
    fun `release date is trimmed`() {
        // Given
        val html = page(row(releaseDate = "<td>\n  October 22, 2025\n</td>"))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.releaseDate).isEqualTo("October 22, 2025")
    }

    @Test
    fun `dash in a version cell yields an unavailable version`() {
        // Given
        val html = page(row(stable = "<td>-</td>"))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.stableVersion).isEqualTo(VersionDto())
    }

    @Test
    fun `version cell without a link yields an unavailable version`() {
        // Given
        val html = page(row(beta = "<td>1.0.0-beta01</td>"))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.betaVersion).isEqualTo(VersionDto())
    }

    @Test
    fun `version link without href keeps the version name but has no release notes url`() {
        // Given
        val html = page(row(rc = "<td><a>1.0.0-rc01</a></td>"))

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.rcVersion)
            .isEqualTo(VersionDto(name = "1.0.0-rc01", releaseNotesUrl = Constants.NOT_AVAILABLE))
    }

    private fun stubVersionsPage(html: String) {
        val response = mockk<Connection.Response>()
        every { response.body() } returns html
        val connection = mockk<Connection>()
        every { connection.execute() } returns response
        mockkStatic(Jsoup::class)
        every { Jsoup.connect(any<String>()) } returns connection
    }

    private fun page(vararg rows: String): String =
        "<html><body><table>${rows.joinToString(separator = "\n")}</table></body></html>"

    private fun row(
        info: String = info(),
        releaseDate: String = "<td>October 22, 2025</td>",
        stable: String = NO_VERSION,
        rc: String = NO_VERSION,
        beta: String = NO_VERSION,
        alpha: String = NO_VERSION
    ): String = "<tr>\n$info\n$releaseDate\n$stable\n$rc\n$beta\n$alpha\n</tr>"

    private fun info(name: String = "sample", description: String = "Sample description."): String =
        """<td><a href="/jetpack/androidx/releases/$name" data-title="$description">$name</a></td>"""

    private fun version(library: String, version: String): String =
        """<td><a href="/jetpack/androidx/releases/$library#$version">$version</a></td>"""

    private companion object {
        const val NO_VERSION = "<td>-</td>"
    }

}
