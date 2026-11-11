package com.icompound.core.data.model.dto

import com.google.gson.annotations.SerializedName

open class BaseDto(
    @SerializedName("message") var message: String? = null,
    @SerializedName("code") var code: String? = null,
)

data class PagingQuery(
    val pageNumber: Int = 1,
    val pageSize: Int = 20,
    val searchTerm: String? = null,
    val sortBy: String? = null,
    val isDescending: Boolean? = null,
) {
    // Query names are PascalCase in the spec: PageNumber, PageSize, SearchTerm, SortBy, IsDescending
    fun toQueryMap(): Map<String, String> =
        buildMap {
            put("PageNumber", pageNumber.toString())
            put("PageSize", pageSize.toString())
            searchTerm?.takeIf { it.isNotBlank() }?.let { put("SearchTerm", it) }
            sortBy?.takeIf { it.isNotBlank() }?.let { put("SortBy", it) }
            isDescending?.let { put("IsDescending", it.toString()) }
        }
}

// ---------- ASSUMED: confirm with backend, the spec gives no response shape ----------
data class ApiResponseDto<T>(
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("isSuccess") val isSuccess: Boolean? = null,
)

data class PagedDto<T>(
    @SerializedName("items") val items: List<T> = emptyList(),
    @SerializedName("pageNumber") val pageNumber: Int = 1,
    @SerializedName("pageSize") val pageSize: Int = 20,
    @SerializedName("totalCount") val totalCount: Int = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
)
