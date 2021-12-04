package tech.amcg.jukebox.domain

import java.time.Instant

data class SpotifyApiToken(
        val accessToken: String,
        val tokenType: String,
        val scope: String?,
        private val expiresIn: Long,
        val refreshToken: String
) {

    private val createdTimestamp  = Instant.now()

    fun hasExpired(): Boolean {
        return (Instant.now().epochSecond - createdTimestamp.epochSecond) > expiresIn
    }

    fun timeToExpire(): Long {
        return (Instant.now().epochSecond - createdTimestamp.epochSecond) - expiresIn
    }

    fun updateWithRefreshedToken(newToken: SpotifyApiRefreshedToken): SpotifyApiToken {
        return this.copy(
                accessToken = newToken.accessToken,
                tokenType = newToken.tokenType,
                scope = newToken.scope,
                expiresIn = newToken.expiresIn
        )
    }

}

data class SpotifyApiRefreshedToken(
        val accessToken: String,
        val tokenType: String,
        val scope: String?,
        val expiresIn: Long
)

data class SpotifyApiRefreshedTokenDto(
        val access_token: String,
        val token_type: String,
        val scope: String?,
        private val expires_in: Long
) {
    fun toSpotifyApiRefreshedToken(): SpotifyApiRefreshedToken {
        return SpotifyApiRefreshedToken(
                access_token,
                token_type,
                scope,
                expires_in
        )
    }
}

data class SpotifyApiTokenDto(
        val access_token: String,
        val token_type: String,
        val scope: String?,
        private val expires_in: Long,
        val refresh_token: String
) {
    fun toSpotifyApiToken(): SpotifyApiToken {
        return SpotifyApiToken(
                access_token,
                token_type,
                scope,
                expires_in,
                refresh_token
        )
    }
}