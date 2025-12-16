package com.example.app_de_gestion_de_horarios_movil.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.app_de_gestion_de_horarios_movil.data.local.dao.TaskDao
import com.example.app_de_gestion_de_horarios_movil.data.local.entity.TaskEntity


// Lista todas las entidades (tablas) que tendrá tu DB
@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false // Ponlo en true luego si quieres versionar esquemas SQL
)
abstract class AppDatabase : RoomDatabase() {

    // Expone el DAO para que el Repositorio lo pueda usar
    abstract fun taskDao(): TaskDao

    // NOTA: Normalmente aquí iría un bloque "companion object" para crear la instancia,
    // pero como usas una estructura moderna con carpeta 'di' (Dependency Injection),
    // crearemos la instancia allí (en AppModule.kt o DatabaseModule.kt).
}