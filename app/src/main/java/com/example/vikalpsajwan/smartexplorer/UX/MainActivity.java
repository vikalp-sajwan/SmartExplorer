package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.models.AndroidDatabaseManager;
import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;

import java.io.File;
import java.util.ArrayList;

import static android.widget.AdapterView.*;

//import static com.example.vikalpsajwan.smartexplorer.R.id.actvTag;


public class MainActivity extends AppCompatActivity {

    // GUI elements layout 1
    EditText searchEditText;
    //CheckBox checkBoxName;
    //CheckBox checkBoxTag;
    //AutoCompleteTextView actvTag;
    Button searchButton;
    ListView recentContentListView;

    private DatabaseHandler dbHandler;

    // Toolbar
    Toolbar myToolbar;

    // data and adapter for recent content list
    ArrayList<SmartContent> sCData;
    FileListArrayAdapter flaa;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up the toolbar
        myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // getting runtime permission for reading storage on marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }

        // binding GUI elements in Layout 1
        searchEditText = (EditText) findViewById(R.id.editText);
        searchEditText.clearFocus();
        //checkBoxName = (CheckBox) findViewById(R.id.checkBoxName);
        //checkBoxTag = (CheckBox) findViewById(R.id.checkBoxTag);
        searchButton = (Button) findViewById(R.id.button);
        //actvTag = (AutoCompleteTextView) findViewById(actvTag);
        recentContentListView = (ListView) findViewById(R.id.recentContentListview);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());


        ArrayList<String> autoCompleteTagList = dbHandler.getTagNames();
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
        //actvTag.setThreshold(1);
        //actvTag.setAdapter(autoCompleteAdapter);

        populateRecentContent();

        // demonstration purpose
        TextView demoTV = (TextView)findViewById(R.id.textView);
        dbHandler.populateDemoTV(demoTV);

        // %%%%%%%%%%%%%%%%%%%%%%%%$$$$$$$$$$############
        searchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startDB(null);
                return true;
            }
        });


        // %%%%%%%%%%%%%%%%%%%%$$$$$$$$$$$$$#############

    }

    /**
     * This hook is called whenever an item in a context menu is selected. The
     * default implementation simply returns false to have the normal processing
     * happen (calling the item's Runnable or sending a message to its Handler
     * as appropriate). You can use this method for any items for which you
     * would like to do processing without those other facilities.
     * <p>
     * Use {@link MenuItem#getMenuInfo()} to get extra information set by the
     * View that added this menu item.
     * <p>
     * Derived classes should call through to the base class for it to perform
     * the default menu handling.
     *
     * @param item The context menu item that was selected.
     * @return boolean Return false to allow normal context menu processing to
     * proceed, true to consume it here.
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        super.onContextItemSelected(item);
        if(item.getTitle() == "Delete") {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("DELETE following content. Continue?");
            alertDialog.setMessage("Are you sure you want to DELETE this content?");
            alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                    SmartContent sC = sCData.get(info.position);
                    flaa.remove(sC);
                    dbHandler.deleteSmartContent(sC);
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


        }
        return  true;
    }

    /**
     * Called when a context menu for the {@code view} is about to be shown.
     * Unlike {@link #onCreateOptionsMenu(Menu)}, this will be called every
     * time the context menu is about to be shown and should be populated for
     * the view (or item inside the view for {@link AdapterView} subclasses,
     * this can be found in the {@code menuInfo})).
     * <p>
     * Use {@link #onContextItemSelected(MenuItem)} to know when an
     * item has been selected.
     * <p>
     * It is not safe to hold onto the context menu after this method returns.
     *
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, v.getId(), 0,  "Delete");

    }

    /**
     * method to load and populate the recent 7 contents in a list
     */
    public void populateRecentContent(){
        sCData = dbHandler.getRecentContentData();
        flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, sCData );
        recentContentListView.setAdapter(flaa);

        registerForContextMenu(recentContentListView);

        // onItemClickListener to open the content by appropriate means
        recentContentListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                ContentTypeEnum contentType = sCData.get(position).getContentUnit().getContentType();

                if(contentType == ContentTypeEnum.Note || contentType == ContentTypeEnum.Location){
                    intent = new Intent(getApplicationContext(), ViewNoteActivity.class);
                    intent.putExtra(ViewNoteActivity.EXTRA_CONTENT_ID, sCData.get(position).getContentID());
                }
                else{
                    File file = new File(sCData.get(position).getContentUnit().getContentAddress());
                    Uri uri = Uri.fromFile(file);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    String  extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    intent.setDataAndType(uri, mimeType);
                }


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
    protected void onResume() {
        super.onResume();
        TextView demoTV = (TextView)findViewById(R.id.textView);
        demoTV.setText(" ");
        dbHandler.populateDemoTV(demoTV);
        populateRecentContent();

    }

    /**
     * overridden method to inflate the Toolbar menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * called on clicking Search searchButton in initial Layout
     *
     * @param view
     */
    public void search(View view) {
//        if (checkBoxTag.isChecked() || checkBoxName.isChecked()) {
//
        String searchString = searchEditText.getText().toString();
//            String tag = actvTag.getText().toString();
//
//            if (checkBoxName.isChecked()) {
        if (searchString.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), "please enter valid search string", Toast.LENGTH_SHORT).show();
            searchEditText.setText("");
            return;
        }
        searchByString(searchString);
//            }
//
//            if (checkBoxTag.isChecked()) {
//                if (tag.trim().isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "please enter valid tag name", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//            }
//
//            if (!checkBoxTag.isChecked()) {
//                searchByName(searchString);
//            } else if (!checkBoxName.isChecked()) {
//                // case for searching by tag
//                searchByTag(tag);
//            } else {
//                // case for searching with both name and tag
//                searchByNameAndTag(searchString, tag);
//            }
//
//
//            Toast t = Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT);
//            t.show();
//
//        } else {
//            Toast t = Toast.makeText(getApplicationContext(), "please select at least one of checkboxes", Toast.LENGTH_SHORT);
//            t.show();
//        }

    }

    private void searchByString(String searchString) {
        Intent intent = new Intent(this, FilesListActivity.class);
        intent.putExtra(FilesListActivity.EXTRA_SEARCH_MODE, FilesListActivity.SEARCH);
        intent.putExtra(FilesListActivity.EXTRA_SEARCH_STRING, searchString);
        startActivity(intent);
    }


    /**
     * function to show all files activity when clicked on the show all files button in the Toolbar
     */
    public void showAllFilesActivity(MenuItem item) {
        Intent intent = new Intent(this, FilesListActivity.class);
        intent.putExtra(FilesListActivity.EXTRA_SEARCH_MODE, FilesListActivity.SHOW_ALL);
        startActivity(intent);
    }

//    /**
//     * function to search files by tag in the sqlite database
//     * @param tag
//     */
//    private void searchByTag(String tag) {
//        Intent intent = new Intent(this, FilesListActivity.class);
//        intent.putExtra(FilesListActivity.EXTRA_SEARCH_MODE, FilesListActivity.SEARCH_BY_TAG);
//        intent.putExtra(FilesListActivity.EXTRA_SEARCH_TAG, tag);
//        startActivity(intent);
//    }


//    public void searchByNameAndTag(String searchString, String tag) {
//
//    }
//
//    /**
//     * function to search files by name in the sqlite database
//     */
//    public void searchByName(String searchString) {
//        Intent intent = new Intent(this, FilesListActivity.class);
//        intent.putExtra(FilesListActivity.EXTRA_SEARCH_MODE, FilesListActivity.SEARCH_BY_NAME);
//        intent.putExtra(FilesListActivity.EXTRA_SEARCH_STRING, searchString);
//        startActivity(intent);
//    }

    /**
     * called on clicking show database (THIRD PARTY FEATURE) button in the main activity
     * @param view
     */
    public void startDB(View view){
        Intent intent = new Intent(this, AndroidDatabaseManager.class);
        startActivity(intent);
    }

    /**
     * called on clicking the capture by camera button in the menu toolbar
     * @param item
     */
    public void captureImage(MenuItem item) {
        Intent intent = new Intent(this, AddContentActivity.class);
        intent.putExtra("EXTRA_MODE", AddContentActivity.EXTRA_MODE_IMAGE_CAPTURE);
        startActivity(intent);
    }
}