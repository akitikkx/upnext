package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktUserSettings

data class NetworkTraktUserSettingsResponse(
    val account: NetworkTraktUserSettingsResponseAccount?,
    val connections: NetworkTraktUserSettingsResponseConnections?,
    val sharing_text: NetworkTraktUserSettingsResponseSharingText?,
    val user: NetworkTraktUserSettingsResponseUser?
)

data class NetworkTraktUserSettingsResponseAccount(
    val cover_image: String?,
    val date_format: String?,
    val time_24hr: Boolean?,
    val timezone: String?
)

data class NetworkTraktUserSettingsResponseConnections(
    val apple: Boolean?,
    val facebook: Boolean?,
    val google: Boolean?,
    val medium: Boolean?,
    val slack: Boolean?,
    val tumblr: Boolean?,
    val twitter: Boolean?
)

data class NetworkTraktUserSettingsResponseSharingText(
    val rated: String?,
    val watched: String?,
    val watching: String?
)

data class NetworkTraktUserSettingsResponseAvatar(
    val full: String?
)

data class NetworkTraktUserSettingsResponseIds(
    val slug: String?,
    val uuid: String?
)

data class NetworkTraktUserSettingsResponseImages(
    val avatar: NetworkTraktUserSettingsResponseAvatar?
)

data class NetworkTraktUserSettingsResponseUser(
    val about: String?,
    val age: Int?,
    val gender: String?,
    val ids: NetworkTraktUserSettingsResponseIds?,
    val images: NetworkTraktUserSettingsResponseImages?,
    val joined_at: String?,
    val location: String?,
    val name: String?,
    val `private`: Boolean?,
    val username: String?,
    val vip: Boolean?,
    val vip_ep: Boolean?,
    val vip_og: Boolean?,
    val vip_years: Int?
)

fun NetworkTraktUserSettingsResponse.asDomainModel(): TraktUserSettings {
    return TraktUserSettings(
        slug = user?.ids?.slug
    )
}