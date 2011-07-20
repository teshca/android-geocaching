package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CacheNotesActivity extends Activity {

    private static String TAG = CacheNotesActivity.class.getCanonicalName();
    private EditText saveText;
    private int id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "CacheNotesActivity Created");

        setContentView(su.geocaching.android.ui.R.layout.cache_notes_activity);
        saveText = (EditText) findViewById(su.geocaching.android.ui.R.id.cache_notes_text);
        Button saveButton = (Button) findViewById(su.geocaching.android.ui.R.id.text_save_button);

        id = getIntent().getIntExtra(NavigationManager.CACHE_ID, 0);
        saveText.setText(Controller.getInstance().getDbManager().getNoteById(id));
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Controller.getInstance().getDbManager().updateNotes(id, saveText.getText().toString());
                finish();
            }
        });
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActvity(this);
    }
}
