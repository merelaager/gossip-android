package ee.merelaager.gossip.data.model

sealed class JSendResponse<out S, out F> {
    data class Success<S>(val data: S) : JSendResponse<S, Nothing>()
    data class Fail<F>(val data: F) : JSendResponse<Nothing, F>()
    data class Error(val message: String) : JSendResponse<Nothing, Nothing>()
}
