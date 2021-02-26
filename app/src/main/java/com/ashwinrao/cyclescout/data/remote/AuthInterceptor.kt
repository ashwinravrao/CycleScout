package com.ashwinrao.cyclescout.data.remote

import android.content.Context
import com.ashwinrao.cyclescout.R
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context): Interceptor {

    // Intercepts outgoing calls to append API key query param
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val url = request.url().newBuilder().addQueryParameter("key", context.getString(R.string.places_key)).build()
        request = request.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}