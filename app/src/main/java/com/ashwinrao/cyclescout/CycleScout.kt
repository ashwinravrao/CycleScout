package com.ashwinrao.cyclescout

import android.app.Application
import com.ashwinrao.cyclescout.data.remote.networkModule
import com.ashwinrao.cyclescout.data.repository.repoModule
import com.ashwinrao.cyclescout.viewmodel.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class CycleScout : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CycleScout)
            modules(listOf(networkModule, repoModule, viewModelModule))
        }
    }
}