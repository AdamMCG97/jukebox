package tech.amcg.jukebox.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import tech.amcg.jukebox.domain.PlaybackState
import tech.amcg.jukebox.domain.SpotifyApiToken
import tech.amcg.jukebox.domain.exception.PlaybackStateNotFoundException
import tech.amcg.jukebox.domain.spotify.Types
import tech.amcg.jukebox.utils.RequestUtils.handle4xxErrors
import tech.amcg.jukebox.utils.RequestUtils.paramsMapToParamString

@Service
class SpotifyApiService(
        private val webClient: WebClient,
        private val sessionService: SessionService,
        private val authService: SpotifyAuthService,
        private val mapper: ObjectMapper
) {

    @Value("\${spotify.api.baseUrl}")
    private lateinit var apiUrl: String

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java)
        const val USER_NOT_FOUND = "user-not-found"
        const val PAGE_SIZE = 50
    }

    private fun getValidTokenFromSession(sessionId: JukeboxSessionId): SpotifyApiToken {
        val token = sessionService.findSession(sessionId)
                ?: throw NoSuchSessionException("Session ($sessionId) does not exist ")
        if(token.hasExpired()) {
            //TODO: Add call to refresh token here when capability developed
            logger.info("Token has expired for session=$sessionId. Beginning token refresh.")
            val refreshedToken = authService.refreshToken(token)
            val updatedToken = token.updateWithRefreshedToken(refreshedToken)
            sessionService.updateSessionWithNewToken(updatedToken, sessionId)
            return updatedToken
        }
        return token
    }

    private fun parsePlaybackState(json: String): Boolean {
        try {
            val parsedJson = mapper.readTree(json)
            val playbackState = parsedJson.findParent("is_active").get("is_active")
            return playbackState.asBoolean()
        } catch (ex: JsonProcessingException) {
            throw PlaybackStateNotFoundException("Could not get playback state for session.")
        }
    }

    fun getPlaybackState(sessionId: JukeboxSessionId): PlaybackState {
        val token = getValidTokenFromSession(sessionId)
        val requestUrl = "$apiUrl/me/player"
        logger.info("Post to $requestUrl with sessionId=$sessionId")
        val start = System.currentTimeMillis()
        val retrieve = webClient
                .get()
                .uri(requestUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "${token.tokenType} ${token.accessToken}")
                .retrieve()
                .onStatus({ it.is4xxClientError }, ::handle4xxErrors)
        val statusCode = retrieve.toBodilessEntity().block()?.statusCode
        val elapsed = System.currentTimeMillis() - start
        logger.info("Received $statusCode response after ${elapsed}ms from Post to $requestUrl")
        if(statusCode == HttpStatus.NO_CONTENT) {
            return PlaybackState.NO_ACTIVE_SESSION
        }
        val response = retrieve.bodyToMono(String::class.java).block()
                ?: throw PlaybackStateNotFoundException("Could not read response after receiving status code=$statusCode")
        return if(parsePlaybackState(response)) {
            PlaybackState.ACTIVE_SESSION
        } else {
            PlaybackState.NO_ACTIVE_SESSION
        }
    }

    fun addTrackToQueue(trackId: String, sessionId: JukeboxSessionId): Boolean {
        val playbackState = getPlaybackState(sessionId)
        if(playbackState == PlaybackState.NO_ACTIVE_SESSION) {
            return false
        }
        val token = getValidTokenFromSession(sessionId)
        val trackQueueUrl = "$apiUrl/me/player/queue"
        logger.info("Post to $trackQueueUrl with trackId=$trackId")
        val requestUrl = "$trackQueueUrl?uri=$trackId"
        val start = System.currentTimeMillis()
        val retrieve = webClient
                .post()
                .uri(requestUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "${token.tokenType} ${token.accessToken}")
                .retrieve()
                .onStatus({ it.is4xxClientError }, ::handle4xxErrors)
        val statusCode = retrieve.toBodilessEntity().block()?.statusCode
        val elapsed = System.currentTimeMillis() - start
        logger.info("Received $statusCode response after ${elapsed}ms from Post to $requestUrl")
        return true
    }

    fun search(sessionId: JukeboxSessionId, query: String) {
        val token = getValidTokenFromSession(sessionId)
        val requestUrl = "$apiUrl/search"
        val params = mapOf(
                "q" to "name:$query",
                "type" to Types.TRACK.value,
                "limit" to PAGE_SIZE
        )
        val urlWithParams = requestUrl + paramsMapToParamString(params)
        logger.info("Get to $urlWithParams")
        val start = System.currentTimeMillis()
        val retrieve = webClient
                .get()
                .uri(urlWithParams)
                .header("Content-Type", "application/json")
                .header("Authorization", "${token.tokenType} ${token.accessToken}")
                .retrieve()
                .onStatus({ it.is4xxClientError }, ::handle4xxErrors)
        val statusCode = retrieve.toBodilessEntity().block()?.statusCode
        val elapsed = System.currentTimeMillis() - start
        logger.info("Received $statusCode response after ${elapsed}ms from Post to $requestUrl")
        val response = retrieve.bodyToFlux(String::class.java)
        response.subscribe(::println)
    }
}