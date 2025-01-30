package com.example.clicknote.data

data class SubscriptionStatus(
    val isActive: Boolean,
    val type: String?,
    val expiryDate: Long?,
    val isGracePeriod: Boolean = false,
    val error: String? = null
) 