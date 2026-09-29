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

package com.theupnextapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.theupnextapp.domain.TraktUserList

@Entity(tableName = "trakt_custom_lists")
data class DatabaseCustomList(
    @PrimaryKey
    val traktId: Int,
    val slug: String?,
    val name: String,
    val description: String?,
    val itemCount: Int,
    val updatedAt: String?,
    val likes: Int,
)

fun DatabaseCustomList.asDomainModel(): TraktUserList {
    return TraktUserList(
        traktId = traktId,
        slug = slug,
        name = name,
        description = description,
        itemCount = itemCount,
        updatedAt = updatedAt,
        likes = likes,
    )
}

fun List<DatabaseCustomList>.asDomainModel(): List<TraktUserList> {
    return map { it.asDomainModel() }
}
