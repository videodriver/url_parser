package com.web.url_maker.parser

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.applinks.AppLinkData
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import com.web.url_maker.util.Constants.AD_GROUP_KEY
import com.web.url_maker.util.Constants.AD_SET_ID_KEY
import com.web.url_maker.util.Constants.AD_SET_KEY
import com.web.url_maker.util.Constants.APPS_FLYER_DEV_KEY
import com.web.url_maker.util.Constants.APPS_FLYER_ID_KEY
import com.web.url_maker.util.Constants.APPS_FLYER_SITE_ID_KEY
import com.web.url_maker.util.Constants.APP_CAMPAIGN_KEY
import com.web.url_maker.util.Constants.CAMPAIGN_ID_KEY
import com.web.url_maker.util.Constants.DEEP_LINK_KEY
import com.web.url_maker.util.Constants.GADID_KEY
import com.web.url_maker.util.Constants.LINK
import com.web.url_maker.util.Constants.ORIG_COST_KEY
import com.web.url_maker.util.Constants.SECURE_GET_PARAMETER
import com.web.url_maker.util.Constants.SECURE_KEY
import com.web.url_maker.util.Constants.SOURCE_KEY
import com.web.url_maker.util.Constants.TIMEZONE_KEY
import com.web.url_maker.util.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UrlParser {
    private var deepLink: String? = null
    private var appsFlyer: MutableMap<String, Any>? = null
    private var observer: DownloadObserver? = null

    fun registerObserver(observer: DownloadObserver) {
        this.observer = observer
    }

    private suspend fun getDeepLink(context: Context): String? {
        delay(1000)
        return suspendCoroutine { continuation ->
            AppLinkData.fetchDeferredAppLinkData(context) { appLinkData ->
                val targetUri = appLinkData?.targetUri?.toString()
                deepLink = targetUri
                observer?.notify(Type.DeepLink(isNull = targetUri == null))
                continuation.resume(targetUri)
            }
        }
    }

    private suspend fun getAppsData(context: Context): MutableMap<String, Any>? {
        delay(1000)
        return suspendCoroutine { continuation ->
            AppsFlyerLib.getInstance().init(
                APPS_FLYER_DEV_KEY,
                object : AppsFlyerConversionListener {
                    override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
                        appsFlyer = p0
                        observer?.notify(Type.AppsFlyer(isNull = p0 == null))
                        Log.d(
                            "UrlParser",
                            "AppsFlyer callback onConversionDataSuccess was triggered ($p0)"
                        )
                        continuation.resume(p0)
                    }

                    override fun onConversionDataFail(p0: String?) {
                        observer?.notify(Type.AppsFlyer(isNull = true))
                        Log.d(
                            "UrlParser",
                            "AppsFlyer callback onConversionDataFail was triggered ($p0)"
                        )
                        continuation.resume(null)
                    }

                    override fun onAppOpenAttribution(p0: MutableMap<String, String>?) = Unit

                    override fun onAttributionFailure(p0: String?) = Unit

                },
                context
            )
            AppsFlyerLib.getInstance().start(context)
        }
    }

    suspend fun create(context: Context): String {
        val googleId = withContext(Dispatchers.IO) {
            AdvertisingIdClient.getAdvertisingIdInfo(context).id.toString()
        }
        deepLink = getDeepLink(context)
        if (deepLink == null) {
            appsFlyer = getAppsData(context)
        }
        return createLink(context, googleId)
    }

    private fun createLink(context: Context, googleId: String): String {
        when {
            deepLink != null -> {
                OneSignal.sendTag(
                    "key2",
                    deepLink?.replace("myapp://", "")?.substringBefore("/")
                )
                Log.d(
                    "UrlParser",
                    "Sent tag ${deepLink?.replace("myapp://", "")?.substringBefore("/")}"
                )
            }
            appsFlyer?.get("campaign") != null -> {
                OneSignal.sendTag(
                    "key2",
                    appsFlyer?.get("campaign").toString().substringBefore("_")
                )
                Log.d(
                    "UrlParser",
                    "Sent tag ${appsFlyer?.get("campaign").toString().substringBefore("_")}"
                )
            }
            appsFlyer?.get("campaign") == null && deepLink == null -> {
                OneSignal.sendTag("key2", "organic")
                Log.d("UrlParser", "Sent tag organic")
            }
        }

        return LINK.toUri().buildUpon().apply {
            appendQueryParameter(SECURE_GET_PARAMETER, SECURE_KEY)
            appendQueryParameter(TIMEZONE_KEY, TimeZone.getDefault().id)
            appendQueryParameter(GADID_KEY, googleId)
            appendQueryParameter(DEEP_LINK_KEY, deepLink.toString())
            appendQueryParameter(
                SOURCE_KEY,
                if (deepLink != null) "deeplink" else appsFlyer?.get("media_source").toString()
            )
            appendQueryParameter(
                APPS_FLYER_ID_KEY, if (deepLink != null) "null" else
                    AppsFlyerLib.getInstance().getAppsFlyerUID(context)
            )
            appendQueryParameter(AD_SET_ID_KEY, appsFlyer?.get("adset_id").toString())
            appendQueryParameter(CAMPAIGN_ID_KEY, appsFlyer?.get("campaign_id").toString())
            appendQueryParameter(APP_CAMPAIGN_KEY, appsFlyer?.get("campaign").toString())
            appendQueryParameter(AD_SET_KEY, appsFlyer?.get("adset").toString())
            appendQueryParameter(AD_GROUP_KEY, appsFlyer?.get("adgroup").toString())
            appendQueryParameter(ORIG_COST_KEY, appsFlyer?.get("orig_cost").toString())
            appendQueryParameter(APPS_FLYER_SITE_ID_KEY, appsFlyer?.get("af_siteid").toString())
        }.toString()
    }
}