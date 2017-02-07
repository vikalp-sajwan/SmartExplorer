package com.example.vikalpsajwan.smartexplorer;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class FilesListViewActivity extends AppCompatActivity {

    public static final int SHOW_ALL = 1;
    public static final int SEARCH_BY_NAME = 2;
    public static final int SEARCH_BY_DOM = 3;
    public static final int SEARCH_BY_NAME_DOM =4;

    public static final String EXTRA_SEARCH_MODE = "EXTRA_SEARCH_MODE";
    public static final String EXTRA_SEARCH_STRING = "EXTRA_SEARCH_STRING";
    public static final String EXTRA_SEARCH_DOM = "EXTRA_SEARCH_DOM";

    ListView listView;
    private DatabaseHandler dbHandler;
    private Cursor resultCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        Intent intent = this.getIntent();
        int mode = intent.getIntExtra(EXTRA_SEARCH_MODE, 1);

        if (mode == SHOW_ALL) {
            resultCursor = dbHandler.getAllMarkedFiles();
        } else {
            String searchString = intent.getStringExtra(EXTRA_SEARCH_STRING);
            resultCursor = dbHandler.searchMarkedFilesByName(searchString);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_listview);

        listView = (ListView) findViewById(R.id.files_listview);

        // source columns from database
        String[] from = new String[]{DatabaseHandler.colFilename, DatabaseHandler.colFileAddress};
        // ids of views in listview UI
        int[] to = new int[]{R.id.textview_filename, R.id.textview_filepath};

        listView.setAdapter(new SimpleCursorAdapter(this, R.layout.list_files_listview, resultCursor, from, to, 0));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv = (TextView)view.findViewById(R.id.textview_filepath);
                File file = new File (tv.getText().toString());
                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                String  extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                intent.setDataAndType(uri, mimeType);

                try{
                    startActivity(intent);
                }
                catch (ActivityNotFoundException e){
                    Toast.makeText(getApplicationContext(),"No suitable app found!!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(resultCursor != null)
            resultCursor.close();
        if(dbHandler != null)
            dbHandler.close();
    }
}
