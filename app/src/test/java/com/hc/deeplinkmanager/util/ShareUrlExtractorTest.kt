package com.hc.deeplinkmanager.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareUrlExtractorTest {

    @Test
    fun `extracts bare deeplink`() {
        assertEquals(
            "hungerstation://?c=SA",
            ShareUrlExtractor.extractFirstUrl("hungerstation://?c=SA")
        )
    }

    @Test
    fun `extracts url from prose`() {
        assertEquals(
            "https://pay.example.com/checkout",
            ShareUrlExtractor.extractFirstUrl("Try https://pay.example.com/checkout now")
        )
    }

    @Test
    fun `strips trailing punctuation from url`() {
        assertEquals(
            "https://example.com/a,b",
            ShareUrlExtractor.extractFirstUrl("see https://example.com/a,b,")
        )
    }

    @Test
    fun `takes first url when multiple present`() {
        assertEquals(
            "https://first.example.com",
            ShareUrlExtractor.extractFirstUrl("https://first.example.com and https://second.example.com")
        )
    }

    @Test
    fun `returns null when no url`() {
        assertNull(ShareUrlExtractor.extractFirstUrl("just some words, no scheme"))
        assertNull(ShareUrlExtractor.extractFirstUrl(""))
    }

    @Test
    fun `deriveName uses host`() {
        assertEquals("pay.example.com", ShareUrlExtractor.deriveName("https://pay.example.com/checkout?x=1"))
    }

    @Test
    fun `deriveName falls back to path segment`() {
        assertEquals("flow", ShareUrlExtractor.deriveName("app:///deep/flow"))
    }

    @Test
    fun `deriveName handles scheme-less string as host`() {
        assertEquals("example.com", ShareUrlExtractor.deriveName("example.com/flow"))
    }

    @Test
    fun `deriveName falls back to generic when no host or path`() {
        assertEquals("Shared deeplink", ShareUrlExtractor.deriveName("hungerstation://?c=SA"))
    }
}
