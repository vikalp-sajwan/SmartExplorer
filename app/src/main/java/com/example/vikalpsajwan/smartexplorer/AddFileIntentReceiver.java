package com.example.vikalpsajwan.smartexplorer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;

/**
 * Created by Vikalp on 04/02/2017.
 */

public class AddFileIntentReceiver extends Activity {

    private CopyFileUtility cpUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uri uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
        cpUtil = new CopyFileUtility(getApplicationContext());

        // check if the external storage is mounted
        if(!isExternalStorageWritable()) {
            Toast.makeText(getApplicationContext(),"External Storage not Mounted !! Try again later.", LENGTH_LONG).show();
            this.finish();
            return;
        }

        // start a background Async task to copy file from intent uri to app's private storage space
        cpUtil.execute(uri);
        this.finish();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
