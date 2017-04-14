package com.example.vikalpsajwan.smartexplorer.models;

import java.util.ArrayList;

/**
 * Created by amitjha on 3/18/2017.
 */

public class SmartContent{


    private long contentID;
    private ContentUnit contentUnit;
    private ContentUnit[] alternateContent;
    private String contentName;
    private String contentDescription;
    private ArrayList<Tag> associatedTags;

    public SmartContent(long contentID, ContentUnit contentUnit, String contentName, String contentDescription){
        this.contentID = contentID;
        this.contentUnit = contentUnit;
        this.contentName = contentName;
        this.contentDescription = contentDescription;
        associatedTags = new ArrayList<Tag>();
        alternateContent = null;
    }

    public SmartContent(long id, String fileAddress, String fileName, String contentDescription, int fileType) {
        this.contentID = id;
        this.contentName = fileName;
        this.contentDescription = contentDescription;
        associatedTags = new ArrayList<Tag>();
        contentUnit = new ContentUnit(fileAddress, fileType);
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void addTag(Tag tag){
        associatedTags.add(tag);
    }

    public long getContentID() {
        return contentID;
    }

    public void setContentID(long contentID) {
        this.contentID = contentID;
    }

    public ContentUnit getContentUnit() {
        return contentUnit;
    }

    public ContentUnit[] getAlternateContent() {
        return alternateContent;
    }

    public String getContentName() {
        return contentName;
    }

    public ArrayList<Tag> getAssociatedTags() {
        return associatedTags;
    }
}
