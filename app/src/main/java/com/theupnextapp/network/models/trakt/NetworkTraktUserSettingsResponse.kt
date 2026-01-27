/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.network.models.trakt

import com.theupnextapp.domain.TraktUserSettings

data class NetworkTraktUserSettingsResponse(
    val account: NetworkTraktUserSettingsResponseAccount?,
    val connections: NetworkTraktUserSettingsResponseConnections?,
    val sharing_text: NetworkTraktUserSettingsResponseSharingText?,
    val user: NetworkTraktUserSettingsResponseUser?,
)

data class NetworkTraktUserSettingsResponseAccount(
    val cover_image: String?,
    val date_format: String?,
    val time_24hr: Boolean?,
    val timezone: String?,
)

data class NetworkTraktUserSettingsResponseConnections(
    val apple: Boolean?,
    val facebook: Boolean?,
    val google: Boolean?,
    val medium: Boolean?,
    val slack: Boolean?,
    val tumblr: Boolean?,
    val twitter: Boolean?,
)

data class NetworkTraktUserSettingsResponseSharingText(
    val rated: String?,
    val watched: String?,
    val watching: String?,
)

data class NetworkTraktUserSettingsResponseAvatar(
    val full: String?,
)

data class NetworkTraktUserSettingsResponseIds(
    val slug: String?,
    val uuid: String?,
)

data class NetworkTraktUserSettingsResponseImages(
    val avatar: NetworkTraktUserSettingsResponseAvatar?,
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
    val vip_years: Int?,
)

fun NetworkTraktUserSettingsResponse.asDomainModel(): TraktUserSettings {
    return TraktUserSettings(
        slug = user?.ids?.slug,
    )
}
