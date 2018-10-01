package com.indieweb.indigenous.model;

import java.io.Serializable;

public class Draft implements Serializable {

    public static final String TABLE_NAME = "drafts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public static final String CREATE_TABLE =
        "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ACCOUNT + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_BODY + " TEXT,"
                + COLUMN_IMAGE + " TEXT,"
                + COLUMN_TAGS + " TEXT,"
                + COLUMN_URL + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

    private Integer id = 0;
    private String account;
    private String type;
    private String name = "";
    private String body = "";
    private String image = "";
    private String tags = "";
    private String url = "";
    private String published;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
