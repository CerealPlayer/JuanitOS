package com.juanitos.data.money.offline

import com.juanitos.data.money.daos.CategoryDao
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.repositories.CategoryRepository
import kotlinx.coroutines.flow.Flow

data class OfflineCategoryRepository(private val categoryDao: CategoryDao) : CategoryRepository {
    override suspend fun insert(name: String, description: String?): Long =
        categoryDao.insert(name, description)

    override suspend fun update(category: Category) = categoryDao.update(category)
    override suspend fun delete(category: Category) = categoryDao.delete(category)
    override suspend fun getById(id: Int): Flow<Category> = categoryDao.getById(id)
    override suspend fun getAll(): Flow<List<Category>> = categoryDao.getAll()
}