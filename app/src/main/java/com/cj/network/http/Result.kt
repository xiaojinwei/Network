package com.cj.network.http

import java.io.Serializable

class Result<T> : Serializable{
    var data: T? = null

    /**
     * 业务信息
     */
    var errorMsg = ""

    /**
     * 业务code
     */
    var errorCode = 0
}