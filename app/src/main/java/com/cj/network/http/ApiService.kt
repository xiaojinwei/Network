package com.cj.network.http

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    /**
     * 获取首页文章数据
     */
    @GET("/article/list/{page}/json")
    suspend fun getHomeList(@Path("page") pageNo: Int): Result<ArticleBean>

    /**
     * 获取首页置顶文章数据
     */
    @GET("/article/top/json")
    suspend fun getTopList(): Result<MutableList<ArticleBean.DatasBean>>

}