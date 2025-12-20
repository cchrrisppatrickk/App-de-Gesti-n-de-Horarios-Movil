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
    version = 2,
    exportSchema = false // Ponlo en true luego si quieres versionar esquemas SQL
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        // Definición de la Migración de la versión 1 a la 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Comando SQL para agregar la columna nueva.
                // TEXT NOT NULL DEFAULT '' significa que las tareas viejas tendrán una lista vacía.
                database.execSQL(
                    "ALTER TABLE tasks ADD COLUMN active_alerts TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}