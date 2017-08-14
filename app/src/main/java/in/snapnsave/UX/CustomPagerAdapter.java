package in.snapnsave.UX;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import in.snapnsave.models.SmartContent;

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
        Fragment fragment = new in.snapnsave.UX.PagerFragment();

//        // Attach some data to it that we'll
//        // use to populate our fragment layouts
        Bundle args = new Bundle();
        args.putLong(in.snapnsave.UX.PagerFragment.CONTENT_ID, smartContentList.get(position).getContentID());
//
//        // Set the arguments on the fragment
//        // that will be fetched in DemoFragment@onCreateView
        fragment.setArguments(args);

        return fragment;
    }



    /**
     * Called when the host view is attempting to determine if an item's position
     * has changed. Returns {@link #POSITION_UNCHANGED} if the position of the given
     * item has not changed or {@link #POSITION_NONE} if the item is no longer present
     * in the adapter.
     * <p>
     * <p>The default implementation assumes that items will never
     * change position and always returns {@link #POSITION_UNCHANGED}.
     *
     * @param object Object representing an item, previously returned by a call to
     *               {@link #instantiateItem(View, int)}.
     * @return object's new position index from [0, {@link #getCount()}),
     * {@link #POSITION_UNCHANGED} if the object's position has not changed,
     * or {@link #POSITION_NONE} if the item is no longer present.
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return smartContentList.size();
    }

}