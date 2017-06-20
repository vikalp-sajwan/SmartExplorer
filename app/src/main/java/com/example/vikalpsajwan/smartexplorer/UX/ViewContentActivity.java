package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
                //todo
                return true;

            case R.id.action_delete_content:
                //todo
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
