package eu.micer.distro.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {
    
    val allApps: Flow<List<AppConfig>> = appDao.getAllApps()
    
    suspend fun getAppById(id: Long): AppConfig? {
        return appDao.getAppById(id)
    }
    
    suspend fun insertApp(app: AppConfig): Long {
        return appDao.insertApp(app)
    }
    
    suspend fun insertApps(apps: List<AppConfig>) {
        appDao.insertApps(apps)
    }
    
    suspend fun updateApp(app: AppConfig) {
        appDao.updateApp(app)
    }
    
    suspend fun deleteApp(app: AppConfig) {
        appDao.deleteApp(app)
    }
}
