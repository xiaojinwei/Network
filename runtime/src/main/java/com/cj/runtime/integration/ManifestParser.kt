package com.cj.runtime.integration

import android.content.Context
import android.content.pm.PackageManager
import java.util.*

class ManifestParser constructor(val context: Context) {
    private val MODULE_VALUE = "ConfigModule"

    fun parse(): List<ConfigModule>? {
        val modules: MutableList<ConfigModule> =
            ArrayList()
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName, PackageManager.GET_META_DATA
            )
            if (appInfo.metaData != null) {
                for (key in appInfo.metaData.keySet()) {
                    if (MODULE_VALUE == appInfo.metaData[key]) {
                        modules.add(parseModule(key))
                    }
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException("Unable to find metadata to parse ConfigModule", e)
        }
        return modules
    }

    private fun parseModule(className: String): ConfigModule {
        val clazz: Class<*>
        clazz = try {
            Class.forName(className)
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException(
                "Unable to find ConfigModule implementation",
                e
            )
        }
        val module: Any
        module = try {
            clazz.newInstance()
        } catch (e: InstantiationException) {
            throw RuntimeException(
                "Unable to instantiate ConfigModule implementation for $clazz",
                e
            )
        } catch (e: IllegalAccessException) {
            throw RuntimeException(
                "Unable to instantiate ConfigModule implementation for $clazz",
                e
            )
        }
        if (module !is ConfigModule) {
            throw RuntimeException("Expected instanceof ConfigModule, but found: $module")
        }
        return module
    }
}