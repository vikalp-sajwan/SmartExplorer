package in.snapnsave.UX;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.util.Linkify;
import android.widget.TextView;

import in.snapnsave.R;
import in.snapnsave.models.DatabaseHandler;


/**
 * Created by Vikalp on 09/04/2017.
 */

public class ViewNoteActivity extends AppCompatActivity {
    private DatabaseHandler dbHandler;
    TextView noteTextView;

    public static final String EXTRA_CONTENT_ID = "EXTRA_CONTENT_ID";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        Intent intent = this.getIntent();
        long contentId = intent.getLongExtra(EXTRA_CONTENT_ID, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);

        noteTextView = (TextView)findViewById(R.id.noteTextView);
        noteTextView.setText(dbHandler.getTextContentHash().get(contentId).getContentText());
        Linkify.addLinks(noteTextView, Linkify.ALL);
    }
}
