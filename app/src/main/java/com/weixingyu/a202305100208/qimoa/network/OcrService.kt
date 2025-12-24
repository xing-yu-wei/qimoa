package com.weixingyu.a202305100208.qimoa.network

import com.weixingyu.a202305100208.qimoa.bean.OcrResponse
import com.weixingyu.a202305100208.qimoa.bean.TokenResponse
import retrofit2.Call
import retrofit2.http.*

interface OcrService {

    // 第一步：获取 Access Token
    @POST("oauth/2.0/token")
    fun getAccessToken(
        @Query("grant_type") grantType: String = "client_credentials",
        @Query("client_id") clientId: String, // API Key
        @Query("client_secret") clientSecret: String // Secret Key
    ): Call<TokenResponse>

    // 第二步：发送图片进行识别 (通用文字识别-高精度版)
    @FormUrlEncoded
    @POST("rest/2.0/ocr/v1/accurate_basic")
    fun recognizeText(
        @Query("access_token") accessToken: String,
        @Field("image") imageBase64: String, // 图片转成的Base64字符串
        @Field("detect_direction") detectDirection: String = "true"
    ): Call<OcrResponse>
}