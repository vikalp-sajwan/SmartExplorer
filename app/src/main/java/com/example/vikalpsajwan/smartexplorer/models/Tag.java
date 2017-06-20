package com.example.vikalpsajwan.smartexplorer.models;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by amitjha on 3/18/2017.
 */

public class Tag implements Serializable{
    private boolean isUniqueContent;
    private long tagId;
    private String tagName;
    private ArrayList<SmartContent> associatedContent;
    public Tag(long tagId, String tagName, boolean isUniqueContent) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.isUniqueContent = isUniqueContent;
        associatedContent = new ArrayList<SmartContent>();

    }

    public Tag(long tagId, String tagName, int isUniqueContent) {
        this.tagId = tagId;
        this.tagName = tagName;
        associatedContent = new ArrayList<SmartContent>();
        if (isUniqueContent == 0)
            this.isUniqueContent = false;
        else
            this.isUniqueContent = true;
    }

    public ArrayList<SmartContent> getAssociatedContent() {
        return associatedContent;
    }

    public void addAssociatedContent(SmartContent sC) {
        if(isUniqueContent && associatedContent.size() >=1 ){
            Log.e("INVALID OPERATION!!!", "cant associate unique tag with more than one Content");
            throw new RuntimeException("INVALID operation");
        }
        associatedContent.add(sC);
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

    public long getTagId() {
        return tagId;
    }

    public void removeAssociatedContent() {
        associatedContent.clear();
    }
}
