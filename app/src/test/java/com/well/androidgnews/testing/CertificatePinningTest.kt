package com.well.androidgnews.testing

import com.well.androidgnews.data.remote.certificatePinnerFor
import com.well.androidgnews.data.remote.parsePins
import java.net.InetAddress
import javax.net.ssl.SSLPeerUnverifiedException
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.CertificatePinner
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Runs real TLS handshakes against a local HTTPS server to check that pinning accepts the
 * server's public-key hash and rejects anything else.
 */
class CertificatePinningTest {

    // The "backend" certificate, plus an unrelated one standing in for an attacker's or a spare.
    private val serverCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .build()
    private val otherCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .build()

    private val serverPin = CertificatePinner.pin(serverCertificate.certificate)
    private val otherPin = CertificatePinner.pin(otherCertificate.certificate)

    private val server = MockWebServer()
    private val loopback = InetAddress.getByName("127.0.0.1")

    @Before
    fun startHttpsServer() {
        val serverCertificates = HandshakeCertificates.Builder()
            .heldCertificate(serverCertificate)
            .build()
        server.useHttps(serverCertificates.sslSocketFactory())
        server.enqueue(MockResponse.Builder().body("ok").build())
        // IPv4 only: "localhost" also resolves to ::1, and after a pinning failure OkHttp would try
        // that route too and report its "connection refused" instead of the pinning error.
        server.start(loopback, 0)
    }

    @After
    fun stopServer() {
        server.close()
    }

    @Test
    fun `matching pin lets the request through`() {
        assertEquals("ok", get(listOf(serverPin, otherPin)))
    }

    @Test
    fun `backup pin alone is enough when the current key changed`() {
        assertEquals("ok", get(listOf(otherPin, serverPin)))
    }

    @Test
    fun `no matching pin rejects the connection`() {
        val unrelated = CertificatePinner.pin(HeldCertificate.Builder().build().certificate)

        val error = assertThrows(SSLPeerUnverifiedException::class.java) {
            get(listOf(otherPin, unrelated))
        }
        assertTrue(error.message!!.startsWith("Certificate pinning failure!"))
    }

    @Test
    fun `no pins means no pinning`() {
        assertNull(certificatePinnerFor("https://backend.example/", emptyList()))
    }

    @Test
    fun `pins on a plain http url are a configuration error`() {
        assertThrows(IllegalArgumentException::class.java) {
            certificatePinnerFor("http://10.0.2.2:8080/", listOf(serverPin, otherPin))
        }
    }

    @Test
    fun `a single pin without a backup is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            certificatePinnerFor("https://backend.example/", listOf(serverPin))
        }
    }

    @Test
    fun `pin list from BuildConfig is split on commas and trimmed`() {
        assertEquals(listOf("sha256/a=", "sha256/b="), parsePins(" sha256/a= , sha256/b=,"))
        assertEquals(emptyList<String>(), parsePins(""))
    }

    /** GETs the server through a client that trusts its self-signed certificate and applies [pins]. */
    private fun get(pins: List<String>): String {
        // Keep the certificate's hostname; the Dns override below sends it to the IPv4 server.
        val url = server.url("/").newBuilder().host("localhost").build()
        val clientCertificates = HandshakeCertificates.Builder()
            .addTrustedCertificate(serverCertificate.certificate)
            .build()
        val client = OkHttpClient.Builder()
            .sslSocketFactory(clientCertificates.sslSocketFactory(), clientCertificates.trustManager)
            .certificatePinner(checkNotNull(certificatePinnerFor(url.toString(), pins)))
            .dns(Dns { listOf(loopback) })
            .build()
        return client.newCall(Request.Builder().url(url).build()).execute().use { it.body.string() }
    }
}
