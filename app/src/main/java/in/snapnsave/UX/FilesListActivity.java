package in.snapnsave.UX;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import in.snapnsave.R;
import in.snapnsave.models.DatabaseHandler;
import in.snapnsave.models.SmartContent;
import in.snapnsave.models.Tag;

import java.util.ArrayList;

import in.snapnsave.UX.FileListArrayAdapter;


public class FilesListActivity extends AppCompatActivity {

    public static final int SEARCH = 1;
    public static final int SHOW_ALL = 2;

    public static final String EXTRA_SEARCH_MODE = "EXTRA_SEARCH_MODE";

    ListView listView;
    private DatabaseHandler dbHandler;
//    private Cursor resultCursor;
//    private FileListAdapter fla;
    private int mode;


    ArrayList<SmartContent> sCData;
    FileListArrayAdapter flaa;

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
                setupInterface();
            }
            else
                isDataSetChanged = true;
        }
    };

//    Toolbar
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
            setupInterface();
            isDataSetChanged = false;
        }
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
        getMenuInflater().inflate(R.menu.activity_files_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_search){
            Intent intent = new Intent(this, in.snapnsave.UX.SearchActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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


//    @Override
//
//    protected void onNewIntent(Intent intent) {
//        setIntent(intent);
////        super.onNewIntent(intent);
//        String searchString = intent.getStringExtra(SearchManager.QUERY);
//        sCData = dbHandler.searchContentByString(searchString);
//        setupInterface();
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        Intent intent = this.getIntent();
        mode = intent.getIntExtra(EXTRA_SEARCH_MODE, 1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_listview);


        //setting up the toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        listView = (ListView) findViewById(R.id.files_listview);

        setupInterface();

        // Register to receive messages.
        // We are registering an observer (mMessageReceiver) to receive Intents
        // with actions named "dbUpdated".
        LocalBroadcastManager.getInstance(this).registerReceiver(mdbUpdateReceiver,
                new IntentFilter(DatabaseHandler.DB_UPDATED));



        // simple cursor adapter
//        // source columns from database
//        String[] from = new String[]{DatabaseHandler.colfilename, DatabaseHandler.colfileAddress};
//        // ids of views in listview UI
//        int[] to = new int[]{R.id.textview_filename, R.id.textview_filepath};
//
//        listView.setAdapter(new SimpleCursorAdapter(this, R.layout.list_files_listview_item, resultCursor, from, to, 0));

        ///////////////############^^^^^^^^^^^^^^^&&&&&&&&&&// custom cursor adapter
//        fla = new FileListAdapter(this, resultCursor, 0);
//        listView.setAdapter(fla);
//
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                TextView tv = (TextView)view.findViewById(R.id.textview_filepath);
//                File file = new File (tv.getText().toString());
//                Uri uri = Uri.fromFile(file);
//                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//
//                String  extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
//                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
//                intent.setDataAndType(uri, mimeType);
//
//                try{
//                    startActivity(intent);
//                }
//                catch (ActivityNotFoundException e){
//                    Toast.makeText(getApplicationContext(),"No suitable app found!!",Toast.LENGTH_LONG).show();
//                }
//            }
//        });

    }


    public void setupInterface(){

        listView.setAdapter(null);

        sCData = dbHandler.getSmartContentData();

        // custom ArrayList adapter
        flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, sCData );
        listView.setAdapter(flaa);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

                try{
                    startActivity(intent);
                }
                catch (ActivityNotFoundException e){
                    Toast.makeText(getApplicationContext(),"No suitable app found!!",Toast.LENGTH_LONG).show();
                }
            }
        });

    }


}
