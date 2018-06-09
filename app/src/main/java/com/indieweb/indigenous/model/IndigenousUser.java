package com.indieweb.indigenous.model;

public class IndigenousUser {

    private String me;
    private String accessToken;
    private String microsubEndpoint;
    private String micropubEndpoint;
    private String tokenEndpoint;
    private String authorizationEndpoint;
    private boolean valid = false;

    public String getMe() {
        return me;
    }

    public void setMe(String me) {
        this.me = me;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMicropubEndpoint() {
        return micropubEndpoint;
    }

    public void setMicropubEndpoint(String micropubEndpoint) {
        this.micropubEndpoint = micropubEndpoint;
    }

    public String getMicrosubEndpoint() {
        return microsubEndpoint;
    }

    public void setMicrosubEndpoint(String microsubEndpoint) {
        this.microsubEndpoint = microsubEndpoint;
    }


    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
