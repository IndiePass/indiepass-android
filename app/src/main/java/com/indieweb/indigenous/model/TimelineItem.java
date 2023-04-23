package com.indieweb.indigenous.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimelineItem implements Serializable {

    private boolean isRead;
    private String sourceId;
    private String id;
    private String type;
    private String published;
    private String name;
    private String textContent = "";
    private String htmlContent = "";
    private String spoilerContent = "";
    private String url;
    private String authorName;
    private String authorPhoto = "";
    private String authorUrl = "";
    private String authorId = "";
    private String actor = "";
    private String channelId = "";
    private String channelName = "";
    private String reference = "";
    private String latitude = "";
    private String longitude = "";
    private String start = "";
    private String end = "";
    private String location = "";
    private String audio = "";
    private String video = "";
    private String json;
    private final ArrayList<String> photos = new ArrayList<>();
    private final ArrayList<String> tags = new ArrayList<>();
    private boolean liked = false;
    private boolean reposted = false;
    private boolean bookmarked = false;
    private int numberOfComments = -1;
    private final Map<String, String> responseType = new LinkedHashMap<>();
    private boolean swapReference = true;

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceId() {
        return sourceId != null ? sourceId : "";
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getResponseType(String key) {
        return responseType.get(key);
    }

    public void addToResponseType(String key, String value) {
        this.responseType.put(key, value);
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getSpoilerContent() {
        return spoilerContent;
    }

    public void setSpoilerContent(String spoilerContent) {
        this.spoilerContent = spoilerContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAuthorPhoto() {
        return authorPhoto;
    }

    public void setAuthorPhoto(String authorPhoto) {
        this.authorPhoto = authorPhoto;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public void setAuthorUrl(String authorUrl) {
        this.authorUrl = authorUrl;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void addPhoto(String photo) {
        if (!this.photos.contains(photo)) {
            this.photos.add(photo);
        }
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean swapReference() {
        return swapReference;
    }

    public void setSwapReference(boolean swapReference) {
        this.swapReference = swapReference;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isReposted() {
        return reposted;
    }

    public void setReposted(boolean reposted) {
        this.reposted = reposted;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    public int getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(int numberOfComments) {
        this.numberOfComments = numberOfComments;
    }
}
