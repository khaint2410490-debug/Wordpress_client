package com.example.wordpressclient.core.config

import okhttp3.logging.HttpLoggingInterceptor

object NetworkConfig {
    const val BASE_URL = "https://public-api.wordpress.com/wp/v2/sites/clientdemo39.wordpress.com/"
    const val TIMEOUT_SECONDS = 30L
    const val IGNORE_UNKNOWN_JSON_KEYS = true
    const val COERCE_JSON_INPUT_VALUES = true
    val LOG_LEVEL = HttpLoggingInterceptor.Level.BASIC
}
