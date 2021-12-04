package tech.amcg.jukebox.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class JukeboxService(
        private val sessionService: SessionService
) {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

}