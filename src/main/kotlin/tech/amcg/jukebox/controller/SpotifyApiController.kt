package tech.amcg.jukebox.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.amcg.jukebox.service.JukeboxSession
import tech.amcg.jukebox.service.SpotifyApiService

@RestController
class SpotifyApiController(private val apiService: SpotifyApiService) {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/queueTrack")
    fun addTrackToQueue(@RequestParam trackId: String, @RequestParam sessionId: String) {
        //TODO: how to pass in which session/login is requesting the track?
        logger.info("Received request to add trackId=$trackId to queue")
        apiService.addTrackToQueue(trackId, JukeboxSession(sessionId))
    }

    @GetMapping("/playbackState")
    fun getPlaybackState(@RequestParam sessionId: String) {
        //TODO: how to pass in which session/login is requesting the track?
        logger.info("Received request to find playback state for session=$sessionId")
        apiService.getPlaybackState(JukeboxSession(sessionId))
    }

}