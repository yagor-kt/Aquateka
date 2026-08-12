package com.subefu.aquateka.model.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.subefu.aquateka.model.data.entity.ClientEntity
import com.subefu.aquateka.model.data.entity.VisitEntity

@Database(
    entities = [ClientEntity::class, VisitEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
abstract class DataBase: RoomDatabase() {
    abstract fun getDao(): DAO
    companion object{
        @Volatile
        private var INSTANCE: DataBase? = null

        fun getDB(context: Context): DataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DataBase::class.java,
                    "my_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}