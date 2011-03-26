package su.geocaching.android.ui;

import su.geocaching.android.controller.LogManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

/**
 * Class for create ListActivity with checkpoints caches
 */
public class CheckpointsFolder extends AbstractCacheFolder implements OnItemClickListener {

    public static final String CACHE_ID = "cacheId";
    public static final String ACTION_KEY = "action";

    private static final String TAG = FavoritesFolder.class.getCanonicalName();
    private final Intent intent = new Intent();
    private AlertDialog alert;
    private int cacheid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cacheid = getIntent().getIntExtra(CACHE_ID, 0);

        // TODO
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Контрольная точка").setCancelable(false).setPositiveButton("Выбрать", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                intent.putExtra(ACTION_KEY, 1);
                finish();
            }
        }).setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                intent.putExtra(ACTION_KEY, 2);
                finish();
            }
        });
        alert = builder.create();
    }

    @Override
    protected void onResume() {
        favoritesList = dbm.getCheckpointsArrayById(cacheid);
        if (favoritesList.isEmpty()) {
            lvListShowCache.setAdapter(null);
            tvNoCache.setText(getString(R.string.favorit_folder_In_DB_not_cache)); // TODO Replace
            LogManager.d(TAG, "checkpoints DB empty");
        } else {
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, createGeoCacheList(favoritesList), R.layout.row_in_favorit_rolder, keys, new int[] { R.id.favorite_list_image_button_type,
                    R.id.favorite_list_text_view_name, R.id.favorites_row_type_text, R.id.favorites_row_status_text });
            lvListShowCache.setAdapter(simpleAdapter);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.checkpoints_folder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //UiHelper.
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        alert.show();
        intent.putExtra(CACHE_ID, favoritesList.get(arg2).getId());
        setResult(RESULT_OK, intent);
    }
}
