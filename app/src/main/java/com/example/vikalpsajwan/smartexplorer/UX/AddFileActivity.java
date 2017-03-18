package com.example.vikalpsajwan.smartexplorer.UX;

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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Vikalp on 04/02/2017.
 */

public class AddFileActivity extends Activity {
    public static final int EXTRA_MODE_FILE_SHARE = 0;
    public static final int EXTRA_MODE_TEXT_SHARE = 1;
    public static final int EXTRA_MODE_IMAGE_CAPTURE = 2;
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

        String sharedText;
        Integer mode = (Integer) bundle.get("EXTRA_MODE");
        if (mode != null)        //explicit intent
            mMode = mode;
        else {                    //implicit intent
            // if the intent contains EXTRA_TEXT then it is a text intent only and does not contains a file
            sharedText = (String) bundle.getCharSequence(Intent.EXTRA_TEXT);
            if(sharedText != null)
                mMode = EXTRA_MODE_TEXT_SHARE;
            else
                mMode = EXTRA_MODE_FILE_SHARE;
        }

        if (mMode == EXTRA_MODE_IMAGE_CAPTURE) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + ".jpg";
                fileNameEditText.setText(imageFileName);

                File photoFile = null;
                try {
                    photoFile = File.createTempFile(imageFileName, ".jpg", this.getExternalFilesDir(null));
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

            if(mMode == EXTRA_MODE_TEXT_SHARE){
                sharedText = (String) bundle.getCharSequence(Intent.EXTRA_TEXT);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
                String noteFileName = "NOTE_" + timeStamp + ".txt";
                fileNameEditText.setText(noteFileName);

                File noteFile = null;
                try {
                    noteFile = File.createTempFile(noteFileName, ".txt", this.getExternalFilesDir(null));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Continue only if the File was successfully created
                if (noteFile.exists()) {
                    try {
                        FileOutputStream fOut = new FileOutputStream(noteFile);
                        OutputStreamWriter out = new OutputStreamWriter(fOut);
                        out.write(sharedText);
                        out.close();
                    } catch (FileNotFoundException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                        e.printStackTrace();
                    }
                }
                mUri = Uri.fromFile(noteFile);
            }
            else if(mMode == EXTRA_MODE_FILE_SHARE){

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


        }


        tagAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                addTagButton.performClick();
            }
        });

        tagAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>2 && s.charAt(s.length()-1) == '\n'){
                    tagAutoCompleteTextView.setText(s.toString().toCharArray(),0, s.length()-1);
                    addTagButton.performClick();
                }
            }
        });

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
        if(mMode == EXTRA_MODE_IMAGE_CAPTURE){
            File capturedFile = new File(mUri.getPath());
            if(capturedFile.exists()){
                capturedFile.delete();
            }
        }
        super.onBackPressed();
    }
}
