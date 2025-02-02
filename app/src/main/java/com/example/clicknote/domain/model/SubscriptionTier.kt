package com.example.clicknote.domain.model

enum class SubscriptionTier(val weeklyLimit: Int) {
    FREE(3),
    MONTHLY(Int.MAX_VALUE),
    ANNUAL(Int.MAX_VALUE)
} 