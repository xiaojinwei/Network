package com.cj.runtime.module

import android.content.Context
import com.cj.runtime.GlobalHttpHandler
import com.cj.runtime.GlobalInterceptor
import com.cj.runtime.NetConfig
import com.cj.runtime.Network
import com.cj.runtime.utils.NetUtil
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

object HttpModule {

    val gsonBuilder: GsonBuilder = GsonBuilder()

    val okHttpBuilder : OkHttpClient.Builder = OkHttpClient.Builder()

    val retrofitBuilder : Retrofit.Builder = Retrofit.Builder()

    val okHttpClient : OkHttpClient
    get() = _okHttpClient!!

    private var _okHttpClient : OkHttpClient? = null
    get() = field?:let{
        var globalHttpHandler = Network.globalConfigModule.getGlobalHttpHandler()
        val globalInterceptor : GlobalInterceptor = GlobalInterceptor(globalHttpHandler)
        field = provideOkHttpClient(Network.context, okHttpBuilder,Network.globalConfigModule.getCacheFile(),
            Network.globalConfigModule.getOkHttpConfiguration(),globalHttpHandler,globalInterceptor)
        return field
    }

    private fun provideOkHttpClient(
        context: Context,
        builder: OkHttpClient.Builder,
        cacheFile: File,
        configuration: OkHttpConfiguration?,
        globalHttpHandler: GlobalHttpHandler,
        globalInterceptor: Interceptor
    ): OkHttpClient {
        /*if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(httpLoggingInterceptor);
        }*/
        builder.addInterceptor(object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                return chain.proceed(globalHttpHandler.onHttpRequest(chain, chain.request()))
            }
        })
        val cache = Cache(cacheFile, NetConfig.NET_CACHE_SIZE)
        val cacheInterceptor: Interceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): Response {
                var request = chain.request()
                if (!NetUtil.isNetworkConnected(context)) {
                    request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE) //?????????????????????CacheControl.FORCE_NETWORK;// ??????????????????
                        .build()
                }
                val response =
                    chain.proceed(request) //java.net.ConnectException: Failed to connect to /192.168.1.102:8080
                if (NetUtil.isNetworkConnected(context)) {
                    val maxAge: Long = NetConfig.NET_CACHE_MAX_AGE
                    //????????????????????????????????????????????????0
                    //Public???????????????????????????????????????
                    response.newBuilder()
                        .header(
                            "Cache-Control",
                            "public, max-age=$maxAge"
                        ) //max-age?????????????????????????????????????????????????????????????????????????????????????????????
                        .removeHeader("Pragma")
                        .build()
                } else {
                    //????????????????????????????????????4???
                    val maxStale: Long = NetConfig.NET_CACHE_MAX_STALE
                    response.newBuilder() //max-stale?????????????????????????????????????????????????????????????????????
                        .header(
                            "Cache-Control",
                            "public,only-if-cached,max-stale=$maxStale"
                        ) //???????????????????????????????????????????????????retrofit2.adapter.rxjava2.HttpException: HTTP 504 Unsatisfiable Request (only-if-cached)
                        .removeHeader("Pragma")
                        .build()
                }
                return response
            }
        }

        //????????????
        builder.addInterceptor(cacheInterceptor)
        builder.addNetworkInterceptor(cacheInterceptor)
        builder.cache(cache)

        //????????????
        builder.addNetworkInterceptor(globalInterceptor)
        //??????????????????
        builder.connectTimeout(NetConfig.NET_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        builder.readTimeout(NetConfig.NET_READ_TIMEOUT, TimeUnit.SECONDS)
        builder.writeTimeout(NetConfig.NET_WRITE_TIMEOUT, TimeUnit.SECONDS)
        //????????????
        builder.retryOnConnectionFailure(true)
        configuration?.configOkHttp(context, builder)
        return builder.build()
    }


    /**
     * ??????????????????????????????Retrofit?????????????????????????????????BaseUrl????????????????????????BaseUrl???????????????Retrofit
     * ????????????[&lt;][.provideAllBaseUrlRetrofit]
     * ????????????Retrofit
     * @param application
     * @param builder
     * @param client
     * @param httpUrl
     * @param configuration
     * @return
     */
    @Deprecated("")
    private fun provideRetrofit(
        context: Context, builder: Retrofit.Builder, client: OkHttpClient,
        httpUrl: HttpUrl, configuration: RetrofitConfiguration,
        gson: Gson
    ): Retrofit? {
        return createRetrofit(context, builder, client, httpUrl, configuration, gson)
    }

    private fun createRetrofit(
        context: Context, builder: Retrofit.Builder, client: OkHttpClient,
        baseUrl: HttpUrl, configuration: RetrofitConfiguration?,
        gson: Gson
    ): Retrofit {
        builder
            .baseUrl(baseUrl)
            .client(client)
            //.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
        configuration?.configRetrofit(context, builder, baseUrl)
        return builder.build()
    }

    val allBaseUrlRetrofit : Map<HttpUrl, Retrofit>
    get() = _allBaseUrlRetrofit!!

    private var _allBaseUrlRetrofit : Map<HttpUrl, Retrofit>? = null
    get() = field?:let {
        field = provideAllBaseUrlRetrofit(Network.context, retrofitBuilder, okHttpClient,Network.globalConfigModule.getBaseUrl(),
            Network.globalConfigModule.getRetrofitConfiguration(), allBaseUrlGson)
        return field
    }

    private fun provideAllBaseUrlRetrofit(
        context: Context, builder: Retrofit.Builder, client: OkHttpClient,
        baseHttpUrls: List<HttpUrl>, configuration: RetrofitConfiguration?,
        gsonMap: Map<HttpUrl, Gson>
    ): Map<HttpUrl, Retrofit> {
        val retrofitMap: MutableMap<HttpUrl, Retrofit> =
            LinkedHashMap()
        for (i in baseHttpUrls.indices) {
            val httpUrl = baseHttpUrls[i]
            val retrofit =
                createRetrofit(context, builder, client, httpUrl, configuration, gsonMap[httpUrl]?:Gson())
            retrofitMap[httpUrl] = retrofit
        }
        return retrofitMap
    }

    val allBaseUrlGson : Map<HttpUrl, Gson>
    get() = _allBaseUrlGson!!

    var _allBaseUrlGson : Map<HttpUrl, Gson>? = null
    get() = field?:let{
        field = provideAllBaseUrlGson(Network.context, gsonBuilder,Network.globalConfigModule.getGsonConfiguration(),
            Network.globalConfigModule.getBaseUrl())
        return  field
    }

    private fun provideAllBaseUrlGson(
        context: Context, gsonBuilder: GsonBuilder,
        gsonConfiguration: GsonConfiguration?,
        baseHttpUrls: List<HttpUrl>
    ): Map<HttpUrl, Gson> {
        val gsonMap: MutableMap<HttpUrl, Gson> =
            LinkedHashMap()
        for (i in baseHttpUrls.indices) {
            val httpUrl = baseHttpUrls[i]
            gsonConfiguration?.configGson(context, gsonBuilder, httpUrl)
            gsonMap[httpUrl] = gsonBuilder.create()
        }
        return gsonMap
    }

    /**
     * ??????????????????Gson????????????Gson????????????BaseUrl
     */
    interface GsonConfiguration {
        fun configGson(
            context: Context,
            builder: GsonBuilder,
            httpUrl: HttpUrl
        )
    }

    /**
     * ??????????????????Retrofit ?????????,Retrofit????????????BaseUrl
     */
    interface RetrofitConfiguration {
        fun configRetrofit(
            context: Context,
            builder: Retrofit.Builder,
            httpUrl: HttpUrl
        )
    }

    /**
     * ??????????????????OkHttp ?????????,?????????
     */
    interface OkHttpConfiguration {
        fun configOkHttp(
            context: Context,
            builder: OkHttpClient.Builder
        )
    }
}