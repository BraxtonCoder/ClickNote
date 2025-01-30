package com.example.clicknote

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.*
import com.example.clicknote.service.VolumeButtonService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class RecordingFlowTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var audioManager: AudioManager

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = ApplicationProvider.getApplicationContext()
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Start the VolumeButtonService
        val serviceIntent = Intent(context, VolumeButtonService::class.java)
        context.startService(serviceIntent)
    }

    @Test
    fun testVolumeButtonRecording() {
        // Initial delay to let service start
        SystemClock.sleep(1000)

        // Press volume up
        device.pressKeyCode(24) // KEYCODE_VOLUME_UP
        SystemClock.sleep(500)

        // Press volume down within 750ms
        device.pressKeyCode(25) // KEYCODE_VOLUME_DOWN
        SystemClock.sleep(500)

        // Verify recording started (you'll need to implement verification logic)
        // For example, check if a notification appears or if a file is created

        // Wait a bit
        SystemClock.sleep(2000)

        // Press volume up again
        device.pressKeyCode(24)
        SystemClock.sleep(500)

        // Press volume down within 750ms to stop recording
        device.pressKeyCode(25)
        SystemClock.sleep(500)

        // Verify recording stopped (implement verification logic)
    }

    @Test
    fun testSingleVolumeButtonBehavior() {
        // Initial delay
        SystemClock.sleep(1000)

        // Get initial volume
        val initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Press volume up once
        device.pressKeyCode(24)
        SystemClock.sleep(1000)

        // Verify volume increased
        val newVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        assert(newVolume > initialVolume)

        // Reset volume
        while (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > initialVolume) {
            device.pressKeyCode(25)
            SystemClock.sleep(100)
        }
    }

    @Test
    fun testDelayedSecondButtonPress() {
        // Initial delay
        SystemClock.sleep(1000)

        // Press volume up
        device.pressKeyCode(24)
        
        // Wait more than 750ms
        SystemClock.sleep(1000)

        // Press volume down
        device.pressKeyCode(25)
        
        // Verify no recording started (implement verification logic)
        SystemClock.sleep(500)
    }
} 