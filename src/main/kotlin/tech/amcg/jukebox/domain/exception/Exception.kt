package tech.amcg.jukebox.domain.exception

import java.lang.Exception
import java.lang.RuntimeException

class ExpiredTokenException(message: String) : RuntimeException(message)

class UserNotFoundException(message: String) : RuntimeException(message)

class Http4xxException(message: String) : RuntimeException(message)

class PlaybackStateNotFoundException(message: String) : RuntimeException(message)