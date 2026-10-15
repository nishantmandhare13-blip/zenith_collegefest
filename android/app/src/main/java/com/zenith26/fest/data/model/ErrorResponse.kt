package com.zenith26.fest.data.model

import com.google.gson.annotations.SerializedName

/**
 * Standard error response structure returned by FastAPI: {"detail": "..."}
 */
data class ApiErrorResponse(
    @SerializedName("detail")
    val detail: String? = null
)
