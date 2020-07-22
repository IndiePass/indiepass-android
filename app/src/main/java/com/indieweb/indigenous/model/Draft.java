package com.indieweb.indigenous.model;

public class Draft {

    public static final String TABLE_NAME = "drafts";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SEND_WHEN_ONLINE = "send_online";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_SPOILER = "spoiler";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_CAPTION = "caption";
    public static final String COLUMN_VIDEO = "video";
    public static final String COLUMN_AUDIO = "audio";
    public static final String COLUMN_TAGS = "tags";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_START_DATE = "start_date";
    public static final String COLUMN_END_DATE = "end_date";
    public static final String COLUMN_SYNDICATION_TARGETS = "syndication_targets";
    public static final String COLUMN_PUBLISH_DATE = "publish_date";
    public static final String COLUMN_PUBLISHED = "published";
    public static final String COLUMN_VISIBILITY = "visibility";
    public static final String COLUMN_SENSITIVITY = "sensitivity";
    public static final String COLUMN_COORDINATES = "coordinates";
    public static final String COLUMN_LOCATION_NAME = "location_name";
    public static final String COLUMN_LOCATION_URL = "location_url";
    public static final String COLUMN_LOCATION_VISIBILITY = "location_visibility";
    public static final String COLUMN_SPINNER = "spinner";

    public static final String CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SEND_WHEN_ONLINE + " INTEGER,"
                + COLUMN_ACCOUNT + " TEXT,"
                + COLUMN_TYPE + " TEXT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_BODY + " TEXT,"
                + COLUMN_SPOILER + " TEXT,"
                + COLUMN_IMAGE + " TEXT,"
                + COLUMN_CAPTION + " TEXT,"
                + COLUMN_VIDEO + " TEXT,"
                + COLUMN_AUDIO + " TEXT,"
                + COLUMN_TAGS + " TEXT,"
                + COLUMN_URL + " TEXT,"
                + COLUMN_START_DATE + " TEXT,"
                + COLUMN_END_DATE + " TEXT,"
                + COLUMN_SYNDICATION_TARGETS + " TEXT,"
                + COLUMN_PUBLISH_DATE + " TEXT,"
                + COLUMN_PUBLISHED + " INTEGER,"
                + COLUMN_VISIBILITY + " TEXT,"
                + COLUMN_SENSITIVITY + " INTEGER,"
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
    private String spoiler = "";
    private String image = "";
    private String caption = "";
    private String video = "";
    private String audio = "";
    private String tags = "";
    private String url = "";
    private String start_date = "";
    private String end_date = "";
    private String syndication_targets = "";
    private String publish_date = "";
    private Integer published = 1;
    private String visibility = "";
    private Integer sensitivity = 0;
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

    public Integer getPublished() {
        return published != null ? published : 1;
    }

    public void setPublished(Integer published) {
        this.published = published;
    }

    public String getVisibility() {
        return visibility != null ? visibility : "";
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Integer getSensitivity() {
        return sensitivity != null ? sensitivity : 0;
    }

    public void setSensitivity(Integer sensitivity) {
        this.sensitivity = sensitivity;
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

    public String getSpoiler() {
        return spoiler != null ? spoiler : "";
    }

    public void setSpoiler(String spoiler) {
        this.spoiler = spoiler;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
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
