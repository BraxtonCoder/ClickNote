package com.example.clicknote.service.recording

import android.content.Context
import android.media.MediaRecorder
import android.telephony.TelephonyManager
import com.example.clicknote.domain.model.CallRecording
import com.example.clicknote.domain.repository.CallRecordingRepository
import com.example.clicknote.service.notification.CallRecordingNotificationService
import com.example.clicknote.service.transcription.TranscriptionManager
import com.example.clicknote.service.AudioEnhancer
import com.example.clicknote.util.ContactUtils
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.File

@ExperimentalCoroutinesApi
class CallRecordingServiceTest {

    @MockK
    private lateinit var repository: CallRecordingRepository

    @MockK
    private lateinit var transcriptionManager: TranscriptionManager

    @MockK
    private lateinit var audioEnhancer: AudioEnhancer

    @MockK
    private lateinit var contactUtils: ContactUtils

    @MockK
    private lateinit var notificationService: CallRecordingNotificationService

    @MockK
    private lateinit var context: Context

    @MockK
    private lateinit var telephonyManager: TelephonyManager

    @MockK
    private lateinit var mediaRecorder: MediaRecorder

    private lateinit var service: CallRecordingService
    private val testScope = TestScope()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        
        every { context.getSystemService(Context.TELEPHONY_SERVICE) } returns telephonyManager
        every { telephonyManager.listen(any(), any()) } just Runs
        
        service = CallRecordingService().apply {
            repository = this@CallRecordingServiceTest.repository
            transcriptionManager = this@CallRecordingServiceTest.transcriptionManager
            audioEnhancer = this@CallRecordingServiceTest.audioEnhancer
            contactUtils = this@CallRecordingServiceTest.contactUtils
            notificationService = this@CallRecordingServiceTest.notificationService
        }
    }

    @Test
    fun `test call recording lifecycle`() = testScope.runTest {
        // Given
        val phoneNumber = "+1234567890"
        val contactName = "John Doe"
        val transcription = "Test transcription"
        val summary = "Test summary"
        val audioFile = mockk<File>()
        val enhancedAudioFile = mockk<File>()

        every { contactUtils.getContactName(phoneNumber) } returns contactName
        every { audioEnhancer.enhanceAudioFile(any()) } returns enhancedAudioFile
        coEvery { transcriptionManager.transcribe(any()) } returns transcription
        coEvery { transcriptionManager.generateSummary(any()) } returns summary
        coEvery { repository.insertCallRecording(any()) } just Runs
        every { notificationService.showRecordingNotification(any(), any()) } just Runs
        every { notificationService.showTranscribingNotification(any()) } just Runs
        every { notificationService.dismissNotification() } just Runs
        every { audioFile.absolutePath } returns "/test/path"
        every { enhancedAudioFile.absolutePath } returns "/test/enhanced/path"
        every { audioFile.delete() } returns true

        // When
        service.onCreate()
        service.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, phoneNumber)
        service.onCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, phoneNumber)
        service.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, phoneNumber)

        // Then
        verify { telephonyManager.listen(any(), TelephonyManager.LISTEN_CALL_STATE) }
        verify { contactUtils.getContactName(phoneNumber) }
        verify { audioEnhancer.enhanceAudioFile(any()) }
        coVerify { transcriptionManager.transcribe(any()) }
        coVerify { transcriptionManager.generateSummary(any()) }
        coVerify { repository.insertCallRecording(any()) }
        verify { notificationService.showRecordingNotification(any(), any()) }
        verify { notificationService.showTranscribingNotification(any()) }
        verify { notificationService.dismissNotification() }
    }

    @Test
    fun `test error handling during recording`() = testScope.runTest {
        // Given
        val phoneNumber = "+1234567890"
        every { mediaRecorder.prepare() } throws Exception("Test error")
        every { mediaRecorder.reset() } just Runs
        every { mediaRecorder.release() } just Runs
        every { notificationService.dismissNotification() } just Runs

        // When
        service.onCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, phoneNumber)

        // Then
        verify { mediaRecorder.reset() }
        verify { mediaRecorder.release() }
        verify { notificationService.dismissNotification() }
    }

    @Test
    fun `test error handling during transcription`() = testScope.runTest {
        // Given
        val phoneNumber = "+1234567890"
        coEvery { transcriptionManager.transcribe(any()) } throws Exception("Test error")
        every { notificationService.dismissNotification() } just Runs

        // When
        service.onCallStateChanged(TelephonyManager.CALL_STATE_OFFHOOK, phoneNumber)
        service.onCallStateChanged(TelephonyManager.CALL_STATE_IDLE, phoneNumber)

        // Then
        verify { notificationService.dismissNotification() }
    }
} 