package su.geocaching.android.ui;

import su.geocaching.android.controller.managers.CacheNotesManager;
import android.R;
import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CacheNotesActivity extends Activity {

    EditText saveText = null;
    Button saveButton = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(su.geocaching.android.ui.R.layout.cache_notes_activity);
        saveText= (EditText)findViewById(su.geocaching.android.ui.R.id.cache_notes_text);
        saveButton=(Button)findViewById(su.geocaching.android.ui.R.id.text_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CacheNotesManager cacheNotes = new CacheNotesManager(CacheNotesActivity.this);
                SQLiteDatabase dataBase = cacheNotes.getWritableDatabase();
                ContentValues contentValues = new ContentValues();
                contentValues.put(CacheNotesManager.TABLE_TEXT, saveText.getText().toString());

                dataBase.insert(CacheNotesManager.TABLE_NAME, null, contentValues);
                dataBase.close();
                saveText.setText("");
                saveText.setText("");
            }
        });
    }
}
