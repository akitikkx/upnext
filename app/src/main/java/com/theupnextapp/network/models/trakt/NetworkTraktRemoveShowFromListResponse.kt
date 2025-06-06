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

data class NotFoundShowItemForRemoval(
    val ids: NetworkTraktRemoveShowFromListRequestShowIds // Use the ID structure from your request
)

data class NetworkTraktRemoveShowFromListResponse(
    val deleted: NetworkTraktRemoveShowFromListResponseDeleted,
    val not_found: NetworkTraktRemoveShowFromListResponseNotFound
)

data class NetworkTraktRemoveShowFromListResponseDeleted(
    val shows: Int,
    val movies: Int? = null,
    val seasons: Int? = null,
    val episodes: Int? = null,
    val people: Int? = null
)

data class NetworkTraktRemoveShowFromListResponseNotFound(
    val shows: List<NotFoundShowItemForRemoval>?,
    val movies: List<NotFoundShowItemForRemoval>? = null,
    val seasons: List<NotFoundShowItemForRemoval>? = null,
    val episodes: List<NotFoundShowItemForRemoval>? = null,
    val people: List<NotFoundShowItemForRemoval>? = null,
    val ids: List<Map<String, Any>>? = null
)
