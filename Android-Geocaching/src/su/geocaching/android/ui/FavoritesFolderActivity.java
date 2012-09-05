package su.geocaching.android.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuInflater;
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
public class FavoritesFolderActivity extends SherlockListActivity {

    private static final String TAG = FavoritesFolderActivity.class.getCanonicalName();
    private static final String FAVORITES_FOLDER = "/FavoritesActivity";
    private static final int SORT_TYPE_DIALOG_ID = 0;
    private static final int DELETE_CACHE_DIALOG_ID = 1;

    private DbManager dbManager;
    private FavoritesArrayAdapter favoriteGeoCachesAdapter;
    private TextView tvNoCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.favorites_folder_activity);

        tvNoCache = (TextView) findViewById(R.id.tvNoCacheFound);

        dbManager = Controller.getInstance().getDbManager();
        getSupportActionBar().setHomeButtonEnabled(true);

        favoriteGeoCachesAdapter = new FavoritesArrayAdapter(this);
        favoriteGeoCachesAdapter.setSortType(FavoritesArrayAdapter.GeoCacheSortType.values()[Controller.getInstance().getPreferencesManager().getFavoritesSortType()]);
        getListView().setTextFilterEnabled(true);
        setListAdapter(favoriteGeoCachesAdapter);
        getListView().getOverScrollMode();
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(FAVORITES_FOLDER);
    }
    
    private void refreshListData() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        // Save ListView state
        Parcelable state = getListView().onSaveInstanceState();
        // Refresh list data
        refreshListData();
        // Restore previous state (including selected item index and scroll position)
        getListView().onRestoreInstanceState(state);

        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean listIsNotEmpty = favoriteGeoCachesAdapter.getAllItemsCount() > 0;
        boolean listIsNotFiltered = favoriteGeoCachesAdapter.getAllItemsCount() == favoriteGeoCachesAdapter.getCount();
        menu.findItem(R.id.menu_delete_all_caches).setVisible(listIsNotEmpty);
        menu.findItem(R.id.menu_search).setVisible(listIsNotEmpty);
        menu.findItem(R.id.menu_sort).setVisible(listIsNotEmpty);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.favorites_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onHome();
                return true;
            case R.id.menu_search:
                onSearchRequested();
                return true;
            case R.id.menu_sort:
                showDialog(SORT_TYPE_DIALOG_ID);
                return true;
            case R.id.menu_delete_all_caches:
                showDialog(DELETE_CACHE_DIALOG_ID);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onHome() {
        NavigationManager.startDashboardActivity(this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        NavigationManager.startInfoActivity(this, ((GeoCache) this.getListAdapter().getItem(position)));
    }

    @Override
    public boolean onSearchRequested() {
        Configuration config = this.getResources().getConfiguration();
        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO)
        {
            Toast.makeText(this, R.string.favorites_hardware_keyboard_alert, Toast.LENGTH_SHORT).show();
        }
        else
        {
            InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            boolean isKeyboardShown = keyboard.showSoftInput(getListView(), InputMethodManager.SHOW_FORCED);
            if (!isKeyboardShown){
                LogManager.e(TAG, "Keyboard is not displayed for filtering");
            }
        }
        return false;
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
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                return builder.create();
            }
            case DELETE_CACHE_DIALOG_ID: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.favorites_activity_confirm_delete_title);
                builder.setMessage(String.format(this.getString(R.string.ask_delete_all_caches_in_database), favoriteGeoCachesAdapter.getAllItemsCount()))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dbManager.clearDB();
                        favoriteGeoCachesAdapter.clear();
                        favoriteGeoCachesAdapter.notifyDataSetChanged();
                        tvNoCache.setVisibility(View.VISIBLE);
                        invalidateOptionsMenu();
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
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
