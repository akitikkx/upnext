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

import com.google.firebase.firestore.FirebaseFirestore
import com.theupnextapp.domain.Result
import com.theupnextapp.network.models.gemini.NetworkGeminiTriviaRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class VertexAIRepository (
    private val firebaseFirestore: FirebaseFirestore
) {

    suspend fun getTrivia(): Flow<Result<NetworkGeminiTriviaRequest?>> {
        return flow {
            val document = firebaseFirestore.collection("generate")
                .document("instruction")

            document.set(NetworkGeminiTriviaRequest("10 random TV shows")).await()
            emit(Result.Loading(true))
            delay(20000L)

            val collections = firebaseFirestore.collection("generate").get().await()
            val response = collections.documents.firstOrNull()
                ?.toObject(NetworkGeminiTriviaRequest::class.java)

            emit(Result.Success(response))

        }.flowOn(Dispatchers.IO)
    }
}