package in.snapnsave.models;

import in.snapnsave.models.Tag;

/**
 * Created by amitjha on 3/18/2017.
 */

public class ValueBasedTag extends Tag {
    private String tagValue;

    public ValueBasedTag(long tagID, String tagName, String tagValue, boolean isUniqueContent) {
        super(tagID, tagName, isUniqueContent);
        this.tagValue = tagValue;

    }
}
