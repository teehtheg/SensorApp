package com.teeh.klimasensor.common.config

import android.content.Context
import android.content.res.AssetManager

import java.io.InputStream
import java.util.Properties

class ConfigService(private val context: Context) {
    private val properties: Properties
    private val file: String = "config.properties"

    init {
        properties = Properties()

        val assetManager = context.assets
        val inputStream = assetManager.open(file)
        properties.load(inputStream)
    }

    fun get(key: String): String {
        return this.properties.getProperty(key)
    }

}
