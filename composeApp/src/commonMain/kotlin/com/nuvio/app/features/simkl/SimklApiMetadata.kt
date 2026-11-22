package com.nuvio.app.features.simkl

import com.nuvio.app.core.build.AppVersionConfig
import io.ktor.http.encodeURLParameter

internal const val SIMKL_API_BASE_URL = "https://api.simkl.com"
// Simkl AUTH V2 (OAuth 2.0). V2 client IDs are rejected by the old /oauth/* endpoints.
internal const val SIMKL_AUTHORIZE_URL = "https://simkl.com/oauth2/authorize"
internal const val SIMKL_TOKEN_URL = "$SIMKL_API_BASE_URL/oauth2/token"
internal const val SIMKL_REVOKE_URL = "$SIMKL_API_BASE_URL/oauth2/revoke"

/** Without a scope, AUTH V2 grants read-only access; Morivia also writes history, ratings and scrobbles. */
internal const val SIMKL_OAUTH_SCOPE = "media:read media:write"

internal val simklAppVersion: String
    get() = AppVersionConfig.VERSION_NAME.ifBlank { "dev" }

internal fun buildSimklApiUrl(
    path: String,
    query: Map<String, String> = emptyMap(),
): String {
    val normalizedPath = path.trim().let { value ->
        if (value.startsWith('/')) value else "/$value"
    }
    val parameters = linkedMapOf<String, String>().apply {
        putAll(query)
        put("client_id", SimklConfig.CLIENT_ID)
        put("app-name", SimklConfig.APP_NAME)
        put("app-version", simklAppVersion)
    }
    return buildString {
        append(SIMKL_API_BASE_URL)
        append(normalizedPath)
        append('?')
        append(
            parameters.entries.joinToString("&") { (key, value) ->
                "${key.encodeURLParameter()}=${value.encodeURLParameter()}"
            },
        )
    }
}

internal fun simklRequestHeaders(
    accessToken: String? = null,
    contentTypeJson: Boolean = false,
): Map<String, String> = buildMap {
    put("User-Agent", "${SimklConfig.APP_NAME}/$simklAppVersion")
    put("Accept", "application/json")
    accessToken?.trim()?.takeIf(String::isNotBlank)?.let { token ->
        put("Authorization", "Bearer $token")
    }
    if (contentTypeJson) put("Content-Type", "application/json")
}
