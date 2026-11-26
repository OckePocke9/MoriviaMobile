package com.morivia.app.features.player

import android.app.Application
import androidx.media3.datasource.DataSpec
import javax.net.ssl.SSLHandshakeException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PlayerPlaybackNetworkingTest {
    @Test
    fun crossOriginRedirectDoesNotForwardAuthorization() {
        MockWebServer().use { source ->
            MockWebServer().use { destination ->
                destination.enqueue(MockResponse().setBody("video"))
                source.enqueue(MockResponse().setResponseCode(302)
                    .setHeader("Location", destination.url("/video")))
                openMedia(source.url("/video").toString())
                assertEquals("Bearer test-only", source.takeRequest().getHeader("Authorization"))
                assertNull(destination.takeRequest().getHeader("Authorization"))
            }
        }
    }

    @Test
    fun sameOriginRedirectKeepsAuthorization() {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(302).setHeader("Location", server.url("/media")))
            server.enqueue(MockResponse().setBody("video"))
            openMedia(server.url("/start").toString())
            repeat(2) { assertEquals("Bearer test-only", server.takeRequest().getHeader("Authorization")) }
        }
    }

    @Test
    fun bothMediaTransportsRejectUntrustedCertificates() {
        val certificate = HeldCertificate.Builder().commonName("localhost")
            .addSubjectAlternativeName("localhost").addSubjectAlternativeName("127.0.0.1").build()
        val certificates = HandshakeCertificates.Builder().heldCertificate(certificate).build()
        MockWebServer().use { server ->
            server.useHttps(certificates.sslSocketFactory(), false)
            server.enqueue(MockResponse().setBody("video"))
            // Use the bound IPv4 address so an IPv6 fallback cannot mask the TLS failure.
            val url = server.url("/video").newBuilder().host("127.0.0.1").build().toString()
            val failure = assertFails { openMedia(url) }
            assertTrue(
                generateSequence(failure) { it.cause }.any { it is javax.net.ssl.SSLException },
                failure.stackTraceToString(),
            )

            val connection = PlayerPlaybackNetworking.openConnection(url, emptyMap(), "GET", 5_000, 5_000)
            try {
                assertFailsWith<SSLHandshakeException> { connection.inputStream.use { it.read() } }
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun openMedia(url: String) {
        val source = PlayerPlaybackNetworking.createHttpDataSourceFactory(
            mapOf("Authorization" to "Bearer test-only"),
        ).createDataSource()
        try {
            source.open(DataSpec.Builder().setUri(url).build())
        } finally {
            source.close()
        }
    }
}
