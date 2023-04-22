package com.indieweb.indiepass.util;

import com.android.volley.VolleyError;

public interface VolleyRequestListener {

    void OnSuccessRequest(String response);
    void OnFailureRequest(VolleyError error);

}