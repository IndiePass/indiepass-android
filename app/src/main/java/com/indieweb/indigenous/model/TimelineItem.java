package com.indieweb.indigenous.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimelineItem {

    private boolean isRead;
    private String id;
    private String type;
    private String published;
    private String name;
    private String textContent;
    private String htmlContent;
    private String url;
    private String authorName;
    private String authorPhoto = "";
    private ArrayList<String> photos = new ArrayList<>();
    // TODO allow multiple audio
    private String audio;
    // TODO there can actually be multiple of say reply
    private Map<String, String> subType = new LinkedHashMap<>();

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType(String key) {
        return subType.get(key);
    }

    public void addToSubType(String key, String value) {
        this.subType.put(key, value);
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
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

    public String getAuthorPhoto() {
        return authorPhoto;
    }

    public void setAuthorPhoto(String authorPhoto) {
        this.authorPhoto = authorPhoto;
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    public void addPhoto(String photo) {
        this.photos.add(photo);
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }
}
