package com.weixingyu.a202305100208.qimoa.bean

import com.google.gson.annotations.SerializedName

// 1. 获取Token返回的结构
data class TokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int
)

// 2. OCR识别返回的结构
data class OcrResponse(
    @SerializedName("words_result") val wordsResult: List<WordNode>?
)

data class WordNode(
    @SerializedName("words") val words: String
)