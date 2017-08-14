package in.snapnsave.models;

import java.util.ArrayList;

import in.snapnsave.models.ValueBasedTag;

/**
 * Created by amitjha on 3/18/2017.
 */

public class SystemLevelValues {
    private ArrayList<ValueBasedTag> system_tags;
    public SystemLevelValues()
    {
        system_tags.add(new ValueBasedTag(0, "capture_date"," ", false));
        system_tags.add(new ValueBasedTag(0, "m_date","", false));
        system_tags.add(new ValueBasedTag(0, "source_app","", false));
        system_tags.add(new ValueBasedTag(0, "capture_location","", false));
        system_tags.add(new ValueBasedTag(0, "c_date","", false));

    }

}


