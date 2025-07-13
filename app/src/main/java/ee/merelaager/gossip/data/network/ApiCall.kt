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

    val json = Json {
        ignoreUnknownKeys = true
    }

    val jsObj = json.parseToJsonElement(jsonStr).jsonObject
    val statusElement = jsObj["status"]?.jsonPrimitive
    val status = statusElement?.content ?: return null

    return when (status) {
        "success" -> {
            val dataElement = jsObj["data"] ?: return null
            val data = json.decodeFromJsonElement<S>(dataElement)
            JSendResponse.Success(data)
        }

        "fail" -> {
            val dataElement = jsObj["data"] ?: return null
            val data = json.decodeFromJsonElement<F>(dataElement)
            JSendResponse.Fail(data)
        }

        "error" -> {
            val message = jsObj["message"]?.jsonPrimitive?.content ?: "Unknown error"
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
