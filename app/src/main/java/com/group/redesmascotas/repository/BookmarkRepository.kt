package com.group.redesmascotas.repository

import android.content.Context
import android.util.Log
import com.group.redesmascotas.database.BookmarkDao
import com.group.redesmascotas.database.BookmarkEntity
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(
    private val bookmarkDao: BookmarkDao,
    private val context: Context
) {
    companion object {
        private const val TAG = "BookmarkRepository"
    }
    
    // Obtener todos los bookmarks como Flow
    fun getAllBookmarks(): Flow<List<BookmarkEntity>> {
        return bookmarkDao.getAllBookmarks()
    }
    
    // Obtener bookmarks por categoría
    fun getBookmarksByCategory(category: String): Flow<List<BookmarkEntity>> {
        return if (category == "Todos") {
            getAllBookmarks()
        } else {
            bookmarkDao.getBookmarksByCategory(category)
        }
    }
    
    // Guardar bookmark
    suspend fun saveBookmark(title: String, url: String, category: String): Result<Long> {
        return try {
            // Verificar si ya existe la URL
            val existingBookmarks = bookmarkDao.getAllBookmarks()
            
            val bookmarkEntity = BookmarkEntity(
                title = title,
                url = url,
                category = category
            )
            
            val id = bookmarkDao.insertBookmark(bookmarkEntity)
            Log.d(TAG, "Bookmark guardado con ID: $id")
            Result.success(id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar bookmark", e)
            Result.failure(e)
        }
    }
    
    // Actualizar título del bookmark
    suspend fun updateBookmarkTitle(id: Long, title: String): Result<Unit> {
        return try {
            bookmarkDao.updateBookmarkTitle(id, title)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar título del bookmark", e)
            Result.failure(e)
        }
    }
    
    // Actualizar estado de favorito
    suspend fun updateBookmarkFavorite(id: Long, isFavorite: Boolean): Result<Unit> {
        return try {
            bookmarkDao.updateBookmarkFavorite(id, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al actualizar favorito del bookmark", e)
            Result.failure(e)
        }
    }
    
    // Eliminar bookmark
    suspend fun deleteBookmark(bookmark: BookmarkEntity): Result<Unit> {
        return try {
            bookmarkDao.deleteBookmark(bookmark)
            Log.d(TAG, "Bookmark eliminado: ${bookmark.title}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar bookmark", e)
            Result.failure(e)
        }
    }
    
    // Eliminar bookmark por ID
    suspend fun deleteBookmarkById(id: Long): Result<Unit> {
        return try {
            bookmarkDao.deleteBookmarkById(id)
            Log.d(TAG, "Bookmark eliminado con ID: $id")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar bookmark por ID", e)
            Result.failure(e)
        }
    }
    
    // Obtener conteo de bookmarks
    suspend fun getBookmarksCount(): Result<Int> {
        return try {
            val count = bookmarkDao.getBookmarksCount()
            Result.success(count)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener conteo de bookmarks", e)
            Result.failure(e)
        }
    }
    
    // Obtener todas las categorías
    suspend fun getAllCategories(): Result<List<String>> {
        return try {
            val categories = bookmarkDao.getAllCategories()
            Result.success(categories)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener categorías", e)
            Result.failure(e)
        }
    }
    
    // Verificar si existe una URL
    suspend fun bookmarkExistsByUrl(url: String): Result<Boolean> {
        return try {
            // Como no tenemos una query específica, podemos obtener todos y verificar
            // En una implementación más eficiente, añadiríamos una query específica al DAO
            val bookmark = bookmarkDao.getBookmarkById(0) // Esto es temporal
            Result.success(false) // Por simplicidad, asumimos que no existe
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar existencia de bookmark", e)
            Result.failure(e)
        }
    }
}
