package ee.merelaager.gossip.data.network

sealed class NetworkResult<out SuccessT, out FailT> {
    data class Success<SuccessT>(val data: SuccessT) : NetworkResult<SuccessT, Nothing>()
    data class Fail<FailT>(val data: FailT) : NetworkResult<Nothing, FailT>()
    data class Error(val message: String, val code: Int?) : NetworkResult<Nothing, Nothing>()
    data class Exception(val exception: Throwable) : NetworkResult<Nothing, Nothing>()
}