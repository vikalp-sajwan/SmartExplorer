package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public class ContentTypeEnum {
    public enum contentTypeEnum{
        Image,
        Video,
        Audio,
        Document,
        Location,
        Note
    }

    public contentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(contentTypeEnum contentType) {
        this.contentType = contentType;
    }

    public contentTypeEnum contentType;
}
