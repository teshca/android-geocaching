package su.geocaching.android.ui;

import java.util.ArrayList;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;

/**
 * Class for create ListActivity with favorites caches
 */
public class FavoritesFolderActivity extends AbstractGeoCacheFolderActivity {

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();
    private static final String FAVORITES_FOLDER = "/FavoritesActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        tvNoCache.setText(getString(R.string.favorite_list_In_DB_not_cache));
        Controller.getInstance().getGoogleAnalyticsManager().trackPageView(FAVORITES_FOLDER);
    }

    @Override
    protected void onResume() {
        
        favoritesList = dbm.getArrayGeoCache();
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            favoritesList = getFoundGeoCachesList(query);
        }
        if (favoritesList.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
            lvListShowCache.setAdapter(null);
        } else {
            tvNoCache.setVisibility(View.GONE);
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, createGeoCacheList(favoritesList), R.layout.row_in_favorite_list, keys, new int[] { R.id.favorite_list_image_button_type,
                    R.id.favorite_list_text_view_name, R.id.favorites_row_type_text, R.id.favorites_row_status_text });
            lvListShowCache.setAdapter(simpleAdapter);
        }

        super.onResume();
    }

    private ArrayList<GeoCache> getFoundGeoCachesList(String searchString){
        ArrayList<GeoCache> foundedCaches = new ArrayList<GeoCache>(); 
        for (GeoCache cache : favoritesList){
            searchString = searchString.toLowerCase();
            String nameCache = cache.getName().toLowerCase();
            
            if ((nameCache.indexOf(searchString)) != -1){
                foundedCaches.add(cache);
            }
        }
        if (foundedCaches.isEmpty()){
            tvNoCache.setText(getString(R.string.caches_with_appropriate_name_not_found));
        }
        return foundedCaches;
    }
    
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(this, InfoActivity.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), favoritesList.get(arg2));
        startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (favoritesList.isEmpty()) {
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_all_cache_in_database:
                createAlertDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(this.getString(R.string.ask_delete_all_cache_in_database)).setPositiveButton(this.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbm.clearDB();
                onResume();
                dialog.cancel();
            }
        }).setNegativeButton(this.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog turnOnInternetAlert = builder.create();
        turnOnInternetAlert.show();
    }

}
