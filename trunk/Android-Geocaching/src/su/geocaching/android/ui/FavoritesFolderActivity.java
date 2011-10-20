package su.geocaching.android.ui;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.adapters.FavoritesArrayAdapter;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;

import java.util.ArrayList;

/**
 * Class for create ListActivity with favorites caches
 */
public class FavoritesFolderActivity extends ListActivity {

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();
    private static final String FAVORITES_FOLDER = "/FavoritesActivity";
    private static final String LIST_STATE = "listState";


    private DbManager dbManager;
    private FavoritesArrayAdapter favoriteGeoCachesAdapter;
    private TextView tvNoCache;
    private Parcelable listState = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.favorites_folder_activity);

        favoriteGeoCachesAdapter = new FavoritesArrayAdapter(this);
        dbManager = Controller.getInstance().getDbManager();
        favoriteGeoCachesAdapter.gcItems = dbManager.getArrayGeoCache();

        tvNoCache = (TextView) findViewById(R.id.favorite_folder_title_text);

        getListView().setTextFilterEnabled(true);

        setListAdapter(favoriteGeoCachesAdapter);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getListView().setFilterText(query);
        }

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(FAVORITES_FOLDER);
    }

    @Override
    protected void onResume() {
        ArrayList<GeoCache> geoCachesList = dbManager.getArrayGeoCache();
        favoriteGeoCachesAdapter.clear();
        if (geoCachesList.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
        } else {
            tvNoCache.setVisibility(View.GONE);
            for (GeoCache gc : geoCachesList) {
                favoriteGeoCachesAdapter.add(gc);
            }
        }
        favoriteGeoCachesAdapter.notifyDataSetChanged();

        if (listState != null) {
            getListView().onRestoreInstanceState(listState);
        }
        listState = null;
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listState = savedInstanceState.getParcelable(LIST_STATE);
        getListView().onRestoreInstanceState(listState);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        listState = getListView().onSaveInstanceState();
        savedInstanceState.putParcelable(LIST_STATE, listState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setVisible(true);
        if (favoriteGeoCachesAdapter.isEmpty()) {
            menu.getItem(0).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
        }
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            menu.getItem(0).setVisible(false);
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
                dbManager.clearDB();
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        NavigationManager.startInfoActivity(this, ((GeoCache) this.getListAdapter().getItem(position)));
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
    }

    public void onSearchClick(View v){
        onSearchRequested();
    }
}
