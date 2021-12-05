package tech.amcg.jukebox.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.amcg.jukebox.domain.JukeboxSessionId
import tech.amcg.jukebox.service.SessionService
import tech.amcg.jukebox.service.SpotifyApiService
import tech.amcg.jukebox.service.SpotifyAuthService

@RestController()
class SessionController(private val apiService: SpotifyApiService, private val authService: SpotifyAuthService, private val sessionService: SessionService) {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/session/{session}")
    fun sessionHomePage(@PathVariable("session") sessionId: String): String {
        val session = sessionService.findSession(JukeboxSessionId(sessionId))
        return """
            Your jukebox session has started!
            
            Share the link below with anyone you want to join the session.
            
            Link: http://localhost:8080/session/$sessionId
            
            Session=$session
        """
    }

    @GetMapping("/session/{session}/queueTrack")
    fun addTrackToQueue(@PathVariable("session") sessionId: String, @RequestParam trackId: String): Boolean {
        //TODO: how to pass in which session/login is requesting the track?
        logger.info("Received request to add trackId=$trackId to queue")
        return apiService.addTrackToQueue(trackId, JukeboxSessionId(sessionId))
    }

    @GetMapping("/session/{session}/playbackState")
    fun getPlaybackState(@PathVariable("session") sessionId: String): String {
        //TODO: how to pass in which session/login is requesting the track?
        logger.info("Received request to find playback state for session=$sessionId")
        return apiService.getPlaybackState(JukeboxSessionId(sessionId)).name
    }

    @GetMapping("/session/{session}/search")
    fun search(@PathVariable("session") sessionId: String, @RequestParam query: String) {
        //TODO: how to pass in which session/login is requesting the track?
        logger.info("Received search request with query=$query")
        apiService.search(JukeboxSessionId(sessionId), query)
    }

    @GetMapping("/session/{session}/end")
    fun endSession(@PathVariable("session") sessionId: String): ResponseEntity<Unit> {
        //TODO: how to pass in which session/login is requesting the track?
        logger.info("Received request to end session=$sessionId")
        return authService.createRedirectToEndSession(JukeboxSessionId(sessionId))
    }

}