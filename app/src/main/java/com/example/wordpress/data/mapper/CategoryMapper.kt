package com.example.wordpressclient.data.mapper

import com.example.wordpressclient.data.remote.dto.CategoryDto
import com.example.wordpressclient.domain.model.Category

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name.htmlToPlainText(),
    count = count
)

fun List<CategoryDto>.toDomain(): List<Category> = map(CategoryDto::toDomain)
