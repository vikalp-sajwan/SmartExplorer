package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public class Tag {
    private boolean isUniqueContent;
    private String tagName;


    public Tag(String tagName, boolean isUniqueContent) {
        this.tagName = tagName;
        this.isUniqueContent = isUniqueContent;
    }
}
