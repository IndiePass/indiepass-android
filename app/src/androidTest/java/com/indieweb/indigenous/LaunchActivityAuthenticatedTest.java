package com.indieweb.indigenous;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.Context.MODE_PRIVATE;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.indieweb.indigenous.indieauth.IndieAuthActivity.ACCOUNT_TYPE;
import static com.indieweb.indigenous.indieauth.IndieAuthActivity.TOKEN_TYPE;

@RunWith(AndroidJUnit4.class)
public class LaunchActivityAuthenticatedTest extends ActivityInstrumentationTestCase2<LaunchActivity> {

    private Activity launchedActivity;

    public LaunchActivityAuthenticatedTest() {
        super(LaunchActivity.class);
    }

    @Rule
    public ActivityTestRule<LaunchActivity> rule = new ActivityTestRule<>(LaunchActivity.class, true, false);

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Context context = (InstrumentationRegistry.getTargetContext());

        String domain = "http://example.com";
        String accessToken = "awesome";

        AccountManager am = AccountManager.get(context);
        Account account = new Account(domain, ACCOUNT_TYPE);
        am.addAccountExplicitly(account, null, null);
        am.setAuthToken(account, TOKEN_TYPE, accessToken);
        am.setUserData(account, "micropub_endpoint", domain + "/micropub");
        am.setUserData(account, "authorization_endpoint", domain + "/auth");
        am.setUserData(account, "token_endpoint", domain + "/token");
        am.setUserData(account, "author_name", "Indigenous");

        // Set first account.
        SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
        editor.putString("account", domain);
        editor.apply();
        editor.commit();

        Intent intent = new Intent();
        launchedActivity = rule.launchActivity(intent);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Context context = (InstrumentationRegistry.getTargetContext());
        User user = new Accounts(context).getCurrentUser();
        AccountManager am = AccountManager.get(context);
        am.removeAccount(user.getAccount(), launchedActivity, null, null);

        SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
        editor.putString("account", "");
        editor.apply();
        editor.commit();
    }

    @Test
    public void testIndieAuthLaunch() {
        onView(withId(R.id.noMicrosubEndpoint))
                .check(matches(withText(R.string.no_microsub_endpoint)));
    }

}
