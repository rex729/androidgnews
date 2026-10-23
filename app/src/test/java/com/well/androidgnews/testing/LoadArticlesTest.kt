package com.well.androidgnews.testing

import com.well.androidgnews.data.article
import com.well.androidgnews.data.httpException
import com.well.androidgnews.ui.common.IArticlesUiState
import com.well.androidgnews.ui.common.loadArticles
import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadArticlesTest {

    @Test
    fun `articles become success`() = runTest {
        val articles = listOf(article("a"))

        assertEquals(IArticlesUiState.Success(articles), loadArticles { articles })
    }

    @Test
    fun `no articles become empty`() = runTest {
        assertEquals(IArticlesUiState.Empty, loadArticles { emptyList() })
    }

    @Test
    fun `http errors map to readable messages`() = runTest {
        assertErrorContains("請求格式", httpException(400))
        assertErrorContains("請求格式", httpException(422))
        assertErrorContains("請求次數", httpException(429))
        assertErrorContains("伺服器", httpException(502))
        assertErrorContains("伺服器", httpException(504))
        assertErrorContains("HTTP 404", httpException(404))
    }

    @Test
    fun `io failure maps to network message`() = runTest {
        assertErrorContains("網路", IOException())
    }

    @Test
    fun `certificate pinning failure is reported as an unverified server, not a network problem`() = runTest {
        assertErrorContains("無法驗證伺服器", SSLPeerUnverifiedException("Certificate pinning failure!"))
        assertErrorContains("無法驗證伺服器", SSLHandshakeException("untrusted certificate"))
    }

    @Test
    fun `unexpected failure maps to generic message`() = runTest {
        assertErrorContains("boom", IllegalStateException("boom"))
    }

    @Test(expected = CancellationException::class)
    fun `cancellation is rethrown, not turned into an error`() = runTest {
        loadArticles { throw CancellationException() }
    }

    private suspend fun assertErrorContains(expected: String, failure: Exception) {
        val state = loadArticles { throw failure }
        assertTrue(
            "expected error containing \"$expected\" but was $state",
            state is IArticlesUiState.Error && expected in state.message,
        )
    }
}
