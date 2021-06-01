package com.cj.runtime

import java.io.File

class NetConfig {

    companion object{

        @kotlin.jvm.JvmField
        val NET_CACHE_SIZE : Long = 1024 * 1024 * 50 //网络缓存的大小
        @kotlin.jvm.JvmField
        val NET_CACHE_MAX_AGE : Long = 0 //有网络时，不缓存，最大保存时长为0
        @kotlin.jvm.JvmField
        val NET_CACHE_MAX_STALE : Long = 60 * 60 * 24 * 28 //无网络时，设置超时时间为4周

        @kotlin.jvm.JvmField
        val NET_CONNECT_TIMEOUT : Long = 30 //连接时间 单位s
        @kotlin.jvm.JvmField
        val NET_READ_TIMEOUT : Long = 30 //读取时间 单位s
        @kotlin.jvm.JvmField
        val NET_WRITE_TIMEOUT : Long = 30 //写时间 单位s


        @kotlin.jvm.JvmField
        val BASE_CACHE_PATH: String =
            Network.context.cacheDir.absolutePath
        @kotlin.jvm.JvmField
        val CACHE_DATA_PATH = BASE_CACHE_PATH + File.separator + "data"
        @kotlin.jvm.JvmField
        val NET_CACHE_PATH = CACHE_DATA_PATH + File.separator + "NetCache"
    }
}