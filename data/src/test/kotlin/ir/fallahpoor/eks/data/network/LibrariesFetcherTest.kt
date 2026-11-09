package ir.fallahpoor.eks.data.network

import com.google.common.truth.Truth
import ir.fallahpoor.eks.data.Constants
import ir.fallahpoor.eks.data.network.dto.LibraryDto
import ir.fallahpoor.eks.data.network.dto.VersionDto
import org.junit.Test

class LibrariesFetcherTest {

    private val librariesFetcher = LibrariesFetcher()

    @Test
    fun `library with a plain description is parsed correctly`() {
        // Given
        val html = page(
            """
            <tr id="activity">
            <td><a href="/jetpack/androidx/releases/activity" data-title="Access composable APIs built on top of Activity.">activity</a></td>
            <td>October 22, 2025</td>
            <td><a href="/jetpack/androidx/releases/activity#1.13.0">1.13.0</a></td>
            <td>-</td>
            <td>-</td>
            <td><a href="/jetpack/androidx/releases/activity#1.14.0-alpha03">1.14.0-alpha03</a></td>
            </tr>
            """
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
                rcVersion = VersionDto(),
                betaVersion = VersionDto(),
                alphaVersion = VersionDto(
                    name = "1.14.0-alpha03",
                    releaseNotesUrl = "https://developer.android.com/jetpack/androidx/releases/activity#1.14.0-alpha03"
                )
            )
        )
    }

    @Test
    fun `unescaped HTML in a description is reduced to plain text`() {
        // Given
        val html = page(
            """
            <tr id="a2ui">
            <td><a href="/jetpack/androidx/releases/a2ui" data-title="The Jetpack Compose Agent-to-UI (A2UI) renderer provides an implementation of the <a href="https://a2ui.org/">A2UI protocol</a>, enabling AI agents to generate rich, interactive user interfaces that render native Compose components—without executing arbitrary code.">a2ui</a></td>
            <td>October 21, 2026</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            <td><a href="/jetpack/androidx/releases/a2ui#1.0.0-alpha02">1.0.0-alpha02</a></td>
            </tr>
            """
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("a2ui")
        Truth.assertThat(library.description).isEqualTo(
            "The Jetpack Compose Agent-to-UI (A2UI) renderer provides an implementation of the A2UI protocol, enabling AI agents to generate rich, interactive user interfaces that render native Compose components—without executing arbitrary code."
        )
        Truth.assertThat(library.url).isEqualTo("https://developer.android.com/jetpack/androidx/releases/a2ui")
        Truth.assertThat(library.alphaVersion.name).isEqualTo("1.0.0-alpha02")
    }

    @Test
    fun `whitespace around a multi-line description is removed`() {
        // Given
        val html = page(
            """
            <tr id="navigation3">
            <td><a href="/jetpack/androidx/releases/navigation3" data-title="
            Navigation 3 is a new navigation library designed to work with Compose.
            ">navigation3</a></td>
            <td>October 22, 2025</td>
            <td><a href="/jetpack/androidx/releases/navigation3#1.0.0">1.0.0</a></td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            </tr>
            """
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.name).isEqualTo("navigation3")
        Truth.assertThat(library.description)
            .isEqualTo("Navigation 3 is a new navigation library designed to work with Compose.")
    }

    @Test
    fun `quotes and ampersands in a description are preserved`() {
        // Given
        val html = page(
            """
            <tr id="sample">
            <td><a href="/jetpack/androidx/releases/sample" data-title="Use &quot;Compose&quot; &amp; Views together.">sample</a></td>
            <td>October 22, 2025</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            </tr>
            """
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo("Use \"Compose\" & Views together.")
    }

    @Test
    fun `library without a description gets the not available placeholder`() {
        // Given
        val html = page(
            """
            <tr id="sample">
            <td><a href="/jetpack/androidx/releases/sample" data-title="">sample</a></td>
            <td>October 22, 2025</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            <td>-</td>
            </tr>
            """
        )

        // When
        val library = librariesFetcher.parseLibraries(html).single()

        // Then
        Truth.assertThat(library.description).isEqualTo(Constants.NOT_AVAILABLE)
    }

    private fun page(rows: String): String =
        "<html><body><table>${rows.trimIndent()}</table></body></html>"

}
