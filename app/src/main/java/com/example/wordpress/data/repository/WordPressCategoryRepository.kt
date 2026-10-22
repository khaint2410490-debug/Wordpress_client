package com.example.wordpressclient.data.repository

import com.example.wordpressclient.data.mapper.toDomain
import com.example.wordpressclient.data.remote.WordPressApi
import com.example.wordpressclient.domain.model.Category
import com.example.wordpressclient.domain.repository.CategoryRepository
import javax.inject.Inject

class WordPressCategoryRepository @Inject constructor(
    private val api: WordPressApi
) : CategoryRepository {
    override suspend fun getCategories(): Result<List<Category>> = safeApiCall {
        api.getCategories().toDomain()
    }
}
