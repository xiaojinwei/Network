package com.cj.network

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cj.network.http.ApiService
import com.cj.runtime.Network
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        GlobalScope.launch {
            val result = Network.repositoryManager.obtainRetrofitService(ApiService::class.java).getTopList()
            println("result:${result}")
        }

    }
}