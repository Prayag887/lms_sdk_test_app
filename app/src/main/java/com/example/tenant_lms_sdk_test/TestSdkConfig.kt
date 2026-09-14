package com.example.tenant_lms_sdk_test

/**
 * Credentials this harness tests against.
 *
 * Tenant UUID and API host must match: each tenant exists in exactly one environment, and a
 * mismatch fails every call with 404 "Tenant not found". The Dev/Live switch on the harness
 * screen sets both together — don't set one without the other.
 *
 * The SDK otherwise derives its host from CLIENT_KEY via the bundled brand table (which points
 * at live), so the host is passed explicitly as TenantDetail.baseUrl.
 *
 * Verified 2026-09-11: sdk-login succeeds only on DEV (tenant 2fbfd06d on lms-api.eynorix.xyz).
 * The live host returned HTTP 500 for both tenants.
 */
object TestSdkConfig {
    const val CLIENT_KEY = "DIGITAL NEPAL"

    const val STUDENT_NAME = "Maksud Ali"
    const val STUDENT_MOBILE_NO = "9860471581"
    const val STUDENT_USERNAME = "Maksud-Ali"
    const val STUDENT_GRADE_CODE = "NWB11MGMT"

    /** Dev tenant — the one the student above belongs to. */
    const val TENANT_ID_DEV = "2fbfd06d-dbdb-41d3-bcb9-3914fd64308d"
    const val BASE_URL_DEV = "https://lms-api.eynorix.xyz"

    /** Live tenant. */
    const val TENANT_ID_LIVE = "c008244c-259c-4a21-8d7a-3e953bd193b5"
    const val BASE_URL_LIVE = "https://lms-api.eynorix.com"

    /** Environment the harness starts in — dev, the only working combination. */
    const val TENANT_ID = TENANT_ID_DEV
    const val BASE_URL = BASE_URL_DEV
}