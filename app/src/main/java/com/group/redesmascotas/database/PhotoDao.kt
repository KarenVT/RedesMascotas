package com.group.redesmascotas.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    
    @Query("SELECT * FROM photos ORDER BY date_added DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>
    
    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): PhotoEntity?
    
    @Insert
    suspend fun insertPhoto(photo: PhotoEntity): Long
    
    @Update
    suspend fun updatePhoto(photo: PhotoEntity)
    
    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)
    
    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)
    
    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getPhotosCount(): Int
    
    @Query("UPDATE photos SET description = :description, is_favorite = :isFavorite, is_with_friends = :isWithFriends WHERE id = :id")
    suspend fun updatePhotoDetails(id: Long, description: String, isFavorite: Boolean, isWithFriends: Boolean)
    
    @Query("UPDATE photos SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updatePhotoFavorite(id: Long, isFavorite: Boolean)
}