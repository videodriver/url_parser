package com.web.url_maker.parser

import com.web.url_maker.util.Type

interface DownloadObserver {
    fun notify(type: Type)
}