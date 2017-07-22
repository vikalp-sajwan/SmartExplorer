package com.example.vikalpsajwan.smartexplorer.UX;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.Tag;

import java.util.ArrayList;

/**
 * Created by Vikalp on 16/06/2017.
 */

public class TagGridAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Tag> tags;

    public TagGridAdapter(Context context, ArrayList<Tag> tags){
        this.context = context;
        this.tags = tags;
    }

    public String getTagName(int position){
        return tags.get(position).getTagName();
    }

    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return tags.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return null;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final Button tagButton;

        if (convertView == null) {

            tagButton = (Button) inflater.inflate(R.layout.tag_item_grid, null);

            tagButton.setText(tags.get(position).getTagName());

        } else {
            tagButton = (Button) convertView;
            tagButton.setText(tags.get(position).getTagName());
        }

        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity)context;
                if(activity instanceof MainActivity){
                    Intent intent = new Intent(context, TagBasedSearchActivity.class);
                    intent.putExtra("TagID", tags.get(position).getTagId());
                    context.startActivity(intent);
                }else{  // tag based search activity
                    ((TagBasedSearchActivity) activity).performTagAddition(tags.get(position).getTagId());
                }


            }
        });

        return tagButton;
    }

    public void changeDataAndNotify(ArrayList<Tag> newData){
        tags = newData;
        this.notifyDataSetChanged();
    }

//    public void performButtonClick(int position){
//        Toast.makeText(
//                        context, tags.get(position).getTagName(), Toast.LENGTH_SHORT).show();
//    }

}
