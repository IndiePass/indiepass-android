package com.indieweb.indigenous.util;

import com.android.volley.VolleyError;

public interface VolleyRequestListener {

    void OnSuccessRequest(String response);

    void OnFailureRequest(VolleyError error);

}