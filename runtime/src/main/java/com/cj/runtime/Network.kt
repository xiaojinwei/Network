package com.cj.runtime

import android.content.Context
import androidx.startup.Initializer
import com.cj.runtime.integration.ConfigModule
import com.cj.runtime.integration.IRepositoryManager
import com.cj.runtime.integration.RepositoryManager
import com.cj.runtime.module.GlobalConfigModule
import com.cj.runtime.module.HttpModule

object Network {

    lateinit var context: Context

    lateinit var globalConfigModule: GlobalConfigModule

    lateinit var repositoryManager : IRepositoryManager

    fun init(context: Context,configModules:List<ConfigModule>){

        this.context = context

        val builder: GlobalConfigModule.Builder = GlobalConfigModule.builder()
        configModules.forEach {
            it.applyOptions(context, builder)
        }
        var configModule = builder.build()
        globalConfigModule = configModule

        repositoryManager = RepositoryManager(HttpModule.allBaseUrlRetrofit)

        configModules.forEach {
            it.registerComponents(context, repositoryManager)
        }
    }
}