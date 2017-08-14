package in.snapnsave.models;

import java.util.ArrayList;

import in.snapnsave.models.ContentUnit;
import in.snapnsave.models.Tag;

/**
 * Created by amitjha on 3/18/2017.
 */

public class SmartContent{


    private long contentID;
    private ContentUnit contentUnit;
    private ContentUnit[] alternateContent;
    private String contentName;
    private String contentDescription;
    private ArrayList<Tag> associatedTags;

    public String[] getContentNameWords() {
        return contentNameWords;
    }

    private String[] contentNameWords;

    public SmartContent(long contentID, ContentUnit contentUnit, String contentName, String contentDescription){
        this.contentID = contentID;
        this.contentUnit = contentUnit;
        this.contentName = contentName;
        this.contentDescription = contentDescription;
        associatedTags = new ArrayList<Tag>();
        alternateContent = null;

        splitContentName();
    }

    public SmartContent(long id, String fileAddress, String fileName, String contentDescription, int fileType) {
        this.contentID = id;
        this.contentName = fileName;
        this.contentDescription = contentDescription;
        associatedTags = new ArrayList<Tag>();
        contentUnit = new ContentUnit(fileAddress, fileType);
        splitContentName();


    }

    private void splitContentName() {
        // removing the extension so that last word in title is properly split
        String nameSansExtension;
        if(contentName.lastIndexOf(".")>0)
            nameSansExtension = contentName.substring(0, contentName.lastIndexOf("."));
        else
            nameSansExtension = contentName;
        contentNameWords = nameSansExtension.split("\\s+");
    }

    public boolean isWordPersentInTitle(String word){
        for(int i = 0; i<contentNameWords.length; i++){
            if(contentNameWords[i].equalsIgnoreCase(word))
                return true;
        }
        return false;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void addTag(Tag tag){
        associatedTags.add(tag);
    }

    public long getContentID() {
        return contentID;
    }

    public void setContentID(long contentID) {
        this.contentID = contentID;
    }

    public ContentUnit getContentUnit() {
        return contentUnit;
    }

    public ContentUnit[] getAlternateContent() {
        return alternateContent;
    }

    public String getContentName() {
        return contentName;
    }

    public ArrayList<Tag> getAssociatedTags() {
        return associatedTags;
    }
}
