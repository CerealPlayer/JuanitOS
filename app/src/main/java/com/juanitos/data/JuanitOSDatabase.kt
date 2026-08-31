package com.juanitos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juanitos.data.migrations.MIGRATION_10_11
import com.juanitos.data.migrations.MIGRATION_11_12
import com.juanitos.data.migrations.MIGRATION_12_13
import com.juanitos.data.migrations.MIGRATION_13_14
import com.juanitos.data.migrations.MIGRATION_28_29
import com.juanitos.data.migrations.MIGRATION_29_30
import com.juanitos.data.migrations.MIGRATION_30_31
import com.juanitos.data.migrations.MIGRATION_31_32
import com.juanitos.data.migrations.MIGRATION_32_33
import com.juanitos.data.migrations.MIGRATION_33_34
import com.juanitos.data.migrations.MIGRATION_34_35
import com.juanitos.data.migrations.MIGRATION_35_36
import com.juanitos.data.migrations.MIGRATION_36_37
import com.juanitos.data.migrations.MIGRATION_9_10
import com.juanitos.data.money.SeedDefaultCategoriesCallback
import com.juanitos.data.money.daos.AccountDao
import com.juanitos.data.money.daos.CategoryDao
import com.juanitos.data.money.daos.CreditCardDao
import com.juanitos.data.money.daos.TransactionDao
import com.juanitos.data.money.entities.Account
import com.juanitos.data.money.entities.Category
import com.juanitos.data.money.entities.CreditCard
import com.juanitos.data.money.entities.Transaction

@Database(
    entities = [
        Account::class, Transaction::class, Category::class, CreditCard::class
    ],
    version = 37,
    exportSchema = false
)
abstract class JuanitOSDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun creditCardDao(): CreditCardDao

    companion object {
        @Volatile
        private var Instance: JuanitOSDatabase? = null

        fun getDatabase(context: Context): JuanitOSDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, JuanitOSDatabase::class.java, "JuanitOS_database")
                    .addMigrations(
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_28_29,
                        MIGRATION_29_30,
                        MIGRATION_30_31,
                        MIGRATION_31_32,
                        MIGRATION_32_33,
                        MIGRATION_33_34,
                        MIGRATION_34_35,
                        MIGRATION_35_36,
                        MIGRATION_36_37
                    )
                    .addCallback(SeedDefaultCategoriesCallback)
                    .fallbackToDestructiveMigration(false)
                    .build().also { Instance = it }
            }
        }

        /**
         * For tests. Runs queries on the calling thread (via a same-thread [java.util.concurrent.Executor])
         * so a suspend repository call started from `viewModelScope.launch` completes synchronously
         * within that launch, instead of racing a real background-thread executor that Compose's
         * test idling can't see.
         */
        fun buildInMemory(context: Context): JuanitOSDatabase {
            val synchronousExecutor = java.util.concurrent.Executor { it.run() }
            return Room.inMemoryDatabaseBuilder(context, JuanitOSDatabase::class.java)
                .addCallback(SeedDefaultCategoriesCallback)
                .setQueryExecutor(synchronousExecutor)
                .setTransactionExecutor(synchronousExecutor)
                .build()
        }
    }
}
