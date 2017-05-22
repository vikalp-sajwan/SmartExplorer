package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.AndroidDatabaseManager;
import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;

import java.io.File;
import java.util.ArrayList;

import static android.widget.AdapterView.AdapterContextMenuInfo;
import static android.widget.AdapterView.OnItemClickListener;


//import static com.example.vikalpsajwan.smartexplorer.R.id.actvTag;


public class MainActivity extends AppCompatActivity {

    // GUI elements layout 1
    //EditText searchEditText;
    //CheckBox checkBoxName;
    //CheckBox checkBoxTag;
    //AutoCompleteTextView actvTag;
    //Button searchButton;
    ListView recentContentListView;
    // data and adapter for recent content list
    ArrayList<SmartContent> sCData;
    FileListArrayAdapter flaa;

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private SpaceMultiAutoCompleteTextView searchMACTV;

    private DatabaseHandler dbHandler;

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     * <p>
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

//     Toolbar
    private Toolbar mToolbar;

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.camera_capture:
                return true;
            case R.id.action_settings:
                return true;
            case R.id.show_all:
                return true;
            case R.id.action_search:
                handleMenuSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void handleMenuSearch() {
        ActionBar action = getSupportActionBar(); //get the actionbar

        if (isSearchOpened) { //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchMACTV.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_search));

            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.search_bar, null);
            action.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            //action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

//            if(searchMACTV == null) {
                searchMACTV = (SpaceMultiAutoCompleteTextView) action.getCustomView().findViewById(R.id.search_mactv); //the text editor
                ArrayList<String> autoCompleteTagList = dbHandler.getTagNames();
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
                searchMACTV.setAdapter(adapter);
                searchMACTV.setThreshold(2);
                //this is a listener to do a search when the user clicks on search button
                searchMACTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                            Toast.makeText(getApplicationContext(), "search done", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });

//            }


            searchMACTV.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchMACTV, InputMethodManager.SHOW_IMPLICIT);


            //add the close icon
            mSearchAction.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_close_search));

            isSearchOpened = true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up the toolbar
//        myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
//        setSupportActionBar(myToolbar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // getting runtime permission for reading storage on marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }

        // binding GUI elements in Layout 1
//        searchEditText = (EditText) findViewById(R.id.editText);
//        searchEditText.clearFocus();
        //checkBoxName = (CheckBox) findViewById(R.id.checkBoxName);
        //checkBoxTag = (CheckBox) findViewById(R.id.checkBoxTag);
        //searchButton = (Button) findViewById(R.id.button);
        //actvTag = (AutoCompleteTextView) findViewById(actvTag);
        recentContentListView = (ListView) findViewById(R.id.recentContentListview);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());


        ArrayList<String> autoCompleteTagList = dbHandler.getTagNames();
        ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
        //actvTag.setThreshold(1);
        //actvTag.setAdapter(autoCompleteAdapter);

        populateRecentContent();

        // demonstration purpose
        TextView demoTV = (TextView) findViewById(R.id.textView);
        dbHandler.populateDemoTV(demoTV);

        // %%%%%%%%%%%%%%%%%%%%%%%%$$$$$$$$$$############
//        searchButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                startDB(null);
//                return true;
//            }
//        });


        // %%%%%%%%%%%%%%%%%%%%$$$$$$$$$$$$$#############

    }

    @Override
    public void onBackPressed() {
        if (isSearchOpened) {
            handleMenuSearch();
            return;
        }

        super.onBackPressed();
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
        if (item.getTitle() == "Delete") {
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
        return true;
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

        menu.add(0, v.getId(), 0, "Delete");

    }

    /**
     * method to load and populate the recent 7 contents in a list
     */
    public void populateRecentContent() {
        sCData = dbHandler.getRecentContentData();
        flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, sCData);
        recentContentListView.setAdapter(flaa);

        registerForContextMenu(recentContentListView);

        // onItemClickListener to open the content by appropriate means
        recentContentListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                ContentTypeEnum contentType = sCData.get(position).getContentUnit().getContentType();

                if (contentType == ContentTypeEnum.Note || contentType == ContentTypeEnum.Location) {
                    intent = new Intent(getApplicationContext(), ViewNoteActivity.class);
                    intent.putExtra(ViewNoteActivity.EXTRA_CONTENT_ID, sCData.get(position).getContentID());
                } else {
                    File file = new File(sCData.get(position).getContentUnit().getContentAddress());
                    Uri uri = Uri.fromFile(file);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    intent.setDataAndType(uri, mimeType);
                }


                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "No suitable app found!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        TextView demoTV = (TextView) findViewById(R.id.textView);
        demoTV.setText(" ");
        dbHandler.populateDemoTV(demoTV);
        populateRecentContent();
        super.onResume();

    }

//    /**
//     * overridden method to inflate the Toolbar menu
//     *
//     * @param menu
//     * @return
//     */
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
//
//        // Associate searchable configuration with the SearchView
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
//                .getActionView();
//        searchView.setSearchableInfo(searchManager
//                .getSearchableInfo(new ComponentName(this, FilesListActivity.class)));
//
//
//        return super.onCreateOptionsMenu(menu);
//    }


//    /**
//     * called on clicking searchButton in initial Layout
//     *
//     * @param view
//     */
//    public void search(View view) {
////        if (checkBoxTag.isChecked() || checkBoxName.isChecked()) {
////
//        String searchString = searchEditText.getText().toString();
////            String tag = actvTag.getText().toString();
////
////            if (checkBoxName.isChecked()) {
//        if (searchString.trim().isEmpty()) {
//            Toast.makeText(getApplicationContext(), "please enter valid search string", Toast.LENGTH_SHORT).show();
//            searchEditText.setText("");
//        }
//        else{
//            // create an instance of background Async task to perform search operation
//            SearchUtility searchUtil = new SearchUtility(getApplicationContext());
//            // start background search
//            searchUtil.execute(searchString);
//        }
//        searchByString(searchString);
////            }
////
////            if (checkBoxTag.isChecked()) {
////                if (tag.trim().isEmpty()) {
////                    Toast.makeText(getApplicationContext(), "please enter valid tag name", Toast.LENGTH_SHORT).show();
////                    return;
////                }
////            }
////
////            if (!checkBoxTag.isChecked()) {
////                searchByName(searchString);
////            } else if (!checkBoxName.isChecked()) {
////                // case for searching by tag
////                searchByTag(tag);
////            } else {
////                // case for searching with both name and tag
////                searchByNameAndTag(searchString, tag);
////            }
////
////
////            Toast t = Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT);
////            t.show();
////
////        } else {
////            Toast t = Toast.makeText(getApplicationContext(), "please select at least one of checkboxes", Toast.LENGTH_SHORT);
////            t.show();
////        }
//
//    }

//    private void searchByString(String searchString) {
//        Intent intent = new Intent(this, FilesListActivity.class);
//        intent.putExtra(FilesListActivity.EXTRA_SEARCH_MODE, FilesListActivity.SEARCH);
//        intent.putExtra(FilesListActivity.EXTRA_SEARCH_STRING, searchString);
//        startActivity(intent);
//    }


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
     *
     * @param view
     */
    public void startDB(View view) {
        Intent intent = new Intent(this, AndroidDatabaseManager.class);
        startActivity(intent);
    }

    /**
     * called on clicking the capture by camera button in the menu toolbar
     *
     * @param item
     */
    public void captureImage(MenuItem item) {
        Intent intent = new Intent(this, AddContentActivity.class);
        intent.putExtra("EXTRA_MODE", AddContentActivity.EXTRA_MODE_IMAGE_CAPTURE);
        startActivity(intent);
    }
}