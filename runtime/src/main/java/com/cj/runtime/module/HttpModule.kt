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
                        .cacheControl(CacheControl.FORCE_CACHE) //仅仅使用缓存，CacheControl.FORCE_NETWORK;// 仅仅使用网络
                        .build()
                }
                val response =
                    chain.proceed(request) //java.net.ConnectException: Failed to connect to /192.168.1.102:8080
                if (NetUtil.isNetworkConnected(context)) {
                    val maxAge: Long = NetConfig.NET_CACHE_MAX_AGE
                    //有网络时，不缓存，最大保存时长为0
                    //Public指示响应可被任何缓存区缓存
                    response.newBuilder()
                        .header(
                            "Cache-Control",
                            "public, max-age=$maxAge"
                        ) //max-age：指示客户机可以接收生存期不大于指定时间（以秒为单位）的响应。
                        .removeHeader("Pragma")
                        .build()
                } else {
                    //无网络时，设置超时时间为4周
                    val maxStale: Long = NetConfig.NET_CACHE_MAX_STALE
                    response.newBuilder() //max-stale：客户机可以接收超出超时期指定值之内的响应消息
                        .header(
                            "Cache-Control",
                            "public,only-if-cached,max-stale=$maxStale"
                        ) //只使用缓存，如果没有缓存，则会报：retrofit2.adapter.rxjava2.HttpException: HTTP 504 Unsatisfiable Request (only-if-cached)
                        .removeHeader("Pragma")
                        .build()
                }
                return response
            }
        }

        //设置缓存
        builder.addInterceptor(cacheInterceptor)
        builder.addNetworkInterceptor(cacheInterceptor)
        builder.cache(cache)

        //日志打印
        builder.addNetworkInterceptor(globalInterceptor)
        //设置超时时间
        builder.connectTimeout(NetConfig.NET_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        builder.readTimeout(NetConfig.NET_READ_TIMEOUT, TimeUnit.SECONDS)
        builder.writeTimeout(NetConfig.NET_WRITE_TIMEOUT, TimeUnit.SECONDS)
        //错误重连
        builder.retryOnConnectionFailure(true)
        configuration?.configOkHttp(context, builder)
        return builder.build()
    }


    /**
     * 该方式原本是提供全局Retrofit，但是该库已经支持多个BaseUrl，也就是会生成和BaseUrl相同数目的Retrofit
     * 所以会走[&lt;][.provideAllBaseUrlRetrofit]
     * 方式提供Retrofit
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
     * 向外提供配置Gson的接口，Gson对应每个BaseUrl
     */
    interface GsonConfiguration {
        fun configGson(
            context: Context,
            builder: GsonBuilder,
            httpUrl: HttpUrl
        )
    }

    /**
     * 向外提供配置Retrofit 的接口,Retrofit对应每个BaseUrl
     */
    interface RetrofitConfiguration {
        fun configRetrofit(
            context: Context,
            builder: Retrofit.Builder,
            httpUrl: HttpUrl
        )
    }

    /**
     * 向外提供配置OkHttp 的接口,全局的
     */
    interface OkHttpConfiguration {
        fun configOkHttp(
            context: Context,
            builder: OkHttpClient.Builder
        )
    }
}