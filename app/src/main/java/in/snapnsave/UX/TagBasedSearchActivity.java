package in.snapnsave.UX;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
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

import in.snapnsave.UX.FileListArrayAdapter;
import in.snapnsave.UX.InMemoryElementsActivity;

import static android.widget.AdapterView.*;

/**
 * Created by Vikalp on 16/06/2017.
 */

public class TagBasedSearchActivity extends AppCompatActivity {

    //     Toolbar
    private Toolbar mToolbar;
    DatabaseHandler dbHandler;
    ActionBar mActionBar;
    LinearLayout tagChainContainer;
    ListView contentListView;
    FlexboxLayout tagsFlexbox;
    ArrayList<SmartContent> sCData;
    ArrayList<Tag> selectedTags;
    // the list of tags which are related to currently selected tags
    ArrayList<Tag> relatedTags;
    // the score of relatedTags on the respective index
    ArrayList<Float> relatedTagsScore;
    FileListArrayAdapter flaa;
    TextView relatedTagsTV;
    // the first tag which was clicked and launched this activity
    long parentTagId;

    boolean isActivityVisible;
    boolean isDataSetChanged;

    ProgressDialog pDialog;

    // Our handler for received Intents. This will be called whenever an Intent
    // with an action named "dbUpdated" is broadcasted.
    private BroadcastReceiver mdbUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);
            if(isActivityVisible) {
                dBUpdationChanges();
            }
            else
                isDataSetChanged = true;
        }
    };

    private void dBUpdationChanges() {
//        ProgressDialog progressdialog = new ProgressDialog(this);
//        progressdialog.setMessage("Please Wait....");
//        progressdialog.setCancelable(false);
//        progressdialog.show();


        ArrayList<Tag> newSelectedTags = new ArrayList<>();
        for(Tag t : selectedTags){
            // get latest copy of tag
            t = dbHandler.getTagHash().get(t.getTagId());
            if( t.getAssociatedContent().size() != 0)
                newSelectedTags.add(t);
        }

        tagChainContainer.removeAllViewsInLayout();
        selectedTags = new ArrayList<>();
        relatedTags = new ArrayList<>();
        relatedTagsScore = new ArrayList<>();

        if(newSelectedTags.size() == 0){
//            progressdialog.dismiss();
            finish();
            return;
        }else{
            parentTagId = newSelectedTags.get(0).getTagId();
        }

        for(Tag t: newSelectedTags){
            performTagAddition(t.getTagId());
        }

//        ArrayList<SmartContent> tempSmartContent = dbHandler.getAssociatedContent(selectedTags.get(0).getTagId());
//        // now filter the tempSmartContent based on if other they contain other selected tags also starting from index 1
//        // if not, then set those SC to null
//        for (int i = 1; i<selectedTags.size(); i++){
//            Tag t = selectedTags.get(i);
//            for(int j=0; j<tempSmartContent.size(); j++){
//                if(tempSmartContent.get(i)!=null && !tempSmartContent.get(i).getAssociatedTags().contains(t))
//                    tempSmartContent.set(i, null);
//            }
//
//        }
//
//        tagGridAdapter.notifyDataSetChanged();
//        flaa.notifyDataSetChanged();
//

//        progressdialog.dismiss();
    }


//    private boolean isSearchOpened = false;

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
            dBUpdationChanges();
            isDataSetChanged = false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mdbUpdateReceiver);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag_based_search);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        relatedTagsTV = (TextView)findViewById(R.id.relatedTagsTV);

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());
        contentListView = (ListView) findViewById(R.id.contentListView);
        tagsFlexbox = (FlexboxLayout) findViewById(R.id.relatedTagsFlexbox);

        mActionBar = getSupportActionBar(); //get the actionbar

        mActionBar.setDisplayShowCustomEnabled(true); //enable it to display a
        // custom view in the mActionBar bar.

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tagChainContainer = (LinearLayout) inflater.inflate(R.layout.tag_chain_container, null);
        mActionBar.setCustomView(tagChainContainer, new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mActionBar.setDisplayShowTitleEnabled(false); //hide the title


        // get the tag id from intent which was clicked
        Intent intent = this.getIntent();
        parentTagId = intent.getLongExtra("TagID", 0);

        selectedTags = new ArrayList<>();
        relatedTags = new ArrayList<>();
        relatedTagsScore = new ArrayList<>();

        performTagAddition(parentTagId);

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "dbUpdated".
        LocalBroadcastManager.getInstance(this).registerReceiver(mdbUpdateReceiver,
                new IntentFilter(DatabaseHandler.DB_UPDATED));

    }

    /**
     * performs all the required operation when user clicks a tag in MainActivity or in TagBasedSearchActivity
    */
     public void performTagAddition(long tagId){

         //update tag access times of all the associated tags of clicked content
         dbHandler.updateTagAccess(tagId);

         addTagToTagChain(tagId);
         populateContent(tagId);
         // populateRelatedTags() has to be called after the populateContent(),
         // as it depends on the new SmartContent generated in populateContent()
         populateRelatedTags(tagId);
    }

    /**
     * adds and inflates tag in the tag chain in action bar
     *
     * @param tagId
     */
    public void addTagToTagChain(Long tagId) {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Tag tag = dbHandler.tagHash.get(tagId);

        TextView tagTV;
        if (tag.isUniqueContent())
            tagTV = (TextView) inflater.inflate(R.layout.tag_item_unique, tagChainContainer, false);
        else
            tagTV = (TextView) inflater.inflate(R.layout.tag_item, tagChainContainer, false);
        tagTV.setText(tag.getTagName());
        tagChainContainer.addView(tagTV);

        selectedTags.add(tag);
    }

    private void populateRelatedTags(long tagId){
        tagsFlexbox.removeAllViews();
        if (tagId == parentTagId) {
            relatedTags = dbHandler.getRelatedTags(tagId);
        } else {
            ArrayList<Tag> newRelatedTags = new ArrayList<>();
            Tag clickedTag = dbHandler.getTagHash().get(tagId);
            relatedTags.remove(clickedTag);

            for(SmartContent sc : sCData){
                ArrayList<Tag> associatedTags = sc.getAssociatedTags();
                for(Tag tag: associatedTags)
                    if(!selectedTags.contains(tag) && !newRelatedTags.contains(tag))
                        newRelatedTags.add(tag);
            }
            relatedTags = newRelatedTags;

        }

        float[] score = new float[relatedTags.size()];
        for(int i = 0 ; i<relatedTags.size(); i++){
            score[i] = dbHandler.getTagScore(relatedTags.get(i));
        }

        // list of up to 12 tags sorted by rank to display in tag grid
        ArrayList<Tag> tagGridTagList = new ArrayList<>();

        for(int i = 0 ; i<12 && i<relatedTags.size() ; i++){
            float max=0.0f;
            int maxIndex=0;
            for(int j=0; j<relatedTags.size(); j++){
                if(score[j]>max){
                    max = score[j];
                    maxIndex= j;
                }

            }
            tagGridTagList.add(relatedTags.get(maxIndex));
            score[maxIndex] = 0;
        }

        if(tagGridTagList.size() == 0) {
            relatedTagsTV.setVisibility(INVISIBLE);
            tagsFlexbox.setVisibility(INVISIBLE);
        }
        else {
            relatedTagsTV.setVisibility(VISIBLE);
            tagsFlexbox.setVisibility(VISIBLE);
        }
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(final Tag t: tagGridTagList){
            final TextView tagButton;

            tagButton = (TextView) inflater.inflate(R.layout.tag_item_flexbox, null);

            tagButton.setText(t.getTagName());

            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performTagAddition(t.getTagId());
                }
            });

            tagsFlexbox.addView(tagButton);
        }
    }

    /**
     * method to load and populate the content in list
     */
    private void populateContent(long tagId) {
        //contentListView.setAdapter(null);
        Tag tag = dbHandler.getTagHash().get(tagId);
        // if first tag then get content from db and instantiate the adapter
        if (tagId == parentTagId) {
            sCData = dbHandler.getAssociatedContent(parentTagId);
            flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, sCData);
            contentListView.setAdapter(flaa);

            registerForContextMenu(contentListView);

            // onItemClickListener to open the content by appropriate means
            contentListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent;

                    intent = new Intent(getApplicationContext(), in.snapnsave.UX.ViewContentActivity.class);
                    long[] contentIDArray = new long[sCData.size()];
                    for (int i = 0; i < sCData.size(); i++)
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
        } else {
            // if not the first tag then update the content in List and return
            ArrayList<SmartContent> newSCData = new ArrayList<>();
            for (SmartContent sc : sCData) {
                if (sc.getAssociatedTags().contains(tag))
                    newSCData.add(sc);
            }
            flaa.clear();
            sCData = newSCData;
            flaa.addAll(sCData);
            flaa.notifyDataSetChanged();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the mActionBar bar if it is present.
        getMenuInflater().inflate(R.menu.activity_tag_based_search_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
                    SmartContent sC = sCData.get(info.position);
//                    flaa.remove(sC);
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

}
