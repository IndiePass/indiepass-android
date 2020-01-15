package com.indieweb.indigenous.general;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.VolleyRequestListener;

abstract public class BaseFragment extends Fragment implements VolleyRequestListener, SwipeRefreshLayout.OnRefreshListener {

    private boolean showRefreshMessage = false;
    protected VolleyRequestListener volleyRequestListener;
    protected RelativeLayout layout;
    private SwipeRefreshLayout refreshLayout;
    protected User user;
    protected String debugResponse;
    private int refreshedMessage = R.string.items_refreshed;
    private LinearLayout noConnection;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = new Accounts(getContext()).getCurrentUser();

        // Refresh layout.
        try {
            refreshLayout = view.findViewById(R.id.refreshList);
        }
        catch (Exception ignored) {}

        // No connection widget.
        try {
            noConnection = view.findViewById(R.id.noConnection);
        }
        catch (Exception ignored) {}

        // Set listener.
        VolleyRequestListener(this);
    }

    @Override
    public void OnSuccessRequest(String response) { }

    @Override
    public void OnFailureRequest() {
        setShowRefreshedMessage(false);
        checkRefreshingStatus();
    }

    /**
     * Set the refreshed message.
     *
     * @param id
     *   The id of the refresh message.
     */
    protected void setRefreshedMessage(int id) {
        this.refreshedMessage = id;
    }

    /**
     * Gets the value of the show refresh message.
     *
     * @return boolean
     */
    private boolean showRefreshMessage() {
        return showRefreshMessage;
    }

    /**
     * Sets the value of the show refresh message variable.
     *
     * @param show
     *   Whether to show or not.
     */
    protected void setShowRefreshedMessage(boolean show) {
        this.showRefreshMessage = show;
    }

    /**
     * Sets the value of the the refresh layout.
     *
     * @param refresh
     *   Whether to layout refreshing or not.
     */
    protected void setLayoutRefreshing(boolean refresh) {
        this.refreshLayout.setRefreshing(refresh);
    }

    /**
     * Sets the refresh listener on the layout.
     */
    protected void setOnRefreshListener() {
        refreshLayout.setOnRefreshListener(this);
    }

    /**
     * Hides the no connection widget.
     */
    protected void showNoConnection() {
        noConnection.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the no connection widget.
     */
    protected void hideNoConnection() {
        noConnection.setVisibility(View.GONE);
    }

    /**
     * Shows the refresh layout.
     */
    protected void showRefreshLayout() {
        refreshLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Hides the refresh layout.
     */
    protected void hideRefreshLayout() {
        refreshLayout.setVisibility(View.GONE);
    }

    /**
     * Checks the state of the pull to refresh.
     */
    protected void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage()) {
                // This can throw an exception sometimes, so let's protect it.
                // (java.lang.IllegalStateException)
                try {
                    Snackbar.make(layout, getString(refreshedMessage), Snackbar.LENGTH_SHORT).show();
                }
                catch (Exception ignored) { }
            }
            setLayoutRefreshing(false);
        }
    }

    /**
     * Set request listener.
     *
     * @param volleyRequestListener
     *   The volley request listener.
     */
    private void VolleyRequestListener(VolleyRequestListener volleyRequestListener) {
        this.volleyRequestListener = volleyRequestListener;
    }

}
