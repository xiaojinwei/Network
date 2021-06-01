package com.cj.runtime.integration

interface IRepositoryManager {
    /**
     * 注入RetrofitService
     * @param services
     */
    fun addInjectRetrofitService(
        baseUrl: String,
        vararg services: Class<*>
    )

    /**
     * 根据传入的Class获取对应的Retrofit Service
     * @param service
     * @param <T>
     * @return
    </T> */
    fun <T> obtainRetrofitService(service: Class<T>): T
}