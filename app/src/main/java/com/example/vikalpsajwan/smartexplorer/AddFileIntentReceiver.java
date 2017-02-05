package com.example.vikalpsajwan.smartexplorer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Vikalp on 04/02/2017.
 */

public class AddFileIntentReceiver extends Activity {

    DatabaseHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);

        // file Uri     --   absolute path is available
        if (uri.toString().startsWith("file")) {
            File file = new File(uri.getPath());
            if (file.exists()) {
                String filename = file.getName();
                String filePath = file.toString();

                dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());
                dbHandler.addFile(filename, filePath);

                Toast.makeText(getApplicationContext(), "ADDED Successfully PATH - " + filePath, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "!!!!! PATH - " + file.toString(), Toast.LENGTH_LONG).show();
            }
        }
        //content Uri  ---   cant get the file path---- cannot ADD in database ----optional solution - search by getting filename
        else {
            Cursor returnCursor = getContentResolver().query(uri, null, null, null, null);

            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            Toast.makeText(getApplicationContext(), "content Uri____" + returnCursor.getString(nameIndex) + "_____" + Long.toString(returnCursor.getLong(sizeIndex)), Toast.LENGTH_LONG).show();
        }

        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dbHandler != null)
            dbHandler.close();
    }
}
