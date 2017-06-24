package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;
import com.example.vikalpsajwan.smartexplorer.models.Tag;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by Vikalp on 20/06/2017.
 */

public class PagerFragment extends Fragment {

    public static final String CONTENT_ID = "ContentID";
    DatabaseHandler dbHandler;

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_view_content, container, false);
        dbHandler = DatabaseHandler.getDBInstance(getContext());



        ImageView sContentThumb;
        ImageView sContentThumbOverlay;
        TextView noteTV;
        FrameLayout clickableFrame;

        // Get the arguments that was supplied when
        // the fragment was instantiated in the
        // CustomPagerAdapter
        Bundle args = getArguments();
        long contentID = args.getLong("ContentID");
        final SmartContent sC = dbHandler.getSmartContentHash().get(contentID);


        sContentThumb = (ImageView)rootView.findViewById(R.id.smart_content_thumb);
        sContentThumbOverlay = (ImageView)rootView.findViewById(R.id.smart_content_thumb_overlay);
        noteTV = (TextView)rootView.findViewById(R.id.note_text_view);
        clickableFrame = (FrameLayout)rootView.findViewById(R.id.clickable_frame);
        noteTV.setVisibility(View.INVISIBLE);

        Glide.clear(sContentThumb);
        sContentThumbOverlay.setVisibility(View.INVISIBLE);



        if(sC != null){

            ContentTypeEnum ContentType = sC.getContentUnit().getContentType();


            Drawable myDrawable;

            if( ContentType == ContentTypeEnum.Image || ContentType == ContentTypeEnum.Video) {
                Glide.with(getContext())
                        .load(new File(sC.getContentUnit().getContentAddress()))
                        .thumbnail(0.1f)
                        .fitCenter()
                        .into(sContentThumb);
                if( ContentType == ContentTypeEnum.Video){
                    sContentThumbOverlay.setVisibility(View.VISIBLE);
                }
                sContentThumb.setVisibility(View.VISIBLE);
            }
            else if(ContentType == ContentTypeEnum.Note){
                noteTV.setVisibility(View.VISIBLE);
                noteTV.setText(dbHandler.getTextContentHash().get(sC.getContentID()).getContentText());
                // not using linkify because it is interfaring with the onClickListener of FrameLayout
                // and click on FrameLayout is not working.
                // Todo: another way could be to use linkify and give user a chance to edit text in edit option in toolbar
//              Linkify.addLinks(noteTV, Linkify.ALL);
            }
            else {
                if (ContentType == ContentTypeEnum.Audio) {
                    myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_audio);
                } else if (ContentType == ContentTypeEnum.Document) {
                    myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_document);
                }else if (ContentType == ContentTypeEnum.Location) {
                    myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_location);
                }else{
                    myDrawable = ContextCompat.getDrawable(getContext(), R.mipmap.ic_other);
                }
                sContentThumb.setImageDrawable(myDrawable);
                sContentThumb.setVisibility(View.VISIBLE);

            }


            LinearLayout sContentTagContainer = (LinearLayout)rootView.findViewById(R.id.smart_content_tag_container);
            TextView sContentDescription = (TextView)rootView.findViewById(R.id.content_description);

            sContentDescription.setText(sC.getContentDescription());
            sContentTagContainer.removeAllViews();
            ArrayList<Tag> associatedTags = sC.getAssociatedTags();
            for(int i=0; i< associatedTags.size(); i++){
                TextView tagTV;
                if(associatedTags.get(i).isUniqueContent())
                    tagTV = (TextView)inflater.inflate(R.layout.tag_item_unique, sContentTagContainer, false);
                else
                    tagTV = (TextView)inflater.inflate(R.layout.tag_item, sContentTagContainer, false);
                tagTV.setText(associatedTags.get(i).getTagName());
                sContentTagContainer.addView(tagTV);
            }
        }







        clickableFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                ContentTypeEnum contentType = sC.getContentUnit().getContentType();

                if (contentType == ContentTypeEnum.Note || contentType == ContentTypeEnum.Location) {
                    intent = new Intent(getContext(), ViewNoteActivity.class);
                    intent.putExtra(ViewNoteActivity.EXTRA_CONTENT_ID, sC.getContentID());

                } else {
                    File file = new File(sC.getContentUnit().getContentAddress());
                    Uri uri = Uri.fromFile(file);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                    String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    intent.setDataAndType(uri, mimeType);
                }

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), "No suitable app found!!", Toast.LENGTH_LONG).show();
                }
            }
        });


        return rootView;
    }
}
