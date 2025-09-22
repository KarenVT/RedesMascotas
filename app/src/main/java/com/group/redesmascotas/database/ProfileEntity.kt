package com.group.redesmascotas.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val id: Long = 1L, // Solo habrá un perfil, por eso ID fijo
    
    @ColumnInfo(name = "pet_name")
    val petName: String = "",
    
    @ColumnInfo(name = "pet_breed")
    val petBreed: String = "",
    
    @ColumnInfo(name = "pet_age")
    val petAge: String = "",
    
    @ColumnInfo(name = "owner_name")
    val ownerName: String = "",
    
    @ColumnInfo(name = "interests")
    val interests: String = "",
    
    @ColumnInfo(name = "profile_image_path")
    val profileImagePath: String = "", // Ruta de la imagen de perfil si el usuario la cambia
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
)
