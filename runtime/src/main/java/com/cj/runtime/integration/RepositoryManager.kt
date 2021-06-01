package com.cj.runtime.integration

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import retrofit2.Retrofit
import java.util.*


class RepositoryManager constructor(var mRetrofit //BaseUrl对应的Retrofit
                                    : Map<HttpUrl, Retrofit>) : IRepositoryManager {

    private val mRetrofitServiceCache: MutableMap<String, Any> =
        LinkedHashMap(2)

    override fun addInjectRetrofitService(baseUrl: String, vararg services: Class<*>) {
        baseUrl.toHttpUrlOrNull()?.apply {
            addInjectRetrofitService(this, *services)
        }
    }

    fun addInjectRetrofitService(
        httpUrl: HttpUrl,
        vararg services: Class<*>
    ) {
        val retrofit = mRetrofit[httpUrl]
            ?: throw RuntimeException("Retrofit without BaseUrl binding ,BaseUrl:${httpUrl}")
        for (service in services) {
            if (mRetrofitServiceCache.containsKey(service.name)) continue
            mRetrofitServiceCache[service.name] = retrofit.create(service)
        }
    }

    override fun <T> obtainRetrofitService(service: Class<T>): T {
        if(!mRetrofitServiceCache.containsKey(service.name)){
            throw java.lang.RuntimeException("Unable to find ${service.name},first call injectRetrofitService(${service.simpleName}) in ConfigModule")
        }
        return mRetrofitServiceCache[service.name] as T
    }
}