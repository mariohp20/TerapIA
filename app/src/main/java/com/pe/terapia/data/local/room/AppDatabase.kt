package com.pe.terapia.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@androidx.room.Database(entities = [CitaEntity::class, NotificacionEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citaDao(): CitaDao
    abstract fun notificacionDao(): NotificacionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE citas_locales ADD COLUMN estado TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE citas_locales ADD COLUMN estadoPago TEXT NOT NULL DEFAULT 'PENDIENTE'")
                // La versión anterior guardaba una hora estimada (ahora + 24 h), no la hora real.
                // Se descarta ese caché local incorrecto; se reconstruye en la siguiente lectura online.
                db.execSQL("DELETE FROM citas_locales")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS notificaciones_locales (" +
                        "id TEXT NOT NULL, usuarioId TEXT NOT NULL, titulo TEXT NOT NULL, " +
                        "mensaje TEXT NOT NULL, tipo TEXT NOT NULL, fechaCreacionMillis INTEGER NOT NULL, " +
                        "leida INTEGER NOT NULL, citaId TEXT, alertaMostrada INTEGER NOT NULL, PRIMARY KEY(id))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_notificaciones_locales_usuarioId_fechaCreacionMillis " +
                        "ON notificaciones_locales(usuarioId, fechaCreacionMillis)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "terapia_room_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
