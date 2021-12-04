package tech.amcg.jukebox.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tech.amcg.jukebox.service.JukeboxSession

import tech.amcg.jukebox.service.SpotifyAuthService


@RestController
class SpotifyAuthController(val authService: SpotifyAuthService) {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/auth")
    fun redirectUserForAuthentication(): ResponseEntity<Unit> {
        return authService.createRedirectToStartSession()
    }

    @GetMapping("/end/auth")
    fun redirectUserToEndSession(@RequestParam session: String): ResponseEntity<Unit> {
        val jukeboxSession = JukeboxSession(session)
        return authService.createRedirectToEndSession(jukeboxSession)
    }

    @GetMapping("/callback")
    fun callback(@RequestParam(required = false) code: String?, @RequestParam state: String, @RequestParam(required = false) error: String?): ResponseEntity<Unit> {
        logger.debug("Handling callback with state=$state")
        return authService.handleAuthCallback(code, state, error)
    }

    @GetMapping("/session/end")
    fun endSessionCallback(@RequestParam(required = false) code: String?, @RequestParam state: String, @RequestParam(required = false) error: String?): ResponseEntity<Unit> {
        logger.debug("Handling callback to endSession with state=$state")
        return authService.handleEndSessionCallback(code, state, error)
    }

}