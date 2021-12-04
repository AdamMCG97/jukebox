package tech.amcg.jukebox.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class JukeboxController {

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }

    @GetMapping("/session/{id}")
    fun sessionHomePage(@PathVariable("id") sessionId: String): String {
        return """
            Your jukebox session has started!
            
            Share the link below with anyone you want to join the session.
            
            Link: http://localhost:8080/session/$sessionId
        """
    }

/*    @GetMapping("/session/{id}")
    fun verifySessionDetails()

    fun endSession()*/
}