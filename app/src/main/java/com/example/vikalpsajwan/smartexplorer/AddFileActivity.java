package com.example.vikalpsajwan.smartexplorer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Dimension;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Vikalp on 04/02/2017.
 */

public class AddFileActivity extends Activity {
    private DatabaseHandler dbHandler;
    private CopyFileUtility copyUtil;
    ArrayList<String> fileTags = new ArrayList<String>();

    Uri uri;

    EditText fileNameEditText;
    AutoCompleteTextView tagAutoCompleteTextView;
    Button addTagButton;
    Button addFileButton;
    LinearLayout tagContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        fileNameEditText = (EditText)findViewById(R.id.filenameEditText);
        tagAutoCompleteTextView = (AutoCompleteTextView)findViewById(R.id.tagAutoCompleteTextView);
        tagContainer = (LinearLayout)findViewById(R.id.tagContainer);
        addFileButton = (Button)findViewById(R.id.addFileButton);
        addTagButton = (Button)findViewById(R.id.addTagButton);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        uri = (Uri) bundle.get(Intent.EXTRA_STREAM);

        ContentResolver cr = this.getContentResolver();
        // get file name based on type Uri
        String fileName;
        // file type Uri
        if (uri.toString().startsWith("file")) {
            File file = new File(uri.getPath());
            fileName = file.getName();
        }
        // content Uri
        else {
            Cursor returnCursor = cr.query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            fileName = returnCursor.getString(nameIndex);
            returnCursor.close();
        }

        fileNameEditText.setText(fileName);

        ArrayList<String> autoCompleteTagList = dbHandler.getTagNames();
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
        tagAutoCompleteTextView.setThreshold(1);
        tagAutoCompleteTextView.setAdapter(autoCompleteAdapter);

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // function called on clicking the add file buttonSearch
    public void addFile(View view) {
        // check if the external storage is mounted
        if(!isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(), "External Storage not Mounted !! Try again later.", LENGTH_LONG).show();
            this.finish();
        }


        String fileName = fileNameEditText.getText().toString().trim();
        if(!fileName.isEmpty()){
            // create an instance of background Async task to copy file from intent uri to app's private storage space
            copyUtil = new CopyFileUtility(getApplicationContext(), fileTags, fileName);
            // start background task and finish UI activity
            copyUtil.execute(uri);
            this.finish();
        }
        else
            Toast.makeText(getApplicationContext(), "please enter valid file name", Toast.LENGTH_SHORT);

    }

    // function called on clicking the add tag buttonSearch
    public void addTag(View view) {
        String tagName = tagAutoCompleteTextView.getText().toString().trim();
        if(!tagName.isEmpty()){
            fileTags.add(tagName);
            LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView tv = (TextView) li.inflate(R.layout.tag_item, tagContainer, false);
            tv.setText(tagName);
            tagContainer.addView(tv);
        }
        else{
            Toast.makeText(getApplicationContext(), "please enter valid tag name", Toast.LENGTH_SHORT);
        }
        tagAutoCompleteTextView.setText("");
    }

}
