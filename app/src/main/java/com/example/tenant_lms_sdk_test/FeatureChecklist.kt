package com.example.tenant_lms_sdk_test

import android.content.Context

/**
 * Manual-test tracker. The SDK exposes no per-feature entry point — everything is reached by
 * navigating inside LmsSdkActivity — so the harness can't automate feature coverage. It instead
 * enumerates every feature area the SDK ships and lets the tester record a verdict per area,
 * persisted across launches so a test run can span several sessions.
 */
enum class TestVerdict { UNTESTED, PASS, FAIL;
    fun next(): TestVerdict = when (this) {
        UNTESTED -> PASS
        PASS -> FAIL
        FAIL -> UNTESTED
    }
}

data class Feature(val id: String, val label: String, val hint: String)

data class FeatureGroup(val title: String, val features: List<Feature>)

/**
 * Mirrors the feature packages compiled into the SDK
 * (the com.dn.erplms.features packages compiled in from composeApp), plus the cross-cutting
 * concerns that only show up at runtime (branding, theming, video, chat, security).
 */
val FEATURE_GROUPS = listOf(
    FeatureGroup(
        "Session & shell",
        listOf(
            Feature("login_headless", "Headless sdk-login", "Opens straight into LMS home, no OTP screen"),
            Feature("login_failure", "Login failure handling", "Bad mobile no. must NOT fall through to OTP entry"),
            Feature("brand_resolution", "Tenant branding", "Logo/primary color come from tenant-validation API"),
            Feature("theme_mode", "Forced theme mode", "Brand forceThemeMode honoured; SDK defaults to light"),
            Feature("close_button", "SDK close control", "Close bar returns to this host app, session intact"),
            Feature("back_behaviour", "Back navigation", "Back from LMS home exits to host, not a blank screen"),
            Feature("relaunch", "Re-launch after exit", "Second launch reuses session without re-login")
        )
    ),
    FeatureGroup(
        "Learning content",
        listOf(
            Feature("home", "LMS home", "Dashboard sections render for the configured grade"),
            Feature("academics", "Academics", "Subject/chapter tree loads"),
            Feature("chapter_content", "Chapter content detail", "Content detail screen opens per chapter"),
            Feature("study", "Study material", "Notes/material list and viewer"),
            Feature("ebook", "Ebook", "Readium-backed ebook opens and paginates"),
            Feature("epub", "EPUB reader", "EPUB navigation, TOC, progress"),
            Feature("search", "Search", "Global content search returns results"),
            Feature("bookmark", "Bookmarks", "Add/remove bookmark persists"),
            Feature("whats_new", "What's new", "Changelog/announcement section")
        )
    ),
    FeatureGroup(
        "Assessment",
        listOf(
            Feature("assignment", "Assignments", "List, detail, submission upload"),
            Feature("test", "Tests", "Test list, attempt, result"),
            Feature("chapter_wise_test", "Chapter-wise test", "Per-chapter test attempt and scoring"),
            Feature("question_bank", "Question bank practice", "Practice flow and PracticeResultScreen"),
            Feature("live_exam", "Live exam", "Scheduled exam join and submit"),
            Feature("latex", "Math / LaTeX rendering", "Formulae render in questions and results")
        )
    ),
    FeatureGroup(
        "Live & media",
        listOf(
            Feature("live_class", "Live class (Convay)", "ConferenceActivity joins, mic/camera permissions"),
            Feature("video_player", "Video playback", "media3 player, HLS streams, controls"),
            Feature("video_pip", "Picture-in-picture", "VideoPiPActivity keeps only the video in PiP"),
            Feature("video_background", "Background playback", "VideoPlaybackService notification controls"),
            Feature("chat", "Chat", "LmsChatGlobalWsManager websocket messages")
        )
    ),
    FeatureGroup(
        "Commerce",
        listOf(
            Feature("subscription", "Subscription", "Plans list, order detail"),
            Feature("payment_esewa", "eSewa payment", "REGRESSION GUARD: must not crash with ClassNotFoundException"),
            Feature("payment_khalti", "Khalti payment", "Khalti checkout launches and returns")
        )
    ),
    FeatureGroup(
        "Profile & notifications",
        listOf(
            Feature("profile", "Profile", "Student profile loads"),
            Feature("my_profile", "My profile edit", "Edit/save profile fields"),
            Feature("guardian", "Guardian", "Guardian section"),
            Feature("erp_with_lms", "ERP with LMS", "ERP-mode screens"),
            Feature("notification_center", "Notification centre", "In-app notification list"),
            Feature("push", "Push notification", "Needs host google-services.json; otherwise expected off"),
            Feature("doc_scanner", "Document scanner", "ML Kit scanner for uploads")
        )
    ),
    FeatureGroup(
        "Security & platform",
        listOf(
            Feature("screenshot_policy", "Screenshot policy", "Blocked screens show the policy message"),
            Feature("integrity", "Integrity check", "Unauthorized-device path renders an error, not a crash"),
            Feature("device_registration", "Device registration", "Public + student device registration succeed"),
            Feature("offline", "Offline / network loss", "Cached content and error states behave"),
            Feature("desugaring", "Desugared APIs", "java.time usage works on minSdk 24 devices")
        )
    )
)

class ChecklistStore(context: Context) {
    private val prefs = context.getSharedPreferences("lms_sdk_checklist", Context.MODE_PRIVATE)

    fun load(): Map<String, TestVerdict> =
        FEATURE_GROUPS.flatMap { it.features }.associate { feature ->
            val stored = prefs.getString(feature.id, null)
            feature.id to (stored?.let { runCatching { TestVerdict.valueOf(it) }.getOrNull() }
                ?: TestVerdict.UNTESTED)
        }

    fun save(id: String, verdict: TestVerdict) {
        prefs.edit().putString(id, verdict.name).apply()
    }

    fun clear() = prefs.edit().clear().apply()
}

/** Plain-text run summary, for pasting into a bug report. */
fun buildReport(verdicts: Map<String, TestVerdict>): String = buildString {
    appendLine("Eynora LMS SDK — manual test run")
    appendLine("tenant=${TestSdkConfig.TENANT_ID} client=${TestSdkConfig.CLIENT_KEY}")
    appendLine("student=${TestSdkConfig.STUDENT_USERNAME} grade=${TestSdkConfig.STUDENT_GRADE_CODE}")
    appendLine()
    FEATURE_GROUPS.forEach { group ->
        appendLine("## ${group.title}")
        group.features.forEach { feature ->
            val verdict = verdicts[feature.id] ?: TestVerdict.UNTESTED
            val mark = when (verdict) {
                TestVerdict.PASS -> "PASS"
                TestVerdict.FAIL -> "FAIL"
                TestVerdict.UNTESTED -> "----"
            }
            appendLine("  [$mark] ${feature.label}")
        }
        appendLine()
    }
    val all = verdicts.values
    appendLine(
        "totals: ${all.count { it == TestVerdict.PASS }} pass, " +
            "${all.count { it == TestVerdict.FAIL }} fail, " +
            "${all.count { it == TestVerdict.UNTESTED }} untested"
    )
}
