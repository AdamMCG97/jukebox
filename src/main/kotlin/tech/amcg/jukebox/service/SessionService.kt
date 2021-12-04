package tech.amcg.jukebox.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.amcg.jukebox.domain.SpotifyApiToken
import tech.amcg.jukebox.domain.StringType
import java.util.*

@Service
class SessionService {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    //TODO: Evaluate options for this, at least make it atomic
    private val currentSessions = mutableMapOf<JukeboxSession, SpotifyApiToken>()

    private val requestsToEndSessions = mutableMapOf<String, JukeboxSession>()

    fun findSession(session: JukeboxSession): SpotifyApiToken? {
        return currentSessions.get(session)
    }

    fun checkSessionExists(session: JukeboxSession): Boolean {
        return findSession(session) != null
    }

    fun requestToEndSession(state: String, session: JukeboxSession) {
        requestsToEndSessions.put(state, session)
    }

    fun validateRequestToEndSession(state: String): Boolean {
        val endSessionIdRequest = requestsToEndSessions.get(state)
        return if(endSessionIdRequest != null) {
            removeSession(endSessionIdRequest)
            logger.info("Ending session with id=$endSessionIdRequest")
            true
        } else {
            false
        }
    }

    private fun removeSession(session: JukeboxSession) {
        currentSessions.remove(session)
    }

    fun createSession(token: SpotifyApiToken): JukeboxSession {
        val jukeboxSession = JukeboxSession(UUID.randomUUID().toString())
        currentSessions.put(jukeboxSession, token)
        return jukeboxSession
    }

}

class JukeboxSession(sessionId: String) : StringType<JukeboxSession>(sessionId)

class NoSuchSessionException(message: String) : RuntimeException(message)