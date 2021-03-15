// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.util;

import com.android.volley.VolleyError;

public interface VolleyRequestListener {

    void OnSuccessRequest(String response);
    void OnFailureRequest(VolleyError error);

}
