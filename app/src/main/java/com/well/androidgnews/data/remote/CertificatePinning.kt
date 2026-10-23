package com.well.androidgnews.data.remote

import okhttp3.CertificatePinner
import okhttp3.HttpUrl.Companion.toHttpUrl

/**
 * SSL pinning by hash comparison: during the TLS handshake OkHttp takes the SHA-256 of each
 * certificate's public key (SPKI) in the backend's chain and refuses the connection unless at
 * least one matches [pins]. Each pin looks like `sha256/<base64>`.
 *
 * Returns null when [pins] is empty (no pinning, e.g. the plain-HTTP local backend in debug).
 * At least two pins are required, a primary and a backup, so that rotating the server's key
 * doesn't lock every installed app out of the backend.
 */
fun certificatePinnerFor(baseUrl: String, pins: List<String>): CertificatePinner? {
    if (pins.isEmpty()) return null
    val url = baseUrl.toHttpUrl()
    require(url.isHttps) { "Certificate pins only apply to an https backend, but the URL is $baseUrl" }
    require(pins.size >= 2) { "Pin a backup key as well as the current one, otherwise a key rotation bricks the app" }
    return CertificatePinner.Builder()
        .add(url.host, *pins.toTypedArray())
        .build()
}

/** Splits the comma-separated pin list stored in BuildConfig. */
fun parsePins(commaSeparated: String): List<String> =
    commaSeparated.split(',').map { it.trim() }.filter { it.isNotEmpty() }
