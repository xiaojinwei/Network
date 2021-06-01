package com.cj.runtime

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Http拦截请求之前的请求体（Request）和响应之后的响应体（Response），响应体先于调用者拿到
 */
interface GlobalHttpHandler {
    /**
     * 网络请求的响应体，可对其做全局操作
     * @param responseBody
     * @param chain
     * @param response
     * @return
     */
    fun onHttpResponse(
        responseBody: String,
        chain: Interceptor.Chain,
        response: Response
    ): Response

    /**
     * 网络请求前的请求体，可对其做全局操作
     * @param chain
     * @param request
     * @return
     */
    fun onHttpRequest(
        chain: Interceptor.Chain,
        request: Request
    ): Request

    /**
     * 空实现，不做任何做操
     */
    companion object{
        @kotlin.jvm.JvmField
        val EMPTY: GlobalHttpHandler = object : GlobalHttpHandler {
            override fun onHttpResponse(
                responseBody: String,
                chain: Interceptor.Chain,
                response: Response
            ): Response {
                //不做任何处理，直接返回
                return response
            }

            override fun onHttpRequest(
                chain: Interceptor.Chain,
                request: Request
            ): Request {
                //不做任何处理，直接返回
                return request
            }
        }
    }

}