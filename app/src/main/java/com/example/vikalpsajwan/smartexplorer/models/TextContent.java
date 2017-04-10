package com.example.vikalpsajwan.smartexplorer.models;

import java.util.ArrayList;

/**
 * Created by Vikalp on 09/04/2017.
 */

public class TextContent {
    private long contentID;

    public String getContentText() {
        return contentText;
    }

    private String contentText;

    public TextContent(long contentID, String contentText) {
        this.contentID = contentID;
        this.contentText = contentText;
    }
}
