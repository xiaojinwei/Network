package com.cj.runtime.module

import android.text.TextUtils
import com.cj.runtime.GlobalHttpHandler
import com.cj.runtime.NetConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.io.File
import java.lang.RuntimeException
import java.util.*

class GlobalConfigModule {
    private var mApiUrls: List<HttpUrl>
    private var mGlobalHttpHandler: GlobalHttpHandler? = null
    private var mCacheFile: File? = null
    private var mOkHttpConfiguration: HttpModule.OkHttpConfiguration? = null
    private var mRetrofitConfiguration: HttpModule.RetrofitConfiguration? = null
    private var mGsonConfiguration: HttpModule.GsonConfiguration? = null

    constructor(builder: Builder){
        mApiUrls = builder.apiUrls
        mCacheFile = builder.cacheFile
        mGlobalHttpHandler = builder.globalHttpHandler
        mOkHttpConfiguration = builder.okHttpConfiguration
        mRetrofitConfiguration = builder.retrofitConfiguration
        mGsonConfiguration = builder.gsonConfiguration
    }

    companion object{

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }
    }

    fun getBaseUrl(): List<HttpUrl> {
        return mApiUrls
    }

    fun getGlobalHttpHandler(): GlobalHttpHandler {
        return if (mGlobalHttpHandler == null) GlobalHttpHandler.EMPTY else mGlobalHttpHandler!!
    }

    fun getCacheFile(): File {
        return if (mCacheFile == null) File(NetConfig.NET_CACHE_PATH) else mCacheFile!!
    }

    fun getOkHttpConfiguration(): HttpModule.OkHttpConfiguration? {
        return mOkHttpConfiguration
    }

    fun getRetrofitConfiguration(): HttpModule.RetrofitConfiguration? {
        return mRetrofitConfiguration
    }

    fun getGsonConfiguration(): HttpModule.GsonConfiguration? {
        return mGsonConfiguration
    }


    class Builder {
        lateinit var apiUrls: List<HttpUrl>
        private set
        var globalHttpHandler: GlobalHttpHandler? = null
        private set
        var cacheFile: File? = null
        private set
        var okHttpConfiguration: HttpModule.OkHttpConfiguration? = null
        private set
        var retrofitConfiguration: HttpModule.RetrofitConfiguration? = null
        private set
        var gsonConfiguration: HttpModule.GsonConfiguration? = null
        private set
        fun baseUrl(vararg baseUrl: String): Builder {
            if (baseUrl.isEmpty()) {
                throw RuntimeException("Please set at least one baseUrl")
            }
            val urls: MutableList<HttpUrl> = ArrayList()
            for (i in baseUrl.indices) {
                if (TextUtils.isEmpty(baseUrl[i])) {
                    throw RuntimeException("baseUrl can not be empty")
                }
                baseUrl[i].toHttpUrlOrNull()?.let { urls.add(it) }
            }
            apiUrls = urls
            return this
        }

        fun cacheFile(cacheFile: File?): Builder {
            this.cacheFile = cacheFile
            return this
        }

        fun globalHttpHandler(globalHttpHandler: GlobalHttpHandler?): Builder {
            this.globalHttpHandler = globalHttpHandler
            return this
        }

        fun okHttpConfiguration(okHttpConfiguration: HttpModule.OkHttpConfiguration?): Builder {
            this.okHttpConfiguration = okHttpConfiguration
            return this
        }

        fun retrofitConfiguration(retrofitConfiguration: HttpModule.RetrofitConfiguration?): Builder {
            this.retrofitConfiguration = retrofitConfiguration
            return this
        }

        fun gsonConfiguration(gsonConfiguration: HttpModule.GsonConfiguration?): Builder {
            this.gsonConfiguration = gsonConfiguration
            return this
        }

        fun build(): GlobalConfigModule {
            return GlobalConfigModule(this)
        }
    }
}