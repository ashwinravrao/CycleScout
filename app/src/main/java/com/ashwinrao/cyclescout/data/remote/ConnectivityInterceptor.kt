package com.ashwinrao.cyclescout.data.remote

import android.content.Context
import android.net.ConnectivityManager
import com.ashwinrao.cyclescout.R
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ConnectivityInterceptor(private val context: Context) : Interceptor {

    // Ensures an active network connection before allowing outgoing call
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isNetworkAvailable()) throw IOException(context.getString(R.string.exception_no_internet))
        return chain.proceed(chain.request())
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager!!.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
    }

}