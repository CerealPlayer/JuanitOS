package com.juanitos.data.climbing.offline

import com.juanitos.data.climbing.daos.ClimbingBoulderDao
import com.juanitos.data.climbing.entities.ClimbingBoulder
import com.juanitos.data.climbing.repositories.ClimbingBoulderRepository
import kotlinx.coroutines.flow.Flow

class OfflineClimbingBoulderRepository(
    private val climbingBoulderDao: ClimbingBoulderDao
) : ClimbingBoulderRepository {
    override suspend fun insert(climbingBoulder: ClimbingBoulder): Long =
        climbingBoulderDao.insert(climbingBoulder)

    override suspend fun update(climbingBoulder: ClimbingBoulder) =
        climbingBoulderDao.update(climbingBoulder)

    override suspend fun delete(climbingBoulder: ClimbingBoulder) =
        climbingBoulderDao.delete(climbingBoulder)

    override fun getAll(): Flow<List<ClimbingBoulder>> = climbingBoulderDao.getAll()
}
