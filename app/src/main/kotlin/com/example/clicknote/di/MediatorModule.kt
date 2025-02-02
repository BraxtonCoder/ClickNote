package com.example.clicknote.di

import com.example.clicknote.data.mediator.PurchaseMediatorImpl
import com.example.clicknote.domain.mediator.PurchaseMediator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MediatorModule {
    @Binds
    @Singleton
    abstract fun bindPurchaseMediator(impl: PurchaseMediatorImpl): PurchaseMediator
} 