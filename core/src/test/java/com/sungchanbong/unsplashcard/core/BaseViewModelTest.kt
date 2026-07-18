package com.sungchanbong.unsplashcard.core

import app.cash.turbine.test
import com.sungchanbong.core.architecture.BaseViewModel
import com.sungchanbong.core.architecture.UIEffect
import com.sungchanbong.core.architecture.UIIntent
import com.sungchanbong.core.architecture.UIState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private data class TestState(val count: Int = 0) : UIState

    private sealed interface TestIntent : UIIntent {
        data object Increment : TestIntent
        data object Fire : TestIntent
    }

    private data class TestEffect(val value: Int) : UIEffect

    private class TestViewModel : BaseViewModel<TestState, TestIntent, TestEffect>(TestState()) {
        override fun onIntent(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> reduce { copy(count = count + 1) }
                TestIntent.Fire -> postSideEffect(TestEffect(currentState.count))
            }
        }

        fun emit(value: Int) = postSideEffect(TestEffect(value))
    }

    @Test
    fun `intent 는 state 를 갱신한다`() = runTest {
        val viewModel = TestViewModel()
        viewModel.onIntent(TestIntent.Increment)
        viewModel.onIntent(TestIntent.Increment)
        assertEquals(2, viewModel.state.value.count)
    }


    @Test
    fun `구독자가 없을 때 발행된 effect 도 유실되지 않는다`() = runTest {
        val viewModel = TestViewModel()
        viewModel.emit(1)
        viewModel.emit(2)
        viewModel.effect.test {
            assertEquals(TestEffect(1), awaitItem())
            assertEquals(TestEffect(2), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reduce 와 postSideEffect 의 순서가 보장된다`() = runTest {
        val viewModel = TestViewModel()
        viewModel.onIntent(TestIntent.Increment)
        viewModel.onIntent(TestIntent.Fire)
        viewModel.effect.test {
            assertEquals(TestEffect(1), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
