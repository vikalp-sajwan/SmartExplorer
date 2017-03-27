package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


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

        ArrayList<Long> associatedTags = new ArrayList<>();

        for(int i = 0; i< tagNames.size(); i++){
            long tagId = dbHandler.isTagPresent(tagNames.get(i));

            if (tagId == -1) {    // tag does not exist in table - add the tag to database
                tagId = dbHandler.addTag(tagNames.get(i), tagsUniqueness.get(i));
            }

            associatedTags.add(tagId);
            // add tag object to Tag object ArrayList in memory
            dbHandler.addTagInMemory(tagId, tagNames.get(i), tagsUniqueness.get(i));

            // add entry in the fileTag table
            dbHandler.addFileTagEntry(insertedFileId, tagId);
        }

        dbHandler.addSmartContentInMemory(insertedFileId, mDest.getAbsolutePath(), fileName, associatedTags );

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
