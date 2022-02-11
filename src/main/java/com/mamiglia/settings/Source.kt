package com.mamiglia.settings
import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val titles: Set<String> = setOf(),
    val flairs: Set<String> = setOf(),
    val subreddits: Set<String> = setOf("wallpaper", "wallpapers"),
    val searchBy: SEARCH_BY = SEARCH_BY.HOT,
    val nsfwLevel: NSFW_LEVEL = NSFW_LEVEL.ALLOW,
    val minScore: Int = 15,
    val maxOldness:TIME = TIME.DAY
)
