package com.subefu.aquateka.model.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Dao
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.subefu.aquateka.model.data.entity.ClientEntity
import com.subefu.aquateka.model.data.entity.VisitEntity

@Database(
    entities = [ClientEntity::class, VisitEntity::class],
    version = 4,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 3, to = 4)]
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
                    "myDatabase.db"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}