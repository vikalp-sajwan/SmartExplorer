package com.example.vikalpsajwan.smartexplorer.models;

import java.util.ArrayList;

/**
 * Created by amitjha on 3/18/2017.
 */

public class SystemLevelValues {
    private ArrayList<ValueBasedTag> system_tags;
    public SystemLevelValues()
    {
        system_tags.add(new ValueBasedTag("capture_date"," ", false));
        system_tags.add(new ValueBasedTag("m_date","", false));
        system_tags.add(new ValueBasedTag("source_app","", false));
        system_tags.add(new ValueBasedTag("capture_location","", false));
        system_tags.add(new ValueBasedTag("c_date","", false));

    }

}


