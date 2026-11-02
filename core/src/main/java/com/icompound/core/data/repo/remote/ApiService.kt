package com.icompound.core.data.repo.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface ApiService {

    @DELETE("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun delete(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): ResponseBody

    @POST("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun post(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): ResponseBody


    @FormUrlEncoded
    @POST("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun postFormData(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @FieldMap fields: Map<String, @JvmSuppressWildcards Any>,
        @HeaderMap headerMap: Map<String, Any> = emptyMap(),
        @QueryMap queryParams: Map<String, Any> = emptyMap()
    ): ResponseBody

    @PUT("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun put(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): ResponseBody

    @PATCH("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun patch(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): ResponseBody


    @POST("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun postWithHeaderResponse(
        @Path(value = "pathUrl", encoded = true) pathUrl: String, @Body body: Any,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): Response<ResponseBody>

    @GET("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun get(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): ResponseBody

    @GET("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun getWithHeaderResponse(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @HeaderMap headerMap: Map<String, Any>, @QueryMap queryParams: Map<String, Any>
    ): Response<ResponseBody>

    @Multipart
    @POST("{pathUrl}")
    @JvmSuppressWildcards
    suspend fun postWithImagesFile(
        @Path(value = "pathUrl", encoded = true) pathUrl: String,
        @Part files: List<MultipartBody.Part>,
        @Part("data") requestBody: Any,
        @HeaderMap headerMap: Map<String, Any>,
        @QueryMap queryParams: Map<String, Any>
    ): Response<ResponseBody>

    @Multipart
    @POST
    suspend fun postMultipart(
        @Url pathUrl: String,
        @HeaderMap headerMap: Map<String, String>,
        @QueryMap queryParams: Map<String, String>,
        @Part("body") body: RequestBody?,
        @Part files: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @Multipart
    @PUT
    suspend fun putMultipart(
        @Url pathUrl: String,
        @HeaderMap headerMap: Map<String, String>,
        @QueryMap queryParams: Map<String, String>,
        @Part("body") body: RequestBody?,
        @Part files: List<MultipartBody.Part>
    ): Response<ResponseBody>

}