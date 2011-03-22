package su.geocaching.android.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import su.geocaching.android.controller.ConnectionManager;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.IInternetAware;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datastorage.DownloadInfoCacheTask;
import su.geocaching.android.model.datastorage.DownloadWebNotebookTask;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.ui.searchgeocache.SearchGeoCacheMap;

import java.util.concurrent.ExecutionException;

/**
 * @author Alekseenko Vladimir
 */
public class ShowGeoCacheInfo extends Activity implements OnCheckedChangeListener, OnClickListener, IInternetAware {
    private static final String TAG = ShowGeoCacheInfo.class.getCanonicalName();
    private static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
    private WebView webView;
    private TextView tvNameText;
    private TextView tvTypeGeoCacheText;
    private TextView tvStatusGeoCacheText;
    private ImageView btGoToSearchGeoCache;
    private CheckBox cbAddDelCache;
    private DbManager dbm;
    private int webViewScrollPositionY = 0;
    private int webViewScrollPositionX = 0;
    private GeoCache GeoCacheForShowInfo;
    private String htmlTextGeoCache = null;
    private String htmlTextNotebookGeoCache = null;
    private boolean isCacheStoredInDataBase;
    private ConnectionManager connectManager;
    private GoogleAnalyticsTracker tracker;
    private boolean isPageNoteBook;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_geocach_activity);
        dbm = Controller.getInstance().getDbManager();
        connectManager = Controller.getInstance().getConnectionManager();
        webView = (WebView) findViewById(R.id.info_web_brouse);
        btGoToSearchGeoCache = (ImageView) findViewById(R.id.info_geocach_Go_button);
        cbAddDelCache = (CheckBox) findViewById(R.id.info_geocache_add_del);
        tvNameText = (TextView) findViewById(R.id.info_text_name);
        tvTypeGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_type);
        tvStatusGeoCacheText = (TextView) findViewById(R.id.info_GeoCache_status);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.scrollTo(webViewScrollPositionX, webViewScrollPositionY);
                super.onPageFinished(view, url);
            }

        });
        isPageNoteBook = false;
        tracker = GoogleAnalyticsTracker.getInstance();
        tracker.start(getString(R.string.id_Google_Analytics), this);
        tracker.trackPageView(getString(R.string.geocache_info_activity_folder));
        tracker.dispatch();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            isCacheStoredInDataBase = true;
            if (!Controller.getInstance().getPreferencesManager().getDownloadNoteBookAlways()){
                if (htmlTextNotebookGeoCache == null || htmlTextNotebookGeoCache.equals("")) {
                    createDownloadNotebookDialog();
                }
            }
            else{
                htmlTextNotebookGeoCache = getHtmlString(!isPageNoteBook);
            }
            dbm.addGeoCache(GeoCacheForShowInfo, htmlTextGeoCache, htmlTextNotebookGeoCache);
        } else {
            dbm.deleteCacheById(GeoCacheForShowInfo.getId());
        }

    }

    private void createDownloadNotebookDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_in_info_activity);
        dialog.setCancelable(true);
        CheckBox check = (CheckBox) dialog.findViewById(R.id.downloadNoteBookAlways);
        check.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                Controller.getInstance().getPreferencesManager().setDownloadNoteBookAlways(true);
            }
        });
        Button buttonYes = (Button) dialog.findViewById(R.id.ButtonYes);
        buttonYes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (connectManager.isInternetConnected()){
                htmlTextNotebookGeoCache = getHtmlString(!isPageNoteBook);
                }
                dialog.dismiss();
            }
        });
        Button buttonNo = (Button) dialog.findViewById(R.id.ButtonNo);
        buttonNo.setOnClickListener(new OnClickListener() {
  
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    
    @Override
    protected void onResume() {
        if (htmlTextGeoCache.equals("")) {
            webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextGeoCache, "text/html", "utf-8", "");
        } else {
            if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase)
                webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
            else {
                htmlTextGeoCache = getHtmlString(isPageNoteBook);
            }
        }
        connectManager.addSubscriber(this);
        super.onResume();
    }

    @Override
    protected void onStop() {
        connectManager.removeSubscriber(this);
        tracker.stop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        GeoCacheForShowInfo = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());

        isCacheStoredInDataBase = (dbm.getCacheByID(GeoCacheForShowInfo.getId()) != null);
        if (isCacheStoredInDataBase) {
            cbAddDelCache.setChecked(true);
            htmlTextGeoCache = dbm.getWebTextById(GeoCacheForShowInfo.getId());
            htmlTextNotebookGeoCache = dbm.getWebNotebookTextById(GeoCacheForShowInfo.getId());
        }

        tvNameText.setText(GeoCacheForShowInfo.getName());
        tvStatusGeoCacheText.setText(Controller.getInstance().getResourceManager().getGeoCacheStatus(GeoCacheForShowInfo));
        tvTypeGeoCacheText.setText(Controller.getInstance().getResourceManager().getGeoCacheType(GeoCacheForShowInfo));

        cbAddDelCache.setOnCheckedChangeListener(this);
        btGoToSearchGeoCache.setOnClickListener(this);

        if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase)
            webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
        else
            htmlTextGeoCache = getHtmlString(isPageNoteBook);
        webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextGeoCache, "text/html", "utf-8", "");
        super.onStart();
    }

    @Override
    protected void onPause() {
        webViewScrollPositionY = webView.getScrollY();
        webViewScrollPositionX = webView.getScrollX();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        if (!isCacheStoredInDataBase) {
            htmlTextNotebookGeoCache = getHtmlString(!isPageNoteBook);
            cbAddDelCache.setChecked(true);
        }
        Intent intent = new Intent(this, SearchGeoCacheMap.class);
        intent.putExtra(GeoCache.class.getCanonicalName(), GeoCacheForShowInfo);
        startActivity(intent);
    }

    public void onHomeClick(View v) {
        UiHelper.goHome(this);
    }

    @Override
    public void onInternetLost() {
        btGoToSearchGeoCache.setOnClickListener(null);
        Toast.makeText(getBaseContext(), getString(R.string.select_geocache_status_without_internet), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInternetFound() {
        btGoToSearchGeoCache.setOnClickListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.info_about_cache, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("htmlGeoCache", htmlTextGeoCache);
        outState.putInt("scrollY", webView.getScrollY());
        outState.putInt("scrollX", webView.getScrollX());
        super.onSaveInstanceState(outState); // To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        htmlTextGeoCache = savedInstanceState.getString("htmlGeoCache");
        webViewScrollPositionY = savedInstanceState.getInt("scrollY");
        webViewScrollPositionX = savedInstanceState.getInt("scrollX");
        webView.scrollTo(webViewScrollPositionX, webViewScrollPositionY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_web_notebook_cache: {
                changeMenuItem(item);
                return true;
            }
            case R.id.show_web_add_delete_cache: {

                if (cbAddDelCache.isChecked()) {
                    cbAddDelCache.setChecked(false);
                } else {
                    cbAddDelCache.setChecked(true);
                }
                return true;
            }
            case R.id.show_web_search_cache: {
                if (!isCacheStoredInDataBase) {
                    cbAddDelCache.setChecked(true);
                }
                Intent intent = new Intent(this, SearchGeoCacheMap.class);
                intent.putExtra(GeoCache.class.getCanonicalName(), GeoCacheForShowInfo);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {

        if (cbAddDelCache.isChecked()) {

            menu.getItem(1).setTitle(R.string.menu_show_web_delete_cache);
        } else {
            menu.getItem(1).setTitle(R.string.menu_show_web_add_cache);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void changeMenuItem(MenuItem item) {
        if (!connectManager.isInternetConnected() && !isCacheStoredInDataBase) {
            webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.info_geocach_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
        } else {

            if (!isPageNoteBook) {
                item.setTitle(R.string.menu_show_info_cache);
                isPageNoteBook = true;
                htmlTextNotebookGeoCache = getHtmlString(isPageNoteBook);
                if (htmlTextNotebookGeoCache == null)
                    webView.loadData("<?xml version='1.0' encoding='utf-8'?>" + "<center>" + getString(R.string.notebook_geocache_not_internet_and_not_in_DB) + "</center>", "text/html", "utf-8");
                else
                    webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextNotebookGeoCache, "text/html", "utf-8", "");
      
            } else {
                isPageNoteBook = false;
                item.setTitle(R.string.menu_show_web_notebook_cache);
                htmlTextGeoCache = getHtmlString(isPageNoteBook);
                webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, htmlTextGeoCache, "text/html", "utf-8", "");
            }

        }
    }

    private String getHtmlString(boolean isPageNoteBook) {
        String exString = "";
        if (!isPageNoteBook)
            try {
                exString = new DownloadInfoCacheTask(dbm, isCacheStoredInDataBase, GeoCacheForShowInfo.getId(), this).execute(htmlTextGeoCache).get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        else {
            try {
                exString = new DownloadWebNotebookTask(dbm, isCacheStoredInDataBase, GeoCacheForShowInfo.getId(), this).execute(htmlTextNotebookGeoCache).get();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ExecutionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return exString;
    }
}