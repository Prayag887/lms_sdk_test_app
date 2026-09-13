package com.example.tenant_lms_sdk_test

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dn.lmssdk.LmsSdk
import com.dn.lmssdk.LmsSdkConfig
import com.dn.lmssdk.LmsUiProvider
import com.dn.lmssdk.SdkLaunchMode
import com.dn.lmssdk.StudentDetail
import com.dn.lmssdk.TenantDetail
import com.dn.lmssdk.android.LmsSdkActivity
import com.dn.lmssdk.android.LmsSdkInitializer
import com.dn.lmssdk.android.SdkLoginState
import com.example.tenant_lms_sdk_test.ui.theme.Tenant_lms_sdk_testTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Test harness for the Eynora LMS SDK.
 *
 * The SDK's whole public surface is `LmsSdk.configure(...)` + `LmsSdkActivity.launch(...)` —
 * there are no per-feature entry points, so features are reached by navigating inside the SDK
 * Activity. This screen therefore exercises every *integration path* the SDK offers, surfaces
 * the headless login state, and keeps a persisted checklist for the feature-by-feature sweep.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tenant_lms_sdk_testTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HarnessScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HarnessScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val credentialsStore = remember { CredentialsStore(context) }
    val saved = remember { credentialsStore.load() }

    var tenantId by rememberSaveable { mutableStateOf(saved.tenantId) }
    var clientKey by rememberSaveable { mutableStateOf(saved.clientKey) }
    var name by rememberSaveable { mutableStateOf(saved.name) }
    var mobileNo by rememberSaveable { mutableStateOf(saved.mobileNo) }
    var username by rememberSaveable { mutableStateOf(saved.username) }
    var gradeCode by rememberSaveable { mutableStateOf(saved.gradeCode) }
    var baseUrl by rememberSaveable { mutableStateOf(saved.baseUrl) }
    var credentialsExpanded by rememberSaveable { mutableStateOf(true) }

    fun currentCredentials() = SdkCredentials(
        tenantId = tenantId.trim(),
        clientKey = clientKey.trim(),
        baseUrl = baseUrl.trim(),
        name = name.trim(),
        mobileNo = mobileNo.trim(),
        username = username.trim(),
        gradeCode = gradeCode.trim()
    )

    fun buildConfig() = LmsSdkConfig(
        tenantDetail = TenantDetail(
            tenantId = tenantId.trim(),
            clientKey = clientKey.trim(),
            baseUrl = baseUrl.trim()
        ),
        studentDetail = StudentDetail(
            name = name.trim(),
            mobileNo = mobileNo.trim(),
            username = username.trim(),
            gradeCode = gradeCode.trim()
        )
    )

    // Persist on launch rather than on every keystroke: one write per run, and the values that
    // get saved are exactly the ones the SDK was handed.
    fun currentConfig(): LmsSdkConfig {
        credentialsStore.save(currentCredentials())
        return buildConfig()
    }

    val store = remember { ChecklistStore(context) }
    val verdicts = remember { mutableStateMapOf<String, TestVerdict>().apply { putAll(store.load()) } }

    val loginState by LmsSdkInitializer.loginState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Eynora LMS SDK — test harness", style = MaterialTheme.typography.titleLarge)
            Text(
                "com.dn:lms-sdk:${BuildConfigInfo.SDK_VERSION} from the GitLab Maven registry",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item { LoginStateCard(loginState) { scope.launch { withContext(Dispatchers.IO) { LmsSdkInitializer.retry() } } } }

        item {
            SectionCard("Credentials") {
                Text(
                    "Every field the host app passes to LmsSdk.configure(). Edits are saved on " +
                        "launch and restored next start.",
                    style = MaterialTheme.typography.bodySmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = {
                        tenantId = TestSdkConfig.TENANT_ID_DEV
                        baseUrl = TestSdkConfig.BASE_URL_DEV
                    }) {
                        Text(if (baseUrl == TestSdkConfig.BASE_URL_DEV) "● Dev" else "Dev")
                    }
                    OutlinedButton(onClick = {
                        tenantId = TestSdkConfig.TENANT_ID_LIVE
                        baseUrl = TestSdkConfig.BASE_URL_LIVE
                    }) {
                        Text(if (baseUrl == TestSdkConfig.BASE_URL_LIVE) "● Live" else "Live")
                    }
                    TextButton(onClick = { credentialsExpanded = !credentialsExpanded }) {
                        Text(if (credentialsExpanded) "Hide" else "Edit")
                    }
                }
                val knownEnv = baseUrl == TestSdkConfig.BASE_URL_DEV ||
                    baseUrl == TestSdkConfig.BASE_URL_LIVE
                val envMismatch = (baseUrl == TestSdkConfig.BASE_URL_DEV &&
                    tenantId != TestSdkConfig.TENANT_ID_DEV) ||
                    (baseUrl == TestSdkConfig.BASE_URL_LIVE &&
                        tenantId != TestSdkConfig.TENANT_ID_LIVE)
                Text(
                    when {
                        envMismatch ->
                            "⚠ This tenant does not belong to $baseUrl — expect 404 Tenant not found"
                        !knownEnv -> "Custom host: $baseUrl"
                        else -> "Environment: $baseUrl"
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                if (credentialsExpanded) {
                    Field("Tenant ID (UUID)", tenantId) { tenantId = it }
                    Field("Client key (ems_id)", clientKey) { clientKey = it }
                    Field("API base URL", baseUrl) { baseUrl = it }
                    Field("Student name", name) { name = it }
                    Field("Mobile number", mobileNo, KeyboardType.Phone) { mobileNo = it }
                    Field("Username", username) { username = it }
                    Field("Grade code", gradeCode) { gradeCode = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            credentialsStore.save(currentCredentials())
                            Toast.makeText(context, "Credentials saved", Toast.LENGTH_SHORT).show()
                        }) { Text("Save") }
                        OutlinedButton(onClick = {
                            credentialsStore.clear()
                            val d = SdkCredentials.DEFAULT
                            tenantId = d.tenantId
                            clientKey = d.clientKey
                            baseUrl = d.baseUrl
                            name = d.name
                            mobileNo = d.mobileNo
                            username = d.username
                            gradeCode = d.gradeCode
                        }) { Text("Reset to TestSdkConfig") }
                    }
                }
            }
        }

        item {
            SectionCard("Integration paths") {
                LaunchRow(
                    title = "1 · configure() + launch()",
                    detail = "Process-wide config, the documented happy path.",
                    button = "Launch"
                ) {
                    LmsSdk.configure(currentConfig())
                    LmsSdkActivity.launch(context)
                }
                LaunchRow(
                    title = "2 · launch(context, config)",
                    detail = "Config carried as intent extras. Note: LmsSdkActivity prefers a " +
                        "process-wide config when one is already set, so run this on a fresh " +
                        "process to actually exercise the extras path.",
                    button = "Launch"
                ) {
                    LmsSdkActivity.launch(context, currentConfig())
                }
                LaunchRow(
                    title = "3 · UI_PROVIDER mode",
                    detail = "Replaces the bundled LMS UI with a host-supplied composable. " +
                        "Path 1 clears it again.",
                    button = "Launch"
                ) {
                    LmsSdk.configure(currentConfig(), DemoUiProvider)
                    context.startActivity(LmsSdkActivity.createIntent(context))
                }
                LaunchRow(
                    title = "4 · Negative: launch with no config",
                    detail = "Expected: the Activity finishes immediately instead of crashing. " +
                        "Only valid before any configure() call in this process.",
                    button = "Launch"
                ) {
                    context.startActivity(
                        LmsSdkActivity.createIntent(context, SdkLaunchMode.LMS_APP)
                    )
                }
                LaunchRow(
                    title = "5 · Negative: bad mobile number",
                    detail = "Expected: SdkLoginState.Failed and NO fallback to the OTP screen.",
                    button = "Launch"
                ) {
                    LmsSdk.configure(
                        currentConfig().copy(
                            studentDetail = buildConfig().studentDetail.copy(mobileNo = "0000000000")
                        )
                    )
                    LmsSdkActivity.launch(context)
                }
            }
        }

        item { ChecklistHeader(verdicts, store, context) }

        FEATURE_GROUPS.forEach { group ->
            item {
                Text(
                    group.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(group.features, key = { it.id }) { feature ->
                FeatureRow(
                    feature = feature,
                    verdict = verdicts[feature.id] ?: TestVerdict.UNTESTED,
                    onCycle = {
                        val next = (verdicts[feature.id] ?: TestVerdict.UNTESTED).next()
                        verdicts[feature.id] = next
                        store.save(feature.id, next)
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginStateCard(state: SdkLoginState, onRetry: () -> Unit) {
    val (label, detail) = when (state) {
        is SdkLoginState.Idle -> "Idle" to "SDK not launched yet in this process."
        is SdkLoginState.NotAttempted -> "Not attempted" to "No mobile number was supplied."
        is SdkLoginState.InProgress ->
            "In progress" to "Headless tenant-validation/device/sdk-login is running."
        is SdkLoginState.Success -> "Success" to "Headless sdk-login completed."
        is SdkLoginState.Failed -> "Failed" to state.message
    }
    SectionCard("Headless login state") {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(detail, style = MaterialTheme.typography.bodySmall)
        if (state is SdkLoginState.Failed) {
            OutlinedButton(onClick = onRetry) { Text("Retry login") }
        }
    }
}

@Composable
private fun ChecklistHeader(
    verdicts: Map<String, TestVerdict>,
    store: ChecklistStore,
    context: Context
) {
    val total = verdicts.size
    val done = verdicts.values.count { it != TestVerdict.UNTESTED }
    val failed = verdicts.values.count { it == TestVerdict.FAIL }

    SectionCard("Feature checklist") {
        Text(
            "$done of $total checked · $failed failing",
            style = MaterialTheme.typography.bodySmall
        )
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else done.toFloat() / total },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Tap a row to cycle untested → pass → fail.",
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("lms-sdk test run", buildReport(verdicts)))
                Toast.makeText(context, "Report copied", Toast.LENGTH_SHORT).show()
            }) { Text("Copy report") }
            OutlinedButton(onClick = {
                store.clear()
                Toast.makeText(context, "Checklist reset — reopen the app", Toast.LENGTH_SHORT).show()
            }) { Text("Reset") }
        }
    }
}

@Composable
private fun FeatureRow(feature: Feature, verdict: TestVerdict, onCycle: () -> Unit) {
    val color = when (verdict) {
        TestVerdict.PASS -> Color(0xFF2E7D32)
        TestVerdict.FAIL -> Color(0xFFC62828)
        TestVerdict.UNTESTED -> MaterialTheme.colorScheme.outline
    }
    Card(
        onClick = onCycle,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(color = color, shape = CircleShape, modifier = Modifier.size(14.dp)) {}
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(feature.label, style = MaterialTheme.typography.bodyMedium)
                Text(feature.hint, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun LaunchRow(title: String, detail: String, button: String, onClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium)
        Text(detail, style = MaterialTheme.typography.bodySmall)
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(button) }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
        modifier = Modifier.fillMaxWidth()
    )
}

/** Minimal custom UI, used to prove SdkLaunchMode.UI_PROVIDER renders host-supplied content. */
private object DemoUiProvider : LmsUiProvider {
    @Composable
    override fun Content() {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Host-supplied UI", style = MaterialTheme.typography.titleLarge)
            Text(
                "Rendered inside LmsSdkActivity via LmsUiProvider — the bundled LMS UI is bypassed.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private object BuildConfigInfo {
    val SDK_VERSION: String = BuildConfig.LMS_SDK_VERSION
}
