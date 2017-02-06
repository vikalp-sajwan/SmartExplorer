package com.example.vikalpsajwan.smartexplorer;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    // GUI elements layout 1
    EditText editText;
    CheckBox checkBoxName;
    CheckBox checkBoxDOM;
    DatePicker datePicker;
    Button button;

    // Toolbar
    Toolbar myToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting up the toolbar
        myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        // getting runtime permission for reading storage on marshmallow and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 1);
        }

        // binding GUI elements in Layout 1
        editText = (EditText) findViewById(R.id.editText);
        checkBoxName = (CheckBox) findViewById(R.id.checkBoxName);
        checkBoxDOM = (CheckBox) findViewById(R.id.checkBoxDOM);
        datePicker = (DatePicker) findViewById(R.id.datePicker);
        button = (Button) findViewById(R.id.button);



    }


    /**
     * overridden method to inflate the Toolbar menu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * called on clicking Search button in initial Layout
     *
     * @param view
     */
    public void search(View view) {
        if (checkBoxDOM.isChecked() || checkBoxName.isChecked()) {

            String searchString = editText.getText().toString();
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();

            if (checkBoxName.isChecked()) {
                if (searchString.trim().isEmpty()) {
                    Toast t = Toast.makeText(getApplicationContext(), "please enter valid search string", Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
            }

            if (!checkBoxDOM.isChecked()) {
                searchFilesByName(searchString);
                ;
            } else if (!checkBoxName.isChecked()) {
                // case for searching based on date of modification
                // not implemented
            } else {
                // case for searching with both name and date of modification
                // not implemented
            }


            Toast t = Toast.makeText(getApplicationContext(), "OK", Toast.LENGTH_SHORT);
            t.show();

        } else {
            Toast t = Toast.makeText(getApplicationContext(), "please select at least one of checkboxes", Toast.LENGTH_SHORT);
            t.show();
            return;
        }

    }

    /**
     * function to show all files activity when clicked on the button on the Toolbar
     *
     * @param item
     */
    public void showAllFilesActivity(MenuItem item) {
        Intent intent = new Intent(this, FilesListViewActivity.class);
        intent.putExtra("EXTRA_MODE", FilesListViewActivity.SHOW_ALL);
        startActivity(intent);

    }

    /**
     * function to search by name in the sqlite database
     */
    public void searchFilesByName(String searchString) {
        Intent intent = new Intent(this, FilesListViewActivity.class);
        intent.putExtra("EXTRA_MODE", FilesListViewActivity.SEARCH_BY_NAME);
        intent.putExtra("EXTRA_SEARCH_STRING", searchString);
        startActivity(intent);

    }
}