package com.example.tenant_lms_sdk_test

import android.content.Context

/** Everything the host app hands to LmsSdk.configure(), editable from the harness UI. */
data class SdkCredentials(
    val tenantId: String,
    val clientKey: String,
    val baseUrl: String,
    val name: String,
    val mobileNo: String,
    val username: String,
    val gradeCode: String
) {
    companion object {
        val DEFAULT = SdkCredentials(
            tenantId = TestSdkConfig.TENANT_ID,
            clientKey = TestSdkConfig.CLIENT_KEY,
            baseUrl = TestSdkConfig.BASE_URL,
            name = TestSdkConfig.STUDENT_NAME,
            mobileNo = TestSdkConfig.STUDENT_MOBILE_NO,
            username = TestSdkConfig.STUDENT_USERNAME,
            gradeCode = TestSdkConfig.STUDENT_GRADE_CODE
        )
    }
}

/**
 * Persists whatever credentials the tester typed, so a custom tenant survives app restarts and
 * a test run doesn't begin by re-entering six fields.
 */
class CredentialsStore(context: Context) {
    private val prefs = context.getSharedPreferences("lms_sdk_credentials", Context.MODE_PRIVATE)

    fun load(): SdkCredentials {
        val d = SdkCredentials.DEFAULT
        return SdkCredentials(
            tenantId = prefs.getString(KEY_TENANT_ID, null) ?: d.tenantId,
            clientKey = prefs.getString(KEY_CLIENT_KEY, null) ?: d.clientKey,
            baseUrl = prefs.getString(KEY_BASE_URL, null) ?: d.baseUrl,
            name = prefs.getString(KEY_NAME, null) ?: d.name,
            mobileNo = prefs.getString(KEY_MOBILE, null) ?: d.mobileNo,
            username = prefs.getString(KEY_USERNAME, null) ?: d.username,
            gradeCode = prefs.getString(KEY_GRADE, null) ?: d.gradeCode
        )
    }

    fun save(credentials: SdkCredentials) {
        prefs.edit()
            .putString(KEY_TENANT_ID, credentials.tenantId)
            .putString(KEY_CLIENT_KEY, credentials.clientKey)
            .putString(KEY_BASE_URL, credentials.baseUrl)
            .putString(KEY_NAME, credentials.name)
            .putString(KEY_MOBILE, credentials.mobileNo)
            .putString(KEY_USERNAME, credentials.username)
            .putString(KEY_GRADE, credentials.gradeCode)
            .apply()
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_TENANT_ID = "tenant_id"
        const val KEY_CLIENT_KEY = "client_key"
        const val KEY_BASE_URL = "base_url"
        const val KEY_NAME = "name"
        const val KEY_MOBILE = "mobile_no"
        const val KEY_USERNAME = "username"
        const val KEY_GRADE = "grade_code"
    }
}
