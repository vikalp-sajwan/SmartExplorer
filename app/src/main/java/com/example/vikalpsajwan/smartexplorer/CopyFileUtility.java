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


/**
 * Created by Vikalp on 07/02/2017.
 */

public class CopyFileUtility extends AsyncTask<Uri, Void, Void> {

    Context context;
    private DatabaseHandler dbHandler;

    public CopyFileUtility(Context context) {

        this.context = context;
    }

    @Override
    protected Void doInBackground(Uri... uris) {

        ContentResolver cr = context.getContentResolver();
        //notificationManager = new (NotificationManager)getSystemService(context.NOTIFICATION_SERVICE);

        for (Uri source : uris) {
            try {

                // get file name based on type Uri
                String fileName;
                // file type Uri
                if (source.toString().startsWith("file")) {
                    File file = new File(source.getPath());
                    fileName = file.getName();
                }
                // content Uri
                else {
                    Cursor returnCursor = cr.query(source, null, null, null, null);
                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();
                    fileName = returnCursor.getString(nameIndex);
                    returnCursor.close();
                }

                // open FileInputStream from source
                FileInputStream in = (FileInputStream) cr.openInputStream(source);

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

                // add entry in app database
                dbHandler = DatabaseHandler.getDBInstance(context);
                dbHandler.addFile(fileName, dest.getAbsolutePath());

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;

    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Toast.makeText(context, "File added successfully", Toast.LENGTH_LONG).show();
        if (dbHandler != null)
            dbHandler.close();

        super.onPostExecute(aVoid);
    }
}
