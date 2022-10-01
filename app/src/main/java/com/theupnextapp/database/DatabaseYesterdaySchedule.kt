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
import com.theupnextapp.domain.ScheduleShow

@Entity(tableName = "schedule_yesterday")
data class DatabaseYesterdaySchedule(
    @PrimaryKey
    val id: Int,
    val image: String?,
    val mediumImage: String?,
    val language: String?,
    val name: String?,
    val officialSite: String?,
    val premiered: String?,
    val runtime: String?,
    val status: String?,
    val summary: String?,
    val type: String?,
    val updated: String?,
    val url: String?
)

fun List<DatabaseYesterdaySchedule>.asDomainModel(): List<ScheduleShow> {
    return map {
        ScheduleShow(
            id = it.id,
            originalImage = it.image,
            mediumImage = it.mediumImage,
            language = it.language,
            name = it.name,
            officialSite = it.officialSite,
            premiered = it.premiered,
            runtime = it.runtime,
            status = it.status,
            summary = it.summary,
            type = it.type,
            updated = it.updated,
            url = it.url
        )
    }
}
