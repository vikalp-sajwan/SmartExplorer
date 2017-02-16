package com.example.vikalpsajwan.smartexplorer;

/**
 * this activity can be called both implicitly(When files are shared by other apps)
 * and explicitly(when the user clicks on camera capture button in main activity)
 * In the later case the intent will have an EXTRA_MODE field which can be checked for existence in bundle and its value for mode
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.util.Log;
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
import java.io.IOException;
import java.security.Permissions;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.widget.Toast.LENGTH_LONG;
import static java.security.AccessController.getContext;

/**
 * Created by Vikalp on 04/02/2017.
 */

public class AddFileActivity extends Activity {
    static final int EXTRA_MODE_SHARED = 0;
    static final int EXTRA_MODE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;

    int mMode;
    ArrayList<String> mfileTags = new ArrayList<String>();
    Uri mUri;
    EditText fileNameEditText;
    AutoCompleteTextView tagAutoCompleteTextView;
    Button addTagButton;
    Button addFileButton;
    LinearLayout tagContainer;
    private DatabaseHandler dbHandler;
    private CopyFileUtility copyUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        fileNameEditText = (EditText) findViewById(R.id.filenameEditText);
        tagAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        tagContainer = (LinearLayout) findViewById(R.id.tagContainer);
        addFileButton = (Button) findViewById(R.id.addFileButton);
        addTagButton = (Button) findViewById(R.id.addTagButton);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        Integer mode = (Integer) bundle.get("EXTRA_MODE");
        if (mode == null)        //implicit intent
            mMode = EXTRA_MODE_SHARED;
        else                    //explicit intent
            mMode = mode;


        if (mMode == EXTRA_MODE_CAPTURE) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + ".jpg";
                fileNameEditText.setText(imageFileName);

                File photoFile = null;
                try {
                    photoFile = File.createTempFile(imageFileName, null, this.getExternalFilesDir(null));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Continue only if the File was successfully created
                if (photoFile.exists()) {
                    // Log.i("GGGGGGGGGGGGGGGGGG",photoFile.toString());
                    mUri = Uri.fromFile(photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
        // else implicit intent
        else {
            mUri = (Uri) bundle.get(Intent.EXTRA_STREAM);

            ContentResolver cr = this.getContentResolver();
            // get file name based on type Uri
            String fileName;
            // file type Uri
            if (mUri.toString().startsWith("file")) {
                File file = new File(mUri.getPath());
                fileName = file.getName();
            }
            // content Uri
            else {
                Cursor returnCursor = cr.query(mUri, null, null, null, null);
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                fileName = returnCursor.getString(nameIndex);
                returnCursor.close();
            }

            fileNameEditText.setText(fileName);

        }


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
        if (!isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(), "External Storage not Mounted !! Try again later.", LENGTH_LONG).show();
            this.finish();
        }


        String fileName = fileNameEditText.getText().toString().trim();
        if (!fileName.isEmpty()) {
            // create an instance of background Async task to copy file from intent mUri to app's private storage space
            copyUtil = new CopyFileUtility(getApplicationContext(), mfileTags, fileName, mMode);
            // start background task and finish UI activity
            copyUtil.execute(mUri);
            this.finish();
        } else
            Toast.makeText(getApplicationContext(), "please enter valid file name", Toast.LENGTH_SHORT);

    }

    // function called on clicking the add tag buttonSearch
    public void addTag(View view) {
        String tagName = tagAutoCompleteTextView.getText().toString().trim();
        if (!tagName.isEmpty()) {
            mfileTags.add(tagName);
            LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TextView tv = (TextView) li.inflate(R.layout.tag_item, tagContainer, false);
            tv.setText(tagName);
            tagContainer.addView(tv);
        } else {
            Toast.makeText(getApplicationContext(), "please enter valid tag name", Toast.LENGTH_SHORT);
        }
        tagAutoCompleteTextView.setText("");
    }

    /**
     * method to delete the captured file if user presses the back button
     */
    @Override
    public void onBackPressed() {
        if(mMode == EXTRA_MODE_CAPTURE){
            File capturedFile = new File(mUri.getPath());
            if(capturedFile.exists()){
                capturedFile.delete();
            }
        }
        super.onBackPressed();
    }
}
