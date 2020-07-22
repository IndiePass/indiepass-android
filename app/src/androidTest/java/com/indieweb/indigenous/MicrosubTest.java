package com.indieweb.indigenous;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MicrosubTest extends ActivityInstrumentationTestCase2<LaunchActivity> {

    private Activity launchedActivity;

    public MicrosubTest() {
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
