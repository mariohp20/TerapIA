package com.pe.terapia.data.local.room

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@androidx.room.Database(entities = [CitaEntity::class, NotificacionEntity::class, AgendaPsicologoEntity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun citaDao(): CitaDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun agendaPsicologoDao(): AgendaPsicologoDao

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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE citas_locales ADD COLUMN sedeLatitud REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE citas_locales ADD COLUMN sedeLongitud REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE citas_locales ADD COLUMN pagado INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS agenda_psicologo_local (psicologoId TEXT NOT NULL, fecha TEXT NOT NULL, citaId TEXT NOT NULL, pacienteId TEXT NOT NULL, hora TEXT NOT NULL, nombrePaciente TEXT NOT NULL, inicioEpochMillis INTEGER NOT NULL, PRIMARY KEY(psicologoId, fecha, citaId))")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_agenda_psicologo_local_psicologoId_fecha ON agenda_psicologo_local(psicologoId, fecha)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "terapia_room_db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
