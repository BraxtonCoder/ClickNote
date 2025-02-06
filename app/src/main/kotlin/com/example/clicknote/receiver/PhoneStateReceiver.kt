package com.example.clicknote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.recording.CallRecordingService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PhoneStateReceiverEntryPoint {
    fun userPreferences(): UserPreferencesDataStore
}

class PhoneStateReceiver : BroadcastReceiver() {

    private lateinit var userPreferences: UserPreferencesDataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PhoneStateReceiverEntryPoint::class.java
        )
        userPreferences = entryPoint.userPreferences()

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        scope.launch {
            val isCallRecordingEnabled = userPreferences.callRecordingEnabled.first()
            
            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    if (isCallRecordingEnabled) {
                        startCallRecordingService(context, phoneNumber)
                    }
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (isCallRecordingEnabled) {
                        startCallRecordingService(context, phoneNumber)
                    }
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    stopCallRecordingService(context)
                }
            }
        }
    }

    private fun startCallRecordingService(context: Context, phoneNumber: String?) {
        val intent = Intent(context, CallRecordingService::class.java).apply {
            action = CallRecordingService.ACTION_START_RECORDING
            putExtra(CallRecordingService.EXTRA_PHONE_NUMBER, phoneNumber)
        }
        context.startService(intent)
    }

    private fun stopCallRecordingService(context: Context) {
        val intent = Intent(context, CallRecordingService::class.java).apply {
            action = CallRecordingService.ACTION_STOP_RECORDING
        }
        context.startService(intent)
    }
} 