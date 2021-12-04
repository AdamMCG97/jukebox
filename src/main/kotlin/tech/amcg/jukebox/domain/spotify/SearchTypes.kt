package tech.amcg.jukebox.domain.spotify

enum class Types(val value: String) {
    ALBUM("album"),
    ARTIST("artist"),
    PLAYLIST("playlist"),
    TRACK("track"),
    SHOW("show"),
    EPISODE("episode");
}

fun allTypes(): List<Types> {
    return listOf(Types.ALBUM, Types.ARTIST, Types.PLAYLIST, Types.TRACK, Types.SHOW, Types.EPISODE)
}

fun allTypesJoinedToString(): String {
    return allTypes().map { it.value }.joinToString(",")
}

fun songTypesJoinedToString(): String {
    return listOf(Types.ALBUM, Types.ARTIST, Types.TRACK).map { it.value }.joinToString(",")
}
