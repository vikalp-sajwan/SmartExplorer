package in.snapnsave.UX;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import in.snapnsave.R;
import in.snapnsave.models.AndroidDatabaseManager;
import in.snapnsave.models.DatabaseHandler;
import in.snapnsave.models.SmartContent;
import in.snapnsave.models.Tag;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;

import static android.widget.AdapterView.AdapterContextMenuInfo;
import static android.widget.AdapterView.OnItemClickListener;


//import static in.smartexplorer.R.id.actvTag;


public class MainActivity extends AppCompatActivity {
    ListView recentContentListView;
    GridView tagsGridView;
    FlexboxLayout tagsFlexboxLayout;
    // data and adapter for recent content list
    ArrayList<SmartContent> sCData;
    FileListArrayAdapter flaa;

    TextView recentContentTV, mostUsedTagsTV;

    // GUI elements layout 1
    //EditText searchEditText;
    //CheckBox checkBoxName;
    //CheckBox checkBoxTag;
    //AutoCompleteTextView actvTag;
    //Button searchButton;
    private DatabaseHandler dbHandler;
    boolean isActivityVisible;
    boolean isDataSetChanged;

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "dbUpdated" is broadcasted.
    private BroadcastReceiver mdbUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            if(isActivityVisible) {
//                updateRecentContent();
                populateRecentContent();
                populateTags();
            }
            else
                isDataSetChanged = true;
        }
    };
    //     Toolbar
    private Toolbar mToolbar;


    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();
        isActivityVisible = false;
    }

    /**
     * Dispatch onResume() to fragments.  Note that for better inter-operation
     * with older versions of the platform, at the point of this call the
     * fragments attached to the activity are <em>not</em> resumed.  This means
     * that in some cases the previous state may still be saved, not allowing
     * fragment transactions that modify the state.  To correctly interact
     * with fragments in their proper state, you should instead override
     * {@link #onResumeFragments()}.
     */
    @Override
    protected void onResume() {
        super.onResume();
        isActivityVisible = true;
        if(isDataSetChanged){
//            updateRecentContent();
            populateRecentContent();
            populateTags();
            isDataSetChanged=false;
        }
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected void onStart() {
        super.onStart();
        //#####******  NOT SO IMPORTANT, CAN BE OMITTED
        dbHandler.updateTagAccessDataFromDB();
        populateTags();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mdbUpdateReceiver);
    }


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
        // Inflate the menu; this adds items to the mActionBar bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {
            case R.id.action_camera_capture:
                captureImage();
                return true;

            case R.id.action_settings:
                return true;

            case R.id.action_show_all:
                showAllFilesActivity();
                return true;

            case R.id.action_show_memory_elements:
                intent = new Intent(this, InMemoryElementsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_show_sql_db:
                intent = new Intent(this, AndroidDatabaseManager.class);
                startActivity(intent);
                return true;

            case R.id.action_search:
                intent = new Intent(this, in.snapnsave.UX.SearchActivity.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
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


        // binding GUI elements in Layout 1
//        searchEditText = (EditText) findViewById(R.id.editText);
//        searchEditText.clearFocus();
        //checkBoxName = (CheckBox) findViewById(R.id.checkBoxName);
        //checkBoxTag = (CheckBox) findViewById(R.id.checkBoxTag);
        //searchButton = (Button) findViewById(R.id.button);
        //actvTag = (AutoCompleteTextView) findViewById(actvTag);
        recentContentListView = (ListView) findViewById(R.id.recentContentListview);
        tagsFlexboxLayout = (FlexboxLayout) findViewById(R.id.popularTagsFlexbox);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());
        // dbHandler.dbResponse = this;

       // ArrayList<String> autoCompleteTagList = dbHandler.getTagNames();
       // ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);

        recentContentTV = (TextView) findViewById(R.id.recentContentTV);
        mostUsedTagsTV = (TextView) findViewById(R.id.mostUsedTagsTV);

        populateRecentContent();

        populateTags();



        // getting runtime permission for reading storage on marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE") == PackageManager.PERMISSION_DENIED) {

                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }
        }


        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "dbUpdated".
        LocalBroadcastManager.getInstance(this).registerReceiver(mdbUpdateReceiver,
                new IntentFilter(DatabaseHandler.DB_UPDATED));

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
                    //flaa.remove(sC);
                    dbHandler.deleteSmartContent(sC.getContentID());
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
    private void populateRecentContent() {
        recentContentListView.setAdapter(null);

        sCData = dbHandler.getRecentContentData();
//        if(sCData.size() == 0)
//            recentContentTV.setVisibility(View.INVISIBLE);
//        else
//            recentContentTV.setVisibility(View.VISIBLE);

        flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, sCData);
        recentContentListView.setAdapter(flaa);

        registerForContextMenu(recentContentListView);

        // onItemClickListener to open the content by appropriate means
        recentContentListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                intent = new Intent(getApplicationContext(), in.snapnsave.UX.ViewContentActivity.class);
                long[] contentIDArray = new long[sCData.size()];
                for(int i = 0; i<sCData.size(); i++)
                    contentIDArray[i] = sCData.get(i).getContentID();
                intent.putExtra(in.snapnsave.UX.ViewContentActivity.EXTRA_CONTENT_ID_ARRAY, contentIDArray);
                intent.putExtra(in.snapnsave.UX.ViewContentActivity.EXTRA_CURRENT_CONTENT_INDEX, position);

                //update tag access times of all the associated tags of clicked content
                SmartContent clickedSC = sCData.get(position);
                for(Tag tag: clickedSC.getAssociatedTags()){
                    dbHandler.updateTagAccess(tag.getTagId());
                }

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "No suitable app found!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * method to fetch tags and process them to calculate ranks for most popular tags
     * then set the highest scoring 12 tags in tag GridAdapter
     */
    private void populateTags() {
        ArrayList<Tag> usedTags = dbHandler.getUsedTags();
        tagsFlexboxLayout.removeAllViews();

        if(usedTags.size() == 0) {
            mostUsedTagsTV.setVisibility(View.INVISIBLE);
            tagsFlexboxLayout.setVisibility(View.INVISIBLE);
        }
        else {
            mostUsedTagsTV.setVisibility(View.VISIBLE);
            tagsFlexboxLayout.setVisibility(View.VISIBLE);
        }

        float[] score = new float[usedTags.size()];
        for(int i = 0 ; i<usedTags.size(); i++){
            score[i] = dbHandler.getTagScore(usedTags.get(i));
        }

        ArrayList<Tag> finalTagList = new ArrayList<>();

        for(int i = 0 ; i<12 && i<usedTags.size() ; i++){
            float max=0.0f;
            int maxIndex=0;
            for(int j=0; j<usedTags.size(); j++){
                if(score[j]>max){
                    max = score[j];
                    maxIndex= j;
                }

            }
            finalTagList.add(usedTags.get(maxIndex));
            score[maxIndex] = 0;
        }

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(final Tag t: finalTagList){
            final TextView tagButton;

            tagButton = (TextView) inflater.inflate(R.layout.tag_item_flexbox, null);

            tagButton.setText(t.getTagName());

            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, in.snapnsave.UX.TagBasedSearchActivity.class);
                    intent.putExtra("TagID", t.getTagId());
                    startActivity(intent);
                }
            });
            tagsFlexboxLayout.addView(tagButton);
        }

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
    public void showAllFilesActivity() {
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
     * called on clicking the capture by camera button in the menu toolbar
     */
    public void captureImage() {
        Intent intent = new Intent(this, AddContentActivity.class);
        intent.putExtra(AddContentActivity.EXTRA_MODE, AddContentActivity.EXTRA_MODE_IMAGE_CAPTURE);
        startActivity(intent);
    }

//    /**
//     * method to be called when loadData() is  called in DatabaseHandler object
//     */
//    @Override
//    public void dataLoadFinish() {
//        populateRecentContent();
//    }
}