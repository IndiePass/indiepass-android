package com.indieweb.indigenous;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
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
public class PostTypeFormsTest extends ActivityInstrumentationTestCase2<LaunchActivity> {

    private Activity launchedActivity;

    public PostTypeFormsTest() {
        super(LaunchActivity.class);
    }

    @Rule
    public final ActivityTestRule<LaunchActivity> rule = new ActivityTestRule<>(LaunchActivity.class, true, false);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Context context = (InstrumentationRegistry.getTargetContext());
        TestUtils.createAccount(context, false);
        Intent intent = new Intent();
        intent.putExtra("indigenousTesting", true);
        launchedActivity = rule.launchActivity(intent);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Context context = (InstrumentationRegistry.getTargetContext());
        TestUtils.removeAccount(context, launchedActivity);
    }

    @Test
    public void testAllPostTypeForms() {
        openDrawer();

        // Article
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createArticle));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Note
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createNote));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Like
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createLike));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());

        // Reply
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createReply));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Bookmark
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createBookmark));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Repost
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createRepost));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());

        // Event
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createEvent));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // RSVP
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createRSVP));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Issue
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createIssue));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Checkin
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createCheckin));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Geocache
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createGeocache));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform((click()));

        // Upload
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.nav_upload));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
    }

    /**
     * Open the navigation drawer.
     */
    public void openDrawer() {
        onView(withId(R.id.actionButton)).perform(click());
    }

}
