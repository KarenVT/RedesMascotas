package com.group.redesmascotas.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.group.redesmascotas.database.ProfileDao
import com.group.redesmascotas.database.ProfileEntity
import com.group.redesmascotas.utils.ImageUtils
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val profileDao: ProfileDao,
    private val context: Context
) {
    companion object {
        private const val TAG = "ProfileRepository"
    }
    
    // Obtener perfil como Flow
    fun getProfile(): Flow<ProfileEntity?> {
        return profileDao.getProfile()
    }
    
    // Obtener perfil una vez (sin observar cambios)
    suspend fun getProfileOnce(): ProfileEntity? {
        return profileDao.getProfileOnce()
    }
    
    // Crear o actualizar perfil completo
    suspend fun saveProfile(
        petName: String,
        petBreed: String,
        petAge: String,
        ownerName: String,
        interests: String,
        profileImagePath: String = ""
    ): Result<Unit> {
        return try {
            val profile = ProfileEntity(
                id = 1L,
                petName = petName.trim(),
                petBreed = petBreed.trim(),
                petAge = petAge.trim(),
                ownerName = ownerName.trim(),
                interests = interests.trim(),
                profileImagePath = profileImagePath,
                lastUpdated = System.currentTimeMillis()
            )
            
            profileDao.insertOrUpdateProfile(profile)
            Log.d(TAG, "Perfil guardado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar perfil", e)
            Result.failure(e)
        }
    }
    
    // Actualizar solo el nombre de la mascota
    suspend fun updatePetName(petName: String): Result<Unit> {
        return try {
            profileDao.updatePetName(petName.trim())
            profileDao.updateTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Nombre de mascota actualizado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar nombre de mascota", e)
            Result.failure(e)
        }
    }
    
    // Actualizar solo la raza
    suspend fun updatePetBreed(petBreed: String): Result<Unit> {
        return try {
            profileDao.updatePetBreed(petBreed.trim())
            profileDao.updateTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Raza actualizada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar raza", e)
            Result.failure(e)
        }
    }
    
    // Actualizar solo la edad
    suspend fun updatePetAge(petAge: String): Result<Unit> {
        return try {
            profileDao.updatePetAge(petAge.trim())
            profileDao.updateTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Edad actualizada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar edad", e)
            Result.failure(e)
        }
    }
    
    // Actualizar solo el nombre del dueño
    suspend fun updateOwnerName(ownerName: String): Result<Unit> {
        return try {
            profileDao.updateOwnerName(ownerName.trim())
            profileDao.updateTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Nombre del dueño actualizado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar nombre del dueño", e)
            Result.failure(e)
        }
    }
    
    // Actualizar solo los intereses
    suspend fun updateInterests(interests: String): Result<Unit> {
        return try {
            profileDao.updateInterests(interests.trim())
            profileDao.updateTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Intereses actualizados")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar intereses", e)
            Result.failure(e)
        }
    }
    
    // Actualizar imagen de perfil desde URI
    suspend fun updateProfileImage(imageUri: Uri): Result<String> {
        return try {
            // Eliminar imagen anterior si existe
            val currentProfile = profileDao.getProfileOnce()
            if (currentProfile != null && currentProfile.profileImagePath.isNotEmpty()) {
                ImageUtils.deleteImageFromInternalStorage(currentProfile.profileImagePath)
            }
            
            // Guardar nueva imagen
            val savedImagePath = ImageUtils.saveImageToInternalStorage(context, imageUri)
            if (savedImagePath != null) {
                profileDao.updateProfileImage(savedImagePath)
                profileDao.updateTimestamp(System.currentTimeMillis())
                Log.d(TAG, "Imagen de perfil actualizada: $savedImagePath")
                Result.success(savedImagePath)
            } else {
                Log.e(TAG, "Error al guardar imagen de perfil")
                Result.failure(Exception("Error al guardar imagen"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar imagen de perfil", e)
            Result.failure(e)
        }
    }
    
    // Actualizar imagen de perfil con ruta específica
    suspend fun updateProfileImagePath(imagePath: String): Result<Unit> {
        return try {
            profileDao.updateProfileImage(imagePath)
            profileDao.updateTimestamp(System.currentTimeMillis())
            Log.d(TAG, "Ruta de imagen de perfil actualizada")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar ruta de imagen de perfil", e)
            Result.failure(e)
        }
    }
    
    // Verificar si hay datos en el perfil
    suspend fun hasProfileData(): Boolean {
        return try {
            profileDao.hasProfileData()
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar datos del perfil", e)
            false
        }
    }
    
    // Limpiar perfil completamente
    suspend fun clearProfile(): Result<Unit> {
        return try {
            profileDao.clearProfile()
            Log.d(TAG, "Perfil limpiado")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al limpiar perfil", e)
            Result.failure(e)
        }
    }
}
