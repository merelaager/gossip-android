package ee.merelaager.gossip.data.network

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class SimpleCookieJar : CookieJar {
    private val cookieStore: MutableMap<String, MutableList<Cookie>> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val existingCookies = cookieStore[url.host] ?: mutableListOf()
        existingCookies.removeAll { oldCookie ->
            cookies.any { it.name == oldCookie.name }
        }
        existingCookies.addAll(cookies)
        cookieStore[url.host] = existingCookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = cookieStore[url.host].orEmpty()
        val validCookies = cookies.filter { !it.hasExpired() }
        cookieStore[url.host] = validCookies.toMutableList()
        return validCookies
    }

    private fun Cookie.hasExpired(): Boolean {
        return this.expiresAt < System.currentTimeMillis()
    }
}

object ApiClient {
    private const val BASE_URL = "https://api.gossip.merelaager.ee"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .cookieJar(SimpleCookieJar())
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .client(client)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authService: AuthService = retrofit.create(AuthService::class.java)
}
