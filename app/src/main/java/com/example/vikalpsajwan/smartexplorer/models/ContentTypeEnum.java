package com.example.vikalpsajwan.smartexplorer.models;

/**
 * Created by amitjha on 3/18/2017.
 */

public enum ContentTypeEnum {
    Image,
    Video,
    Audio,
    Document,
    Location,
    Note,
    Other;

    public static ContentTypeEnum enumFromInt(int x) {
        switch(x) {
            case 0:
                return Image;
            case 1:
                return Video;
            case 2:
                return Audio;
            case 3:
                return Document;
            case 4:
                return Location;
            case 5:
                return Note;
            case 6:
                return Other;
        }
        return null;
    }
}

