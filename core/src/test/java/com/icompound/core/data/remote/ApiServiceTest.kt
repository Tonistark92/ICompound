package com.icompound.core.data.remote


import com.icompound.core.data.repo.remote.ApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

import org.junit.After
import org.junit.Before
import org.junit.Test

import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import kotlin.test.assertFailsWith


class ApiServiceTest {

    private lateinit var server: MockWebServer
    private lateinit var api: ApiService


    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    val converterFactory =
        json.asConverterFactory("application/json".toMediaType())

    @Before
    fun setUp() {
        System.setProperty(
            "okhttp.platform",
            "jdk"
        )
        server = MockWebServer().also { it.start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun ok(body: String = "{}") = MockResponse().setResponseCode(200).setBody(body)

    @Test
    fun `get sends path, headers and query map`() = runTest {
        server.enqueue(ok("""{"a":1}"""))

        val result = api.get(
            pathUrl = "v1/users/5",
            headerMap = mapOf("X-Test" to "yes", "X-Num" to 7),
            queryParams = mapOf("page" to 2, "q" to "abc"),
        )

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v1/users/5?page=2&q=abc", req.path)   // encoded = true keeps the slashes
        assertEquals("yes", req.getHeader("X-Test"))
        assertEquals("7", req.getHeader("X-Num"))
        assertEquals("""{"a":1}""", result.string())
    }

    @Test
    fun `post serializes body as json`() = runTest {
        server.enqueue(ok())
        api.post("v1/login", mapOf("phone" to "010"), emptyMap(), emptyMap())

        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/v1/login", req.path)
        assertEquals("""{"phone":"010"}""", req.body.readUtf8())
        assertTrue(req.getHeader("Content-Type")!!.startsWith("application/json"))
    }

    @Test
    fun `put and patch use the right verbs`() = runTest {
        server.enqueue(ok()); server.enqueue(ok())
        api.put("v1/x", mapOf("a" to 1), emptyMap(), emptyMap())
        api.patch("v1/x", mapOf("a" to 1), emptyMap(), emptyMap())
        assertEquals("PUT", server.takeRequest().method)
        assertEquals("PATCH", server.takeRequest().method)
    }

    @Test
    fun `postFormData sends urlencoded fields`() = runTest {
        server.enqueue(ok())
        api.postFormData("v1/token", mapOf("key" to "a b", "n" to 1))

        val req = server.takeRequest()
        assertEquals("application/x-www-form-urlencoded", req.getHeader("Content-Type"))
        val body = req.body.readUtf8()
        assertTrue(body.contains("key=a%20b") || body.contains("key=a+b"))
        assertTrue(body.contains("n=1"))
    }

    @Test
    fun `getWithHeaderResponse exposes response headers`() = runTest {
        server.enqueue(ok().addHeader("X-Token", "abc"))
        val res = api.getWithHeaderResponse("v1/me", emptyMap(), emptyMap())
        assertEquals("abc", res.headers()["X-Token"])
    }

    @Test
    fun `postWithImagesFile sends multipart with files and data part`() = runTest {
        server.enqueue(ok())
        val file = MultipartBody.Part.createFormData(
            "photos", "a.jpg", "img".toRequestBody("image/jpeg".toMediaType())
        )
        api.postWithImagesFile(
            "v1/tickets",
            listOf(file),
            mapOf("title" to "t"),
            emptyMap(),
            emptyMap()
        )

        val req = server.takeRequest()
        assertTrue(req.getHeader("Content-Type")!!.startsWith("multipart/form-data"))
        val body = req.body.readUtf8()
        assertTrue(body.contains("name=\"photos\"; filename=\"a.jpg\""))
        assertTrue(body.contains("name=\"data\""))
    }

    @Test
    fun `postMultipart uses full url from @Url`() = runTest {
        server.enqueue(ok())
        api.postMultipart(
            pathUrl = "v1/upload",
            headerMap = mapOf("X-A" to "1"),
            queryParams = mapOf("k" to "v"),
            body = "x".toRequestBody("text/plain".toMediaType()),
            files = emptyList(),
        )
        val req = server.takeRequest()
        assertEquals("/v1/upload?k=v", req.path)
        assertEquals("1", req.getHeader("X-A"))
    }

    @Test
    fun `non-2xx throws HttpException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"nope"}"""))
        val e = assertFailsWith<HttpException> {
            api.get("v1/missing", emptyMap(), emptyMap())
        }
        assertEquals(404, e.code())
    }

    @Test
    fun `delete with body fails - documents the bug until it is fixed`() = runTest {
        val e = assertFailsWith<IllegalArgumentException> {
            api.delete("v1/x", mapOf("a" to 1), emptyMap(), emptyMap())
        }
        assertTrue(e.message!!.contains("Non-body HTTP method cannot contain @Body"))
    }
}