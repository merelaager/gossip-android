package ee.merelaager.gossip.data.network

import android.content.Context
import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class SerializableCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String,
    val expiresAt: Long,
    val secure: Boolean,
    val httpOnly: Boolean
) {
    fun toOkHttpCookie(): Cookie = Cookie.Builder()
        .name(name)
        .value(value)
        .domain(domain)
        .path(path)
        .expiresAt(expiresAt)
        .apply {
            if (secure) secure()
            if (httpOnly) httpOnly()
        }
        .build()

    companion object {
        fun from(cookie: Cookie) = SerializableCookie(
            name = cookie.name,
            value = cookie.value,
            domain = cookie.domain,
            path = cookie.path,
            expiresAt = cookie.expiresAt,
            secure = cookie.secure,
            httpOnly = cookie.httpOnly
        )
    }
}

object CookieListSerializer : Serializer<List<SerializableCookie>> {
    override val defaultValue: List<SerializableCookie> = emptyList()

    override suspend fun readFrom(input: InputStream): List<SerializableCookie> {
        println("READING COOKIES")
        return try {
            Json.decodeFromString(
                deserializer = ListSerializer(SerializableCookie.serializer()),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read cookies", e)
        }
    }

    override suspend fun writeTo(t: List<SerializableCookie>, output: OutputStream) {
        println("Saving ${t.size} cookies")
        println("Saved: ${t}")
        output.write(
            Json.encodeToString(
                serializer = ListSerializer(SerializableCookie.serializer()),
                value = t
            ).encodeToByteArray()
        )
    }
}

object CookieStorage {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val cookieDataStore: DataStore<List<SerializableCookie>> by lazy {
        DataStoreFactory.create(
            serializer = CookieListSerializer,
            produceFile = { File(appContext.filesDir, "cookies.json") }
        )
    }
}

class PersistentCookieJar(
    private val cookieDataStore: DataStore<List<SerializableCookie>>
) : CookieJar {

    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadCookiesFromStorage()
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        val existing = cookieStore[host] ?: mutableListOf()

        cookies.forEach { newCookie ->
            existing.removeAll { oldCookie -> oldCookie.name == newCookie.name }
            existing.add(newCookie)
        }
        cookieStore[host] = existing
        persistCookies()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val validCookies = cookieStore.values.flatten().filter { it.expiresAt > now }
        println("Loading all cookies, count: ${validCookies.size}")
        println("Loading: ${validCookies}")
        return validCookies
    }

    private fun persistCookies() {
        CoroutineScope(Dispatchers.IO).launch {
            val allCookies = cookieStore.values.flatten()
                .filter { it.expiresAt > System.currentTimeMillis() }
                .map { SerializableCookie.from(it) }
            cookieDataStore.updateData { allCookies }
        }
    }

    suspend fun loadCookiesFromStorage() {
        val stored = cookieDataStore.data.first()
        val valid = stored.map { it.toOkHttpCookie() }
        cookieStore.clear()
        for (cookie in valid) {
            val list = cookieStore.getOrPut(cookie.domain) { mutableListOf() }
            list.add(cookie)
        }
        Log.d("PersistentCookieJar", "Loaded ${valid.size} cookies from DataStore")
        println("Loaded: ${cookieStore}")
    }
}

object ApiClient {
    private const val BASE_URL = "https://api.gossip.merelaager.ee"

    private lateinit var retrofit: Retrofit
    private lateinit var authServiceInternal: AuthService

    fun init(context: Context) {
        println("CREATING COOKIE JAR")
        val cookieJar = PersistentCookieJar(CookieStorage.cookieDataStore)
        CoroutineScope(Dispatchers.IO).launch {
            cookieJar.loadCookiesFromStorage()
            println("CREATED COOKIE JAR")
        }

        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()

        authServiceInternal = retrofit.create(AuthService::class.java)
    }

    val authService: AuthService
        get() {
            check(::authServiceInternal.isInitialized) { "ApiClient not initialized. Call ApiClient.init(context) first." }
            return authServiceInternal
        }

}
