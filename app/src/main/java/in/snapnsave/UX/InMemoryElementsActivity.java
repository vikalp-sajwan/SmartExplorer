package in.snapnsave.UX;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import in.snapnsave.R;
import in.snapnsave.models.DatabaseHandler;

/**
 * Created by Vikalp on 31/05/2017.
 */

public class InMemoryElementsActivity extends AppCompatActivity {

    private DatabaseHandler dbHandler;
    TextView memoryElementsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_memory_elements);
        dbHandler = DatabaseHandler.getDBInstance(getApplicationContext());

        memoryElementsTextView = (TextView)findViewById(R.id.memoryElementsTextView);
        memoryElementsTextView.setText(dbHandler.populateDemoTV());
    }
}
