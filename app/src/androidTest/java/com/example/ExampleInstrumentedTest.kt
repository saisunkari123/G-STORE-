package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    // This rule automatically launches your MainActivity before each test
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFullCustomerFlow() {
        // --- 1. LOGIN FLOW ---
        // Wait for the splash screen to finish (up to 5 seconds)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("10 digit number").fetchSemanticsNodes().isNotEmpty()
        }
        
        // Enter credentials and click Login
        // TODO: REPLACE "6300028491" WITH A VALID PHONE NUMBER REGISTERED IN YOUR APP
        composeTestRule.onNodeWithText("10 digit number").performTextInput("6300028491")
        // TODO: REPLACE "Sai@1234" WITH THE ACTUAL PASSWORD
        composeTestRule.onNodeWithText("••••••••").performTextInput("Sai@1234")
        composeTestRule.onNode(hasText("Login") and hasClickAction()).performClick()
        
        // --- 2. SHOPPING FILTERS FLOW ---
        // Wait for Home Screen to appear after login (up to 8 seconds)
        composeTestRule.waitUntil(timeoutMillis = 8000) {
            composeTestRule.onAllNodesWithText("Continue Shopping").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Continue Shopping").assertIsDisplayed()

        // Click the "Rice Bags" category tab at the top
        composeTestRule.onNodeWithText("Rice Bags").performClick()

        // Open the Sort options
        composeTestRule.onNodeWithText("Sort By").performClick()

        // Select "Price: Low to High"
        composeTestRule.onNodeWithText("Price: Low to High").performClick()

        // Verify that the UI successfully registered the click
        composeTestRule.onNodeWithText("Sort By").assertIsDisplayed()
    }
}
