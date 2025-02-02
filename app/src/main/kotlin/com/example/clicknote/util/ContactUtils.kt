package com.example.clicknote.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getContactName(phoneNumber: String): String? {
        var contactName: String? = null
        
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor?.moveToFirst() == true) {
                contactName = cursor.getString(
                    cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        
        return contactName
    }

    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove any non-digit characters
        val digitsOnly = phoneNumber.replace(Regex("[^0-9+]"), "")
        
        // Format based on length and starting characters
        return when {
            digitsOnly.startsWith("+") -> {
                when (digitsOnly.length) {
                    12 -> { // +1 format (US/Canada)
                        "+${digitsOnly.substring(1, 2)} (${digitsOnly.substring(2, 5)}) ${digitsOnly.substring(5, 8)}-${digitsOnly.substring(8)}"
                    }
                    13 -> { // +44 format (UK)
                        "+${digitsOnly.substring(1, 3)} ${digitsOnly.substring(3, 7)} ${digitsOnly.substring(7)}"
                    }
                    else -> digitsOnly
                }
            }
            digitsOnly.length == 10 -> { // US format without country code
                "(${digitsOnly.substring(0, 3)}) ${digitsOnly.substring(3, 6)}-${digitsOnly.substring(6)}"
            }
            digitsOnly.length == 11 && digitsOnly.startsWith("1") -> { // US format with country code
                "+1 (${digitsOnly.substring(1, 4)}) ${digitsOnly.substring(4, 7)}-${digitsOnly.substring(7)}"
            }
            else -> digitsOnly
        }
    }
} 