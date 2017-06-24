package com.example.vikalpsajwan.smartexplorer.UX;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.CustomComponents.SpaceMultiAutoCompleteTextView;
import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.AndroidDatabaseManager;
import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.TextContent;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Vikalp on 28/05/2017.
 */

public class SearchActivity extends AppCompatActivity {

    //     Toolbar
    private Toolbar mToolbar;
//    private MenuItem mSearchAction;
    private SpaceMultiAutoCompleteTextView searchMACTV;
    private DatabaseHandler dbHandler;
    ActionBar mActionBar;
    FileListArrayAdapter flaa;
    ListView listView;

    ArrayList<SmartContent> matchSC;

//    private boolean isSearchOpened = false;

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
     * @return You mus/t return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
//        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the mActionBar bar if it is present.
        getMenuInflater().inflate(R.menu.activity_search_menu, menu);
//        mSearchAction = menu.findItem(R.id.action_clear_text);
//        clearText();
        setUpSearch();
        return super.onCreateOptionsMenu(menu);
    }

    private void setUpSearch() {
        mActionBar = getSupportActionBar(); //get the actionbar

        mActionBar.setDisplayShowCustomEnabled(true); //enable it to display a
        // custom view in the mActionBar bar.

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_bar, null);
        mActionBar.setCustomView(view, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //mActionBar.setCustomView(R.layout.search_bar);//add the custom view
        mActionBar.setDisplayShowTitleEnabled(false); //hide the title

//            if(searchMACTV == null) {
        searchMACTV = (SpaceMultiAutoCompleteTextView) mActionBar.getCustomView().findViewById(R.id.search_mactv); //the text editor
        searchMACTV.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_search, 0, 0, 0);
        ArrayList<String> autoCompleteTagList = dbHandler.getAutoCompleteTerms();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, autoCompleteTagList);
        searchMACTV.setAdapter(adapter);
        searchMACTV.setThreshold(2);
        //this is a listener to do a search when the user clicks on search button om keyboard
        searchMACTV.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    return prepareSearch();
                }
                return false;
            }
        });

        searchMACTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                prepareSearch();
            }
        });


        searchMACTV.requestFocus();

        //open the keyboard focused in the edtSearch
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);   // to show/hide keyboard
        imm.showSoftInput(searchMACTV, InputMethodManager.SHOW_IMPLICIT);


        //add the close icon
//        mSearchAction.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_close_search));

    }

    /**
     * checks for valid search string and calls the search() method
     * @return true if search was done else false if Search String is empty.
     */
    public boolean prepareSearch(){
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);   // to show/hide keyboard
        imm.hideSoftInputFromWindow(searchMACTV.getWindowToken(), 0);

        searchMACTV.dismissDropDown();

        String searchString = searchMACTV.getText().toString().trim();
        if (searchString.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter a valid search string", Toast.LENGTH_SHORT).show();
            return false;
        }

        //show progress dialog
        ProgressDialog pDialog = new ProgressDialog(SearchActivity.this);
        pDialog.setMessage("Search in Progress..."); //Set the message for the loading window
        pDialog.setCancelable(true);
        pDialog.setIndeterminate(false);
        pDialog.show();

        search(searchString);

        pDialog.dismiss();

        Toast.makeText(getApplicationContext(), "search done", Toast.LENGTH_SHORT).show();
        return true;
    }

    /**
     * @param searchString performs search operation on searchString and updates UI to display results
     */
    private void search(String searchString) {
        ArrayList<SmartContent> sCData = dbHandler.getSmartContentData();

        FoundContentData fCData = new FoundContentData();

        String[] words = searchString.split("\\s+");
        for (int i_W = 0; i_W < words.length; i_W++) {

            // searching in SmartContent for filename match
            for (int i_SC = 0; i_SC < sCData.size(); i_SC++) {
                SmartContent sC = sCData.get(i_SC);
                if (sC.isWordPersentInTitle(words[i_W])) {
                    fCData.addFoundContent(sC.getContentID(), FoundContent.FILENAME_SCORE);
                }
            }

            // searching in Note type SmartContent's TextContent for content match
            for (int i_SC = 0; i_SC < sCData.size(); i_SC++) {
                SmartContent sC = sCData.get(i_SC);
                long contentId = sC.getContentID();
                if (sC.getContentUnit().getContentType() == ContentTypeEnum.Note) {
                    TextContent tC = dbHandler.getTextContentHash().get(contentId);
                    if (tC.isWordPresentInContent(words[i_W])) {
                        fCData.addFoundContent(contentId, FoundContent.CONTENT_SCORE);
                    }
                }
            }

            // searching in tags for a tag match and then getting all the associated SmartContent if the tag is found
            ArrayList<SmartContent> tagAssocSC;
            long tagId = dbHandler.isTagPresent(words[i_W]);
            if (tagId >= 0)
                tagAssocSC = dbHandler.getTagHash().get(tagId).getAssociatedContent();
            else
                tagAssocSC = new ArrayList<>();
            for (int i_SC = 0; i_SC < tagAssocSC.size(); i_SC++) {
                SmartContent sC = tagAssocSC.get(i_SC);
                fCData.addFoundContent(sC.getContentID(), FoundContent.TAGNAME_SCORE);
            }

        }


        fCData.sort();
        matchSC= new ArrayList<>();
        for(int i = 0 ;i<fCData.getfCData().size(); i++){
            long contentId = fCData.getfCData().get(i).getContentId();
            matchSC.add(dbHandler.getSmartContentHash().get(contentId));
        }
        updateInterface(matchSC);

    }

    private void updateInterface(final ArrayList<SmartContent> fCData) {
        // custom ArrayList adapter
        flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, fCData );
        listView.setAdapter(flaa);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                ContentTypeEnum contentType = fCData.get(position).getContentUnit().getContentType();

                if(contentType == ContentTypeEnum.Note || contentType == ContentTypeEnum.Location){
                    intent = new Intent(getApplicationContext(), ViewNoteActivity.class);
                    intent.putExtra(ViewNoteActivity.EXTRA_CONTENT_ID, fCData.get(position).getContentID());
                }
                else{
                    File file = new File(fCData.get(position).getContentUnit().getContentAddress());
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
                    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                    SmartContent sC = matchSC.get(info.position);
                    flaa.remove(sC);
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
        return  true;
    }

    /**
     * Called when a context menu for the {@code view} is about to be shown.
     * this will be called every
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {

            case R.id.action_clear_text:
                clearText();
                return true;
            case R.id.action_show_memory_elements:
                intent = new Intent(this, InMemoryElementsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_show_sql_db:
                intent = new Intent(this, AndroidDatabaseManager.class);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void clearText() {
//
        if (searchMACTV.getText().toString().trim().isEmpty()) {
            finish();
        } else {
            //hides the keyboard
//            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(searchMACTV.getWindowToken(), 0);
            searchMACTV.setText("");
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        listView = (ListView)findViewById(R.id.files_listview);
        matchSC= new ArrayList<>();

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());


//        isSearchOpened = false;
//        clearText();

    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }


}

class FoundContent {
    public long getContentId() {
        return contentId;
    }

    public int getScore() {
        return score;
    }


    long contentId;

    int score;

    public static int FILENAME_SCORE = 5;
    public static int TAGNAME_SCORE = 4;
    public static int CONTENT_SCORE = 2;
    public static int FILENAME_SCORE_PARTIAL = 1;
    public static int TAGNAME_SCORE_PARTIAL = 0;
    public static int CONTENT_SCORE_PARTIAL = 1;

    public FoundContent(long contentId, int score) {
        this.contentId = contentId;

        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }
}

class FoundContentData {
    public ArrayList<FoundContent> getfCData() {
        return fCData;
    }

    ArrayList<FoundContent> fCData;

    public FoundContentData() {
        fCData = new ArrayList<>();
    }

    /**
     * checks if found content is already present. If not present, then adds it.
     *
     * @param contentId
     * @return index
     */
    public void addFoundContent(long contentId, int score) {
        int index = isPresent(contentId);
        if (index >= 0)
            fCData.get(index).addScore(score);
        else {
            fCData.add(new FoundContent(contentId, score));

        }
    }

    /**
     * searches if the FoundContent is already present in the FoundContentData
     *
     * @param contentId
     * @return -1 if not present
     * else returns the index of FoundContent
     */
    public int isPresent(long contentId) {
        for (int i = 0; i < fCData.size(); i++) {
            if (fCData.get(i).getContentId() == contentId)
                return i;
        }
        return -1;
    }


    public void sort() {
        /**
         * TODO
         * Count sort
         */

        int i, j;
        FoundContent tempFC;
        for (i = 0; i < fCData.size() - 1; i++)

            // Last i elements are already in place
            for (j = 0; j < fCData.size() - i - 1; j++)
                if (fCData.get(j).getScore() < fCData.get(j + 1).getScore()) {
                    tempFC = fCData.get(j);
                    fCData.set(j, fCData.get(j+1));
                    fCData.set(j+1, tempFC);
                }

    }
}