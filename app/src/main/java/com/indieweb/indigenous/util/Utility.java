package com.indieweb.indigenous.util;

import android.content.Context;

public class Utility {

    private final Context context;

    public Utility(Context context) {
        this.context = context;
    }

    /**
     * Copy to clipboard.
     *
     * @param copyText
     *   The text to copy to clipboard.
     * @param label
     *   The clipboard label
     */
    @SuppressWarnings("deprecation")
    public void copyToClipboard(String copyText, String label) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setText(copyText);
            }
        }
        else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, copyText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
        }
    }

}
