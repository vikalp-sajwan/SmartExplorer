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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import static android.R.attr.tag;
import static android.R.id.list;


/**
 * Created by Vikalp on 07/02/2017.
 */

public class CopyFileUtility extends AsyncTask<Uri, Void, Void> {

    private Context context;
    private DatabaseHandler dbHandler;
    private ArrayList<String> tagNames;
    private ArrayList<Boolean> tagsUniqueness;
    private String fileName;
    private int mode;
    private ContentTypeEnum contentType;
    private File mDest;

    public CopyFileUtility(Context context, ArrayList<String> TagNames, ArrayList<Boolean> mfileTagsUniqueness, String filename, ContentTypeEnum contentType, int mode) {
        this.tagNames = TagNames;
        this.tagsUniqueness = mfileTagsUniqueness;
        this.context = context;
        this.fileName = filename;
        this.mode = mode;
        this.contentType = contentType;
    }

    @Override
    public Void doInBackground(Uri... uris) {

        // get destination directory
        // option 1 - save in private space in external storage
        mDest = new File(context.getExternalFilesDir(null) + File.separator + fileName);

        //      option 2 - save in public space by making an app Folder to save files external storage root
        //      File dest = new File(Environment.getExternalStorageDirectory()+File.separator+"Smart Explorer");
        //      if(!dest.exists() || !dest.isDirectory()){
        //          dest.mkdir();
        //      }
        //      dest = new File(dest+File.separator+fileName);

        if (mode == AddFileActivity.EXTRA_MODE_FILE_SHARE) {
            copyFile(uris);
        } else if (mode == AddFileActivity.EXTRA_MODE_IMAGE_CAPTURE || mode == AddFileActivity.EXTRA_MODE_TEXT_SHARE) {
            renameFile(uris);
        }

        // add entry of file in app database and save the autoincrement id of file
        dbHandler = DatabaseHandler.getDBInstance(context);
        long insertedFileId = dbHandler.addFile(fileName, mDest.getAbsolutePath(), contentType);
        SmartContent sC = dbHandler.addSmartContentInMemory(insertedFileId, mDest.getAbsolutePath(), fileName, new ArrayList<Long>(), contentType);

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
                        dbHandler.deleteSmartContent(associatedFiles.get(0));
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
        }

        for(long addedTagId: addedTags){
            dbHandler.addFileTagEntry(insertedFileId, addedTagId);
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

        super.onPostExecute(aVoid);
    }
}
