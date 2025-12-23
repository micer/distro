package eu.micer.distro.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM app_configs ORDER BY name ASC")
    fun getAllApps(): Flow<List<AppConfig>>
    
    @Query("SELECT * FROM app_configs WHERE id = :id")
    suspend fun getAppById(id: Long): AppConfig?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppConfig): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppConfig>)
    
    @Update
    suspend fun updateApp(app: AppConfig)
    
    @Delete
    suspend fun deleteApp(app: AppConfig)
}
