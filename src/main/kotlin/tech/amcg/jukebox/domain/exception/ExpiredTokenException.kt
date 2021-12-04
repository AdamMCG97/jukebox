package tech.amcg.jukebox.domain.exception

import java.lang.RuntimeException

class ExpiredTokenException(message: String) : RuntimeException(message)