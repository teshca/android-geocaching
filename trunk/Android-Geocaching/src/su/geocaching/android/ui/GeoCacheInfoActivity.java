package su.geocaching.android.ui;

import java.util.concurrent.ExecutionException;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.DownloadPageTask;
import su.geocaching.android.model.datatype.GeoCache;
import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Alekseenko Vladimir
 */
public class GeoCacheInfoActivity extends Activity {

    private static final String TAG = GeoCacheInfoActivity.class.getCanonicalName();
    private static final String GEOCACHE_INFO_ACTIVITY_FOLDER = "/GeoCacheInfoActivity";
    private static final String HTML_ENCODING = "UTF-8";
    private static final String PAGE_TYPE = "page type", SCROOLX = "scrollX", SCROOLY = "scrollY"; 

    public enum PageType {
        INFO, NOTEBOOK
    }

    private WebView webView;
    private CheckBox cbFavoriteCache;
    private Controller controller;
    private DbManager dbManager;
    private GeoCache geoCache;
    private AsyncTask<Void, Void, String> infoTask;
    private AsyncTask<Void, Void, String> notebookTask;
  
    private PageType pageType = PageType.INFO;
    private int webViewScrollY;
    private int webViewScrollX;
    private boolean isCacheStoredInDataBase;

    private boolean goToMap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.info_geocache_activity);
        controller = Controller.getInstance();
        dbManager = controller.getDbManager();
        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        if (savedInstanceState != null) {
            pageType = PageType.values()[savedInstanceState.getInt(PAGE_TYPE, PageType.INFO.ordinal())];
        }
        initViews();

        isCacheStoredInDataBase = dbManager.isCacheStored(geoCache.getId());
        if (isCacheStoredInDataBase) {
            cbFavoriteCache.setChecked(true);
        }
       
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
        webView = (WebView) findViewById(R.id.info_web_brouse);
        cbFavoriteCache = (CheckBox) findViewById(R.id.info_geocache_add_del);

        webView.getSettings().setJavaScriptEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        loadWebView(pageType);

        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        pageType = PageType.values()[savedInstanceState.getInt(PAGE_TYPE)];
        webViewScrollX = savedInstanceState.getInt(SCROOLX, 0);
        webViewScrollY = savedInstanceState.getInt(SCROOLY, 0);
    
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_TYPE, pageType.ordinal());
        outState.putInt(SCROOLX, webView.getScrollX());
        outState.putInt(SCROOLY, webView.getScrollY());
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return new DialogDownloadNotebook(this, infoTask, geoCache);
    }

    private void saveCacheInDB() {
        try {
            isCacheStoredInDataBase = true;
            if (infoTask == null) {
                infoTask = new DownloadPageTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, null, null, PageType.INFO).execute();
            }
            if (notebookTask == null) {
                if (!goToMap && controller.getConnectionManager().isInternetConnected()) {
                    if (!controller.getPreferencesManager().getDownloadNoteBookAlways()) {
                        {
                            showDialog(0);
                        }
                    } else {
                        notebookTask = new DownloadPageTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, null, null, PageType.NOTEBOOK).execute();
                        dbManager.addGeoCache(geoCache, infoTask.get(), notebookTask.get());
                    }
                } else {
                    dbManager.addGeoCache(geoCache, infoTask.get(), "");
                }
            } else {
                dbManager.addGeoCache(geoCache, infoTask.get(), notebookTask.get());
            }
        } catch (InterruptedException e) {
            LogManager.e(TAG, "InterruptedException", e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, "ExecutionException", e);
        }
    }

    private void delCacheFromDB() {
        isCacheStoredInDataBase = false;
        dbManager.deleteCacheById(geoCache.getId());
    }

    public void onAddDelGeoCacheInDatabaseClick(View v) {
        if (!cbFavoriteCache.isChecked()) {
            delCacheFromDB();
        } else {
            saveCacheInDB();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_about_cache, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.show_web_notebook_cache:
                togglePageType();
                return true;
            case R.id.show_web_add_delete_cache:
                cbFavoriteCache.performClick();
                return true;
            case R.id.show_web_search_cache:
                goToMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (pageType == PageType.INFO) {
            menu.getItem(0).setTitle(R.string.menu_show_web_notebook_cache);
            menu.getItem(0).setIcon(R.drawable.ic_menu_notebook);
            if (!isCacheStoredInDataBase && !controller.getConnectionManager().isInternetConnected()) {
                menu.getItem(0).setEnabled(false);
            } else {
                menu.getItem(0).setEnabled(true);
            }

        } else {
            menu.getItem(0).setTitle(R.string.menu_show_info_cache);
            menu.getItem(0).setIcon(R.drawable.ic_menu_info_details);
        }

        if (isCacheStoredInDataBase) {
            menu.getItem(1).setTitle(R.string.menu_show_web_delete_cache);
            menu.getItem(1).setIcon(R.drawable.ic_menu_off);
        } else {
            menu.getItem(1).setTitle(R.string.menu_show_web_add_cache);
            menu.getItem(1).setIcon(android.R.drawable.ic_menu_save);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void togglePageType() {

        switch (pageType) {
            case INFO:
                pageType = PageType.NOTEBOOK;
                break;
            case NOTEBOOK:
                pageType = PageType.INFO;
                break;
        }
        loadWebView(pageType);
    }

    private void loadWebView(PageType type) {
        LogManager.d(TAG, "loadWebView PageType " + type);
        if (isCacheStoredInDataBase || controller.getConnectionManager().isInternetConnected()) {
            try {
                switch (type) {
                    case INFO:
                        if (infoTask == null) {
                            infoTask = new DownloadPageTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, null, PageType.INFO).execute();
                        } else {
                            String infoText = infoTask.get();
                            infoTask = new DownloadPageTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, infoText, PageType.INFO).execute();
                        }
                        break;
                    case NOTEBOOK:

                        if (notebookTask == null) {
                            notebookTask = new DownloadPageTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, null, PageType.NOTEBOOK).execute();
                        } else {
                            String notebookText = notebookTask.get();
                            notebookTask = new DownloadPageTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, notebookText, PageType.NOTEBOOK).execute();
                        }
                        break;
                }
            } catch (InterruptedException e) {
                LogManager.e(TAG, "InterruptedException", e);
            } catch (ExecutionException e) {
                LogManager.e(TAG, "ExecutionException", e);
            }
        } else {
            webView.loadData("<?xml version='1.0' encoding='utf-8'?><center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", HTML_ENCODING);
        }
    }

    public void onMapClick(View v) {
        goToMap();
    }

    private void goToMap() {
        if (!isCacheStoredInDataBase) {
            goToMap = true;
            cbFavoriteCache.setChecked(true);
            saveCacheInDB();
        }
        goToMap = false;
        UiHelper.startSearchMapActivity(this, geoCache);
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }
}