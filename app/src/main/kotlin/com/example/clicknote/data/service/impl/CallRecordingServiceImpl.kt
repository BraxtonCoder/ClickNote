package com.example.clicknote.data.service.impl

import android.content.Context
import com.example.clicknote.domain.service.CallRecordingService
import com.example.clicknote.domain.repository.CallRecordingRepository
import com.example.clicknote.di.qualifiers.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecordingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: CallRecordingRepository,
    @ApplicationScope private val scope: CoroutineScope
) : CallRecordingService {
    // ... rest of the implementation
} 