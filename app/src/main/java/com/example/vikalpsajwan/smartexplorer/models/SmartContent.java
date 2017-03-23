package com.example.vikalpsajwan.smartexplorer.models;

import java.util.ArrayList;

/**
 * Created by amitjha on 3/18/2017.
 */

public class SmartContent{
    private long contentID;
    private ContentUnit contentUnit;
    private ContentUnit[] alternateContent;
    private String contentFileName;
    private String comment;
    private ArrayList<Tag> associatedTags;

    public SmartContent(long contentID, ContentUnit contentUnit, String contentFileName){
        this.contentID = contentID;
        this.contentUnit = contentUnit;
        this.contentFileName = contentFileName;
        associatedTags = new ArrayList<Tag>();
        comment = null;
        alternateContent = null;
    }

    public SmartContent(long id, String fileAddress, String fileName, int fileType) {
        this.contentID = id;
        this.contentFileName = fileName;
        associatedTags = new ArrayList<Tag>();
        contentUnit = new ContentUnit(fileAddress, fileType);
    }

    public void addTag(Tag tag){
        associatedTags.add(tag);
    }

    public long getContentID() {
        return contentID;
    }

    public ContentUnit getContentUnit() {
        return contentUnit;
    }

    public ContentUnit[] getAlternateContent() {
        return alternateContent;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public String getComment() {
        return comment;
    }

    public ArrayList<Tag> getAssociatedTags() {
        return associatedTags;
    }
}
