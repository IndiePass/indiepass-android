package com.indieweb.indigenous.indieauth;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.StrictMode;

import android.widget.Toast;

import com.indieweb.indigenous.model.HCard;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.mf2.Mf2Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

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
        String authorAvatar = "";
        String authorName = "";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            boolean foundInfo = false;
            String url = user.getMe();

            org.jsoup.Connection connection = Jsoup.connect(url);
            org.jsoup.Connection.Response response = connection.execute();

            if (response.hasHeader("Link")) {
                String[] headers = response.header("Link").split(",");
                if (headers.length > 0) {

                    for (String link: headers) {
                        String[] split = link.split(";");
                        String endpoint = split[0].replace("<", "").replace(">", "").trim();
                        String rel = split[1].trim().replace("rel=", "").replace("\"", "");

                        switch (rel) {
                            case "micropub":
                                foundInfo = true;
                                micropubEndpoint = endpoint;
                                break;
                            case "microsub":
                                foundInfo = true;
                                microsubEndpoint = endpoint;
                                break;
                            case "micropub_media":
                                foundInfo = true;
                                micropubMediaEndpoint = endpoint;
                                break;
                        }
                    }
                }
            }

            Document doc = connection.get();
            Elements links = doc.select("link[href]");
            for (Element link : links) {
                if (micropubEndpoint.length() == 0 && link.attr("rel").equals("micropub")) {
                    foundInfo = true;
                    micropubEndpoint = link.attr("abs:href");
                }

                if (micropubMediaEndpoint.length() == 0 && link.attr("rel").equals("micropub_media")) {
                    foundInfo = true;
                    micropubMediaEndpoint = link.attr("abs:href");
                }

                if (microsubEndpoint.length() == 0 && link.attr("rel").equals("microsub")) {
                    foundInfo = true;
                    microsubEndpoint = link.attr("abs:href");
                }
            }

            // Author h-card.
            // TODO cleanup discovery as we have the same in IndieAuthActivity
            String noProtocolUrl = user.getMeWithoutProtocol();
            try {

                Mf2Parser parser = new Mf2Parser();
                ArrayList<HCard> cards = parser.parse(doc, new URI(url));

                for (HCard c : cards) {
                    if (c.getUrl() != null && c.getName() != null) {
                        String HCardURL = c.getUrl().replace("https://","").replace("http://", "");
                        if (HCardURL.equals(noProtocolUrl) || HCardURL.equals(noProtocolUrl + "/")) {
                            foundInfo = true;
                            authorAvatar = c.getAvatar();
                            authorName = c.getName();
                            break;
                        }
                    }
                }
            }
            catch (Exception ignored) { }

            if (foundInfo) {
                AccountManager am = AccountManager.get(context);
                am.setUserData(user.getAccount(), "micropub_endpoint", micropubEndpoint);
                am.setUserData(user.getAccount(), "microsub_endpoint", microsubEndpoint);
                am.setUserData(user.getAccount(), "micropub_media_endpoint", micropubMediaEndpoint);
                am.setUserData(user.getAccount(), "author_name", authorName);
                am.setUserData(user.getAccount(), "author_avatar", authorAvatar);
                Toast.makeText(context, "Endpoints and metadata updated. You might need to restart the app for some changes to see effect.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (IllegalArgumentException e) {
            Toast.makeText(context, "Error syncing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Toast.makeText(context, "Could not connect to domain: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
