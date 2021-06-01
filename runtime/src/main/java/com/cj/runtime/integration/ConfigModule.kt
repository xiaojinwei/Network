package com.cj.runtime.integration

import android.content.Context
import com.cj.runtime.module.GlobalConfigModule

interface ConfigModule {
    /**
     * 使用[GlobalConfigModule.Builder]给框架配置一些配置参数
     * @param context
     * @param builder
     */
    fun applyOptions(
        context: Context,
        builder: GlobalConfigModule.Builder
    )

    /**
     * 使用[IRepositoryManager]给框架注入一些网络请求和数据缓存等服务
     * @param context
     * @param repositoryManager
     */
    fun registerComponents(
        context: Context,
        repositoryManager: IRepositoryManager
    )
}