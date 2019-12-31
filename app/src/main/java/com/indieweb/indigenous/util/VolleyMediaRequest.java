package com.indieweb.indigenous.util;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;

public class VolleyMediaRequest extends VolleyMultipartRequest {

    public VolleyMediaRequest(int method, String url, Response.Listener<NetworkResponse> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }



}
