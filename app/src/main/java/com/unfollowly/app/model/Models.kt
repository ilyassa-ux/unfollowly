package com.unfollowly.app.model

data class Snapshot(
    val createdAt: Long,
    val followers: Set<String>,
    val following: Set<String>
)

data class Insights(
    val unfollowers: Set<String> = emptySet(),
    val newFollowers: Set<String> = emptySet(),
    val notFollowingBack: Set<String> = emptySet(),
    val fans: Set<String> = emptySet(),
    val mutuals: Set<String> = emptySet()
)

fun Snapshot.compare(previous: Snapshot?): Insights = Insights(
    unfollowers = previous?.followers.orEmpty() - followers,
    newFollowers = followers - previous?.followers.orEmpty(),
    notFollowingBack = following - followers,
    fans = followers - following,
    mutuals = followers intersect following
)
