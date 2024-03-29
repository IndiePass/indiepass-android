package com.indieweb.indigenous.post;

import android.view.MenuItem;

public interface SendPostInterface {

    /**
     * Onclick method that is called when hitting the send post button.
     * <p>
     * Add extra params to send in the post request into bodyParams.
     * <p>
     * Call sendBasePost(item); at the end to send the request.
     */
    @SuppressWarnings("unused")
    void onPostButtonClick(MenuItem item);

}
