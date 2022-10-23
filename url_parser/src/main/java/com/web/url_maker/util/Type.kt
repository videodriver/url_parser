package com.web.url_maker.util

sealed class Type(val isEmpty: Boolean) {
    data class DeepLink(val isNull: Boolean) : Type(isNull)
    data class AppsFlyer(val isNull: Boolean) : Type(isNull)
}