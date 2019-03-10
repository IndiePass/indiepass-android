package com.indieweb.indigenous;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MicrosubTest extends ActivityInstrumentationTestCase2<LaunchActivity> {

    private Activity launchedActivity;

    public MicrosubTest() {
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
    public void testIndieAuthLaunch() {
        onView(withId(R.id.noMicrosubEndpoint))
                .check(matches(withText(R.string.no_microsub_endpoint)));
    }

}
