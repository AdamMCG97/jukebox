package tech.amcg.jukebox.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.amcg.jukebox.domain.JukeboxSession
import tech.amcg.jukebox.domain.SpotifyApiToken
import tech.amcg.jukebox.domain.StringType
import java.util.*

@Service
class SessionService {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    //TODO: Evaluate options for this, at least make it atomic
    private val currentSessions = mutableMapOf<JukeboxSessionId, JukeboxSession>()

    private val requestsToEndSessions = mutableMapOf<String, JukeboxSessionId>()

    fun findSession(sessionId: JukeboxSessionId): SpotifyApiToken? {
        return currentSessions.get(sessionId)?.authToken
    }

    fun checkSessionExists(sessionId: JukeboxSessionId): Boolean {
        return findSession(sessionId) != null
    }

    fun requestToEndSession(state: String, sessionId: JukeboxSessionId) {
        requestsToEndSessions.put(state, sessionId)
    }

    fun validateRequestToEndSession(state: String, userRequestingSessionEnd: String): Boolean {
        val endSessionIdRequest = requestsToEndSessions.get(state)
        if(endSessionIdRequest != null) {
            if(currentSessions.get(endSessionIdRequest)?.sessionHost == userRequestingSessionEnd) {
                removeSession(endSessionIdRequest)
                logger.info("Ending session with id=$endSessionIdRequest")
                return true
            } else {
                logger.info("Attempt made to end session by non-host user. Session=$endSessionIdRequest kept alive.")
            }
        }
        return false
    }

    private fun removeSession(sessionId: JukeboxSessionId) {
        currentSessions.remove(sessionId)
    }

    fun createSession(token: SpotifyApiToken, host: String): JukeboxSessionId {
        val sessionId = JukeboxSessionId(UUID.randomUUID().toString())
        val jukeboxSession = JukeboxSession(sessionId, host, token)
        currentSessions.put(jukeboxSession.sessionId, jukeboxSession)
        return sessionId
    }

    fun updateSessionWithNewToken(token: SpotifyApiToken, sessionId: JukeboxSessionId) {
        val session = currentSessions.get(sessionId)
                ?: throw NoSuchSessionException("Could not find session=$sessionId to update auth token.")
        val updatedSession = session.copy(authToken = token)
        currentSessions.replace(sessionId, updatedSession)
        logger.info("Updated session=$sessionId with new auth token.")
    }

}

class JukeboxSessionId(sessionId: String) : StringType<JukeboxSessionId>(sessionId)

class NoSuchSessionException(message: String) : RuntimeException(message)