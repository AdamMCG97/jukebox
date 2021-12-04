package tech.amcg.jukebox.service

import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import tech.amcg.jukebox.domain.*
import tech.amcg.jukebox.domain.spotify.UserProfile
import tech.amcg.jukebox.utils.RequestUtils.handle4xxErrors
import java.lang.IllegalStateException
import java.lang.RuntimeException
import java.net.URI
import javax.naming.AuthenticationException

@Service
class SpotifyAuthService(
        val webClient: WebClient,
        val sessionService: SessionService
    ) {

    @Value("\${spotify.auth.baseUrl}")
    private lateinit var authUrl: String

    @Value("\${spotify.api.baseUrl}")
    private lateinit var apiUrl: String

    @Value("\${spotify.auth.client.id}")
    private lateinit var clientId: String

    @Value("\${spotify.auth.client.secret}")
    private lateinit var clientSecret: String

    @Value("\${spotify.auth.redirect.url}")
    private lateinit var redirectUri: String

    @Value("\${spotify.auth.endRedirect.url}")
    private lateinit var endSessionRedirectUri: String

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
        const val STRING_LENGTH = 16
        const val CODE = "code"
        val scope = listOf(SpotifyScope.USER_MODIFY_PLAYBACK_STATE, SpotifyScope.USER_READ_PLAYBACK_STATE)
        const val KILL_SESSION = "XXX"
    }
    //TODO: make this atomic
    val validStates = emptyList<String>().toMutableList()

    fun createRedirectToEndSession(sessionId: JukeboxSessionId): ResponseEntity<Unit> {
        val killSessionState = KILL_SESSION + RandomStringUtils.randomAlphanumeric(STRING_LENGTH - 3)
        sessionService.requestToEndSession(killSessionState, sessionId)
        return createRedirectResponseEntity(killSessionState, endSessionRedirectUri)
    }

    fun createRedirectToStartSession(): ResponseEntity<Unit> {
        val state = RandomStringUtils.randomAlphanumeric(STRING_LENGTH)
        return createRedirectResponseEntity(state, redirectUri)
    }

    private fun createRedirectResponseEntity(
            state: String = RandomStringUtils.randomAlphanumeric(STRING_LENGTH),
            redirect: String
    ): ResponseEntity<Unit> {
        validStates.add(state)
        val queryParams = mapOf(
                "response_type" to CODE,
                "client_id" to clientId,
                "redirect_uri" to redirect,
                "scope" to scope.map { it.permissionName }.joinToString(","),
                "state" to state)
        val redirectUrl = authUrl + "/authorize" + "?${queryParams.map { "${it.key}=${it.value}" }.joinToString("&")}"
        logger.info("Redirecting user to: $redirectUrl")
        return createResponseEntityAtUri(redirectUrl)
    }

    fun handleAuthCallback(code: String?, state: String, error: String?): ResponseEntity<Unit> {
        if(null != error) {
            if(validStates.contains(state)) {
                validStates.remove(state)
            }
            throw AuthenticationException("Error authenticating user: error=$error")
        }
        if(code == null) {
            if(validStates.contains(state)) {
                validStates.remove(state)
            }
            throw AuthenticationException("Error authenticating user: No auth code returned")
        }
        logger.info("Callback called. code=$code and state=$state")
        if(!validStates.contains(state)) {
            throw IllegalStateException("Returned state is invalid")
        }
        else {
            validStates.remove(state)
            val token = getAccessToken(code, redirectUri)
            logger.info("Token received: $token")
            val sessionHost = getUserId(token)
            val session = sessionService.createSession(token, sessionHost)
            logger.info("Started session with id=${session.value}")
            return createResponseEntityAtUri("/session/${session.value}")
        }
    }

    fun handleEndSessionCallback(code: String?, state: String, error: String?): ResponseEntity<Unit> {
        if(null == error && code != null && state.startsWith("XXX") && validStates.contains(state)) {
            validStates.remove(state)
            val token = getAccessToken(code, endSessionRedirectUri)
            val userRequestingSessionEnd = getUserId(token)
            if(sessionService.validateRequestToEndSession(state, userRequestingSessionEnd)) {
                return createResponseEntityAtUri("/finishedSession")
            }
        }
        return createResponseEntityAtUri("/home")
    }

    fun refreshToken(expiredToken: SpotifyApiToken): SpotifyApiRefreshedToken {
        val accessTokenUrl = "$authUrl/api/token"
        logger.info("Post to $accessTokenUrl")
        val start = System.currentTimeMillis()
        val bodyParams = BodyInserters
                .fromFormData("grant_type" , "refresh_token")
                .with("refresh_token", expiredToken.refreshToken)
                .with("client_id", clientId)
                .with("client_secret", clientSecret)
        val retrieve = webClient
                .post()
                .uri(accessTokenUrl)
                .body(bodyParams)
                .retrieve()
        val dtoToken = retrieve.bodyToMono(SpotifyApiRefreshedTokenDto::class.java).block()
                ?: throw RuntimeException("Token could not be formed from response=${retrieve.bodyToMono(String::class.java).block()}")
        return dtoToken.toSpotifyApiRefreshedToken()
    }

    private fun getAccessToken(authCode: String, redirect: String): SpotifyApiToken {
        val accessTokenUrl = "$authUrl/api/token"
        logger.info("Post to $accessTokenUrl")
        val start = System.currentTimeMillis()
        val bodyParams = BodyInserters
                .fromFormData("grant_type" , "authorization_code")
                .with("code", authCode)
                .with("redirect_uri", redirect)
                .with("client_id", clientId)
                .with("client_secret", clientSecret)
        val retrieve = webClient
                .post()
                .uri(accessTokenUrl)
                .body(bodyParams)
                .retrieve()
/*        val statusCode = retrieve.toBodilessEntity().block()?.statusCode
        val elapsed = System.currentTimeMillis() - start
        logger.info("Received $statusCode response after ${elapsed}ms from Post to $accessTokenUrl")*/
        val dtoToken = retrieve.bodyToMono(SpotifyApiTokenDto::class.java).block()
                ?: throw RuntimeException("Token could not be formed from response=${retrieve.bodyToMono(String::class.java).block()}")
        return dtoToken.toSpotifyApiToken()
    }

    private fun getUserId(token: SpotifyApiToken): String {
        val requestUrl = "$apiUrl/me"
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
        SpotifyApiService.logger.info("Received $statusCode response after ${elapsed}ms from Post to $requestUrl")
        val response = retrieve.bodyToMono(UserProfile::class.java).block()
        return response?.id ?: SpotifyApiService.USER_NOT_FOUND
    }

    private fun createResponseEntityAtUri(uri: String): ResponseEntity<Unit> {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI(uri)).build()
    }

}