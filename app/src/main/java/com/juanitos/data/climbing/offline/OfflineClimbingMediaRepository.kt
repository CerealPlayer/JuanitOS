package com.juanitos.data.climbing.offline

import com.juanitos.data.climbing.daos.ClimbingMediaDao
import com.juanitos.data.climbing.entities.ClimbingMedia
import com.juanitos.data.climbing.repositories.ClimbingMediaRepository
import kotlinx.coroutines.flow.Flow

class OfflineClimbingMediaRepository(
    private val climbingMediaDao: ClimbingMediaDao
) : ClimbingMediaRepository {
    override suspend fun insert(climbingMedia: ClimbingMedia): Long =
        climbingMediaDao.insert(climbingMedia)

    override suspend fun update(climbingMedia: ClimbingMedia) =
        climbingMediaDao.update(climbingMedia)

    override suspend fun delete(climbingMedia: ClimbingMedia) =
        climbingMediaDao.delete(climbingMedia)

    override fun getAll(): Flow<List<ClimbingMedia>> = climbingMediaDao.getAll()
}
