package tech.amcg.jukebox.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import tech.amcg.jukebox.domain.ErrorrResponseBody

@Service
class SpotifyApiService(
        private val webClient: WebClient,
        private val sessionService: SessionService
) {

    @Value("\${spotify.api.baseUrl}")
    lateinit var apiUrl: String

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    fun getPlaybackState(session: JukeboxSession) {
        val token = sessionService.findSession(session)
                ?: throw NoSuchSessionException("Session ($session) does not exist ")
        if(token.hasExpired()) {
            //TODO: Add call to refresh token here when capability developed
            TODO()
        }
        val requestUrl = "$apiUrl/me/player"
        logger.info("Post to $requestUrl with sessionId=$session")
        val start = System.currentTimeMillis()
        val retrieve = webClient
                .get()
                .uri(requestUrl)
                .header("Content-Type", "application/json")
                .header("Authorization", "${token.tokenType} ${token.accessToken}")
                .retrieve()
                .onStatus({ it.is4xxClientError }, ::handle4xxErrors)
        val response = retrieve.bodyToMono(String::class.java).block()
        println(response)
        val statusCode = retrieve.toBodilessEntity().block()?.statusCode
        val elapsed = System.currentTimeMillis() - start
        logger.info("Received $statusCode response after ${elapsed}ms from Post to $requestUrl")
    }

    fun addTrackToQueue(trackId: String, session: JukeboxSession) {
        val token = sessionService.findSession(session)
                ?: throw NoSuchSessionException("Session ($session) does not exist ")
        if(token.hasExpired()) {
            //TODO: Add call to refresh token here when capability developed
            TODO()
        }
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
    }

    private fun handle4xxErrors(response: ClientResponse): Mono<Throwable> {
        return response.bodyToMono(ErrorrResponseBody::class.java).flatMap {
            logger.error("Error: $it")
            Mono.error<Exception>(Exception())
        }
    }
}