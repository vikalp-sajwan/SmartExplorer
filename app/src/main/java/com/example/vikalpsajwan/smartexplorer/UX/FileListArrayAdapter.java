package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.Tag;

import java.util.ArrayList;

/**
 * Created by Vikalp on 24/03/2017.
 */

public class FileListArrayAdapter extends ArrayAdapter<SmartContent> {

    private ArrayList<SmartContent> SmartContentData;

    public FileListArrayAdapter(Context context, int textViewResID, ArrayList<SmartContent> SmartContentData){
        super(context, textViewResID, SmartContentData);
        this.SmartContentData = SmartContentData;
    }


    public View getView(int position, View convertView, ViewGroup parent){
        View v = convertView;

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(v==null){
            v = inflater.inflate(R.layout.smart_content_list_item, null);
        }

        SmartContent sC = SmartContentData.get(position);
         if(sC != null){
             TextView sContentName = (TextView)v.findViewById(R.id.smart_content_name);
             LinearLayout sContentTagContainer = (LinearLayout)v.findViewById(R.id.smart_content_tag_container);

             sContentName.setText(sC.getContentFileName());
             sContentTagContainer.removeAllViews();
             for(Tag tag : sC.getAssociatedTags()){
                 TextView tagTV;
                 if(tag.isUniqueContent())
                     tagTV = (TextView)inflater.inflate(R.layout.tag_item_unique, sContentTagContainer, false);
                 else
                     tagTV = (TextView)inflater.inflate(R.layout.tag_item, sContentTagContainer, false);
                 tagTV.setText(tag.getTagName());
                 sContentTagContainer.addView(tagTV);

             }
         }
         return v;
    }
}
