package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public class ContentUnit {
    private String address;
    private ContentTypeEnum contentType;

    public ContentUnit(String source, ContentTypeEnum contentType){
        this.address = source;
        this.contentType = contentType;
    }

    public ContentUnit(String fileAddress, int fileType) {
        contentType = ContentTypeEnum.enumFromInt(fileType);
        address = fileAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ContentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }
}
