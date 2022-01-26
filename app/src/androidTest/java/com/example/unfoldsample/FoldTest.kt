package com.example.unfoldsample

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.testing.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.microsoft.device.dualscreen.windowstate.WindowState
import kotlinx.coroutines.flow.collect
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FoldTest {
    private val composeTestRule = createAndroidComposeRule<MainActivity>()
    private val publisherRule = WindowLayoutInfoPublisherRule()

    @get: Rule
    val testRule: TestRule

    init {
        testRule = RuleChain.outerRule(publisherRule).around(composeTestRule)
        RuleChain.outerRule(composeTestRule)
    }

    /**
     * Test passes - changes to "Fold present" screen
     */
    @Test
    fun simulate_fold() {
        composeTestRule.setContent {
            // Initialize app with no fold present (hasFold = false)
            val hasFold = rememeberFoldState(initialValue = false).first
            UnfoldSample(WindowState(hasFold = hasFold))
        }

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()

        // Simulate span/vertical fold
        publisherRule.simulateSpan(composeTestRule.activityRule)

        // Check that text says "vertical fold present"
        composeTestRule.onNodeWithText("Vertical fold present").assertIsDisplayed()
    }

    /**
     * Test fails - does not change to "No fold present" screen
     */
    @Test
    fun simulate_unspan() {
        composeTestRule.setContent {
            // Initialize app with vertical fold present (hasFold = true)
            val hasFold = rememeberFoldState(initialValue = true).first
            UnfoldSample(WindowState(hasFold = hasFold))
        }

        // Check that text says "vertical fold present"
        composeTestRule.onNodeWithText("Vertical fold present").assertIsDisplayed()

        // Simulate unspan/no fold
        publisherRule.simulateUnspan()

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()
    }

    /**
     * Test fails - does not change to "No fold present" screen at line 107
     */
    @Test
    fun simulate_span_then_unspan() {
        composeTestRule.setContent {
            // Initialize app with no fold present (hasFold = false)
            val hasFold = rememeberFoldState(initialValue = false).first
            UnfoldSample(WindowState(hasFold = hasFold))
        }

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()

        // Simulate span/fold
        publisherRule.simulateSpan(composeTestRule.activityRule)

        // Check that text says "vertical fold present"
        composeTestRule.onNodeWithText("Vertical fold present").assertIsDisplayed()

        // Simulate unspan/no fold
        publisherRule.simulateUnspan()

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()
    }

    /**
     * Test passes - screen changes successfully after two simulate fold events
     */
    @Test
    fun simulate_vert_fold_then_horiz_fold() {
        composeTestRule.setContent {
            // Initialize app with no fold present (hasFold = false)
            val foldState = rememeberFoldState(initialValue = false)
            val hasFold = foldState.first
            val foldIsHorizontal = foldState.second
            UnfoldSample(WindowState(hasFold = hasFold, isFoldHorizontal = foldIsHorizontal))
        }

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()

        // Simulate span/vertical fold
        publisherRule.simulateSpan(composeTestRule.activityRule)

        // Check that text says "vertical fold present"
        composeTestRule.onNodeWithText("Vertical fold present").assertIsDisplayed()

        // Simulate span/horizontal fold
        publisherRule.simulateSpan(composeTestRule.activityRule, orientation = FoldingFeature.Orientation.HORIZONTAL)

        // Check that text says "horizontal fold present"
        composeTestRule.onNodeWithText("Horizontal fold present").assertIsDisplayed()
    }

    /**
     * Helper method for simulating no folds/unspanning event
     */
    private fun WindowLayoutInfoPublisherRule.simulateUnspan() {
        val windowLayoutInfo = TestWindowLayoutInfo(emptyList())
        overrideWindowLayoutInfo(windowLayoutInfo)
    }

    /**
     * Helper method for simulating a fold/spanning event
     */
    private fun <A : ComponentActivity> WindowLayoutInfoPublisherRule.simulateSpan(
        activityRule: ActivityScenarioRule<A>,
        center: Int = -1,
        size: Int = 0,
        state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED,
        orientation: FoldingFeature.Orientation = FoldingFeature.Orientation.VERTICAL,
    ) {
        activityRule.scenario.onActivity { activity ->
            val fold = FoldingFeature(
                activity = activity,
                center = center,
                size = size,
                state = state,
                orientation = orientation
            )
            val windowLayoutInfo = TestWindowLayoutInfo(listOf(fold))
            overrideWindowLayoutInfo(windowLayoutInfo)
        }
    }

    @Composable
    private fun rememeberFoldState(initialValue: Boolean, orientation: FoldingFeature.Orientation = FoldingFeature.Orientation.VERTICAL): Pair<Boolean, Boolean> {
        val windowLayoutInfo =
            WindowInfoTracker.getOrCreate(composeTestRule.activity).windowLayoutInfo(composeTestRule.activity)

        var hasFold by remember { mutableStateOf(initialValue) }
        var foldIsHorizontal by remember { mutableStateOf(initialValue) }

        LaunchedEffect(windowLayoutInfo) {
            windowLayoutInfo.collect { newLayoutInfo ->
                val foldingFeature =
                    newLayoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                foldingFeature?.let {
                    hasFold = true
                    foldIsHorizontal = it.orientation == FoldingFeature.Orientation.HORIZONTAL
                }
            }
        }

        return Pair(hasFold, foldIsHorizontal)
    }
}