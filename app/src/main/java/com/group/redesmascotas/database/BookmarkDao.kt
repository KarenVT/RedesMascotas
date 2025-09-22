package com.group.redesmascotas.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    
    @Query("SELECT * FROM bookmarks ORDER BY date_added DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE category = :category ORDER BY date_added DESC")
    fun getBookmarksByCategory(category: String): Flow<List<BookmarkEntity>>
    
    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getBookmarkById(id: Long): BookmarkEntity?
    
    @Insert
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long
    
    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)
    
    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)
    
    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)
    
    @Query("SELECT COUNT(*) FROM bookmarks")
    suspend fun getBookmarksCount(): Int
    
    @Query("UPDATE bookmarks SET title = :title WHERE id = :id")
    suspend fun updateBookmarkTitle(id: Long, title: String)
    
    @Query("UPDATE bookmarks SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateBookmarkFavorite(id: Long, isFavorite: Boolean)
    
    @Query("SELECT DISTINCT category FROM bookmarks ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
}
