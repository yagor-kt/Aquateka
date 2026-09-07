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
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 2, to = 3)]
)
abstract class DataBase: RoomDatabase() {
    abstract fun getDao(): DAO
    companion object{
        @Volatile
        private var INSTANCE: DataBase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // --- 1. МИГРАЦИЯ ТАБЛИЦЫ Client ---

                // Создаем новую таблицу Client с правильным именем колонки client_id и period_month
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `Client_new` (
                `client_id` INTEGER PRIMARY KEY AUTOINCREMENT, 
                `name` TEXT NOT NULL, 
                `phone` TEXT NOT NULL, 
                `address` TEXT, 
                `latitude` REAL NOT NULL, 
                `longitude` REAL NOT NULL, 
                `period_month` INTEGER, 
                `comment` TEXT NOT NULL
            )
        """.trimIndent())

                // Переносим данные (в старой таблице были clientId и periodMonth)
                db.execSQL("""
            INSERT INTO `Client_new` (
                client_id, name, phone, address, latitude, longitude, period_month, comment
            )
            SELECT 
                clientId, name, phone, address, latitude, longitude, periodMonth, comment
            FROM `Client`
        """.trimIndent())

                // Пересоздаем индекс для Client
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_Client_client_id` ON `Client_new` (`client_id`)")

                // Удаляем старую таблицу Client и переименовываем новую
                db.execSQL("DROP TABLE `Client`")
                db.execSQL("ALTER TABLE `Client_new` RENAME TO `Client`")


                // --- 2. МИГРАЦИЯ ТАБЛИЦЫ Visit ---

                // Создаем новую таблицу Visit с обновленными именами колонок и FOREIGN KEY
                db.execSQL("""
            CREATE TABLE IF NOT EXISTS `Visit_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT, 
                `client_id` INTEGER NOT NULL, 
                `address` TEXT, 
                `latitude` REAL NOT NULL, 
                `longitude` REAL NOT NULL, 
                `planned_month` INTEGER NOT NULL, 
                `planned_year` INTEGER NOT NULL, 
                `actual_date` INTEGER NOT NULL, 
                `status` TEXT NOT NULL, 
                `work_type` TEXT NOT NULL, 
                `price` INTEGER NOT NULL, 
                `parts` TEXT NOT NULL, 
                `period` INTEGER NOT NULL, 
                `comment` TEXT NOT NULL,
                FOREIGN KEY(`client_id`) REFERENCES `Client`(`client_id`) ON UPDATE CASCADE ON DELETE CASCADE 
            )
        """.trimIndent())

                // Переносим данные из старой таблицы Visit, маппим старые camelCase имена на новые snake_case
                db.execSQL("""
            INSERT INTO `Visit_new` (
                id, client_id, address, latitude, longitude, 
                planned_month, planned_year, actual_date, 
                status, work_type, price, parts, period, comment
            )
            SELECT 
                id, clientId, address, latitude, longitude, 
                plannedMonth, plannedYear, actualDate, 
                status, workType, price, parts, period, comment
            FROM `Visit`
        """.trimIndent())

                // Восстанавливаем составной индекс, указанный в вашей аннотации @Entity(indices = [...])
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_Visit_id_client_id` ON `Visit_new` (`id`, `client_id`)")
                // Отдельный индекс на foreign key (создается Room автоматически из-за ColumnInfo(index = true))
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_Visit_client_id` ON `Visit_new` (`client_id`)")

                // Удаляем старую таблицу Visit и переименовываем новую
                db.execSQL("DROP TABLE `Visit`")
                db.execSQL("ALTER TABLE `Visit_new` RENAME TO `Visit`")
            }
        }

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