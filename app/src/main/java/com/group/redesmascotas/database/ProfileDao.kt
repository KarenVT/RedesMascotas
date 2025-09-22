package com.group.redesmascotas.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    
    @Query("SELECT * FROM profile WHERE id = 1")
    fun getProfile(): Flow<ProfileEntity?>
    
    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun getProfileOnce(): ProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: ProfileEntity)
    
    @Update
    suspend fun updateProfile(profile: ProfileEntity)
    
    @Query("UPDATE profile SET pet_name = :petName WHERE id = 1")
    suspend fun updatePetName(petName: String)
    
    @Query("UPDATE profile SET pet_breed = :petBreed WHERE id = 1")
    suspend fun updatePetBreed(petBreed: String)
    
    @Query("UPDATE profile SET pet_age = :petAge WHERE id = 1")
    suspend fun updatePetAge(petAge: String)
    
    @Query("UPDATE profile SET owner_name = :ownerName WHERE id = 1")
    suspend fun updateOwnerName(ownerName: String)
    
    @Query("UPDATE profile SET interests = :interests WHERE id = 1")
    suspend fun updateInterests(interests: String)
    
    @Query("UPDATE profile SET profile_image_path = :imagePath WHERE id = 1")
    suspend fun updateProfileImage(imagePath: String)
    
    @Query("UPDATE profile SET last_updated = :timestamp WHERE id = 1")
    suspend fun updateTimestamp(timestamp: Long)
    
    @Query("DELETE FROM profile WHERE id = 1")
    suspend fun clearProfile()
    
    @Query("SELECT COUNT(*) > 0 FROM profile WHERE id = 1 AND (pet_name != '' OR pet_breed != '' OR pet_age != '' OR owner_name != '' OR interests != '')")
    suspend fun hasProfileData(): Boolean
}
