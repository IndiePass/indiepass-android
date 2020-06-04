package com.indieweb.indigenous;

import android.accounts.Account;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.general.AboutFragment;
import com.indieweb.indigenous.general.SettingsFragment;
import com.indieweb.indigenous.indieauth.AnonymousFragment;
import com.indieweb.indigenous.indieauth.UsersFragment;
import com.indieweb.indigenous.micropub.BaseCreate;
import com.indieweb.indigenous.micropub.contact.ContactFragment;
import com.indieweb.indigenous.micropub.draft.DraftFragment;
import com.indieweb.indigenous.micropub.post.ArticleActivity;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.CheckinActivity;
import com.indieweb.indigenous.micropub.post.EventActivity;
import com.indieweb.indigenous.micropub.post.GeocacheActivity;
import com.indieweb.indigenous.micropub.post.IssueActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.NoteActivity;
import com.indieweb.indigenous.micropub.post.ReadActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.micropub.post.TripActivity;
import com.indieweb.indigenous.micropub.post.UploadActivity;
import com.indieweb.indigenous.micropub.post.VenueActivity;
import com.indieweb.indigenous.micropub.source.PostListFragment;
import com.indieweb.indigenous.microsub.channel.ChannelFragment;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.pushy.sdk.Pushy;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DraftFragment.OnDraftChangedListener, SettingsFragment.onPreferenceChangeListener {

    User user;
    Menu drawerMenu;
    DrawerLayout drawer;
    NavigationView navigationView;
    View headerView;
    public static final int CREATE_DRAFT = 1001;
    public static final int POST_DRAFT = 1002;
    public static final int RESULT_DRAFT_SAVED = 1005;
    public static final int UPDATE_POST = 1006;
    public static final int EDIT_IMAGE = 1007;

    @Override
    public void onAttachFragment(@NonNull Fragment fragment) {

        if (fragment instanceof DraftFragment) {
            DraftFragment draftFragment = (DraftFragment) fragment;
            draftFragment.OnDraftChangedListener(this);
        }

        if (fragment instanceof SettingsFragment) {
            SettingsFragment settingsFragment = (SettingsFragment) fragment;
            settingsFragment.OnPreferenceChangeListener(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_DRAFT && resultCode == RESULT_OK) {
            closeDrawer(true);
            Snackbar.make(drawer, getString(R.string.post_success), Snackbar.LENGTH_SHORT).show();
        }

        if (requestCode == UPDATE_POST && resultCode == RESULT_OK) {
            Snackbar.make(drawer, getString(R.string.post_update_success), Snackbar.LENGTH_SHORT).show();
        }

        if (requestCode == CREATE_DRAFT && resultCode == RESULT_DRAFT_SAVED) {
            closeDrawer(true);
            setDraftMenuItemTitle(true);
            Snackbar.make(drawer, getString(R.string.draft_saved), Snackbar.LENGTH_SHORT).show();
        }

        if (requestCode == POST_DRAFT && resultCode == RESULT_OK) {
            setDraftMenuItemTitle(true);
            Snackbar.make(drawer, getString(R.string.post_success), Snackbar.LENGTH_SHORT).show();
            startFragment(new DraftFragment());
        }
    }

    /**
     * Set first navigation view.
     */
    private void setFirstItemNavigationView() {
        navigationView.setCheckedItem(R.id.nav_reader);
        drawerMenu.performIdentifierAction(R.id.nav_reader, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user = new Accounts(this).getDefaultUser();

        // Let pushy listener restart if necessary, and if configured.
        //noinspection ConstantConditions
        if (
            BuildConfig.SITE_DEVICE_REGISTRATION_ENDPOINT.length() > 0 &&
            BuildConfig.SITE_ACCOUNT_CHECK_ENDPOINT.length() > 0 &&
            Preferences.getPreference(getApplicationContext(), "push_notification_type", "none").equals("pushy") &&
            user.isAuthenticated()
        ) {
            Pushy.listen(this);
        }

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerMenu = navigationView.getMenu();
        setFirstItemNavigationView();

        // Set user information.
        headerView = navigationView.getHeaderView(0);
        User user = new Accounts(getApplicationContext()).getDefaultUser();
        setAccountInfo(user);
        if (new Accounts(this).getCount() > 1) {

            Button swapAccount = headerView.findViewById(R.id.navAccountSwitch);
            if (swapAccount != null) {
                swapAccount.setVisibility(View.VISIBLE);
                swapAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final List<String> accounts = new ArrayList<>();
                        final Account[] AllAccounts = new Accounts(MainActivity.this).getAllAccounts();
                        for (Account account: AllAccounts) {
                            accounts.add(account.name);
                        }
                        final CharSequence[] accountItems = accounts.toArray(new CharSequence[accounts.size()]);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.default_user));
                        builder.setCancelable(true);
                        builder.setItems(accountItems, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int index) {
                                User defaultUser = new Accounts(getApplicationContext()).getUser(accounts.get(index), false);
                                setAccountInfo(defaultUser);
                                Snackbar.make(drawer, String.format(getString(R.string.account_selected), accounts.get(index)), Snackbar.LENGTH_SHORT).show();
                                SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                                editor.putString("account", accounts.get(index));
                                editor.apply();
                            }
                        });
                        builder.show();
                    }
                });
            }
        }

        hideItemsInDrawerMenu();
        setDraftMenuItemTitle(false);
        Utility.setNightTheme(getApplicationContext());

        if (user.isAnonymous()) {
            Snackbar.make(drawer, getString(R.string.anonymous_info_snack), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            closeDrawer(false);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        boolean close = false;

        Fragment fragment = null;
        switch (item.getItemId()) {

            case R.id.nav_reader:
                close = true;
                fragment = new ChannelFragment();
                break;

            case R.id.nav_create:
                toggleGroupItems(false);
                break;

            case R.id.nav_main_menu:
                toggleGroupItems(true);
                break;

            case R.id.nav_drafts:
                close = true;
                fragment = new DraftFragment();
                break;

            case R.id.nav_contacts:
                close = true;
                fragment = new ContactFragment();
                break;

            case R.id.nav_posts:
                close = true;
                fragment = new PostListFragment();
                break;

            case R.id.nav_accounts:
                close = true;
                if (user.isAuthenticated()) {
                    fragment = new UsersFragment();
                }
                else {
                    fragment = new AnonymousFragment();
                }
                break;

            case R.id.nav_settings:
                close = true;
                fragment = new SettingsFragment();
                break;

            case R.id.nav_about:
                close = true;
                fragment = new AboutFragment();
                break;

            case R.id.createArticle:
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                startActivityForResult(CreateArticle, CREATE_DRAFT);
                break;

            case R.id.createNote:
                Intent CreateNote = new Intent(getBaseContext(), NoteActivity.class);
                startActivityForResult(CreateNote, CREATE_DRAFT);
                break;

            case R.id.createLike:
                Intent CreateLike = new Intent(getBaseContext(), LikeActivity.class);
                startActivityForResult(CreateLike, CREATE_DRAFT);
                break;

            case R.id.createReply:
                Intent CreateReply = new Intent(getBaseContext(), ReplyActivity.class);
                startActivityForResult(CreateReply, CREATE_DRAFT);
                break;

            case R.id.createBookmark:
                Intent CreateBookmark = new Intent(getBaseContext(), BookmarkActivity.class);
                startActivityForResult(CreateBookmark, CREATE_DRAFT);
                break;

            case R.id.createRead:
                Intent CreateRead = new Intent(getBaseContext(), ReadActivity.class);
                startActivityForResult(CreateRead, CREATE_DRAFT);
                break;

            case R.id.createRepost:
                Intent CreateRepost = new Intent(getBaseContext(), RepostActivity.class);
                startActivityForResult(CreateRepost, CREATE_DRAFT);
                break;

            case R.id.createEvent:
                Intent CreateEvent = new Intent(getBaseContext(), EventActivity.class);
                startActivityForResult(CreateEvent, CREATE_DRAFT);
                break;

            case R.id.createRSVP:
                Intent CreateRSVP = new Intent(getBaseContext(), RsvpActivity.class);
                startActivityForResult(CreateRSVP, CREATE_DRAFT);
                break;

            case R.id.createIssue:
                Intent CreateIssue = new Intent(getBaseContext(), IssueActivity.class);
                startActivityForResult(CreateIssue, CREATE_DRAFT);
                break;

            case R.id.createCheckin:
                Intent CreateCheckin = new Intent(getBaseContext(), CheckinActivity.class);
                startActivityForResult(CreateCheckin, CREATE_DRAFT);
                break;

            case R.id.createTrip:
                Intent CreateTrip = new Intent(getBaseContext(), TripActivity.class);
                startActivityForResult(CreateTrip, CREATE_DRAFT);
                break;

            case R.id.createVenue:
                Intent CreateVenue = new Intent(getBaseContext(), VenueActivity.class);
                startActivityForResult(CreateVenue, CREATE_DRAFT);
                break;

            case R.id.createGeocache:
                Intent CreateGeocache = new Intent(getBaseContext(), GeocacheActivity.class);
                startActivityForResult(CreateGeocache, CREATE_DRAFT);
                break;

            case R.id.nav_upload:
            case R.id.nav_upload2:
                Intent CreateMedia = new Intent(getBaseContext(), UploadActivity.class);
                startActivity(CreateMedia);
                break;
        }

        if (close) {
            closeDrawer(false);
        }

        // Update main content frame.
        if (fragment != null) {
            startFragment(fragment);
        }

        return true;
    }

    /**
     * Start a fragment.
     *
     * @param fragment
     *   Start a fragment.
     */
    public void startFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    /**
     * Open the navigation drawer.
     *
     * @param id
     *   The menu item id to perform an action on.
     */
    public void openDrawer(int id) {
        if (drawer != null) {
            drawer.openDrawer(GravityCompat.START);
        }

        if (id > 0) {
            navigationView.setCheckedItem(id);
            drawerMenu.performIdentifierAction(id, 0);
        }
    }

    /**
     * Close drawer.
     */
    public void closeDrawer(boolean checkIfOpened) {
        if (drawer == null) {
            return;
        }

        if (checkIfOpened && !drawer.isDrawerOpen(GravityCompat.START)) {
            return;
        }

        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Sets the Draft menu item.
     */
    public void setDraftMenuItemTitle(boolean callbackOrPost) {
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        int draftCount = db.getDraftCount();
        if (draftCount > 0) {
            MenuItem draftItem = drawerMenu.findItem(R.id.nav_drafts);
            if (draftItem != null) {
                draftItem.setTitle(getString(R.string.drafts) + " (" + draftCount + ")");
            }
        }
        else if (callbackOrPost) {
            MenuItem draftItem = drawerMenu.findItem(R.id.nav_drafts);
            draftItem.setTitle(getString(R.string.drafts));
        }
    }

    /**
     * Toggle the visibility of the drawer menu groups.
     *
     * @param mainGroupVisibility
     *  Whether the main group is visible or not.
     */
    public void toggleGroupItems(boolean mainGroupVisibility) {
        drawerMenu.setGroupVisible(R.id.navMainGroup, mainGroupVisibility);
        drawerMenu.setGroupVisible(R.id.navPostGroup, !mainGroupVisibility);

        // setGroupVisible sets all menu items to visible, so we need to check whether some need to
        // be invisible.
        if (!mainGroupVisibility) {
            if (Preferences.getPreference(this, "pref_key_post_type_hide", false)) {
                hidePostTypes();
            }
        }

        hideItemsInDrawerMenu();
    }

    /**
     * Hide items in the drawer menu.
     */
    public void hideItemsInDrawerMenu() {

        // Hide Media if micropub media endpoint is empty.
        String micropubMediaEndpoint = user.getMicropubMediaEndpoint();
        if (micropubMediaEndpoint == null || micropubMediaEndpoint.length() == 0 || user.isAnonymous()) {
            setMenuItemVisibility(R.id.nav_upload, false);
            setMenuItemVisibility(R.id.nav_upload2, false);
        }

        if (user.isAnonymous()) {
            setMenuItemVisibility(R.id.nav_settings, false);
        }

        // Hide Posts if setting is not enabled.
        if (!Preferences.getPreference(this, "pref_key_source_post_list", false) || user.isAnonymous()) {
            setMenuItemVisibility(R.id.nav_posts, false);
        }

        // Hide Contacts if setting is not enabled.
        if (!Preferences.getPreference(this, "pref_key_contact_manage", false) || user.isAnonymous()) {
            setMenuItemVisibility(R.id.nav_contacts, false);
        }

    }

    /**
     * Sets the visibility of a menu item.
     *
     * @param id
     *   The menu item id
     */
    public void setMenuItemVisibility(int id, boolean visibility) {
        MenuItem target = drawerMenu.findItem(id);
        target.setVisible(visibility);
    }

    /**
     * Hide post type menu items.
     */
    public void hidePostTypes() {

        if (user.isAnonymous()) {
            return;
        }

        String postTypes = user.getPostTypes();
        ArrayList<String> postTypeList = new ArrayList<>();
        if (postTypes != null && postTypes.length() > 0) {
            try {
                JSONObject object;
                JSONArray itemList = new JSONArray(postTypes);

                for (int i = 0; i < itemList.length(); i++) {
                    object = itemList.getJSONObject(i);
                    String type = object.getString("type");
                    postTypeList.add(type);
                }

            }
            catch (JSONException ignored) { }
        }

        if (postTypeList.size() == 0) {
            return;
        }

        // Loop over menu items.
        MenuItem item;
        String menuType = null;
        for (int i = 0; i < drawerMenu.size(); i++) {
            item = drawerMenu.getItem(i);
            int id = item.getItemId();
            switch (id) {
                case R.id.createNote:
                    menuType = "note";
                    break;
                case R.id.createArticle:
                    menuType = "article";
                    break;
                case R.id.createLike:
                    menuType = "like";
                    break;
                case R.id.createBookmark:
                    menuType = "bookmark";
                    break;
                case R.id.createReply:
                    menuType = "reply";
                    break;
                case R.id.createRepost:
                    menuType = "repost";
                    break;
                case R.id.createEvent:
                    menuType = "event";
                    break;
                case R.id.createRSVP:
                    menuType = "rsvp";
                    break;
                case R.id.createRead:
                    menuType = "read";
                    break;
                case R.id.createIssue:
                    menuType = "issue";
                    break;
                case R.id.createCheckin:
                    menuType = "checkin";
                    break;
                case R.id.createVenue:
                    menuType = "venue";
                    break;
                case R.id.createGeocache:
                    menuType = "geocache";
                    break;
            }

            if (menuType != null && !postTypeList.contains(menuType)) {
                setMenuItemVisibility(id, false);
            }

            // Reset.
            menuType = null;
        }
    }

    @Override
    public void onDraftChanged() {
        setDraftMenuItemTitle(true);
    }

    @Override
    public void onPreferenceChanged(int id, boolean enabled) {
        drawerMenu.findItem(id).setVisible(enabled);
    }

    /**
     * Set account info
     *
     * @param user
     *   The user
     */
    public void setAccountInfo(User user) {
        TextView authorUrl = headerView.findViewById(R.id.navAuthorUrl);
        if (authorUrl != null) {
            authorUrl.setVisibility(View.VISIBLE);
            authorUrl.setText(Utility.stripEndingSlash(user.getMeWithoutProtocol()));
        }

        if (user.getName().length() > 0) {
            TextView authorName = headerView.findViewById(R.id.navAuthorName);
            if (authorName != null) {
                authorName.setVisibility(View.VISIBLE);
                authorName.setText(user.getName());
            }
        }

        if (user.getAvatar().length() > 0) {
            ImageView authorAvatar = headerView.findViewById(R.id.navAuthorAvatar);
            if (authorAvatar != null) {
                Glide.with(getApplicationContext())
                        .load(user.getAvatar())
                        .apply(RequestOptions.circleCropTransform())
                        .into(authorAvatar);
            }
        }


    }

}
