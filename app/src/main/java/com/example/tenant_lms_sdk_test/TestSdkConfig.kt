package com.example.tenant_lms_sdk_test

/**
 * Fixed credentials this harness tests against.
 *
 * These belong to the **dev** environment: the tenant resolves on
 * https://lms-api.eynorix.xyz and returns 404 "Tenant not found" on the live host. The SDK
 * otherwise derives its host from CLIENT_KEY via the bundled brand table, which points at live,
 * so BASE_URL has to be passed explicitly in TenantDetail.
 */
object TestSdkConfig {
    const val TENANT_ID = "ac918c76-fe74-419b-b7e9-15e7603f558c"
    const val CLIENT_KEY = "eynorix"

    const val STUDENT_NAME = "Test User"
    const val STUDENT_MOBILE_NO = "9808092396"
    const val STUDENT_USERNAME = "sujan_check"
    const val STUDENT_GRADE_CODE = "class8"

    /** Dev API host these credentials live on. */
    const val BASE_URL_DEV = "https://lms-api.eynorix.xyz"

    /** Live API host, for comparison runs (the credentials above do NOT exist there). */
    const val BASE_URL_LIVE = "https://lms-api.eynorix.com"
}
