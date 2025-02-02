package com.example.clicknote.domain.model

import android.Manifest
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.clicknote.R

enum class PermissionType {
    RECORD_AUDIO,
    NOTIFICATIONS,
    PHONE_STATE,
    ACCESSIBILITY_SERVICE,
    STORAGE
}

data class Permission(
    val type: PermissionType,
    val permission: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val isRequired: Boolean = true
)

object Permissions {
    val list = listOf(
        Permission(
            type = PermissionType.RECORD_AUDIO,
            permission = Manifest.permission.RECORD_AUDIO,
            titleRes = R.string.permission_record_audio_title,
            descriptionRes = R.string.permission_record_audio_description,
            iconRes = R.drawable.ic_mic,
            isRequired = true
        ),
        Permission(
            type = PermissionType.NOTIFICATIONS,
            permission = Manifest.permission.POST_NOTIFICATIONS,
            titleRes = R.string.permission_notifications_title,
            descriptionRes = R.string.permission_notifications_description,
            iconRes = R.drawable.ic_notifications,
            isRequired = false
        ),
        Permission(
            type = PermissionType.PHONE_STATE,
            permission = Manifest.permission.READ_PHONE_STATE,
            titleRes = R.string.permission_phone_state_title,
            descriptionRes = R.string.permission_phone_state_description,
            iconRes = R.drawable.ic_phone,
            isRequired = false
        ),
        Permission(
            type = PermissionType.STORAGE,
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
            titleRes = R.string.permission_storage_title,
            descriptionRes = R.string.permission_storage_description,
            iconRes = R.drawable.ic_storage,
            isRequired = true
        )
    )
} 