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
import com.microsoft.device.dualscreen.windowstate.FoldState
import com.microsoft.device.dualscreen.windowstate.WindowState
import com.microsoft.device.dualscreen.windowstate.rememberWindowState
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

    @Test
    fun simulate_fold() {
        composeTestRule.setContent {
            // Initialize app with no fold present (hasFold = false)
            val hasFold = rememeberFold(initialValue = false)
            UnfoldSample(WindowState(hasFold = hasFold))
        }

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()

        // Simulate fold
        publisherRule.simulateFold(composeTestRule.activityRule)

        // Check that text says "fold present"
        composeTestRule.onNodeWithText("Fold present").assertIsDisplayed()
    }

    @Test
    fun simulate_unfold() {
        composeTestRule.setContent {
            // Initialize app with fold present (hasFold = true)
            val hasFold = rememeberFold(initialValue = false)
            UnfoldSample(WindowState(hasFold = hasFold))
        }

        // Check that text says "fold present"
        composeTestRule.onNodeWithText("Fold present").assertIsDisplayed()

        // Simulate fold
        publisherRule.simulateUnfold()

        // Check that text says "no fold present"
        composeTestRule.onNodeWithText("No fold present").assertIsDisplayed()
    }

    /**
     * Helper method for simulating "unfold"
     */
    private fun WindowLayoutInfoPublisherRule.simulateUnfold() {
        val windowLayoutInfo = TestWindowLayoutInfo(emptyList())
        overrideWindowLayoutInfo(windowLayoutInfo)
    }

    /**
     * Helper method for simulating "fold"
     */
    private fun <A : ComponentActivity> WindowLayoutInfoPublisherRule.simulateFold(
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
    private fun rememeberFold(initialValue: Boolean): Boolean {
        val windowLayoutInfo =
            WindowInfoTracker.getOrCreate(composeTestRule.activity).windowLayoutInfo(composeTestRule.activity)

        var hasFold by remember { mutableStateOf(initialValue) }

        LaunchedEffect(windowLayoutInfo) {
            windowLayoutInfo.collect { newLayoutInfo ->
                val foldingFeature =
                    newLayoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
                foldingFeature?.let {
                    hasFold = true
                }
            }
        }

        return hasFold
    }
}