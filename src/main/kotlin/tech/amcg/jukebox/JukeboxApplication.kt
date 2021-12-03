package tech.amcg.jukebox

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JukeboxApplication

fun main(args: Array<String>) {
	runApplication<JukeboxApplication>(*args)
}
