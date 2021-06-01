package com.cj.runtime.utils

import android.content.Context
import android.net.ConnectivityManager

class NetUtil {

    companion object{
        /**
         * 检查是否有可用网络
         */
        @kotlin.jvm.JvmStatic
        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager =
                context.applicationContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo != null
        }
    }
}