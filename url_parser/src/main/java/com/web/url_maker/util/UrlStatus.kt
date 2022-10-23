package com.web.url_maker.util

sealed class UrlStatus {
    data class User(val data: String) : UrlStatus()
    object Admin : UrlStatus()
    object Empty : UrlStatus()
}
