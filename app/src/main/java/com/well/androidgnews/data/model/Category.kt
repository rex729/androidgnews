package com.well.androidgnews.data.model

/** GNews top-headlines categories, with Traditional Chinese labels for the tabs. */
enum class Category(val apiValue: String, val label: String) {
    GENERAL("general", "頭條"),
    NATION("nation", "國內"),
    WORLD("world", "國際"),
    BUSINESS("business", "財經"),
    TECHNOLOGY("technology", "科技"),
    ENTERTAINMENT("entertainment", "娛樂"),
    SPORTS("sports", "體育"),
    SCIENCE("science", "科學"),
    HEALTH("health", "健康"),
}
