// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.model;

import android.accounts.Account;

public class User {

    private String accountName;
    private String externalId;
    private String accessToken;
    private String microsubEndpoint;
    private String micropubEndpoint;
    private String micropubMediaEndpoint;
    private String tokenEndpoint;
    private String authorizationEndpoint;
    private String syndicationTargets;
    private String avatar;
    private String name;
    private String postTypes;
    private String displayName;
    private boolean valid = false;
    private boolean anonymous = false;
    private Account account;
    private String accountType;
    private String clientId;
    private String clientSecret;

    public String getAccountName() {
        return accountName;
    }

    public String getAccountNameWithoutProtocol() {
        return accountName.replace("https://","").replace("http://", "");
    }

    public String getBaseUrl() {
        String url = getAccountName();
        if (url.contains("@")) {
            String[] parts = url.split("@");
            url = parts[0];
        }
        return url;
    }

    public String getBaseUrlWithoutProtocol() {
        return  getBaseUrl().replace("https://","").replace("http://", "");
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getExternalId() {
        return externalId != null ? externalId : "";
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getMicropubEndpoint() {
        return micropubEndpoint != null ? micropubEndpoint : "";
    }

    public void setMicropubEndpoint(String micropubEndpoint) {
        this.micropubEndpoint = micropubEndpoint;
    }

    public String getMicropubMediaEndpoint() {
        return micropubMediaEndpoint != null ? micropubMediaEndpoint : "";
    }

    public void setMicropubMediaEndpoint(String micropubMediaEndpoint) {
        this.micropubMediaEndpoint = micropubMediaEndpoint;
    }

    public String getMicrosubEndpoint() {
        return microsubEndpoint != null ? microsubEndpoint : "";
    }

    public void setMicrosubEndpoint(String microsubEndpoint) {
        this.microsubEndpoint = microsubEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint != null ? tokenEndpoint : "";
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint != null ? authorizationEndpoint : "";
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

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isAuthenticated() {
        return !anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getSyndicationTargets() {
        return syndicationTargets != null ? syndicationTargets : "";
    }

    public void setSyndicationTargets(String syndicationTargets) {
        this.syndicationTargets = syndicationTargets;
    }

    public String getPostTypes() {
        return postTypes != null ? postTypes : "";
    }

    public void setPostTypes(String postTypes) {
        this.postTypes = postTypes;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getAvatar() {
        return avatar != null ? avatar : "";
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
