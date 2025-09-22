package com.group.redesmascotas.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [VideoEntity::class, PhotoEntity::class, ProfileEntity::class, BookmarkEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AllDatabase : RoomDatabase() {
    
    abstract fun videoDao(): VideoDao
    abstract fun photoDao(): PhotoDao
    abstract fun profileDao(): ProfileDao
    abstract fun bookmarkDao(): BookmarkDao
    
    companion object {
        @Volatile
        private var INSTANCE: AllDatabase? = null
        
        fun getDatabase(context: Context): AllDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AllDatabase::class.java,
                    "media_database"
                )
                .fallbackToDestructiveMigration() // Room crea las tablas automáticamente
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
