package com.example.wordpressclient

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * WordPressClientApp = lớp Application của app, là "công tắc tổng" bật Hilt.
 *
 * Android tạo đúng một đối tượng này khi app khởi động, trước mọi Activity.
 * Annotation @HiltAndroidApp khiến Hilt sinh code tạo kho chứa dependency mức toàn app
 * (nơi sống của Json, OkHttpClient, Retrofit, WordPressApi, PostRepository...).
 *
 * Thân lớp để trống vì hiện chưa cần khởi tạo gì thêm lúc app mở.
 * Lớp này phải được khai báo trong AndroidManifest.xml (android:name=".WordPressClientApp").
 */
@HiltAndroidApp
class WordPressClientApp : Application()