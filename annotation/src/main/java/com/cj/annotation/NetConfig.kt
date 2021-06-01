package com.cj.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
@MustBeDocumented //包含在API文档中
annotation class NetConfig(
    val enabled : Boolean = true
)