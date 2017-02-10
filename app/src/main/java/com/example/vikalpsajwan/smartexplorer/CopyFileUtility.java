package com.example.vikalpsajwan.smartexplorer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Created by Vikalp on 07/02/2017.
 */

public class CopyFileUtility extends AsyncTask<Uri, Void, Void> {

    private Context context;
    private DatabaseHandler dbHandler;
    private ArrayList<String> fileTags;
    private String fileName;

    public CopyFileUtility(Context context, ArrayList<String> fileTags, String filename) {
        this.fileTags = fileTags;
        this.context = context;
        this.fileName = filename;
    }

    @Override
    protected Void doInBackground(Uri... uris ) {

        ContentResolver cr = context.getContentResolver();
        //notificationManager = new (NotificationManager)getSystemService(context.NOTIFICATION_SERVICE);


        for (Uri sourceUri : uris) {
            try {

                // open FileInputStream from sourceUri
                FileInputStream in = (FileInputStream) cr.openInputStream(sourceUri);

                // get destination directory and open FileOutputStream for destination
                // method 1 - save in private space in external storage
                File dest = new File(context.getExternalFilesDir(null) + File.separator + fileName);

                // method 2 - save in public space by making an app Folder to save files external storage root
//                File dest = new File(Environment.getExternalStorageDirectory()+File.separator+"Smart Explorer");
//                if(!dest.exists() || !dest.isDirectory()){
//                    dest.mkdir();
//                }
//                dest = new File(dest+File.separator+fileName);


                FileOutputStream out = (FileOutputStream) cr.openOutputStream(Uri.fromFile(dest), "w");


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

                // add entry of file in app database and save the autoincrement id of file
                dbHandler = DatabaseHandler.getDBInstance(context);
                long insertedFileId = dbHandler.addFile(fileName, dest.getAbsolutePath());

                for(String tag: fileTags){
                    long tagId = dbHandler.isTagPresent(tag);

                    if(tagId == -1){    // tag does not exist in table - add the tag to database
                        tagId = dbHandler.addTag(tag);
                    }

                    // add entry in the fileTag table
                    dbHandler.addFileTagEntry(insertedFileId, tagId);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File added successfully", Toast.LENGTH_LONG).show();

        super.onPostExecute(aVoid);
    }
}
