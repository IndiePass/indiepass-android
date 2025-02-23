package com.indieweb.indigenous.util;

import static com.indieweb.indigenous.users.AuthActivity.INDIEWEB_TOKEN_TYPE;

import android.accounts.AccountManager;
import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.AuthActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

class TokenRefreshRequest extends StringRequest {
    private final Context context;
    private final User user;
    private final Semaphore done;
    private VolleyError failure;

    TokenRefreshRequest(Context context, User user) {
        super(Request.Method.POST, user.getTokenEndpoint(), null, null);
        this.context = context;
        this.user = user;
        done = new Semaphore(0);
        failure = null;
    }

    @Override
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", AuthActivity.ClientId);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", user.getRefreshToken());
        return params;
    }

    @Override
    public Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        return headers;
    }

    @Override
    protected void deliverResponse(String response) {
         try {
            JSONObject refreshTokenResponse = new JSONObject(response);

            AccountManager am = AccountManager.get(context);

            String accessToken = refreshTokenResponse.optString("access_token");
            if (!accessToken.isEmpty()) {
                user.setAccessToken(accessToken);
                am.setAuthToken(user.getAccount(), INDIEWEB_TOKEN_TYPE, accessToken);
            }

            String refreshToken = refreshTokenResponse.optString("refresh_token");
            if (!refreshToken.isEmpty()) {
                user.setRefreshToken(refreshToken);
                am.setUserData(user.getAccount(), "refresh_token", refreshToken);
            }

        } catch (JSONException e) {
            failure = new VolleyError(e);
        }
        done.release();
    }

    @Override
    public void deliverError(VolleyError error) {
        failure = error;
        done.release();
    }

    public VolleyError await() throws InterruptedException {
        done.acquire();
        return failure;
    }
}

class TokenRetryPolicy extends DefaultRetryPolicy {
    private final Context context;
    private final RequestQueue queue;
    private final User user;
    private boolean retried;

    TokenRetryPolicy(Context context, RequestQueue queue, User user) {
        this.context = context;
        this.queue = queue;
        this.user = user;
        retried = false;
    }

    @Override
    protected boolean hasAttemptRemaining() {
        return !retried;
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
        if (!hasAttemptRemaining()) {
            throw error;
        }

        String hdr = error.networkResponse.headers.get("WWW-Authenticate");
        if (hdr.contains("invalid-token")) {
            // Our access token has expired. Do we have a refresh token? If so, try to use it.

            String refreshToken = user.getRefreshToken();
            if (refreshToken != null && !refreshToken.isEmpty()) {
                TokenRefreshRequest req = new TokenRefreshRequest(context, user);
                queue.add(req);
                VolleyError failure;
                try {
                    failure = req.await();
                } catch(InterruptedException e) {
                    failure = new VolleyError(e);
                }
                if (failure != null) {
                    throw failure;
                }
                retried = true;
                return;
            }

            // We don't have a refresh token. For now, just fail.
            // TODO: Restart the OAuth2 flow to get a new token.
        }
        throw error;
    }
}

public class HTTPRequest {

    private final Context context;
    private final User user;
    private final VolleyRequestListener volleyRequestListener;

    public HTTPRequest(VolleyRequestListener volleyRequestListener, User user, Context context) {
        this.volleyRequestListener = volleyRequestListener;
        this.user = user;
        this.context = context;
    }

    /**
     * Do a Volley String GET Request.
     *
     * @param endpoint The endpoint to query.
     */
    public void doGetRequest(final String endpoint) {
        StringRequest request = new StringRequest(Request.Method.GET, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        volleyRequestListener.OnSuccessRequest(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyRequestListener.OnFailureRequest(error);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                if (user != null) {
                    String accessToken = user.getAccessToken();

                    // Send empty access token in case the user is anonymous and the microsub endpoint
                    // is still set to the IndiePass site.
                    if (user.isAnonymous() && endpoint.contains(context.getString(R.string.anonymous_microsub_endpoint))) {
                        accessToken = "";
                    }

                    headers.put("Authorization", "Bearer " + accessToken);
                }

                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new TokenRetryPolicy(context, queue, user));
        queue.add(request);
    }

    /**
     * Do a Volley String POST Request.
     *
     * @param endpoint The endpoint to query.
     * @param params   The params to send.
     */
    public void doPostRequest(String endpoint, final Map<String, String> params) {
        StringRequest request = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (volleyRequestListener != null) {
                            volleyRequestListener.OnSuccessRequest(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            volleyRequestListener.OnFailureRequest(error);
                        }
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                if (user != null) {
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                }
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new TokenRetryPolicy(context, queue, user));
        queue.add(request);
    }

    /**
     * Do a Volley String DELETE Request.
     *
     * @param endpoint The endpoint to query.
     * @param params   The params to send.
     */
    public void doDeleteRequest(String endpoint, final Map<String, String> params) {
        StringRequest request = new StringRequest(Request.Method.DELETE, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (volleyRequestListener != null) {
                            volleyRequestListener.OnSuccessRequest(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (volleyRequestListener != null) {
                            volleyRequestListener.OnFailureRequest(error);
                        }
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                if (user != null) {
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                }
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        request.setRetryPolicy(new TokenRetryPolicy(context, queue, user));
        queue.add(request);
    }
}
