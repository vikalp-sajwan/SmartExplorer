package com.example.vikalpsajwan.smartexplorer.UX;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.vikalpsajwan.smartexplorer.R;
import com.example.vikalpsajwan.smartexplorer.models.ContentTypeEnum;
import com.example.vikalpsajwan.smartexplorer.models.DatabaseHandler;
import com.example.vikalpsajwan.smartexplorer.models.SmartContent;

import java.io.File;
import java.util.ArrayList;


public class FilesListActivity extends AppCompatActivity {

    public static final int SHOW_ALL = 1;
    public static final int SEARCH = 2;

    public static final String EXTRA_SEARCH_MODE = "EXTRA_SEARCH_MODE";
    public static final String EXTRA_SEARCH_STRING = "EXTRA_SEARCH_STRING";

    ListView listView;
    private DatabaseHandler dbHandler;
//    private Cursor resultCursor;
    private FileListAdapter fla;
    private int mode;

    ArrayList<SmartContent> sCData;
    FileListArrayAdapter flaa;


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
                    flaa.remove(sC);
                    dbHandler.deleteSmartContent(sC);
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
    protected void onCreate(Bundle savedInstanceState) {

        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        Intent intent = this.getIntent();
        mode = intent.getIntExtra(EXTRA_SEARCH_MODE, 1);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files_listview);


        if( mode == SHOW_ALL){
            sCData = dbHandler.getSmartContentData();
        } else{     // SEARCH mode
            String searchString = intent.getStringExtra(EXTRA_SEARCH_STRING);
            sCData = dbHandler.searchContentByString(searchString);
        }

        // Using SmartContent data and ArrayListAdapter
        listView = (ListView) findViewById(R.id.files_listview);

        // custom ArrayList adapter
        flaa = new FileListArrayAdapter(this, R.layout.smart_content_list_item, sCData );
        listView.setAdapter(flaa);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent;

                ContentTypeEnum contentType = sCData.get(position).getContentUnit().getContentType();

                if(contentType == ContentTypeEnum.Note || contentType == ContentTypeEnum.Location){
                    intent = new Intent(getApplicationContext(), ViewNoteActivity.class);
                    intent.putExtra(ViewNoteActivity.EXTRA_CONTENT_ID, sCData.get(position).getContentID());
                }
                else{
                    File file = new File(sCData.get(position).getContentUnit().getContentAddress());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(resultCursor != null)
//            resultCursor.close();
        if(dbHandler != null)
            dbHandler.close();
    }
}
