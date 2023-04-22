package com.indieweb.indiepass.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indiepass.R;
import com.indieweb.indiepass.model.User;

import java.util.HashMap;
import java.util.Map;

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
     * @param endpoint
     *   The endpoint to query.
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
        )
        {
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
        queue.add(request);
    }

    /**
     * Do a Volley String POST Request.
     *
     * @param endpoint
     *   The endpoint to query.
     * @param params
     *   The params to send.
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
        )
        {

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
        queue.add(request);
    }

    /**
     * Do a Volley String DELETE Request.
     *
     * @param endpoint
     *   The endpoint to query.
     * @param params
     *   The params to send.
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
        )
        {

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
        queue.add(request);
    }
}
