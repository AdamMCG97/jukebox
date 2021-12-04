package tech.amcg.jukebox.domain.spotify

data class SearchResult(
        val href: String,
        val items: List<Any>,
        val limit: Int,
        val next: String,
        val offset: Int,
        val previous: String,
        val total: Int
)