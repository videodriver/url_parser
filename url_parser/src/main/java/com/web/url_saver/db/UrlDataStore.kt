package com.web.url_saver.db

import android.app.Application
import com.web.url_maker.util.UrlStatus

class UrlDataStore(
    app: Application
) {
    private val db = UrlDatabase(app)

    suspend fun putAdminEntity(admin: Boolean) = db.dao().insert(UrlData(admin = admin, user = ""))

    suspend fun putUserEntity(user: String) {
        if (getUserStatus().isEmpty()
            && "https://jokersun.online/ccc.php" !in user
            && "https://trident.website" !in user
        ) {
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