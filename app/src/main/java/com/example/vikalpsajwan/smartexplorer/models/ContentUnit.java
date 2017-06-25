package com.example.vikalpsajwan.smartexplorer.models;

import java.io.Serializable;

/**
 * Created by amitjha on 3/18/2017.
 */

public class ContentUnit{
    private String contentAddress;
    private ContentTypeEnum contentType;

    public ContentUnit(ContentTypeEnum contentType){
        this.contentType = contentType;
    }

//    public ContentUnit(String source, ContentTypeEnum contentType){
//        this.contentAddress = source;
//        this.contentType = contentType;
//
//    }



    public ContentUnit(String fileAddress, int fileType) {
        contentType = ContentTypeEnum.enumFromInt(fileType);
        contentAddress = fileAddress;

    }

    public String getContentAddress() {
        return contentAddress;
    }

    public void setContentAddress(String contentAddress) {
        this.contentAddress = contentAddress;
    }

    public ContentTypeEnum getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeEnum contentType) {
        this.contentType = contentType;
    }
}
