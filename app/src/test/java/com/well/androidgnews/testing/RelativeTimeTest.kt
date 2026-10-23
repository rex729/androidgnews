package com.well.androidgnews.testing

import com.well.androidgnews.util.formatRelativeTime
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {

    private val now = Instant.parse("2026-10-23T12:00:00Z")
    private val taipei = ZoneId.of("Asia/Taipei")

    private fun format(ago: Duration) = formatRelativeTime(now.minus(ago), now, taipei)

    @Test
    fun `under a minute is just now`() {
        assertEquals("剛剛", format(Duration.ofSeconds(30)))
    }

    @Test
    fun `future time is just now`() {
        assertEquals("剛剛", format(Duration.ofMinutes(-5)))
    }

    @Test
    fun `under an hour shows minutes`() {
        assertEquals("5 分鐘前", format(Duration.ofMinutes(5)))
        assertEquals("59 分鐘前", format(Duration.ofMinutes(59)))
    }

    @Test
    fun `under a day shows hours`() {
        assertEquals("1 小時前", format(Duration.ofMinutes(60)))
        assertEquals("23 小時前", format(Duration.ofHours(23)))
    }

    @Test
    fun `a day or more shows the local date and time`() {
        // 2026-10-21T12:00Z is 20:00 in Taipei (UTC+8).
        assertEquals("10/21 20:00", format(Duration.ofDays(2)))
    }
}
