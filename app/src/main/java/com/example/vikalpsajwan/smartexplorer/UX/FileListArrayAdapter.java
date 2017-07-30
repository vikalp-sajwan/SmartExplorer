package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.Tag;

import java.io.File;
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
        ImageView sContentThumb;
        ImageView sContentThumbOverlay;

        View v = convertView;

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(v==null){
            v = inflater.inflate(R.layout.smart_content_list_item, null);
        }

        sContentThumb = (ImageView)v.findViewById(R.id.smart_content_thumb);
        sContentThumbOverlay = (ImageView)v.findViewById(R.id.smart_content_thumb_overlay);
        sContentThumb.setImageDrawable(null);
        Glide.clear(sContentThumb);
        sContentThumbOverlay.setVisibility(View.INVISIBLE);

        SmartContent sC = SmartContentData.get(position);
         if(sC != null){

             ContentTypeEnum ContentType = sC.getContentUnit().getContentType();
            // contentThumbOverlay.setVisibility(View.INVISIBLE);

             Drawable myDrawable;

             if( ContentType == ContentTypeEnum.Image || ContentType == ContentTypeEnum.Video) {
                 Glide.with(getContext())
                         .load(new File(sC.getContentUnit().getContentAddress()))
                         .thumbnail(0.1f)
                         .centerCrop().
                         into(sContentThumb);
                 if( ContentType == ContentTypeEnum.Video){
                     sContentThumbOverlay.setVisibility(View.VISIBLE);
                 }
             }
             else {
                 if (ContentType == ContentTypeEnum.Audio) {
                     myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_audio);
                 } else if (ContentType == ContentTypeEnum.Document) {
                     myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_document);
                 }else if (ContentType == ContentTypeEnum.Note) {
                     myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_note);
                 }else if (ContentType == ContentTypeEnum.Location) {
                     myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_location);
                 }else{
                     myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_other);
                 }
                 sContentThumb.setImageDrawable(myDrawable);
             }


             TextView sContentName = (TextView)v.findViewById(R.id.smart_content_name);
             LinearLayout sContentTagContainer = (LinearLayout)v.findViewById(R.id.smart_content_tag_container);
             TextView sContentDescription = (TextView)v.findViewById(R.id.content_description);

             sContentName.setText(sC.getContentName());
             sContentDescription.setText(sC.getContentDescription());
             sContentTagContainer.removeAllViews();
             ArrayList<Tag> associatedTags = sC.getAssociatedTags();
             for(int i=0; i< associatedTags.size(); i++){
                 TextView tagTV;
                 if(associatedTags.get(i).isUniqueContent())
                     tagTV = (TextView)inflater.inflate(R.layout.tag_item_unique, sContentTagContainer, false);
                 else
                     tagTV = (TextView)inflater.inflate(R.layout.tag_item, sContentTagContainer, false);
                 tagTV.setText(associatedTags.get(i).getTagName());
                 sContentTagContainer.addView(tagTV);
             }
         }
         return v;
    }
}
