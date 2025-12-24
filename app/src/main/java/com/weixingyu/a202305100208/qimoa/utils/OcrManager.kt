package com.weixingyu.a202305100208.qimoa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.weixingyu.a202305100208.qimoa.bean.OcrResponse
import com.weixingyu.a202305100208.qimoa.bean.TokenResponse
import com.weixingyu.a202305100208.qimoa.network.OcrService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.InputStream

object OcrManager {

    // 【请填入你刚才申请的 Key !!!】
    private const val API_KEY = "HjQY51FDVGnJZuJiJSFleGGz"
    private const val SECRET_KEY = "KZQMk62GB2k2Wb1v1bS92lY5zwXqylPL"

    private const val BASE_URL = "https://aip.baidubce.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(OcrService::class.java)

    // 对外暴露的方法：识别图片
    fun recognize(context: Context, imageUri: Uri, callback: (Double?) -> Unit) {
        // 1. 先把 Uri 转成 Base64 字符串
        val base64Img = uriToBase64(context, imageUri)
        if (base64Img == null) {
            callback(null)
            return
        }

        // 2. 获取 Token
        service.getAccessToken(clientId = API_KEY, clientSecret = SECRET_KEY)
            .enqueue(object : Callback<TokenResponse> {
                override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                    val token = response.body()?.accessToken
                    if (token != null) {
                        // 3. 拿到Token后，去识别图片
                        startOcr(token, base64Img, callback)
                    } else {
                        Log.e("OCR", "Token获取失败")
                        callback(null)
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                    Log.e("OCR", "网络错误: ${t.message}")
                    callback(null)
                }
            })
    }

    private fun startOcr(token: String, base64Img: String, callback: (Double?) -> Unit) {
        service.recognizeText(token, base64Img).enqueue(object : Callback<OcrResponse> {
            override fun onResponse(call: Call<OcrResponse>, response: Response<OcrResponse>) {
                val result = response.body()?.wordsResult
                if (result != null) {
                    // 4. 解析结果，寻找像金额的数字
                    val amount = findAmountInText(result)
                    callback(amount)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<OcrResponse>, t: Throwable) {
                callback(null)
            }
        })
    }

    // 【升级版】智能查找金额算法
    private fun findAmountInText(words: List<com.weixingyu.a202305100208.qimoa.bean.WordNode>): Double? {
        // 匹配规则：可以是小数 (31.34) 也可以是整数 (25)
        val pattern = Regex("\\d+(\\.\\d{1,2})?")

        // 策略1：最高优先级 —— 找 "合计" 或 "实付" (这通常是真正的付款额)
        for (node in words) {
            val text = node.words
            if (text.contains("合计") || text.contains("实付")) {
                // 还要注意：如果这一行同时有 "数量" (比如 0.56) 和 "合计"，要取最后的那个数字
                val matches = pattern.findAll(text)
                // 取最后一个匹配到的数字 (通常合计都在最右边)
                val lastMatch = matches.lastOrNull()
                if (lastMatch != null) {
                    return lastMatch.value.toDoubleOrNull()
                }
            }
        }

        // 策略2：次级优先级 —— 找 "金额" 或 "总"，但坚决排除 "优惠" (防止识别成 0.00)
        for (node in words) {
            val text = node.words
            if ((text.contains("金额") || text.contains("总")) && !text.contains("优惠")) {
                val match = pattern.find(text)
                if (match != null) return match.value.toDoubleOrNull()
            }
        }

        // 策略3：保底大招 —— 如果上面都没找到，就找全篇最大的那个数字 (通常总价就是最大的)
        var maxAmount = 0.0
        for (node in words) {
            val matches = pattern.findAll(node.words)
            for (match in matches) {
                val valStr = match.value
                // 排除年份 (比如 2025) 和太大的离谱数字
                val value = valStr.toDoubleOrNull() ?: 0.0
                if (value > maxAmount && value < 100000) { // 假设大学生消费不超过10万
                    maxAmount = value
                }
            }
        }

        return if (maxAmount > 0.0) maxAmount else null
    }

    // 图片转 Base64 工具方法
    private fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            // 压缩一下，不然太大传不上去
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}