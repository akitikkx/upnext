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

package com.theupnextapp.network

import com.theupnextapp.database.DatabaseTodaySchedule
import com.theupnextapp.database.DatabaseTomorrowSchedule
import com.theupnextapp.database.DatabaseYesterdaySchedule
import com.theupnextapp.network.models.tvmaze.NetworkTodayScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkTomorrowScheduleResponse
import com.theupnextapp.network.models.tvmaze.NetworkYesterdayScheduleResponse

fun NetworkYesterdayScheduleResponse.asDatabaseModel(): DatabaseYesterdaySchedule {
    return DatabaseYesterdaySchedule(
        id = this.id,
        showId = this.show.id,
        image = this.show.image?.original,
        mediumImage = this.show.image?.medium,
        language = this.show.language,
        name = this.show.name,
        officialSite = this.show.officialSite,
        premiered = this.show.premiered,
        runtime = this.show.runtime.toString(),
        status = this.show.status,
        summary = this.summary,
        type = this.show.type,
        updated = this.show.updated.toString(),
        url = this.url,
    )
}

fun NetworkTodayScheduleResponse.asDatabaseModel(): DatabaseTodaySchedule {
    return DatabaseTodaySchedule(
        id = this.id,
        showId = this.show.id,
        image = this.show.image?.original,
        mediumImage = this.show.image?.medium,
        language = this.show.language,
        name = this.show.name,
        officialSite = this.show.officialSite,
        premiered = this.show.premiered,
        runtime = this.show.runtime.toString(),
        status = this.show.status,
        summary = this.summary,
        type = this.show.type,
        updated = this.show.updated.toString(),
        url = this.url,
    )
}

fun NetworkTomorrowScheduleResponse.asDatabaseModel(): DatabaseTomorrowSchedule {
    return DatabaseTomorrowSchedule(
        id = this.id,
        showId = this.show.id,
        image = this.show.image?.original,
        mediumImage = this.show.image?.medium,
        language = this.show.language,
        name = this.show.name,
        officialSite = this.show.officialSite,
        premiered = this.show.premiered,
        runtime = this.show.runtime.toString(),
        status = this.show.status,
        summary = this.summary,
        type = this.show.type,
        updated = this.show.updated.toString(),
        url = this.url,
    )
}
