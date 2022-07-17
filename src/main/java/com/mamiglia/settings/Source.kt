package com.mamiglia.settings

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class Source(
    val id: String = UUID.randomUUID().toString(),
    var titles: Set<String> = setOf(),
    var flairs: Set<String> = setOf(),
    var subreddits: Set<String> = setOf("wallpaper", "wallpapers"),
    var searchBy: SEARCH_BY = SEARCH_BY.HOT,
    var nsfwLevel: NSFW_LEVEL = NSFW_LEVEL.ALLOW,
    var minScore: Int = 15,
    var maxOldness:TIME = TIME.DAY,
) {
    @Transient
    var name : String = ""
        get() = if (field == "") "$subreddits${if (titles.isNotEmpty()) " | $titles" else ""}${if (flairs.isNotEmpty()) " | $flairs" else ""}" else field

    override fun toString() : String {
        return name
    }

}
