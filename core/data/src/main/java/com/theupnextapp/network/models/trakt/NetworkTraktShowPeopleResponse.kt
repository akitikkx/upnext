/*
 * MIT License
 *
 * Copyright (c) 2024 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.network.models.trakt

data class NetworkTraktShowPeopleResponse(
    val cast: List<NetworkTraktCast>?,
    val crew: NetworkTraktCrew?
)

data class NetworkTraktCast(
    val characters: List<String>?,
    val person: NetworkTraktPerson?,
    val episode_count: Int?,
    val series_regular: Boolean?
)

data class NetworkTraktCrew(
    val production: List<NetworkTraktCrewMember>?,
    val art: List<NetworkTraktCrewMember>?,
    val crew: List<NetworkTraktCrewMember>?,
    val costume_and_make_up: List<NetworkTraktCrewMember>?,
    val directing: List<NetworkTraktCrewMember>?,
    val writing: List<NetworkTraktCrewMember>?,
    val sound: List<NetworkTraktCrewMember>?,
    val camera: List<NetworkTraktCrewMember>?
)

data class NetworkTraktCrewMember(
    val jobs: List<String>?,
    val person: NetworkTraktPerson?
)

data class NetworkTraktPerson(
    val name: String?,
    val ids: NetworkTraktPersonIds?
)

data class NetworkTraktPersonIds(
    val trakt: Int?,
    val slug: String?,
    val imdb: String?,
    val tmdb: Int?,
    val tvrage: Int?
)
