package com.example.app_de_gestion_de_horarios_movil.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.app_de_gestion_de_horarios_movil.data.local.converter.Converters
import com.example.app_de_gestion_de_horarios_movil.data.local.dao.TaskDao
import com.example.app_de_gestion_de_horarios_movil.data.local.entity.TaskEntity


// Lista todas las entidades (tablas) que tendrá tu DB
@Database(
    entities = [TaskEntity::class],
    version = 3,
    exportSchema = false // Ponlo en true luego si quieres versionar esquemas SQL
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        // Definición de la Migración de la versión 1 a la 2
        // NUEVA MIGRACIÓN 2 -> 3
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregamos la columna 'type' con valor por defecto 'TASK'
                // para que los registros existentes se traten como tareas normales.
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN type TEXT NOT NULL DEFAULT 'TASK'"
                )
            }
        }
    }
}