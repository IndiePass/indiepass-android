package com.indieweb.indigenous.indieauth;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.indieweb.indigenous.model.User;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Endpoints {

    private User user;
    private Context context;

    public Endpoints(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    public void refresh() {

        String micropubEndpoint = "";
        String microsubEndpoint = "";
        String micropubMediaEndpoint = "";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Document doc = Jsoup.connect(user.getMe()).get();
            Elements imports = doc.select("link[href]");
            for (Element link : imports) {
                if (link.attr("rel").equals("micropub")) {
                    micropubEndpoint = link.attr("abs:href");
                }

                if (link.attr("rel").equals("micropub_media")) {
                    micropubMediaEndpoint = link.attr("abs:href");
                }

                if (link.attr("rel").equals("microsub")) {
                    microsubEndpoint = link.attr("abs:href");
                }
            }

            if (micropubEndpoint.length() > 0 || microsubEndpoint.length() > 0 || micropubMediaEndpoint.length() > 0) {
                AccountManager am = AccountManager.get(context);
                am.setUserData(user.getAccount(), "micropub_endpoint", micropubEndpoint);
                am.setUserData(user.getAccount(), "microsub_endpoint", microsubEndpoint);
                am.setUserData(user.getAccount(), "micropub_media_endpoint", micropubMediaEndpoint);
                Toast.makeText(context, "Endpoints refreshed", Toast.LENGTH_SHORT).show();
            }
        }
        catch (IllegalArgumentException ignored) {
            Toast.makeText(context, "Could not connect to domain", Toast.LENGTH_SHORT).show();
        }
        catch (IOException error) {
            Toast.makeText(context, "Could not connect to domain", Toast.LENGTH_SHORT).show();
        }
    }

}
