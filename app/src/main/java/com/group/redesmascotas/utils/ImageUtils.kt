package com.group.redesmascotas.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {
    
    private const val PROFILE_IMAGES_FOLDER = "profile_images"
    private const val MAX_IMAGE_SIZE = 800 // Tamaño máximo en píxeles
    
    /**
     * Guarda una imagen desde URI al almacenamiento interno
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen seleccionada
     * @return Ruta del archivo guardado o null si hay error
     */
    fun saveImageToInternalStorage(context: Context, imageUri: Uri): String? {
        return try {
            // Crear carpeta para imágenes de perfil si no existe
            val profileImagesDir = File(context.filesDir, PROFILE_IMAGES_FOLDER)
            if (!profileImagesDir.exists()) {
                profileImagesDir.mkdirs()
            }
            
            // Generar nombre único para la imagen
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "profile_$timestamp.jpg"
            val imageFile = File(profileImagesDir, fileName)
            
            // Leer y procesar la imagen
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // Redimensionar imagen si es muy grande
            val resizedBitmap = resizeImage(originalBitmap, MAX_IMAGE_SIZE)
            
            // Guardar imagen comprimida
            val outputStream = FileOutputStream(imageFile)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            
            // Liberar memoria
            if (resizedBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()
            
            imageFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Carga una imagen desde el almacenamiento interno
     * @param imagePath Ruta de la imagen
     * @return Bitmap de la imagen o null si hay error
     */
    fun loadImageFromInternalStorage(imagePath: String): Bitmap? {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(imagePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Elimina una imagen del almacenamiento interno
     * @param imagePath Ruta de la imagen a eliminar
     * @return true si se eliminó correctamente
     */
    fun deleteImageFromInternalStorage(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            if (file.exists()) {
                file.delete()
            } else {
                true // Ya no existe
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Redimensiona una imagen manteniendo la proporción
     * @param bitmap Imagen original
     * @param maxSize Tamaño máximo en píxeles (ancho o alto)
     * @return Imagen redimensionada
     */
    private fun resizeImage(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }
        
        val ratio = if (width > height) {
            maxSize.toFloat() / width
        } else {
            maxSize.toFloat() / height
        }
        
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Verifica si un archivo de imagen existe
     * @param imagePath Ruta de la imagen
     * @return true si existe
     */
    fun imageExists(imagePath: String): Boolean {
        return try {
            val file = File(imagePath)
            file.exists() && file.isFile()
        } catch (e: Exception) {
            false
        }
    }
}
