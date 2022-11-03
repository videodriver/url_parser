package com.web.url_saver.db

import android.app.Application
import android.util.Log
import com.web.url_maker.util.UrlStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class UrlDataStore(
    app: Application
) {
    private val db = UrlDatabase(app)

    suspend fun putAdminEntity(admin: Boolean) = db.dao().insert(UrlData(admin = admin, user = ""))

    fun putUserEntity(user: String) = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
        if (getUserStatus().isEmpty()
            && "https://funbet.website/fb.php" !in user
            && "https://trident.website" !in user
        ) {
            Log.d("UrlDataStore", "User saved: $user")
            val current = db.dao().getEntity()
            db.dao().insert(current.copy(user = user))
        }
    }

    suspend fun getStatus(): UrlStatus? {
        if (isEmpty()) return null
        if (getAdminStatus()) return UrlStatus.Admin
        if (getUserStatus().isEmpty()) return UrlStatus.Empty
        return UrlStatus.User(getUserStatus())
    }

    private suspend fun isEmpty(): Boolean = db.dao().count() == 0

    private suspend fun getAdminStatus(): Boolean = db.dao().getAdmin()

    private suspend fun getUserStatus(): String = db.dao().getUser()
}