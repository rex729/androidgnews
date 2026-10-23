package com.well.androidgnews.ui.common

import com.well.androidgnews.data.model.Article
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

sealed interface ArticlesUiState {
    /** Nothing requested yet (search screen before the first query). */
    data object Idle : ArticlesUiState
    data object Loading : ArticlesUiState
    data object Empty : ArticlesUiState
    data class Success(val articles: List<Article>) : ArticlesUiState
    data class Error(val message: String) : ArticlesUiState
}

/** Runs [fetch] and maps its result or failure to a UI state, letting cancellation propagate. */
suspend fun loadArticles(fetch: suspend () -> List<Article>): ArticlesUiState =
    try {
        val articles = fetch()
        if (articles.isEmpty()) ArticlesUiState.Empty else ArticlesUiState.Success(articles)
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        ArticlesUiState.Error(
            when (e.code()) {
                400 -> "請求格式錯誤"
                401, 403 -> "API Key 無效，請檢查 local.properties 的 GNEWS_API_KEY"
                429 -> "已超過今日請求次數上限，請明天再試"
                in 500..599 -> "新聞伺服器暫時無法使用"
                else -> "讀取失敗 (HTTP ${e.code()})"
            },
        )
    } catch (e: IOException) {
        ArticlesUiState.Error("網路連線失敗，請檢查網路後再試")
    } catch (e: Exception) {
        ArticlesUiState.Error("發生未預期的錯誤：${e.message}")
    }
