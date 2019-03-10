package com.indieweb.indigenous;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.EditText;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PostTypeFormsTest extends ActivityInstrumentationTestCase2<LaunchActivity> {

    private Activity launchedActivity;

    public PostTypeFormsTest() {
        super(LaunchActivity.class);
    }

    @Rule
    public ActivityTestRule<LaunchActivity> rule = new ActivityTestRule<>(LaunchActivity.class, true, false);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Context context = (InstrumentationRegistry.getTargetContext());
        TestUtils.createAccount(context, false);
        Intent intent = new Intent();
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
        onView(withId(R.id.nav_view)).perform(NavigationViewActions.navigateTo(R.id.createNote));
        onView(withId(R.id.body)).perform(replaceText("Hello!"));
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        onView(withText(R.string.confirm_close)).inRoot(isDialog()).check(matches(isDisplayed()));
    }

    /**
     * Open the navigation drawer.
     */
    public void openDrawer() {
        onView(withId(R.id.actionButton)).perform(click());
    }

}
