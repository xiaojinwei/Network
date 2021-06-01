package com.cj.runtime

import okhttp3.Interceptor
import okhttp3.Response

class GlobalInterceptor constructor(var globalHttpHandler: GlobalHttpHandler) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val proceed = chain.proceed(request)

        //这里可以比客户端提前一步拿到服务器返回的结果,可以做一些操作,比如token超时,重新获取
        return globalHttpHandler.onHttpResponse(proceed.body.toString(), chain, proceed)
    }
}