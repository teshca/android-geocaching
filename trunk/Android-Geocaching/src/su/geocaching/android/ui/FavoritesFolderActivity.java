package su.geocaching.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
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

import java.util.List;

/**
 * Class for create ListActivity with favorites caches
 */
public class FavoritesFolderActivity extends ListActivity {

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();
    private static final String FAVORITES_FOLDER = "/FavoritesActivity";
    private static final String LIST_STATE = "listState";
    private static final int SORT_TYPE_DIALOG_ID = 0;
    private static final int DELETE_CACHE_DIALOG_ID = 1;

    private DbManager dbManager;
    private FavoritesArrayAdapter favoriteGeoCachesAdapter;
    private TextView tvNoCache;
    private Parcelable listState = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.favorites_folder_activity);

        tvNoCache = (TextView) findViewById(R.id.tvNoCacheFound);
        dbManager = Controller.getInstance().getDbManager();

        favoriteGeoCachesAdapter = new FavoritesArrayAdapter(this);
        favoriteGeoCachesAdapter.setSortType(FavoritesArrayAdapter.GeoCacheSortType.values()[Controller.getInstance().getPreferencesManager().getFavoritesSortType()]);
        getListView().setTextFilterEnabled(true);
        setListAdapter(favoriteGeoCachesAdapter);

        Intent intent = getIntent();
        handleIntent(intent);

        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(FAVORITES_FOLDER);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<GeoCache> favoritesList = dbManager.getFavoritesGeoCaches();

        favoriteGeoCachesAdapter.clear();
        favoriteGeoCachesAdapter.add(favoritesList);
        favoriteGeoCachesAdapter.setAllItemsArray(favoritesList);
        favoriteGeoCachesAdapter.sort();
        favoriteGeoCachesAdapter.notifyDataSetChanged();

        if (favoriteGeoCachesAdapter.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
        } else {
            tvNoCache.setVisibility(View.GONE);
        }

        if (listState != null) {
            getListView().onRestoreInstanceState(listState);
        }
        listState = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getListView().setFilterText(query);
        }
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
                showDialog(DELETE_CACHE_DIALOG_ID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        NavigationManager.startInfoActivity(this, ((GeoCache) this.getListAdapter().getItem(position)));
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActivity(this);
    }

    public void onSearchClick(View v) {
        onSearchRequested();
    }

    public void onSortClick(View v) {
        showDialog(SORT_TYPE_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SORT_TYPE_DIALOG_ID: {
                final String sortBy = getString(R.string.sort_by);
                final String byName = getString(R.string.by_name);
                final String byDistance = getString(R.string.by_distance);
                final String[] items = {byDistance, byName};
                int selectedPosition = Controller.getInstance().getPreferencesManager().getFavoritesSortType();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(sortBy);
                builder.setSingleChoiceItems(items, selectedPosition, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        sort(FavoritesArrayAdapter.GeoCacheSortType.values()[position]);
                        dialog.cancel();
                    }
                });
                return builder.create();
            }
            case DELETE_CACHE_DIALOG_ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(this.getString(R.string.ask_delete_all_cache_in_database)).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbManager.clearDB();
                        favoriteGeoCachesAdapter.clear();
                        favoriteGeoCachesAdapter.notifyDataSetChanged();
                        tvNoCache.setVisibility(View.VISIBLE);
                        dialog.cancel();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                return builder.create();
            }
            default: {
                return null;
            }
        }

    }

    private void sort(FavoritesArrayAdapter.GeoCacheSortType sortType) {
        favoriteGeoCachesAdapter.setSortType(sortType);
        favoriteGeoCachesAdapter.sort();
        favoriteGeoCachesAdapter.notifyDataSetChanged();
        Controller.getInstance().getPreferencesManager().setFavoritesSortType(sortType.ordinal());
    }

}
