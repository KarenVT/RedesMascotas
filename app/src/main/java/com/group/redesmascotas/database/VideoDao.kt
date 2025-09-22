package com.group.redesmascotas.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    
    @Query("SELECT * FROM videos ORDER BY date_added DESC")
    fun getAllVideos(): Flow<List<VideoEntity>>
    
    @Query("SELECT * FROM videos WHERE id = :id")
    suspend fun getVideoById(id: Long): VideoEntity?
    
    @Insert
    suspend fun insertVideo(video: VideoEntity): Long
    
    @Update
    suspend fun updateVideo(video: VideoEntity)
    
    @Delete
    suspend fun deleteVideo(video: VideoEntity)
    
    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideoById(id: Long)
    
    @Query("SELECT COUNT(*) FROM videos")
    suspend fun getVideosCount(): Int
    
    @Query("UPDATE videos SET name = :newName WHERE id = :id")
    suspend fun updateVideoName(id: Long, newName: String)
    
    @Query("UPDATE videos SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateVideoFavorite(id: Long, isFavorite: Boolean)
}
