package com.well.androidgnews.ui.common

import com.well.androidgnews.data.model.Article
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

sealed interface IArticlesUiState {
    /** Nothing requested yet (search screen before the first query). */
    data object Idle : IArticlesUiState
    data object Loading : IArticlesUiState
    data object Empty : IArticlesUiState
    data class Success(val articles: List<Article>) : IArticlesUiState
    data class Error(val message: String) : IArticlesUiState
}

/** Runs [fetch] and maps its result or failure to a UI state, letting cancellation propagate. */
suspend fun loadArticles(fetch: suspend () -> List<Article>): IArticlesUiState =
    try {
        val articles = fetch()
        if (articles.isEmpty()) IArticlesUiState.Empty else IArticlesUiState.Success(articles)
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        IArticlesUiState.Error(
            when (e.code()) {
                400 -> "請求格式錯誤"
                401, 403 -> "API Key 無效，請檢查 local.properties 的 GNEWS_API_KEY"
                429 -> "已超過今日請求次數上限，請明天再試"
                in 500..599 -> "新聞伺服器暫時無法使用"
                else -> "讀取失敗 (HTTP ${e.code()})"
            },
        )
    } catch (e: IOException) {
        IArticlesUiState.Error("網路連線失敗，請檢查網路後再試")
    } catch (e: Exception) {
        IArticlesUiState.Error("發生未預期的錯誤：${e.message}")
    }
