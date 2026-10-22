package com.example.wordpressclient.domain.repository

import com.example.wordpressclient.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
}
