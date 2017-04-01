package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;

import java.util.ArrayList;



/**
 * Created by Vikalp on 11/02/2017.
 */

public class FileListAdapter extends CursorAdapter {
    private DatabaseHandler dbHandler;
    LayoutInflater inflater;
    int fileNameIndex;
    int filePathIndex;
    int fileIdIndex;
    TextView filenameTV;
    TextView filePathTV;

    LinearLayout tagContainerLL;





    public FileListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        dbHandler = DatabaseHandler.getDBInstance(context);
        inflater = LayoutInflater.from(context);
        fileNameIndex = c.getColumnIndex(DatabaseHandler.getColFilename());
        filePathIndex = c.getColumnIndex(DatabaseHandler.getColfileAddress());
        fileIdIndex = c.getColumnIndex(DatabaseHandler.getColfileid());

    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_files_listview_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        filenameTV = (TextView)view.findViewById(R.id.textview_filename);
        filePathTV = (TextView)view.findViewById(R.id.textview_filepath);

        filenameTV.setText(cursor.getString(fileNameIndex));
        filePathTV.setText(cursor.getString(filePathIndex));


        tagContainerLL = (LinearLayout) view.findViewById(R.id.listViewTagContainer);
        tagContainerLL.removeAllViews();


        // get the file id -- find the associated tags and then add them to tag container
        long fileid = cursor.getLong(fileIdIndex);
        ArrayList<String> associatedTags = dbHandler.getAssociatedTags(fileid);

        for (String tag : associatedTags) {
            TextView tv = (TextView) inflater.inflate(R.layout.tag_item, tagContainerLL, false);
            tv.setText(tag);
            tagContainerLL.addView(tv);
        }

    }
}
