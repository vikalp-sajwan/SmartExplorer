package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public class Tag {
    private boolean isUniqueContent;
    private long tagId;
    private String tagName;

    public long getTagId() {
        return tagId;
    }

    public void setTagId(long tagId) {
        this.tagId = tagId;
    }


    public Tag(long tagId, String tagName, boolean isUniqueContent) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.isUniqueContent = isUniqueContent;
    }

    public Tag(long tagId, String tagName, int isUniqueContent) {
        this.tagId = tagId;
        this.tagName = tagName;
        if(isUniqueContent == 0)
            this.isUniqueContent = false;
        else
            this.isUniqueContent = true;
    }

    public boolean isUniqueContent() {
        return isUniqueContent;
    }

    public void setUniqueContent(boolean uniqueContent) {
        isUniqueContent = uniqueContent;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
