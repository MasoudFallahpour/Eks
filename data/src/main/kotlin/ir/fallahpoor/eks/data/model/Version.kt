package ir.fallahpoor.eks.data.model

import ir.fallahpoor.eks.data.Constants
import ir.fallahpoor.eks.data.entity.VersionEntity

data class Version(
    val name: String = Constants.NOT_AVAILABLE,
    val releaseNotesUrl: String = Constants.NOT_AVAILABLE
)

internal fun Version.toVersionEntity() = VersionEntity(
    name = this.name,
    releaseNotesUrl = this.releaseNotesUrl
)