package ee.merelaager.gossip.data.network

import ee.merelaager.gossip.data.model.JSendResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import retrofit2.Response

inline fun <reified S, reified F> parseJSendResponse(body: ResponseBody?): JSendResponse<S, F>? {
    if (body == null) return null

    val jsonStr = body.string()
    if (jsonStr.isEmpty()) return null

    val json = Json.parseToJsonElement(jsonStr).jsonObject

    val statusElement = json["status"]?.jsonPrimitive
    val status = statusElement?.content ?: return null

    return when (status) {
        "success" -> {
            val dataElement = json["data"] ?: return null
            val data = Json.decodeFromJsonElement<S>(dataElement)
            JSendResponse.Success(data)
        }

        "fail" -> {
            val dataElement = json["data"] ?: return null
            val data = Json.decodeFromJsonElement<F>(dataElement)
            JSendResponse.Fail(data)
        }

        "error" -> {
            val message = json["message"]?.jsonPrimitive?.content ?: "Unknown error"
            JSendResponse.Error(message)
        }

        else -> null
    }
}

suspend inline fun <reified S, reified F> executeJSendCall(
    crossinline call: suspend () -> Response<ResponseBody>
): JSendResponse<S, F>? {
    val response = call()
    if (!response.isSuccessful) {
        val errorResponse = parseJSendResponse<S, F>(response.errorBody())
        if (errorResponse != null) return errorResponse

        return JSendResponse.Error("HTTP error ${response.code()}")
    }

    val body = response.body()
    return parseJSendResponse<S, F>(body)
}

//suspend fun <SuccessT, FailT> apiCall(
//    call: suspend () -> Response<String>,
//    gson: Gson = Gson()
//): NetworkResult<SuccessT?, FailT> {
//    return try {
//        val response = call()
//
//        if (response.isSuccessful) {
//            val body = response.body()
//            if (!body.isNullOrBlank()) {
//                val jsend = gson.fromJson(body, JSendWrapper::class.java)
//                when (jsend.status) {
//                    "success" -> {
//                        val data = gson.fromJson<SuccessT>(
//                            gson.toJson(jsend.data),
//                            successType
//                        )
//                        NetworkResult.Success(data)
//                    }
//
//                    else -> {
//                        val errorMessage = jsend.message ?: "Unexpected status: ${jsend.status}"
//                        NetworkResult.Error(errorMessage, response.code())
//                    }
//                }
//            } else {
//                NetworkResult.Success(null)
//            }
//        } else {
//            val errorBody = response.errorBody()?.string()
//            if (!errorBody.isNullOrBlank()) {
//                val jsend = gson.fromJson(errorBody, JSendWrapper::class.java)
//                when (jsend.status) {
//                    "fail" -> {
//                        val data = gson.fromJson<FailT>(
//                            gson.toJson(jsend.data),
//                            failType
//                        )
//                        NetworkResult.Fail(data)
//                    }
//
//                    "error" -> {
//                        NetworkResult.Error(jsend.message ?: "Unknown error", jsend.code)
//                    }
//
//                    else -> {
//                        NetworkResult.Error(
//                            "Unknown error status '${jsend.status}'",
//                            response.code()
//                        )
//                    }
//                }
//            } else {
//                NetworkResult.Error("Unknown error", response.code())
//            }
//        }
//    } catch (ex: Exception) {
//        NetworkResult.Exception(ex)
//    }
//}
