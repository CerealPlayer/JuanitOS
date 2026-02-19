package com.juanitos.data.money.repositories

import com.juanitos.data.money.entities.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    suspend fun insert(name: String, description: String?): Long
    suspend fun update(category: Category)
    suspend fun delete(category: Category)
    fun getById(id: Int): Flow<Category>
    fun getAll(): Flow<List<Category>>
}