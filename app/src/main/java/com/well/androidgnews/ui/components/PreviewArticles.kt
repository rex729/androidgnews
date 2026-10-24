package com.well.androidgnews.ui.components

import com.well.androidgnews.data.model.Article
import java.time.Duration
import java.time.Instant

/** Sample articles for @Preview functions. No image URLs: previews can't fetch from the network. */
internal val previewArticles = listOf(
    Article(
        title = "台積電法說會：AI 需求強勁，上調全年營收預估",
        description = "台積電今日舉行法說會，預期 AI 相關營收將持續成長。",
        url = "https://example.com/1",
        imageUrl = null,
        publishedAt = Instant.now().minus(Duration.ofMinutes(25)),
        sourceName = "中央社",
    ),
    Article(
        title = "颱風外圍環流影響，北部今晚起雨勢增強，氣象署發布豪雨特報，提醒山區注意落石與坍方",
        description = "氣象署提醒民眾注意強降雨及山區落石。",
        url = "https://example.com/2",
        imageUrl = null,
        publishedAt = Instant.now().minus(Duration.ofHours(3)),
        sourceName = "公視新聞",
    ),
    Article(
        title = "中華隊經典賽名單出爐",
        description = null,
        url = "https://example.com/3",
        imageUrl = null,
        publishedAt = Instant.now().minus(Duration.ofDays(1)),
        sourceName = "自由時報",
    ),
)
