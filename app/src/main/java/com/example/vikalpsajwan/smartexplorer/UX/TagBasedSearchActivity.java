package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.CustomComponents.SpaceMultiAutoCompleteTextView;
import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.AndroidDatabaseManager;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.Tag;

import java.util.ArrayList;

import static android.R.attr.tag;
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
    GridView tagsGridView;
    ArrayList<SmartContent> sCData;
    ArrayList<Tag> selectedTags;
    // the list of tags which are related to currently selected tags
    ArrayList<Tag> relatedTags;
    // the score of relatedTags on the respective index
    ArrayList<Float> relatedTagsScore;
    FileListArrayAdapter flaa;
    // the first tag which was clicked and launched this activity
    long parentTagId;
    TagGridAdapter tagGridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tag_based_search);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());
        contentListView = (ListView) findViewById(R.id.contentListView);
        tagsGridView = (GridView) findViewById(R.id.relatedTagsGridView);

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

        calculateScoreAndSort();

    }

    /**
     * performs all the required operation when user clicks a tag in MainActivity or in TagBasedSearchActivity
    */
     public void performTagAddition(long tagId){
        addTagToTagChain(tagId);
        populateContent(tagId);
         // populateRelatedTags() has to be called after the populateContent(),
         // as it depends on the new SmartContent generated in populateContent()
        populateRelatedTags(tagId);
    }

    private void calculateScoreAndSort() {
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
        if (tagId == parentTagId) {
            relatedTags = dbHandler.getRelatedTags(tagId);
            tagGridAdapter = new TagGridAdapter(this, relatedTags);
            tagsGridView.setAdapter(tagGridAdapter);
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
            tagGridAdapter.changeDataAndNotify(newRelatedTags);
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

                    intent = new Intent(getApplicationContext(), ViewContentActivity.class);
                    long[] contentIDArray = new long[sCData.size()];
                    for (int i = 0; i < sCData.size(); i++)
                        contentIDArray[i] = sCData.get(i).getContentID();
                    intent.putExtra(ViewContentActivity.EXTRA_CONTENT_ID_ARRAY, contentIDArray);
                    intent.putExtra(ViewContentActivity.EXTRA_CURRENT_CONTENT_INDEX, position);

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
