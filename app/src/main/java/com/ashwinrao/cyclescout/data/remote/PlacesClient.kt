package com.ashwinrao.cyclescout.data.remote

import android.content.Context
import com.ashwinrao.cyclescout.BuildConfig
import com.ashwinrao.cyclescout.R
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    factory { AuthInterceptor(androidContext()) }
    factory { ConnectivityInterceptor(androidContext()) }
    factory { HttpLoggingInterceptor() }
    factory { provideOkHttpClient(get(), get(), get()) }
    factory { providePlacesApi(get()) }
    single { provideRetrofit(androidContext(), get()) }
}

fun provideRetrofit(context: Context, okHttpClient: OkHttpClient): Retrofit =
    Retrofit.Builder().baseUrl(context.getString(R.string.places_url)).client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create()).build()

fun provideOkHttpClient(
    authInterceptor: AuthInterceptor,
    connectivityInterceptor: ConnectivityInterceptor,
    loggingInterceptor: HttpLoggingInterceptor
): OkHttpClient =
    OkHttpClient().newBuilder()
        .addInterceptor(authInterceptor)
        .addInterceptor(connectivityInterceptor)
        .addInterceptor(loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)).build()

fun providePlacesApi(retrofit: Retrofit): PlacesApi = retrofit.create(PlacesApi::class.java)
