package su.geocaching.android.ui;

import java.io.File;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.ApiManager;
import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class InfoActivity extends Activity {

    private static final String TAG = InfoActivity.class.getCanonicalName();
    private static final String GEOCACHE_INFO_ACTIVITY_FOLDER = "/GeoCacheInfoActivity";
    private static final String PAGE_TYPE = "page type", SCROOLX = "scrollX", SCROOLY = "scrollY", ZOOM = "ZOOM", TEXT_INFO = "info", TEXT_NOTEBOOK = "notebook";

    public enum PageState {
        INFO, NOTEBOOK, PHOTO, NO_INTERNET
    }

    private GeoCache geoCache;
    private String info, notebook;
    private Controller controller;
    private WebView webView;
    private CheckBox cbFavoriteCache;
    private boolean isCacheStored, isPhotoStored;
    private PageState pageState = PageState.INFO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.info_activity);
        controller = Controller.getInstance();

        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        initViews();

        isCacheStored = controller.getDbManager().isCacheStored(geoCache.getId());
        if (isCacheStored) {
            cbFavoriteCache.setChecked(true);
            info = Controller.getInstance().getDbManager().getWebTextById(geoCache.getId());
            notebook = Controller.getInstance().getDbManager().getWebNotebookTextById(geoCache.getId());
        }
        controller.getGoogleAnalyticsManager().trackPageView(GEOCACHE_INFO_ACTIVITY_FOLDER);
    }

    private void initViews() {
        TextView tvName = (TextView) findViewById(R.id.info_text_name);
        TextView tvTypeGeoCache = (TextView) findViewById(R.id.info_GeoCache_type);
        TextView tvStatusGeoCache = (TextView) findViewById(R.id.info_GeoCache_status);
        tvName.setText(geoCache.getName());
        tvTypeGeoCache.setText(controller.getResourceManager().getGeoCacheType(geoCache));
        tvStatusGeoCache.setText(controller.getResourceManager().getGeoCacheStatus(geoCache));
        ImageView image = (ImageView) findViewById(R.id.imageCache);
        image.setImageDrawable(controller.getResourceManager(this).getMarker(geoCache.getType(), geoCache.getStatus()));
        cbFavoriteCache = (CheckBox) findViewById(R.id.info_geocache_add_del);
        webView = (WebView) findViewById(R.id.info_web_brouse);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        loadWebView(pageState);
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return new DownloadNotebookDialog(this, this, geoCache.getId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(PAGE_TYPE, pageState.ordinal());
        outState.putString(TEXT_INFO, info);
        outState.putString(TEXT_NOTEBOOK, notebook);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        pageState = PageState.values()[savedInstanceState.getInt(PAGE_TYPE)];
        info = savedInstanceState.getString(TEXT_INFO);
        notebook = savedInstanceState.getString(TEXT_NOTEBOOK);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.geocache_info_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        isPhotoStored = isPhotoStored(geoCache.getId());
        if (isPhotoStored) {
            menu.getItem(2).setTitle(R.string.menu_show_cache_photos);
            menu.getItem(2).setIcon(R.drawable.ic_menu_gallery);

        } else {
            menu.getItem(2).setTitle(R.string.menu_download_cache_photos);
            menu.getItem(2).setIcon(R.drawable.ic_menu_upload);
        }

        if (pageState == PageState.INFO) {
            menu.getItem(0).setTitle(R.string.menu_show_web_notebook_cache);
            menu.getItem(0).setIcon(R.drawable.ic_menu_notebook);
        } else {
            menu.getItem(0).setTitle(R.string.menu_show_info_cache);
            menu.getItem(0).setIcon(R.drawable.ic_menu_info_details);
        }

        if (isCacheStored) {
            menu.getItem(3).setTitle(R.string.menu_show_web_delete_cache);
            menu.getItem(3).setIcon(R.drawable.ic_menu_off);
        } else {
            menu.getItem(3).setTitle(R.string.menu_show_web_add_cache);
            menu.getItem(3).setIcon(android.R.drawable.ic_menu_save);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.show_web_notebook_cache:
                togglePageType();
                loadWebView(pageState);
                return true;
            case R.id.show_web_add_delete_cache:
                cbFavoriteCache.performClick();
                return true;
            case R.id.show_web_search_cache:
                goToMap();
                return true;
            case R.id.show_geocache_notes:
                startActivity(new Intent(this, CacheNotesActivity.class));
                return true;
            case R.id.show_cache_photos:
                showPhotos();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void togglePageType() {
        switch (pageState) {
            case INFO:
                pageState = PageState.NOTEBOOK;
                break;
            case NOTEBOOK:
                pageState = PageState.INFO;
                break;
        }
    }

    private void loadWebView(PageState pageState) {
        LogManager.d(TAG, "loadWebView PageType " + pageState);

        switch (pageState) {
            case INFO:
                if (info == null) {
                    Controller.getInstance().getApiManager().downloadInfo(this, DownloadInfoState.SHOW_INFO, this, geoCache.getId());
                } else {
                    webView.loadDataWithBaseURL(ApiManager.HTTP_PDA_GEOCACHING_SU, info, "text/html", ApiManager.UTF8_ENCODING, null);
                }
                break;
            case NOTEBOOK:
                if (notebook == null) {
                    Controller.getInstance().getApiManager().downloadInfo(this, DownloadInfoState.SHOW_NOTEBOOK, this, geoCache.getId());
                } else {
                    webView.loadDataWithBaseURL(ApiManager.HTTP_PDA_GEOCACHING_SU, notebook, "text/html", ApiManager.UTF8_ENCODING, null);
                }
                break;
            case NO_INTERNET:
                webView.loadData("<?xml version='1.0' encoding='utf-8'?><center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", ApiManager.UTF8_ENCODING);// TODO
                break;
        }
    }

    public void onFavoritesStarClick(View v) {
        if (cbFavoriteCache.isChecked()) {
            if (notebook == null) {
                if (controller.getPreferencesManager().getDownloadNoteBookAlways()) {
                    {
                        Controller.getInstance().getApiManager().downloadInfo(this, DownloadInfoState.SAVE_CACHE_NOTEBOOK, this, geoCache.getId());
                    }
                } else {
                    showDialog(0);
                }
            }
            saveCache();
        } else {
            deleteCache();
        }
    }

    private void saveCache() {
        if (isCacheStored) {
            Controller.getInstance().getDbManager().updateInfoText(geoCache.getId(), info);
            Controller.getInstance().getDbManager().updateNotebookText(geoCache.getId(), notebook);
        } else {
            isCacheStored = true;
            Controller.getInstance().getDbManager().addGeoCache(geoCache, info, notebook);
        }
    }

    private void deleteCache() {
        isCacheStored = false;
        Controller.getInstance().getDbManager().deleteCacheById(geoCache.getId());
    }

    public void showInfo(String info) {
        this.info = info;
        loadWebView(PageState.INFO);
    }

    public void showNotebook(String notebook) {
        this.notebook = notebook;
        loadWebView(PageState.NOTEBOOK);
    }

    public void showPhotos() {
        isPhotoStored = isPhotoStored(geoCache.getId());
        if (isPhotoStored) {
            NavigationManager.startPhotoGalery(this, geoCache.getId());
        } else {
            Controller.getInstance().getApiManager().downloadPhotos(this, PageState.PHOTO, this, geoCache.getId());
        }
    }

    public void showErrorMessage() {
        loadWebView(PageState.NO_INTERNET);
    }

    public void saveNotebook(String notebook) {
        this.notebook = notebook;
        saveCache();
    }

    public void saveNotebookAndGoToMap(String notebook) {
        saveNotebook(notebook);
        goToMap();
    }

    public void onMapClick(View v) {
        goToMap();
    }

    private void goToMap() {
        if (!isCacheStored) {
            cbFavoriteCache.setChecked(true);
            if ((notebook == null) && controller.getPreferencesManager().getDownloadNoteBookAlways()) {
                Controller.getInstance().getApiManager().downloadInfo(this, DownloadInfoState.SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP, this, geoCache.getId());
                return;
            }
        }
        saveCache();
        NavigationManager.startSearchMapActivity(this, geoCache);
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActvity(this);
    }

    private boolean isPhotoStored(int cacheId) {
        File dir = new File(Environment.getExternalStorageDirectory(), String.format(this.getString(R.string.cache_directory), cacheId));
        String[] imageNames = dir.list();
        if (imageNames != null) {
            return imageNames.length != 0;
        }
        return imageNames != null;
    }
}
