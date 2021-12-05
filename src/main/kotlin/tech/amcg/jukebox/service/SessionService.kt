package tech.amcg.jukebox.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import tech.amcg.jukebox.domain.JukeboxSession
import tech.amcg.jukebox.domain.JukeboxSessionId
import tech.amcg.jukebox.domain.SessionState
import tech.amcg.jukebox.domain.SpotifyApiToken
import tech.amcg.jukebox.domain.exception.NoSuchActiveSessionException
import java.time.Instant
import java.util.UUID

@Service
class SessionService {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    //TODO: Evaluate options for this, at least make it atomic
    private val sessions = mutableMapOf<JukeboxSessionId, JukeboxSession>()

    private val requestsToEndSessions = mutableMapOf<String, JukeboxSessionId>()

    private fun findInactiveSession(sessionId: JukeboxSessionId): JukeboxSession? {
        return sessions.filter { it.value.state == SessionState.INACTIVE }.get(sessionId)
    }

    fun findActiveSession(sessionId: JukeboxSessionId): JukeboxSession? {
        return sessions.filter { it.value.state == SessionState.ACTIVE }.get(sessionId)
    }

    fun findSession(sessionId: JukeboxSessionId): JukeboxSession? {
        return sessions.get(sessionId)
    }

    fun requestToEndSession(state: String, sessionId: JukeboxSessionId) {
        requestsToEndSessions.put(state, sessionId)
    }

    fun validateRequestToEndSession(state: String, userRequestingSessionEnd: String): JukeboxSessionId? {
        val endSessionIdRequest = requestsToEndSessions.get(state)
        if(endSessionIdRequest != null) {
            if(sessions.get(endSessionIdRequest)?.sessionHost == userRequestingSessionEnd) {
                removeSession(endSessionIdRequest)
                logger.info("Ending session with id=$endSessionIdRequest")
            } else {
                logger.info("Attempt made to end session by non-host user. Session=$endSessionIdRequest kept alive.")
            }
        }
        return endSessionIdRequest
    }

    private fun removeSession(sessionId: JukeboxSessionId) {
        val session = findActiveSession(sessionId)
                ?: throw NoSuchActiveSessionException("Could not find active session with id=$sessionId to end session.")
        val inactiveSession = session.copy(state = SessionState.INACTIVE, ended = Instant.now())
        sessions.replace(sessionId, inactiveSession)
    }

    fun createSession(token: SpotifyApiToken, host: String): JukeboxSessionId {
        val sessionId = JukeboxSessionId(UUID.randomUUID().toString())
        val jukeboxSession = JukeboxSession(sessionId, host, token, SessionState.ACTIVE)
        sessions.put(jukeboxSession.sessionId, jukeboxSession)
        return sessionId
    }

    fun updateSessionWithNewToken(token: SpotifyApiToken, sessionId: JukeboxSessionId) {
        val session = findActiveSession(sessionId)
                ?: throw NoSuchActiveSessionException("Could not find active session=$sessionId to update auth token.")
        val updatedSession = session.copy(authToken = token)
        sessions.replace(sessionId, updatedSession)
        logger.info("Updated session=$sessionId with new auth token.")
    }

}