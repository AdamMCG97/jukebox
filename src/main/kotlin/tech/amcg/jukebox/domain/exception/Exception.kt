package tech.amcg.jukebox.domain.exception

import java.lang.RuntimeException

class Http4xxException(message: String) : RuntimeException(message)

class PlaybackStateNotFoundException(message: String) : RuntimeException(message)

class NoSuchSessionException(message: String) : RuntimeException(message)

class NoSuchActiveSessionException(message: String): RuntimeException(message)