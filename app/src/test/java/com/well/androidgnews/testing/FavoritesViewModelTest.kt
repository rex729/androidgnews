package com.well.androidgnews.testing

import com.well.androidgnews.data.FakeAuthRepository
import com.well.androidgnews.data.FakeFavoritesRepository
import com.well.androidgnews.data.article
import com.well.androidgnews.rule.MainDispatcherRule
import com.well.androidgnews.ui.favorites.FavoritesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alice = FakeAuthRepository.user("alice")
    private val bob = FakeAuthRepository.user("bob")
    private val auth = FakeAuthRepository(initialUser = alice)
    private val repository = FakeFavoritesRepository()

    private fun viewModel() = FavoritesViewModel(auth, repository)

    @Test
    fun `toggling an article saves it, toggling again removes it`() {
        val viewModel = viewModel()
        val story = article("https://news/1")

        viewModel.toggle(story)

        assertEquals(listOf(story), viewModel.favorites.value)
        assertEquals(setOf(story.url), viewModel.favoriteUrls.value)
        assertEquals(listOf(story), repository.favoritesOf("alice"))

        viewModel.toggle(story)

        assertTrue(viewModel.favorites.value.isEmpty())
        assertTrue(repository.favoritesOf("alice").isEmpty())
    }

    @Test
    fun `newest favorite comes first`() {
        val viewModel = viewModel()

        viewModel.toggle(article("https://news/old"))
        viewModel.toggle(article("https://news/new"))

        assertEquals(listOf("https://news/new", "https://news/old"), viewModel.favorites.value.map { it.url })
    }

    @Test
    fun `favorites belong to the signed-in user`() {
        val viewModel = viewModel()
        viewModel.toggle(article("https://news/alice"))

        auth.switchTo(bob)

        assertTrue(viewModel.favorites.value.isEmpty())
        viewModel.toggle(article("https://news/bob"))
        assertEquals(listOf("https://news/bob"), repository.favoritesOf("bob").map { it.url })
        assertEquals(listOf("https://news/alice"), repository.favoritesOf("alice").map { it.url })
    }

    @Test
    fun `signed out shows nothing and toggling does nothing`() {
        auth.switchTo(null)
        val viewModel = viewModel()

        viewModel.toggle(article("https://news/1"))

        assertTrue(viewModel.favorites.value.isEmpty())
        assertTrue(repository.favoritesOf("alice").isEmpty())
    }

    @Test
    fun `failed save reports an error and keeps the list unchanged`() = runTest {
        val viewModel = viewModel()
        repository.failWrites = true

        viewModel.toggle(article("https://news/1"))

        assertEquals("收藏失敗，請再試一次", viewModel.errors.first())
        assertTrue(viewModel.favorites.value.isEmpty())
    }

    @Test
    fun `failed read reports an error and shows an empty list`() = runTest {
        repository.failReads = true

        val viewModel = viewModel()

        assertEquals("無法讀取收藏", viewModel.errors.first())
        assertTrue(viewModel.favorites.value.isEmpty())
    }
}
