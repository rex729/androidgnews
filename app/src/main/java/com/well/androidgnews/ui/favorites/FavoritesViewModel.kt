package com.well.androidgnews.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.well.androidgnews.analytics.IAnalytics
import com.well.androidgnews.data.auth.IAuthRepository
import com.well.androidgnews.data.favorites.IFavoritesRepository
import com.well.androidgnews.data.model.Article
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The signed-in user's favorites, shared by every screen that shows a heart on its articles.
 * Follows the current user: signing out empties it, signing in as someone else switches lists.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModel(
    private val auth: IAuthRepository,
    private val favoritesRepository: IFavoritesRepository,
    private val analytics: IAnalytics,
) : ViewModel() {

    private val _errors = Channel<String>(Channel.BUFFERED)

    /** One-off messages to show (e.g. a failed save), each delivered once. */
    val errors: Flow<String> = _errors.receiveAsFlow()

    val favorites: StateFlow<List<Article>> = auth.currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                favoritesRepository.favorites(user.id).catch {
                    _errors.trySend("無法讀取收藏")
                    emit(emptyList())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val favoriteUrls: StateFlow<Set<String>> = favorites
        .map { articles -> articles.mapTo(HashSet()) { it.url } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun toggle(article: Article) {
        val userId = auth.currentUser.value?.id ?: return
        val isFavorite = article.url in favoriteUrls.value
        viewModelScope.launch {
            try {
                if (isFavorite) {
                    favoritesRepository.remove(userId, article.url)
                    analytics.favoriteRemoved(article)
                } else {
                    favoritesRepository.add(userId, article)
                    analytics.favoriteAdded(article)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errors.trySend(if (isFavorite) "取消收藏失敗，請再試一次" else "收藏失敗，請再試一次")
            }
        }
    }
}
