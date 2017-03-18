package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public class ContentUnit {
    private String source;
    private ContentTypeEnum contentType ;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ContentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }
}
