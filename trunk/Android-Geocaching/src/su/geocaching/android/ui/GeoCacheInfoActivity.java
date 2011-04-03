package su.geocaching.android.ui;

import java.util.concurrent.ExecutionException;

import su.geocaching.android.controller.ConnectionManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.DownloadInfoCacheTask;
import su.geocaching.android.model.datastorage.DownloadWebNotebookTask;
import su.geocaching.android.model.datatype.GeoCache;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * @author Alekseenko Vladimir
 */
public class GeoCacheInfoActivity extends Activity implements OnCheckedChangeListener {

    private static final String TAG = GeoCacheInfoActivity.class.getCanonicalName();
    public static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
    public static final String HTML_ENCODING = "UTF-8";
    public static final String PAGE_TYPE = "page type", SCROOLX = "scrollX", SCROOLY = "scrollY";

    private enum PageType {
        INFO, NOTEBOOK
    }

    private WebView webView;
    private CheckBox cbFavoriteCache;
    private Controller controller;
    private DbManager dbManager;
    private ConnectionManager connectManager;
    private GoogleAnalyticsTracker tracker;
    private Context context;
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
        connectManager = controller.getConnectionManager();
        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        context = this;
        if (savedInstanceState != null) {
            pageType = PageType.values()[savedInstanceState.getInt(PAGE_TYPE, PageType.INFO.ordinal())];
        }
        initViews();

        isCacheStoredInDataBase = dbManager.isCacheStored(geoCache.getId());
        if (isCacheStoredInDataBase) {
            cbFavoriteCache.setChecked(true);
        }

        cbFavoriteCache.setOnCheckedChangeListener(this);

        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.geocache_info_activity_folder));
        tracker.dispatch();
    }

    private void initViews() {
        TextView tvName = (TextView) findViewById(R.id.info_text_name);
        TextView tvTypeGeoCache = (TextView) findViewById(R.id.info_GeoCache_type);
        TextView tvStatusGeoCache = (TextView) findViewById(R.id.info_GeoCache_status);
        tvName.setText(geoCache.getName());
        tvTypeGeoCache.setText(controller.getResourceManager().getGeoCacheType(geoCache));
        tvStatusGeoCache.setText(controller.getResourceManager().getGeoCacheStatus(geoCache));
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
        tracker.stop();
        super.onStop();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return new DialogDownloadNotebook(this, infoTask, geoCache);
    }

    // TODO download notebook only when necessary
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            if (isChecked) {
                isCacheStoredInDataBase = true;
                if (infoTask == null) {
                    infoTask = new DownloadInfoCacheTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, null, null).execute();
                }
                
                if (notebookTask == null) {
                    if (!goToMap && connectManager.isInternetConnected()) {
                        if (!controller.getPreferencesManager().getDownloadNoteBookAlways()) {
                            {
                                showDialog(0);
                            }
                        } else {
                            notebookTask = new DownloadWebNotebookTask(context, geoCache.getId(), webViewScrollX, webViewScrollY, null, null).execute();
                            dbManager.addGeoCache(geoCache, infoTask.get(), notebookTask.get());

                        }
                    } else {
                        dbManager.addGeoCache(geoCache, infoTask.get(), "");
                    }
                } else {
                    dbManager.addGeoCache(geoCache, infoTask.get(), notebookTask.get());
                }

            } else {
                isCacheStoredInDataBase = false;
                dbManager.deleteCacheById(geoCache.getId());
            }
        } catch (InterruptedException e) {
            LogManager.e(TAG, "InterruptedException", e);
        } catch (ExecutionException e) {
            LogManager.e(TAG, "ExecutionException", e);
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
                changePageType();
                return true;
            case R.id.show_web_add_delete_cache:
                cbFavoriteCache.setChecked(!isCacheStoredInDataBase);
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

    private void changePageType() {

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
        if (isCacheStoredInDataBase || connectManager.isInternetConnected()) {
            try {
                switch (type) {
                    case INFO:
                        if (infoTask == null) {
                            infoTask = new DownloadInfoCacheTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, null).execute();
                        } else {
                            String infoText = infoTask.get();
                            infoTask = new DownloadInfoCacheTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, infoText).execute();
                        }
                        break;
                    case NOTEBOOK:

                        if (notebookTask == null) {
                            notebookTask = new DownloadWebNotebookTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, null).execute();
                        } else {
                            String notebookText = notebookTask.get();
                            notebookTask = new DownloadWebNotebookTask(this, geoCache.getId(), webViewScrollX, webViewScrollY, webView, notebookText).execute();
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
        }
        goToMap = false;
        UiHelper.startSearchMapActivity(this, geoCache);
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }
}