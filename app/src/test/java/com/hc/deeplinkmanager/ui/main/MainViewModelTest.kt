package com.hc.deeplinkmanager.ui.main

import com.hc.deeplinkmanager.data.local.TagEntity
import com.hc.deeplinkmanager.data.repo.DeeplinkRepository
import com.hc.deeplinkmanager.data.repo.TagRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val deeplinkRepo = mockk<DeeplinkRepository>()
    private val tagRepo = mockk<TagRepository>()

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.createViewModel(): MainViewModel {
        every { tagRepo.observeAll() } returns flowOf(emptyList())
        every { deeplinkRepo.observeAll() } returns flowOf(emptyList())
        every { deeplinkRepo.observeByTag(any()) } returns flowOf(emptyList())
        val dispatcher = StandardTestDispatcher(scheduler = testScheduler)
        Dispatchers.setMain(dispatcher)
        val vm = MainViewModel(deeplinkRepo, tagRepo, dispatcher)
        backgroundScope.launch { vm.uiState.collect { } }
        return vm
    }

    @Test
    fun `valid shared url opens Add sheet with derived name`() = runTest {
        val vm = createViewModel()

        vm.onSharedText("Try https://pay.example.com/checkout now", null)
        advanceUntilIdle()

        val sheet = vm.uiState.value.sheet as? SheetState.Add
        assertEquals("pay.example.com", sheet?.name)
        assertEquals("https://pay.example.com/checkout", sheet?.url)
        assertEquals(TagEntity.UNGROUPED_ID, sheet?.tagId)
    }

    @Test
    fun `subject overrides derived name`() = runTest {
        val vm = createViewModel()

        vm.onSharedText("https://foo.example.com/x", "Checkout Flow")
        advanceUntilIdle()

        val sheet = vm.uiState.value.sheet as? SheetState.Add
        assertEquals("Checkout Flow", sheet?.name)
        assertEquals("https://foo.example.com/x", sheet?.url)
    }

    @Test
    fun `no url in shared text shows message and keeps sheet hidden`() = runTest {
        val vm = createViewModel()

        vm.onSharedText("just some words", null)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.sheet is SheetState.Hidden)
        assertEquals("No valid URL found in shared text", state.transientMessage)
    }

    @Test
    fun `shared deeplink defaults to active filter tag`() = runTest {
        val vm = createViewModel()

        vm.selectFilter(TagFilter.Tag(5L))
        vm.onSharedText("myapp://open", null)
        advanceUntilIdle()

        val sheet = vm.uiState.value.sheet as? SheetState.Add
        assertEquals(5L, sheet?.tagId)
    }
}
