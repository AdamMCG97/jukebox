package tech.amcg.jukebox.domain

enum class SpotifyScope(val permissionName: String) {
    USER_MODIFY_PLAYBACK_STATE("user-modify-playback-state"),
    USER_READ_PLAYBACK_STATE("user-read-playback-state")
}