package tech.amcg.jukebox.domain

import tech.amcg.jukebox.service.JukeboxSessionId

data class JukeboxSession(
        val sessionId: JukeboxSessionId,
        val sessionHost: String,
        val authToken: SpotifyApiToken
)