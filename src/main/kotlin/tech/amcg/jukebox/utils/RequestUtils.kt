package tech.amcg.jukebox.utils

import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import tech.amcg.jukebox.domain.HttpErrorResponse
import tech.amcg.jukebox.domain.exception.Http4xxException
import tech.amcg.jukebox.service.SpotifyApiService

object RequestUtils {

    fun paramsMapToParamString(map: Map<String, Any>): String {
        val mapAsParams = map.map { "${it.key}=${it.value}" }.joinToString("&")
        return "?$mapAsParams"
    }

    fun handle4xxErrors(response: ClientResponse): Mono<Throwable> {
        return response.bodyToMono(HttpErrorResponse::class.java).flatMap {
            SpotifyApiService.logger.error("Error: $it")
            Mono.error<Http4xxException>(Http4xxException("Request returned status: ${it.error.status} with message=${it.error.message} and reason=${it.error.reason}"))
        }
    }
}