/*
package tech.amcg.jukebox.domain.spotify

data class PlaybackState(
        val device: Device,
        val repeatState: String,
        val shuffleState: String,
        val context: Context,
        val timestamp: Int,
        val progressMs: Int,
        val isPlaying: Boolean,
        val item: Item,
        val currentPlayingType: String,
        val action: Action
)

data class PlaybackStateDto(
        val device: DeviceDto,
        val repeat_state: String,
        val shuffle_state: String,
        val context: ContextDto,
        val timestamp: Int,
        val progress_ms: Int,
        val is_playing: Boolean,
        val item: Item,
        val current_playing_type: String,
        val action: Action
) {
    fun toPlaybackState(): PlaybackState {
        return PlaybackState(
                device.toDevice(),
                repeat_state,
                shuffle_state,
                context.toContext(),
                timestamp,
                progress_ms,
                is_playing,
                item.toItem(),
                current_playing_type,
                action.toAction()

        )
    }
}

data class Device(
        val id: String,
        val isActive: Boolean,
        val isPrivateSession: Boolean,
        val isRestricted: Boolean,
        val name: String,
        val type: String,
        val volumePercent: Int
)

data class DeviceDto(
        val id: String,
        val is_active: Boolean,
        val is_private_session: Boolean,
        val is_restricted: Boolean,
        val name: String,
        val type: String,
        val volume_percent: Int
) {
    fun toDevice(): Device {
        return Device(
                id,
                is_active,
                is_private_session,
                is_restricted,
                name,
                type,
                volume_percent
        )
    }
}

data class Context(
        val type: String,
        val href: String,
        val externalUrls: Map<String, String>,
        val uri: String
)

data class ContextDto(
        val type: String,
        val href: String,
        val external_urls: Map<String, String>,
        val uri: String
) {
    fun toContext(): Context {
        return Context(
                type,
                href,
                external_urls,
                uri
        )
    }
}

data class item(
        val album: Album,
        val artists: List<Artist>,
        val availableMarkets: List<String>,
        val discNumber: Int,
        val durationMs: Int,
        val explicit: Boolean,
        val externalIds: ExternalId,
        val externalUrls: Map<String, String>,
        val href: String,
        val id: String,
        val isPlayable: Boolean,
        val linkedFrom: LinkedFrom,
        val restrictions:

)*/
