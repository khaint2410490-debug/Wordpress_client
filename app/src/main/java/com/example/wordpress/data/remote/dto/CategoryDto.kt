package com.example.wordpressclient.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: Int,
    val count: Int = 0,
    val name: String
)
