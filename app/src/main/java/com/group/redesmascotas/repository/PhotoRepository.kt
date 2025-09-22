package com.group.redesmascotas.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.group.redesmascotas.database.PhotoDao
import com.group.redesmascotas.database.PhotoEntity
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoRepository(
    private val photoDao: PhotoDao,
    private val context: Context
) {
    companion object {
        private const val TAG = "PhotoRepository"
        private const val PHOTOS_FOLDER = "photos"
    }
    
    // Obtener todas las fotos como Flow
    fun getAllPhotos(): Flow<List<PhotoEntity>> {
        return photoDao.getAllPhotos()
    }
    
    // Guardar foto en almacenamiento interno y base de datos
    suspend fun insertPhoto(uri: Uri, description: String, isFavorite: Boolean, isWithFriends: Boolean): Long? {
        return try {
            // Crear carpeta de fotos si no existe
            val photosDir = File(context.filesDir, PHOTOS_FOLDER)
            if (!photosDir.exists()) {
                photosDir.mkdirs()
            }
            
            // Generar nombre único para el archivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val extension = getFileExtension(uri)
            val fileName = "${description.replace("[^a-zA-Z0-9]".toRegex(), "_")}_$timestamp.$extension"
            val destinationFile = File(photosDir, fileName)
            
            // Copiar archivo al almacenamiento interno
            copyPhotoToInternalStorage(uri, destinationFile)
            
            // Crear entidad y guardar en base de datos
            val photoEntity = PhotoEntity(
                description = description,
                internalPath = destinationFile.absolutePath,
                isFavorite = isFavorite,
                isWithFriends = isWithFriends
            )
            
            val id = photoDao.insertPhoto(photoEntity)
            Log.d(TAG, "Foto guardada con ID: $id")
            id
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar foto", e)
            null
        }
    }
    
    // Actualizar detalles de la foto
    suspend fun updatePhotoDetails(id: Long, description: String, isFavorite: Boolean, isWithFriends: Boolean) {
        try {
            photoDao.updatePhotoDetails(id, description, isFavorite, isWithFriends)
            Log.d(TAG, "Foto actualizada con ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar foto", e)
            throw e
        }
    }
    
    // Actualizar estado de favorito
    suspend fun updatePhotoFavorite(id: Long, isFavorite: Boolean) {
        try {
            photoDao.updatePhotoFavorite(id, isFavorite)
            Log.d(TAG, "Estado de favorito actualizado para foto ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar favorito", e)
            throw e
        }
    }
    
    // Eliminar foto (archivo y registro de BD)
    suspend fun deletePhoto(photo: PhotoEntity): Result<Unit> {
        return try {
            // Eliminar archivo físico si existe
            val file = File(photo.internalPath)
            if (file.exists()) {
                file.delete()
                Log.d(TAG, "Archivo eliminado: ${photo.internalPath}")
            }
            
            // Eliminar de la base de datos
            photoDao.deletePhoto(photo)
            Log.d(TAG, "Foto eliminada de BD con ID: ${photo.id}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar foto", e)
            Result.failure(e)
        }
    }
    
    // Obtener conteo de fotos
    suspend fun getPhotosCount(): Int {
        return photoDao.getPhotosCount()
    }
    
    // Funciones privadas de utilidad
    private fun copyPhotoToInternalStorage(sourceUri: Uri, destinationFile: File) {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IOException("No se pudo abrir el archivo de origen")
    }
    
    private fun getFileExtension(uri: Uri): String {
        return context.contentResolver.getType(uri)?.let { mimeType ->
            when {
                mimeType.contains("jpeg") || mimeType.contains("jpg") -> "jpg"
                mimeType.contains("png") -> "png"
                mimeType.contains("webp") -> "webp"
                mimeType.contains("gif") -> "gif"
                else -> "jpg" // Por defecto
            }
        } ?: "jpg"
    }
}