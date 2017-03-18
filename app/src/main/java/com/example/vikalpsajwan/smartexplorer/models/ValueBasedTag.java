package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public class ValueBasedTag extends Tag {
    private String tagValue;

    public ValueBasedTag(String tagName, String tagValue, boolean isUniqueContent) {
        super(tagName, isUniqueContent);
        this.tagValue = tagValue;

    }
}
