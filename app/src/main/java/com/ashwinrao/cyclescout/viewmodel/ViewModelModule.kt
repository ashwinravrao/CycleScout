package com.ashwinrao.cyclescout.viewmodel

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    // todo: add: viewModel { DetailViewModel(get()) }
}