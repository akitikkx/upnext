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

package com.theupnextapp.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot
import com.theupnextapp.domain.ErrorResponse
import com.theupnextapp.domain.Result
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber

fun CollectionReference.getQuerySnapshotAsFlow(): Flow<QuerySnapshot?> {
    return callbackFlow {
        val listener = addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                cancel(
                    message = "Error retrieving snapshot data at $path",
                    cause = error
                )
                return@addSnapshotListener
            }
            trySend(querySnapshot)
        }
        awaitClose {
            Timber.d("The query snapshot listener is being closed at $path")
            listener.remove()
        }
    }
}

fun <T> CollectionReference.getFirestoreDataAsFlow(mapper: (QuerySnapshot?) -> T): Flow<T> {
    return getQuerySnapshotAsFlow().map { querySnapshot ->
        return@map mapper(querySnapshot)
    }
}