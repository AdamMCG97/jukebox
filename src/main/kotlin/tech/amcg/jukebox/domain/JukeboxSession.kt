package tech.amcg.jukebox.domain

import java.time.Instant

data class JukeboxSession(
        val sessionId: JukeboxSessionId,
        val sessionHost: String,
        val authToken: SpotifyApiToken,
        val state: SessionState,
        val sessionTracks: MutableList<SpotifyTrack> = mutableListOf(),
        val currentQueue: MutableList<SpotifyTrack> = mutableListOf(),
        val started: Instant = Instant.now(),
        val ended: Instant? = null
)

enum class SessionState {
    ACTIVE,
    INACTIVE
}

class JukeboxSessionId(sessionId: String) : StringType<JukeboxSessionId>(sessionId)

