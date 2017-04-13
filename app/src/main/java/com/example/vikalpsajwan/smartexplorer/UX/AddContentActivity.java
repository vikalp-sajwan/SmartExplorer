package com.example.vikalpsajwan.smartexplorer.UX;

/**
 * this activity can be called both implicitly(When files are shared by other apps)
 * and explicitly(when the user clicks on camera capture button in main activity)
 * In the later case the intent will have an EXTRA_MODE field which can be checked for existence in bundle and its value for mode
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.StopWordsProvider;
import com.example.vikalpsajwan.smartexplorer.models.Tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_LONG;
import static com.example.vikalpsajwan.smartexplorer.R.id.view;

/**
 * Created by Vikalp on 04/02/2017.
 */

public class AddContentActivity extends AppCompatActivity {
    public static final int EXTRA_MODE_FILE_SHARE = 0;
    public static final int EXTRA_MODE_TEXT_SHARE = 1;
    public static final int EXTRA_MODE_IMAGE_CAPTURE = 2;

    private static final int CAMERA_REQUEST_CODE = 111;

    int mMode;
    ArrayList<String> mfileTags = new ArrayList<String>();
    ArrayList<Boolean> mfileTagsUniqueness = new ArrayList<Boolean>();
    HashMap<String, Boolean> addedTags = new HashMap<>();
    Uri mUri;
    EditText fileNameEditText;
//    AutoCompleteTextView tagAutoCompleteTextView;
//    Button addTagButton;
    Button addContentButton;
    LinearLayout tagContainer;
    Spinner contentCategorySpinner;
    MultiAutoCompleteTextView mactv;
    private DatabaseHandler dbHandler;
    private CopyFileUtility copyUtil;

    // a variable to keep count of length of text in description box - used in afterTextChanged
    private int previousDescriptionLength = 0 ;
    // HashMap of once removed tags by user and which are to be avoided when typed again in same session
    HashMap<String, Boolean> removedTags;

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE && resultCode != Activity.RESULT_OK){
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        fileNameEditText = (EditText) findViewById(R.id.filenameEditText);
//        tagAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.tagAutoCompleteTextView);
        tagContainer = (LinearLayout) findViewById(R.id.tag_container);
        addContentButton = (Button) findViewById(R.id.addFileButton);
//        addTagButton = (Button) findViewById(R.id.addTagButton);
        contentCategorySpinner = (Spinner) findViewById(R.id.contentCategorySpinner);
        mactv = (MultiAutoCompleteTextView) findViewById(R.id.description_mactv);

        removedTags = new HashMap<String, Boolean>();

        contentCategorySpinner.setAdapter(new ArrayAdapter<ContentTypeEnum>(this, R.layout.file_category_spinner_item, ContentTypeEnum.values()));

        ArrayList<String> autoCompleteTagList = dbHandler.getTagNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
        mactv.setAdapter(adapter);
        mactv.setThreshold(2);
        mactv.setTokenizer(new SpaceTokenizer());

        mactv.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String text = mactv.getText().toString();
                int len = text.length();
                //Log.i("TESTING %%%%", text+" "+len+" " +previousDescriptionLength);
                if(len == 0 || len<previousDescriptionLength) { // to avoid the cases deletion by user
                    previousDescriptionLength = len;
                    return;
                }
                int lastIndex = len-1;
                if( len - previousDescriptionLength == 1){
                    // case of typed character
                    // just check the last character inputted, if its a space then extract last word
                    int i;
                    if(text.charAt(lastIndex) == ' '){
                        i = lastIndex-1;
                        // for the case when space is pressed consecutively
                        // if last inputted character was space then don't do anything
                        if(lastIndex>=0 && !Character.isWhitespace(text.charAt(lastIndex)))
                        extractWordAndAddTag(text, i);
                    }
                }else{
                    // case of pasted text -- extract all the words from string and add as tag
                    // it is taken care of that the already added tags are not added again
                    // ****###&&&*****   misses the test case when there is already some text in the description box and user replaces that text
                    // by pasting some different text whose length is 1 more than the length of previous text.
                    int i = lastIndex;
                    while(i >= 0 ){
                        if(!Character.isWhitespace(text.charAt(i)))
                            i = extractWordAndAddTag(text, i);

                        i--;
                    }
                }

                previousDescriptionLength = len;

            }

            /**
             * this method extracts a word from the text by treating space as a word separator and adds it to the tag container
             * if it is not a stop-word or one from the earlier removed tags
             * @param text  the text String
             * @param lastIndex the index of the last character of the word to be extracted
             * @return returns the starting index of the extracted word
             */
            public int extractWordAndAddTag(String text, int lastIndex){
                int i, startIndex;

                i = lastIndex;
                while(i>0 && text.charAt(i)!=' ')
                    i--;
                if(i==0)
                    startIndex = i;
                else
                    startIndex = i+1;
                String word = text.subSequence(startIndex, lastIndex+1).toString();

                if(removedTags.get(word) == null)
                    addTagInContainer(word);

                return startIndex;
            }

        });
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        String sharedText;
        Integer mode = (Integer) bundle.get("EXTRA_MODE");
        if (mode != null)        //explicit intent
            mMode = mode;
        else {                    //implicit intent
            // if the intent contains EXTRA_TEXT then it is a text intent only and does not contains a file
            sharedText = (String) bundle.getCharSequence(Intent.EXTRA_TEXT);
            if (sharedText != null)
                mMode = EXTRA_MODE_TEXT_SHARE;
            else
                mMode = EXTRA_MODE_FILE_SHARE;
        }

        if (mMode == EXTRA_MODE_IMAGE_CAPTURE) {
            contentCategorySpinner.setSelection(ContentTypeEnum.Image.ordinal(), true);
            contentCategorySpinner.setEnabled(false);
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
                    mUri = Uri.fromFile(photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                }
            }
        }
        // else implicit intent
        else {

            if (mMode == EXTRA_MODE_TEXT_SHARE) {
                contentCategorySpinner.setSelection(ContentTypeEnum.Note.ordinal(), true);
                contentCategorySpinner.setEnabled(false);

                sharedText = (String) bundle.getCharSequence(Intent.EXTRA_TEXT);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
                String noteFileName = "NOTE_" + timeStamp;
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
            } else if (mMode == EXTRA_MODE_FILE_SHARE) {

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
                fileNameEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String fileName = s.toString();
                        if(isFilenameValid(fileName)){
                            String extension = getExtensionFromName(fileName);

                            ContentTypeEnum filetype = dbHandler.fileExtensionHash.get(extension);
                            if (filetype != null) {
                                contentCategorySpinner.setSelection(filetype.ordinal());
                            } else {
                                contentCategorySpinner.setSelection(ContentTypeEnum.Other.ordinal());
                            }
                        } else {
                            contentCategorySpinner.setSelection(ContentTypeEnum.Other.ordinal());
                        }
                    }

                });

            }


        }


//        tagAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                addTagButton.performClick();
//            }
//        });
//
//        tagAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.length() > 2 && s.charAt(s.length() - 1) == '\n') {
//                    tagAutoCompleteTextView.setText(s.toString().toCharArray(), 0, s.length() - 1);
//                    addTagButton.performClick();
//                }
//            }
//        });



//        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
//        tagAutoCompleteTextView.setThreshold(1);
//        tagAutoCompleteTextView.setAdapter(autoCompleteAdapter);

        // command to trigger textchanged event for determination of filetype
        fileNameEditText.setText(fileNameEditText.getText());

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    /**
     * method called on clicking the add file button.
     * this function checks for the Storage state and for Unique tags property,
     * that if adding this file will replace pre-existing files and displays the conformation dialog box with details.
     * @param view
     */
    public void addContentCheck(View view) {
        // check if the external storage is mounted
        if (!isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(), "External Storage not Mounted !! Try again later.", LENGTH_LONG).show();
            this.finish();
        }

        String dialogBoxMessage = new String();
        for (int i = 0; i < tagContainer.getChildCount(); i++) {
            LinearLayout ll = (LinearLayout) tagContainer.getChildAt(i);
            TextView tv = (TextView) ll.getChildAt(0);
            CheckBox cb = (CheckBox) ll.getChildAt(2);

            String tag = tv.getText().toString();
            long tagId = dbHandler.isTagPresent(tag);

            if(cb.isChecked() && tagId != -1 && !dbHandler.tagHash.get(tagId).getAssociatedContent().isEmpty()){    // pre existing tag and user has marked it as unique
                Tag tagObject = dbHandler.tagHash.get(tagId);
                dialogBoxMessage = dialogBoxMessage.concat(tagObject.getTagName()+" : ");
                for(SmartContent sC: tagObject.getAssociatedContent()){
                    dialogBoxMessage = dialogBoxMessage.concat("\n\t"+sC.getContentFileName());
                }
            }

        }

        if(!dialogBoxMessage.isEmpty()){

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("DELETE following content. Continue?");
            alertDialog.setMessage(dialogBoxMessage);
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    addFile();
                }
            });
            alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setCancelable(false);
            alertDialog.show();
        }else{
            addFile();
        }

    }

    public void addFile(){

        for (int i = 0; i < tagContainer.getChildCount(); i++) {
            LinearLayout ll = (LinearLayout) tagContainer.getChildAt(i);
            TextView tv = (TextView) ll.getChildAt(0);
            CheckBox cb = (CheckBox) ll.getChildAt(2);

            String tag = tv.getText().toString();

            mfileTags.add(tag);
            if (cb.isChecked())
                mfileTagsUniqueness.add(true);
            else
                mfileTagsUniqueness.add(false);

        }

        ContentTypeEnum contentType = ContentTypeEnum.enumFromInt(contentCategorySpinner.getSelectedItemPosition());

        String fileName = fileNameEditText.getText().toString().trim();
        if (contentType == ContentTypeEnum.Note || isFilenameValid(fileName)) {
            if (contentType == ContentTypeEnum.Note){
                fileName = fileName.concat(".txt");
            }
            else{
            String extension = getExtensionFromName(fileName);

            // if the fileType extension in new and user has selected filetype category other than "Other" then save it in database

                if (dbHandler.fileExtensionHash.get(extension) == null) {
                    if (contentType != ContentTypeEnum.Other)
                        dbHandler.saveExtensionType(extension, contentType);
                }
            }
            // create an instance of background Async task to copy file from intent mUri to app's private storage space
            copyUtil = new CopyFileUtility(getApplicationContext(), mfileTags, mfileTagsUniqueness, fileName, contentType, mMode);
            // start background task and finish UI activity
            copyUtil.execute(mUri);

            this.finish();
        } else
            Toast.makeText(getApplicationContext(), "please enter valid file name", Toast.LENGTH_SHORT);
    }

    private String getExtensionFromName(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".")+1);
    }

//    // function called on clicking the add tag searchButton
//    public void addTag(View view) {
//        String tagName = tagAutoCompleteTextView.getText().toString().trim();
//        if (!tagName.isEmpty()) {
//            //mfileTags.add(tagName);
//            if (addedTags.get(tagName) == null)
//                addedTags.put(tagName, true);
//            else
//                return;
//            LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            LinearLayout ll = (LinearLayout) li.inflate(R.layout.tag_item_with_choice, tagContainer, false);
//            long tagId = dbHandler.isTagPresent(tagName);
//            if (tagId != -1) {
//                // disable the checkbox for already existing tags
//                CheckBox cb = (CheckBox) ll.getChildAt(2);
//
//                if (dbHandler.getTagHash().get(tagId).isUniqueContent()) {
//                    ll.getChildAt(0).setBackgroundColor(Color.parseColor("#cf2376"));
//                    cb.setChecked(true);
//                }
//            }
//            TextView tv = (TextView) ll.getChildAt(0);
//            tv.setText(tagName);
//            tagContainer.addView(ll);
//        } else {
//            Toast.makeText(getApplicationContext(), "please enter valid tag name", Toast.LENGTH_SHORT);
//        }
//        tagAutoCompleteTextView.setText("");
//    }

    // function to add tag
    public void addTagInContainer(String tagName) {

        if (!tagName.isEmpty() && !StopWordsProvider.isStopWord(tagName)) {

            if (addedTags.get(tagName) == null)
                addedTags.put(tagName, true);
            else
                return;
            LayoutInflater li = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout ll = (LinearLayout) li.inflate(R.layout.tag_item_with_choice, tagContainer, false);
            Button removeButton = (Button)ll.findViewById(R.id.remove_tag_button);
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout ll = (LinearLayout)v.getParent();
                    TextView tv = (TextView) ll.getChildAt(0);
                    removedTags.put(tv.getText().toString(), true);
                    tagContainer.removeView((View)v.getParent());
                }
            });
            long tagId = dbHandler.isTagPresent(tagName);
            if (tagId != -1) {
                // disable the checkbox for already existing tags
                CheckBox cb = (CheckBox) ll.getChildAt(2);

                if (dbHandler.getTagHash().get(tagId).isUniqueContent()) {
                    ll.getChildAt(0).setBackgroundColor(Color.parseColor("#cf2376"));
                    cb.setChecked(true);
                }
            }
            TextView tv = (TextView) ll.getChildAt(0);
            tv.setText(tagName);
            tagContainer.addView(ll, 0 );
        }

    }

    public boolean isFilenameValid(String name){
        if(name.length()>3 && name.lastIndexOf(".")>0 && name.lastIndexOf(".") < name.length()-1)
            return true;
        return false;
    }

    /**
     * method to delete the captured file if user presses the back button
     */
    @Override
    public void onBackPressed() {
        if (mMode == EXTRA_MODE_IMAGE_CAPTURE) {
            File capturedFile = new File(mUri.getPath());
            if (capturedFile.exists()) {
                capturedFile.delete();
            }
        }
        super.onBackPressed();
    }

}
