package com.mytheclipse.quizbattle.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mytheclipse.quizbattle.ui.theme.QuizBattleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StateComponentsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_displaysMessageAndSpinner() {
        composeTestRule.setContent {
            QuizBattleTheme {
                LoadingState(message = "Loading test...")
            }
        }

        composeTestRule.onNodeWithText("Loading test...").assertIsDisplayed()
        // Progress indicator should be visible (has semantics)
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun errorState_displaysMessageAndRetryButton() {
        var retryClicked = false

        composeTestRule.setContent {
            QuizBattleTheme {
                ErrorState(
                    message = "Something went wrong",
                    onRetry = { retryClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Terjadi Kesalahan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        
        val retryButton = composeTestRule.onNodeWithText("Coba Lagi")
        retryButton.assertIsDisplayed()
        retryButton.performClick()

        assert(retryClicked)
    }

    @Test
    fun errorState_withoutRetry_doesNotShowButton() {
        composeTestRule.setContent {
            QuizBattleTheme {
                ErrorState(
                    message = "Error without retry",
                    onRetry = null
                )
            }
        }

        composeTestRule.onNodeWithText("Error without retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Coba Lagi").assertDoesNotExist()
    }

    @Test
    fun emptyState_displaysIconAndMessage() {
        composeTestRule.setContent {
            QuizBattleTheme {
                EmptyState(
                    icon = Icons.Default.CheckCircle,
                    title = "No Data",
                    message = "Nothing to show here"
                )
            }
        }

        composeTestRule.onNodeWithText("No Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nothing to show here").assertIsDisplayed()
    }

    @Test
    fun emptyState_withAction_displaysButton() {
        var actionClicked = false

        composeTestRule.setContent {
            QuizBattleTheme {
                EmptyState(
                    title = "Empty",
                    message = "Add something",
                    actionText = "Add Now",
                    onAction = { actionClicked = true }
                )
            }
        }

        val button = composeTestRule.onNodeWithText("Add Now")
        button.assertIsDisplayed()
        button.performClick()

        assert(actionClicked)
    }

    @Test
    fun emptyState_withoutAction_doesNotShowButton() {
        composeTestRule.setContent {
            QuizBattleTheme {
                EmptyState(
                    title = "Empty",
                    message = "Nothing here",
                    actionText = null,
                    onAction = null
                )
            }
        }

        composeTestRule.onNodeWithText("Empty").assertIsDisplayed()
        // No button should be present
        composeTestRule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }
}
