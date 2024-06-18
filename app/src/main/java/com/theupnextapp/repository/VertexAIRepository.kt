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

//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.gson.Gson
//import com.theupnextapp.domain.ErrorResponse
//import com.theupnextapp.domain.Result
//import com.theupnextapp.domain.TriviaQuestion
//import com.theupnextapp.network.models.gemini.GeminiMultimodalRequest
//import com.theupnextapp.network.models.gemini.NetworkGeminiTriviaResponse
//import com.theupnextapp.network.models.gemini.toTriviaQuestions
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOn
//import kotlinx.coroutines.tasks.await
//import org.json.JSONObject
//
//class VertexAIRepository(
//    private val firebaseFirestore: FirebaseFirestore
//) {
//
//    suspend fun getTrivia(instruction: String): Flow<Result<List<TriviaQuestion>?>> {
//        // used convert the json response from Firebase into a data object
//        val gson = Gson()
//
//        val document = firebaseFirestore.collection(COLLECTION_PATH)
//            .document(DOCUMENT_PATH)
//
//        // Execute the GenAI query by uploading a document with the instruction
//        document.set(GeminiMultimodalRequest(instruction)).await()
//
//        return firebaseFirestore
//            .collection(COLLECTION_PATH)
//            .getFirestoreDataAsFlow { querySnapshot ->
//                when (querySnapshot) {
//                    is Result.Success -> {
//                        val response = querySnapshot.data?.documents?.lastOrNull()
//                            ?.toObject(GeminiMultimodalRequest::class.java)
//                        val output = response?.output?.filterNot { c -> "`".contains(c) }
//
//                        val jsonObj = output?.let { JSONObject(it.replace("json\n", "")) }
//
//                        try {
//                            val networkGeminiTriviaResponse = gson.fromJson(
//                                jsonObj?.toString(),
//                                NetworkGeminiTriviaResponse::class.java
//                            )
//                            Result.Success(networkGeminiTriviaResponse.triviaQuiz.toTriviaQuestions())
//                        } catch (e: Exception) {
//                            Result.GenericError(
//                                error = ErrorResponse(
//                                    message =
//                                    "Error: ${e.message} | Stacktrace: ${e.stackTraceToString()}"
//                                )
//                            )
//                        }
//                    }
//
//                    is Result.GenericError -> Result.GenericError(error = querySnapshot.error)
//                    is Result.Loading -> Result.Loading(true)
//                    is Result.NetworkError -> Result.NetworkError
//                }
//            }.flowOn(Dispatchers.IO)
//    }
//
//    companion object {
//        const val COLLECTION_PATH = "generate"
//        const val DOCUMENT_PATH = "instruction"
//    }
//
//}