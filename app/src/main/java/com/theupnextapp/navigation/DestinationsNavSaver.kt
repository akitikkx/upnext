/*
 * MIT License
 *
 * Copyright (c) 2022 Ahmed Tikiwa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.theupnextapp.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.serialization.json.Json
import timber.log.Timber

val DestinationsNavSaver: Saver<SnapshotStateList<Any>, ArrayList<String>> =
    object : Saver<SnapshotStateList<Any>, ArrayList<String>> {
        override fun SaverScope.save(value: SnapshotStateList<Any>): ArrayList<String> {
            val list = ArrayList<String>(value.size)
            for (item in value) {
                if (item is Destinations) {
                    list.add(Json.encodeToString(Destinations.serializer(), item))
                }
            }
            return list
        }

        override fun restore(value: ArrayList<String>): SnapshotStateList<Any> {
            val stateList = mutableStateListOf<Any>()
            for (jsonStr in value) {
                try {
                    val destination = Json.decodeFromString(Destinations.serializer(), jsonStr)
                    stateList.add(destination)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to restore destination from JSON: $jsonStr")
                }
            }
            if (stateList.isEmpty()) {
                stateList.add(Destinations.EmptyDetail)
            }
            return stateList
        }
    }
