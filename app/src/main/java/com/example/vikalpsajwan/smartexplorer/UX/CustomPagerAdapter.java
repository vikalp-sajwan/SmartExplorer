package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.vikalpsajwan.smartexplorer.models.SmartContent;

import java.util.ArrayList;

/**
 * Created by Vikalp on 20/06/2017.
 */

public class CustomPagerAdapter extends FragmentStatePagerAdapter {
    protected Context mContext;
    ArrayList<SmartContent> smartContentList;
//    int currentIndex;

    public CustomPagerAdapter(FragmentManager fm, Context context, ArrayList<SmartContent> smartContentList) {
        super(fm);
        this.smartContentList = smartContentList;
        mContext = context;
//        this.currentIndex = currentIndex;
    }

    @Override
    // This method returns the fragment associated with
    // the specified position.
    //
    // It is called when the Adapter needs a fragment
    // and it does not exists.
    public Fragment getItem(int position) {

        // Create fragment object
        Fragment fragment = new PagerFragment();

//        // Attach some data to it that we'll
//        // use to populate our fragment layouts
        Bundle args = new Bundle();
        args.putLong(PagerFragment.CONTENT_ID, smartContentList.get(position).getContentID());
//
//        // Set the arguments on the fragment
//        // that will be fetched in DemoFragment@onCreateView
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public int getCount() {
        return smartContentList.size();
    }

}