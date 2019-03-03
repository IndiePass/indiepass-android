package com.indieweb.indigenous.model;

public class Draft {

    public static final String TABLE_NAME = "drafts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SEND_WHEN_ONLINE = "send_online";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_IMAGES = "images";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_SYNDICATION_TARGETS = "syndication_targets";
    public static final String COLUMN_PUBLISH_DATE = "publish_date";
    public static final String COLUMN_COORDINATES = "coordinates";
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_LOCATION_URL = "location_url";
    public static final String COLUMN_LOCATION_VISIBILITY = "location_visibility";
    public static final String COLUMN_SPINNER = "spinner";

    public static final String CREATE_TABLE =
        "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SEND_WHEN_ONLINE + " INTEGER,"
                + COLUMN_ACCOUNT + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_BODY + " TEXT,"
                + COLUMN_IMAGES + " TEXT,"
                + COLUMN_TAGS + " TEXT,"
                + COLUMN_URL + " TEXT,"
                + COLUMN_START_DATE + " TEXT,"
                + COLUMN_END_DATE + " TEXT,"
                + COLUMN_SYNDICATION_TARGETS + " TEXT,"
                + COLUMN_PUBLISH_DATE + " TEXT,"
                + COLUMN_COORDINATES + " TEXT,"
                + COLUMN_LOCATION_NAME + " TEXT,"
                + COLUMN_LOCATION_URL + " TEXT,"
                + COLUMN_LOCATION_VISIBILITY + " TEXT,"
                + COLUMN_SPINNER + " TEXT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";

    private Integer id = 0;
    private Integer sendWhenOnline = 0;
    private String account;
    private String type;
    private String name = "";
    private String body = "";
    private String images = "";
    private String tags = "";
    private String url = "";
    private String start_date = "";
    private String end_date = "";
    private String syndication_targets = "";
    private String publish_date = "";
    private String coordinates = "";
    private String location_name = "";
    private String location_url = "";
    private String location_visibility = "";
    private String spinner = "";
    private String timestamp;

    public Integer getSendWhenOnline() {
        return sendWhenOnline;
    }

    public void setSendWhenOnline(Integer sendWhenOnline) {
        this.sendWhenOnline = sendWhenOnline;
    }

    public String getEndDate() {
        return end_date;
    }

    public void setEndDate(String end_date) {
        this.end_date = end_date;
    }

    public String getSyndicationTargets() {
        return syndication_targets;
    }

    public void setSyndicationTargets(String syndication_targets) {
        this.syndication_targets = syndication_targets;
    }

    public String getPublishDate() {
        return publish_date;
    }

    public void setPublishDate(String publish_date) {
        this.publish_date = publish_date;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public String getLocationName() {
        return location_name;
    }

    public void setLocationName(String location_name) {
        this.location_name = location_name;
    }

    public String getLocationUrl() {
        return location_url;
    }

    public void setLocationUrl(String location_url) {
        this.location_url = location_url;
    }

    public String getLocationVisibility() {
        return location_visibility;
    }

    public void setLocationVisibility(String location_visibility) {
        this.location_visibility = location_visibility;
    }

    public String getSpinner() {
        return spinner;
    }

    public void setSpinner(String spinner) {
        this.spinner = spinner;
    }

    public String getStartDate() {
        return start_date;
    }

    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
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

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
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
