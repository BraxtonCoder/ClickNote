package com.example.clicknote.data.repository

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.repository.SubscriptionStatus
import com.example.clicknote.domain.repository.SubscriptionTier
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallable
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException

class SubscriptionRepositoryImplTest {
    @MockK
    private lateinit var auth: FirebaseAuth

    @MockK
    private lateinit var firestore: FirebaseFirestore

    @MockK
    private lateinit var functions: FirebaseFunctions

    @MockK
    private lateinit var user: FirebaseUser

    private lateinit var repository: SubscriptionRepositoryImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(LocalDateTime::class)
        
        // Mock current time
        val now = LocalDateTime.of(2024, 3, 1, 12, 0)
        every { LocalDateTime.now() } returns now

        // Mock Firebase Auth
        every { auth.currentUser } returns user
        every { user.uid } returns "test-user-id"

        repository = SubscriptionRepositoryImpl(auth, firestore)
    }

    @Test
    fun `getSubscriptionPlans returns all available plans`() = runTest {
        val plans = repository.getSubscriptionPlans().first()
        assertEquals(3, plans.size)
        assertTrue(plans.any { it.id == "free" })
        assertTrue(plans.any { it.id == "monthly" })
        assertTrue(plans.any { it.id == "annual" })
    }

    @Test
    fun `purchaseSubscription creates subscription successfully`() = runTest {
        // Mock Firebase Functions
        val callable = mockk<HttpsCallable>()
        val callableResult = mockk<HttpsCallableResult>()
        val resultData = mapOf("subscriptionId" to "test-subscription-id")
        
        every { functions.getHttpsCallable("createSubscription") } returns callable
        coEvery { callable.call(any()) } returns callableResult
        every { callableResult.data } returns resultData

        // Mock Firestore
        val docRef = mockk<DocumentReference>()
        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.set(any()) } returns mockk()

        val result = repository.purchaseSubscription("monthly")
        assertTrue(result.isSuccess)

        // Verify Firestore update
        coVerify {
            docRef.set(match {
                it["tier"] == "MONTHLY" &&
                it["isActive"] == true &&
                it["subscriptionId"] == "test-subscription-id"
            })
        }
    }

    @Test
    fun `cancelSubscription updates status correctly`() = runTest {
        // Mock current subscription status
        val status = SubscriptionStatus(
            tier = SubscriptionTier.MONTHLY,
            subscriptionId = "test-subscription-id",
            isActive = true
        )
        
        // Mock Firebase Functions
        val callable = mockk<HttpsCallable>()
        val callableResult = mockk<HttpsCallableResult>()
        
        every { functions.getHttpsCallable("cancelSubscription") } returns callable
        coEvery { callable.call(any()) } returns callableResult

        // Mock Firestore
        val docRef = mockk<DocumentReference>()
        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.update(any<Map<String, Any>>()) } returns mockk()

        val result = repository.cancelSubscription()
        assertTrue(result.isSuccess)

        // Verify Firestore update
        coVerify {
            docRef.update(match {
                it["isActive"] == false &&
                it["isGracePeriod"] == true
            })
        }
    }

    @Test
    fun `getRemainingFreeNotes returns correct count for free plan`() = runTest {
        // Mock Firestore document
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        val docData = mapOf(
            "tier" to "FREE",
            "weeklyUsageCount" to 1,
            "isActive" to true
        )

        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.get() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.data } returns docData

        val remainingNotes = repository.getRemainingFreeNotes()
        assertEquals(2, remainingNotes) // 3 (limit) - 1 (used) = 2
    }

    @Test
    fun `isSubscriptionActive returns correct status`() = runTest {
        // Mock Firestore document
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        val docData = mapOf(
            "tier" to "MONTHLY",
            "isActive" to true,
            "endDate" to LocalDateTime.now().plusDays(7).toEpochSecond(ZoneOffset.UTC)
        )

        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.get() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.data } returns docData

        val isActive = repository.isSubscriptionActive()
        assertTrue(isActive)
    }

    @Test
    fun `purchaseSubscription handles network error gracefully`() = runTest {
        // Mock Firebase Functions to throw network error
        val callable = mockk<HttpsCallable>()
        every { functions.getHttpsCallable("createSubscription") } returns callable
        coEvery { callable.call(any()) } throws java.net.UnknownHostException()

        val result = repository.purchaseSubscription("monthly")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is Exception)
    }

    @Test
    fun `purchaseSubscription handles invalid plan ID`() = runTest {
        val result = repository.purchaseSubscription("invalid_plan")
        assertTrue(result.isFailure)
        assertEquals("Invalid plan ID", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getRemainingFreeNotes handles weekly reset correctly`() = runTest {
        // Mock Firestore document with expired weekly reset date
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        val now = LocalDateTime.now()
        val lastWeek = now.minusDays(8)
        
        val docData = mapOf(
            "tier" to "FREE",
            "weeklyUsageCount" to 3,
            "weeklyResetDate" to lastWeek.toEpochSecond(ZoneOffset.UTC),
            "isActive" to true
        )

        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.get() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.data } returns docData

        // Mock reset weekly usage
        coEvery { docRef.update(any<Map<String, Any>>()) } returns mockk()

        val remainingNotes = repository.getRemainingFreeNotes()
        assertEquals(3, remainingNotes) // Should be reset to full limit
    }

    @Test
    fun `cancelSubscription handles non-existent subscription`() = runTest {
        // Mock current subscription status with no subscription ID
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()
        val docData = mapOf(
            "tier" to "FREE",
            "isActive" to true
        )

        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.get() } returns docSnapshot
        every { docSnapshot.exists() } returns true
        every { docSnapshot.data } returns docData

        val result = repository.cancelSubscription()
        assertTrue(result.isSuccess) // Should succeed without making API call
    }

    @Test
    fun `switchToFreePlan preserves usage statistics`() = runTest {
        val docRef = mockk<DocumentReference>()
        coEvery { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
                .set(any())
        } returns mockk()

        repository.switchToFreePlan()

        coVerify {
            docRef.set(match {
                it["tier"] == "FREE" &&
                it["weeklyUsageCount"] == 0 &&
                it["weeklyResetDate"] != null &&
                it["isActive"] == true
            })
        }
    }

    @Test
    fun `getSubscriptionStatus creates free plan for new users`() = runTest {
        // Mock non-existent document
        val docRef = mockk<DocumentReference>()
        val docSnapshot = mockk<DocumentSnapshot>()

        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef
        coEvery { docRef.get() } returns docSnapshot
        every { docSnapshot.exists() } returns false

        // Mock switchToFreePlan
        coEvery { docRef.set(any()) } returns mockk()

        val status = repository.getSubscriptionStatus()
        assertEquals(SubscriptionTier.FREE, status.tier)
        assertEquals(0, status.weeklyUsageCount)
        assertTrue(status.isActive)
    }

    @Test
    fun `incrementUsageCount handles concurrent updates`() = runTest {
        val docRef = mockk<DocumentReference>()
        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef

        // Simulate concurrent update conflict then success
        coEvery { 
            docRef.update("weeklyUsageCount", any())
        } throws FirebaseFirestoreException("Update conflict", FirebaseFirestoreException.Code.ABORTED) andThen mockk()

        // Should not throw exception
        repository.incrementUsageCount()

        // Verify retry attempt
        coVerify(exactly = 2) { docRef.update(any<String>(), any()) }
    }

    @Test
    fun `observeSubscriptionStatus handles stream errors`() = runTest {
        val docRef = mockk<DocumentReference>()
        every { 
            firestore.collection("users")
                .document("test-user-id")
                .collection("subscription")
                .document("status")
        } returns docRef

        // Mock snapshot listener that emits error
        every { 
            docRef.addSnapshotListener(any())
        } answers {
            val listener = arg<EventListener<DocumentSnapshot>>(0)
            listener.onEvent(null, FirebaseFirestoreException("Network error", FirebaseFirestoreException.Code.UNAVAILABLE))
            mockk()
        }

        val flow = repository.observeSubscriptionStatus()
        assertThrows(FirebaseFirestoreException::class.java) {
            flow.collect { }
        }
    }
} 