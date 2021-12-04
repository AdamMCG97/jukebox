package tech.amcg.jukebox.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ApplicationConfiguration {

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }

    @Bean
    fun objectMapper(): ObjectMapper {
        return ObjectMapper()
    }

}