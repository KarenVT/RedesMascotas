package com.group.redesmascotas.repository

import android.content.Context
import android.net.Uri
import android.media.MediaMetadataRetriever
import android.util.Log
import com.group.redesmascotas.database.VideoDao
import com.group.redesmascotas.database.VideoEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class VideoRepository(
    private val videoDao: VideoDao,
    private val context: Context
) {
    companion object {
        private const val TAG = "VideoRepository"
        private const val VIDEOS_FOLDER = "videos"
    }
    
    // Obtener todos los videos como Flow
    fun getAllVideos(): Flow<List<VideoEntity>> {
        return videoDao.getAllVideos()
    }
    
    // Guardar video en almacenamiento interno y base de datos
    suspend fun saveVideo(uri: Uri, name: String): Result<Long> {
        return try {
            // Crear carpeta de videos si no existe
            val videosDir = File(context.filesDir, VIDEOS_FOLDER)
            if (!videosDir.exists()) {
                videosDir.mkdirs()
            }
            
            // Generar nombre único para el archivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val extension = getFileExtension(uri)
            val fileName = "${name.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$timestamp.$extension"
            val destinationFile = File(videosDir, fileName)
            
            // Copiar archivo al almacenamiento interno
            copyVideoToInternalStorage(uri, destinationFile)
            
            // Obtener metadatos del video
            val duration = getVideoDuration(destinationFile.absolutePath)
            val fileSize = destinationFile.length()
            
            // Crear entidad y guardar en base de datos
            val videoEntity = VideoEntity(
                name = name,
                internalPath = destinationFile.absolutePath,
                duration = duration,
                fileSize = fileSize
            )
            
            val id = videoDao.insertVideo(videoEntity)
            Log.d(TAG, "Video guardado con ID: $id")
            Result.success(id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar video", e)
            Result.failure(e)
        }
    }
    
    // Actualizar nombre del video
    suspend fun updateVideoName(id: Long, newName: String): Result<Unit> {
        return try {
            videoDao.updateVideoName(id, newName)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar nombre del video", e)
            Result.failure(e)
        }
    }
    
    // Actualizar estado de favorito
    suspend fun updateVideoFavorite(id: Long, isFavorite: Boolean): Result<Unit> {
        return try {
            videoDao.updateVideoFavorite(id, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar favorito del video", e)
            Result.failure(e)
        }
    }
    
    // Eliminar video (archivo y registro de BD)
    suspend fun deleteVideo(video: VideoEntity): Result<Unit> {
        return try {
            // Eliminar archivo físico
            val file = File(video.internalPath)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Archivo eliminado: ${file.absolutePath}")
            }
            
            // Eliminar de la base de datos
            videoDao.deleteVideo(video)
            Log.d(TAG, "Video eliminado de BD con ID: ${video.id}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar video", e)
            Result.failure(e)
        }
    }
    
    // Obtener conteo de videos
    suspend fun getVideosCount(): Int {
        return videoDao.getVideosCount()
    }
    
    // Funciones privadas de utilidad
    private fun copyVideoToInternalStorage(sourceUri: Uri, destinationFile: File) {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("No se pudo abrir el archivo de origen")
    }
    
    private fun getFileExtension(uri: Uri): String {
        return context.contentResolver.getType(uri)?.let { mimeType ->
            when {
                mimeType.contains("mp4") -> "mp4"
                mimeType.contains("avi") -> "avi"
                mimeType.contains("mkv") -> "mkv"
                mimeType.contains("mov") -> "mov"
                else -> "mp4" // Por defecto
            }
        } ?: "mp4"
    }
    
    private fun getVideoDuration(filePath: String): String {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            
            val minutes = (duration / 1000) / 60
            val seconds = (duration / 1000) % 60
            String.format("%02d:%02d", minutes, seconds)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener duración del video", e)
            "00:00"
        }
    }
}
