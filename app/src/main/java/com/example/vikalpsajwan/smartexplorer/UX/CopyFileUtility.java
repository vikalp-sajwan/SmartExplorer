package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.Tag;
import com.example.vikalpsajwan.smartexplorer.models.TextContent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


/**
 * Created by Vikalp on 07/02/2017.
 */

public class CopyFileUtility extends AsyncTask<Uri, Void, Void> {
    private Context context;
    private DatabaseHandler dbHandler;
    private ArrayList<String> tagNames;
    private ArrayList<Boolean> tagsUniqueness;
    private String contentName;
    private Integer mode;
    private ContentTypeEnum contentType;
    private File mDest;
    private SmartContent sC;    // this object will not have the contentId and contentAddress and needs to be updated in this class
    private String contentDescription;

    public CopyFileUtility(Context context, SmartContent sC, ArrayList<String> TagNames, ArrayList<Boolean> mfileTagsUniqueness, int mode) {
        this.tagNames = TagNames;
        this.tagsUniqueness = mfileTagsUniqueness;
        this.context = context;
        this.sC = sC;
        this.mode = mode;
        this.contentName = sC.getContentName();
        this.contentType = sC.getContentUnit().getContentType();
        this.contentDescription = sC.getContentDescription();
    }

    @Override
    public Void doInBackground(Uri... uris) {

        // get destination directory
        // option 1 - save in private space in external storage
        mDest = new File(context.getExternalFilesDir(null) + File.separator + contentName);

        //      option 2 - save in public space by making an app Folder to save files external storage root
        //      File dest = new File(Environment.getExternalStorageDirectory()+File.separator+"Smart Explorer");
        //      if(!dest.exists() || !dest.isDirectory()){
        //          dest.mkdir();
        //      }
        //      dest = new File(dest+File.separator+contentName);

        if (mode == AddContentActivity.EXTRA_MODE_FILE_SHARE) {
            copyFile(uris);
        } else if (mode == AddContentActivity.EXTRA_MODE_IMAGE_CAPTURE || mode == AddContentActivity.EXTRA_MODE_TEXT_SHARE) {
            renameFile(uris);
        }

        // add entry of file in app database and save the autoincrement id of SmartContent
        dbHandler = DatabaseHandler.getDBInstance(context);
        long contentId = dbHandler.addFile(contentName, mDest.getAbsolutePath(), contentDescription, contentType);

        dbHandler.addSmartContentInMemory(sC);

        // update the content id and address of content in the sC object
        sC.setContentID(contentId);
        sC.getContentUnit().setContentAddress(mDest.getAbsolutePath());

        // if content is of type text then copy its text in in-memory textContentHashMap structure
        String contentText = new String();
        HashMap<Long, TextContent> textContentHashMap = dbHandler.getTextContentHash();
        if (sC.getContentUnit().getContentType() == ContentTypeEnum.Note
                || sC.getContentUnit().getContentType() == ContentTypeEnum.Location) {
            File file = new File(sC.getContentUnit().getContentAddress());
            Scanner scanner = null;
            try {
                scanner = new Scanner(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                while (scanner.hasNextLine()) {
                    contentText = contentText.concat(scanner.nextLine() + "\n");
                }
            } finally {
                scanner.close();
            }
            textContentHashMap.put(sC.getContentID(),new TextContent(sC.getContentID(), contentText));

        }

        ArrayList<Long> associatedTags = new ArrayList<>();

        // keep arrayList of tagId of tags to add in them the reference of SmartContent which is being added
        ArrayList<Long> addedTags = new ArrayList<Long>();






        for(int i = 0; i< tagNames.size(); i++){
            Tag tag;
            long tagId = dbHandler.isTagPresent(tagNames.get(i));

            if(tagId == -1){    // new tag
                tagId = dbHandler.addTag(tagNames.get(i), tagsUniqueness.get(i));
                tag = dbHandler.addTagInMemory(tagId, tagNames.get(i), tagsUniqueness.get(i));
                addedTags.add(tagId);
            }
            else{   // existing tag
                tag = dbHandler.tagHash.get(tagId);
                if(tagsUniqueness.get(i) == true){  // marked as unique by user
                    // delete all associated files with this tag( one or multiple)
                    // and also remove entry of those files from the tags that were associated with them

                    ArrayList<SmartContent> associatedFiles = tag.getAssociatedContent();
                    while(associatedFiles.size()!=0){
                        dbHandler.deleteSmartContent(associatedFiles.get(0).getContentID());
                    }

                    // update in memory
                    if(!tag.isUniqueContent()){
                        tag.setUniqueContent(true);
                        tag.removeAssociatedContent();
                        // update in database
                        dbHandler.updateTagUniqueness(tagId, true);
                    }
                }
                else{   // NOT marked as unique by user --> therefore change tag type to NON-unique if it is unique
                    // update in memory
                    if(tag.isUniqueContent()){
                        tag.setUniqueContent(false);
                        // update in database
                        dbHandler.updateTagUniqueness(tagId, false);
                    }

                }

                addedTags.add(tagId);
            }
            // update in memory for tag
            tag.addAssociatedContent(sC);

            // update entry in tagAccess table for the tag
            dbHandler.updateTagAccess(tagId);
        }

        for(long addedTagId: addedTags){
            dbHandler.addFileTagEntry(contentId, addedTagId);
            sC.addTag(dbHandler.tagHash.get(addedTagId));
        }



        return null;
    }

    /**
     * method to copy file in case of shared file
     *
     * @param uris
     * @return
     */
    private boolean copyFile(Uri... uris) {
        ContentResolver cr = context.getContentResolver();
        //notificationManager = new (NotificationManager)getSystemService(context.NOTIFICATION_SERVICE);


        for (Uri sourceUri : uris) {
            try {

                // open FileInputStream from sourceUri
                FileInputStream in = (FileInputStream) cr.openInputStream(sourceUri);

                // open FileOutputStream for destination
                FileOutputStream out = (FileOutputStream) cr.openOutputStream(Uri.fromFile(mDest), "w");

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                in = null;

                out.flush();
                out.close();
                out = null;


            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }
        return true;
    }

    /**
     * method to rename file in case of captured image which is already saved in the app files directory
     *
     * @param uris
     * @return
     */
    private boolean renameFile(Uri... uris) {
        for (Uri sourceUri : uris) {
            File from = new File(sourceUri.getPath());
            if (from.exists())
                from.renameTo(mDest);

        }
        return true;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File added successfully", Toast.LENGTH_LONG).show();
        /** Todo:
         * lading data again
         * could be inefficient in future
         */
        try {
            dbHandler.loadData();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        super.onPostExecute(aVoid);
    }
}
