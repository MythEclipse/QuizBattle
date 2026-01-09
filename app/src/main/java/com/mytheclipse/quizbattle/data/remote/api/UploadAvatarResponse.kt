package com.mytheclipse.quizbattle.data.remote.api

import com.google.gson.annotations.SerializedName

data class UploadAvatarResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("url")
    val url: String?,
    
    @SerializedName("error")
    val error: String?
)
