package tech.amcg.jukebox.domain.spotify

data class UserProfile(
        val display_name: String,
        val external_urls: Map<String, String>,
        val followers: Map<String, String>,
        val href: String,
        val id: String,
        val images: List<Map<String, String>>,
        val type: String,
        val uri: String
)