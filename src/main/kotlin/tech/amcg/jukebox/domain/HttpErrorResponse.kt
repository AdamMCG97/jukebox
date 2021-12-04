package tech.amcg.jukebox.domain

data class HttpErrorResponse(
        val error: ErrorrResponseBody
)

data class ErrorrResponseBody(
        val status: String?,
        val message: String?,
        val reason: String?
)
