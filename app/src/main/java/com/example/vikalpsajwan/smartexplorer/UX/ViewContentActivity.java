package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.AndroidDatabaseManager;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;

import java.util.ArrayList;

/**
 * Created by Vikalp on 20/06/2017.
 */

public class ViewContentActivity extends AppCompatActivity {
    public static final String EXTRA_CONTENT_ARRAYLIST = "ContentArrayList";
    //     Toolbar
    private Toolbar mToolbar;
    DatabaseHandler dbHandler;
    ActionBar mActionBar;
    ViewPager mViewPager;
    TextView contentTitleTV;
    ArrayList<SmartContent> scData;
    CustomPagerAdapter mCustomPagerAdapter;


    public static final String EXTRA_CURRENT_CONTENT_INDEX = "CurrentContentIndex";

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
                mCustomPagerAdapter.notifyDataSetChanged();
                int position = mViewPager.getCurrentItem();
                // update the current smart content reference
                scData.set(position, dbHandler.getSmartContentHash().get(scData.get(position).getContentID()));
                contentTitleTV.setText(scData.get(position).getContentName());

            }
            else
                isDataSetChanged = true;
        }
    };

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
            mCustomPagerAdapter.notifyDataSetChanged();
            int position = mViewPager.getCurrentItem();
            // update the current smart content reference
            scData.set(position, dbHandler.getSmartContentHash().get(scData.get(position).getContentID()));
            contentTitleTV.setText(scData.get(position).getContentName());
            isDataSetChanged=false;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mdbUpdateReceiver);
    }

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_content);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());


        mActionBar = getSupportActionBar(); //get the actionbar

        mActionBar.setDisplayShowCustomEnabled(true); //enable it to display a
        // custom view in the mActionBar bar.

        mViewPager = (ViewPager)findViewById(R.id.content_pager);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentTitleTV = new TextView(this);
        mActionBar.setCustomView(contentTitleTV, new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mActionBar.setDisplayShowTitleEnabled(false); //hide the title

        // get the tag id from intent which was clicked
        Intent intent = this.getIntent();
        scData = (ArrayList<SmartContent>)intent.getSerializableExtra(EXTRA_CONTENT_ARRAYLIST);
        int currentContentIndex = intent.getIntExtra(EXTRA_CURRENT_CONTENT_INDEX, 0);


        contentTitleTV.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary));
        contentTitleTV.setText(scData.get(currentContentIndex).getContentName());

        mCustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), this, scData);


        mViewPager.setAdapter(mCustomPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                contentTitleTV.setText(scData.get(position).getContentName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

//        mViewPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
//        {
//            @Override
//            public void onGlobalLayout()
//            {
//                mViewPager.setCurrentItem(currentContentIndex);
//            }
//        });
        //Toast.makeText(this, currentContentIndex+"", Toast.LENGTH_LONG).show();
        mViewPager.setCurrentItem(currentContentIndex,false);

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "dbUpdated".
        LocalBroadcastManager.getInstance(this).registerReceiver(mdbUpdateReceiver,
                new IntentFilter(DatabaseHandler.DB_UPDATED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the mActionBar bar if it is present.
        getMenuInflater().inflate(R.menu.activity_view_content_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        switch (id) {

            case R.id.action_edit_content:
                intent = new Intent(this, AddContentActivity.class);
                intent.putExtra(AddContentActivity.EXTRA_MODE, AddContentActivity.EXTRA_MODE_EDIT_CONTENT);
                int sCIndex = mViewPager.getCurrentItem();
                SmartContent sC = scData.get(sCIndex);
                intent.putExtra(AddContentActivity.EXTRA_CONTENT_ID, sC.getContentID());
                startActivity(intent);
                return true;

            case R.id.action_delete_content:
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("DELETE following content. Continue?");
                alertDialog.setMessage("Are you sure you want to DELETE this content?");
                alertDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        int sCIndex = mViewPager.getCurrentItem();
                        SmartContent sC = scData.get(sCIndex);
                        scData.remove(sCIndex);
                        mCustomPagerAdapter.notifyDataSetChanged();

                        // finish the activity if deletes content was the only one
                        if(scData.size()==0) {
                            finish();
                        }
//                        else if(scData.size() > sCIndex)
//                            mViewPager.setCurrentItem(sCIndex);
//                        else
//                            mViewPager.setCurrentItem(sCIndex-1);
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
}
