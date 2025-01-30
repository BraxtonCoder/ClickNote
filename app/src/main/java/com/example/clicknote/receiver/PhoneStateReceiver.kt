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

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PhoneStateReceiverEntryPoint {
    fun userPreferences(): UserPreferencesDataStore
}

class PhoneStateReceiver : BroadcastReceiver() {

    private lateinit var userPreferences: UserPreferencesDataStore

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            PhoneStateReceiverEntryPoint::class.java
        )
        userPreferences = entryPoint.userPreferences()

        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                if (userPreferences.isCallRecordingEnabled()) {
                    startCallRecordingService(context, phoneNumber)
                }
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                if (userPreferences.isCallRecordingEnabled()) {
                    startCallRecordingService(context, phoneNumber)
                }
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                stopCallRecordingService(context)
            }
        }
    }

    private fun startCallRecordingService(context: Context, phoneNumber: String?) {
        val intent = Intent(context, CallRecordingService::class.java).apply {
            putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER, phoneNumber)
        }
        context.startService(intent)
    }

    private fun stopCallRecordingService(context: Context) {
        val intent = Intent(context, CallRecordingService::class.java)
        context.stopService(intent)
    }
} 