package com.mytheclipse.quizbattle.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mytheclipse.quizbattle.ui.theme.QuizBattleTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quizBattleButton_displaysText() {
        composeTestRule.setContent {
            QuizBattleTheme {
                QuizBattleButton(
                    text = "Test Button",
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test Button").assertIsDisplayed()
    }

    @Test
    fun quizBattleButton_clickable() {
        var clicked = false

        composeTestRule.setContent {
            QuizBattleTheme {
                QuizBattleButton(
                    text = "Click Me",
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Click Me").performClick()
        assert(clicked)
    }

    @Test
    fun quizBattleButton_disabledState() {
        var clicked = false

        composeTestRule.setContent {
            QuizBattleTheme {
                QuizBattleButton(
                    text = "Disabled",
                    onClick = { clicked = true },
                    enabled = false
                )
            }
        }

        val button = composeTestRule.onNodeWithText("Disabled")
        button.assertIsDisplayed()
        button.assertIsNotEnabled()
        button.performClick()
        assert(!clicked) // Should not trigger onClick when disabled
    }

    @Test
    fun quizBattleOutlinedButton_displaysText() {
        composeTestRule.setContent {
            QuizBattleTheme {
                QuizBattleOutlinedButton(
                    text = "Outlined",
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Outlined").assertIsDisplayed()
    }

    @Test
    fun quizBattleOutlinedButton_clickable() {
        var clicked = false

        composeTestRule.setContent {
            QuizBattleTheme {
                QuizBattleOutlinedButton(
                    text = "Outlined Click",
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("Outlined Click").performClick()
        assert(clicked)
    }

    @Test
    fun quizAnswerButton_showsCorrectState() {
        composeTestRule.setContent {
            QuizBattleTheme {
                QuizAnswerButton(
                    text = "Answer A",
                    onClick = {},
                    isSelected = true,
                    isCorrect = true,
                    enabled = false
                )
            }
        }

        composeTestRule.onNodeWithText("Answer A").assertIsDisplayed()
    }

    @Test
    fun quizAnswerButton_clickWhenEnabled() {
        var clicked = false

        composeTestRule.setContent {
            QuizBattleTheme {
                QuizAnswerButton(
                    text = "Answer B",
                    onClick = { clicked = true },
                    isSelected = false,
                    isCorrect = null,
                    enabled = true
                )
            }
        }

        composeTestRule.onNodeWithText("Answer B").performClick()
        assert(clicked)
    }
}
