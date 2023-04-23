package com.indieweb.indigenous;

import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class WritePostTest extends ActivityTestRule<LaunchActivity> {

    public WritePostTest() {
        super(LaunchActivity.class);
    }

    @Rule
    public final ActivityTestRule<LaunchActivity> rule = new ActivityTestRule<>(LaunchActivity.class, true, true);

    @Test
    public void testDiscardDialog() {
        openDrawer();

        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createArticle));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));
    }

    /**
     * Open the navigation drawer.
     */
    public void openDrawer() {
        onView(withId(R.id.actionButton)).perform(click());
    }

}
