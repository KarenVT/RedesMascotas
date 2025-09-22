package com.group.redesmascotas.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "internal_path")
    val internalPath: String, // Ruta del archivo en almacenamiento interno
    
    @ColumnInfo(name = "duration")
    val duration: String,
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long,
    
    @ColumnInfo(name = "date_added")
    val dateAdded: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
