package su.geocaching.android.ui.info;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Picture;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import su.geocaching.android.controller.apimanager.DownloadInfoTask.DownloadInfoState;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.GeocacheInfo;
import su.geocaching.android.ui.R;


/**
 * Activity to display cache's information, notebook and photos
 *
 * @author Nikita Bumakov
 */
public class InfoActivity extends Activity {

    private static final String TAG = InfoActivity.class.getCanonicalName();
    private static final String GEOCACHE_INFO_ACTIVITY_FOLDER = "/GeoCacheInfoActivity";
    private static final String PAGE_TYPE = "page type", SCROLLY = "scrollY", WIDTH = "width", ZOOM = "ZOOM", TEXT_INFO = "info", TEXT_NOTEBOOK = "notebook";

    private enum PageState {
        INFO, NOTEBOOK, PHOTO, ERROR
    }

    private GeoCache geoCache;
    private String info, notebook;
    private Controller controller;
    private WebView webView;
    private CheckBox cbFavoriteCache;
    private GalleryView galleryView;
    private GalleryImageAdapter galleryAdapter;
    private Context context;
    private boolean isCacheStored, isPhotoStored;
    private PageState pageState = PageState.INFO;
    private ImageView ivInfo, ivNotebook, ivPhoto;

    private int scroll = 0;
    private String errorMessage;
    private int lastWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogManager.d(TAG, "onCreate");
        setContentView(R.layout.info_activity);
        controller = Controller.getInstance();
        context = this;
        geoCache = getIntent().getParcelableExtra(GeoCache.class.getCanonicalName());
        initViews();
        GeocacheInfo info = controller.getPreferencesManager().getLastGeocacheInfo();
        if (info.getCacheId() == geoCache.getId()) {
            pageState = PageState.values()[info.getPageState()];
            scroll = info.getScroll();
            webView.setInitialScale((int) (info.getScale() * 100));
            lastWidth = info.getWidth();
        }
        controller.getGoogleAnalyticsManager().trackPageView(GEOCACHE_INFO_ACTIVITY_FOLDER);
    }

    private void initViews() {
        TextView tvName = (TextView) findViewById(R.id.info_text_name);
        TextView tvTypeGeoCache = (TextView) findViewById(R.id.info_GeoCache_type);
        TextView tvStatusGeoCache = (TextView) findViewById(R.id.info_GeoCache_status);
        ImageView image = (ImageView) findViewById(R.id.imageCache);
        ivInfo = (ImageView) findViewById(R.id.ivInfo);
        ivNotebook = (ImageView) findViewById(R.id.ivNotebook);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);

        tvName.setText(geoCache.getName());
        tvTypeGeoCache.setText(controller.getResourceManager().getGeoCacheType(geoCache));
        tvStatusGeoCache.setText(controller.getResourceManager().getGeoCacheStatus(geoCache));
        image.setImageDrawable(controller.getResourceManager(this).getMarker(geoCache.getType(), geoCache.getStatus()));

        cbFavoriteCache = (CheckBox) findViewById(R.id.info_geocache_add_del);
        galleryView = (GalleryView) findViewById(R.id.galleryView);

        webView = (WebView) findViewById(R.id.info_web_brouse);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setPictureListener(new PictureListener() {

            @Override
            public void onNewPicture(WebView view, Picture picture) {
                if (scroll != 0) {
                    int w = view.getWidth();
                    scroll = scroll * lastWidth / w;
                    view.scrollTo(0, scroll);
                    scroll = 0;
                }
            }
        });

        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                String urlNotebook = String.format(GeocachingSuApiManager.LINK_NOTEBOOK_TEXT, geoCache.getId());
                String urlInfo = String.format(GeocachingSuApiManager.LINK_INFO_CACHE, geoCache.getId());
                String urlPhoto = String.format(GeocachingSuApiManager.LINK_PHOTO_PAGE, geoCache.getId());

                if (urlInfo.contains(url)) {
                    loadView(PageState.INFO);
                    return true;
                }

                if (urlNotebook.contains(url)) {
                    loadView(PageState.NOTEBOOK);
                    return true;
                }

                if (urlPhoto.contains(url)) {
                    loadView(PageState.PHOTO);
                    return true;
                }
                if (url.contains("geo:")) {
                    if (!isCacheStored) {
                        Toast.makeText(context, R.string.ask_add_cache_in_db, Toast.LENGTH_LONG).show();
                        return true;
                    }

                    String[] coordinates = url.split("[^0-9]++");
                    int lat = Integer.parseInt(coordinates[1]);
                    int lng = Integer.parseInt(coordinates[2]);
                    GeoCache checkpoint = new GeoCache();
                    checkpoint.setId(geoCache.getId());
                    checkpoint.setType(GeoCacheType.CHECKPOINT);
                    checkpoint.setLocationGeoPoint(new GeoPoint(lat, lng));
                    NavigationManager.startCreateCheckpointActivity(context, checkpoint);
                    return true;
                }

                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                shouldOverrideUrlLoading(view, url);
            }
        };

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(webViewClient);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        context = this;
        isCacheStored = controller.getDbManager().isCacheStored(geoCache.getId());
        if (isCacheStored) {
            cbFavoriteCache.setChecked(true);
            info = controller.getDbManager().getCacheInfoById(geoCache.getId());
            notebook = controller.getDbManager().getCacheNotebookTextById(geoCache.getId());
        }

        loadView(pageState);
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        controller.getPreferencesManager().setLastGeocacheInfo(new GeocacheInfo(geoCache.getId(), webView.getScrollY(), pageState.ordinal(), webView.getWidth(), webView.getScale()));
        super.onPause();
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
        outState.putInt(SCROLLY, webView.getScrollY());
        outState.putFloat(ZOOM, webView.getScale());
        outState.putInt(WIDTH, webView.getWidth());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        pageState = PageState.values()[savedInstanceState.getInt(PAGE_TYPE)];
        info = savedInstanceState.getString(TEXT_INFO);
        notebook = savedInstanceState.getString(TEXT_NOTEBOOK);
        scroll = savedInstanceState.getInt(SCROLLY);
        webView.setInitialScale((int) (savedInstanceState.getFloat(ZOOM) * 100));
        lastWidth = savedInstanceState.getInt(WIDTH);
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
        if (pageState == PageState.PHOTO) {
            menu.getItem(3).setTitle(R.string.menu_delete_photos_cache);
            menu.getItem(3).setIcon(R.drawable.ic_menu_delete);

        } else {
            menu.getItem(3).setTitle(R.string.menu_show_cache_photos);
            menu.getItem(3).setIcon(R.drawable.ic_menu_gallery);
        }

        if (pageState == PageState.INFO) {
            menu.getItem(2).setTitle(R.string.menu_show_web_notebook_cache);
            menu.getItem(2).setIcon(R.drawable.ic_menu_notebook);
        } else {
            menu.getItem(2).setTitle(R.string.menu_show_info_cache);
            menu.getItem(2).setIcon(R.drawable.ic_menu_info_details);
        }

        if (isCacheStored) {
            menu.getItem(4).setTitle(R.string.menu_show_web_delete_cache);
            menu.getItem(4).setIcon(R.drawable.ic_menu_off);
            menu.getItem(5).setEnabled(true);
        } else {
            menu.getItem(4).setTitle(R.string.menu_show_web_add_cache);
            menu.getItem(4).setIcon(android.R.drawable.ic_menu_save);
            menu.getItem(5).setEnabled(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.show_web_notebook_cache:
                if (pageState == PageState.INFO) {
                    loadView(PageState.NOTEBOOK);
                } else {
                    loadView(PageState.INFO);
                }
                return true;
            case R.id.show_web_add_delete_cache:
                cbFavoriteCache.performClick();
                return true;
            case R.id.show_web_search_cache:
                goToMap();
                return true;
            case R.id.show_geocache_notes:
                NavigationManager.startNotesActivity(this, geoCache.getId());
                return true;
            case R.id.show_cache_photos:
                if (pageState == PageState.PHOTO) {
                    controller.getDbManager().deletePhotos(geoCache.getId());
                    galleryAdapter = null;
                    galleryView.setAdapter(null);
                    loadView(PageState.INFO);
                } else {
                    loadView(PageState.PHOTO);
                }
                return true;
            case R.id.refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setPageType(PageState state) {
        switch (state) {
            case INFO:
                galleryView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                ivInfo.setImageResource(R.drawable.ic_info_selected);
                ivNotebook.setImageResource(R.drawable.ic_notebook_default);
                ivPhoto.setImageResource(R.drawable.ic_gallery_default);
                break;
            case NOTEBOOK:
                galleryView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                ivInfo.setImageResource(R.drawable.ic_info_default);
                ivNotebook.setImageResource(R.drawable.ic_notebook_selected);
                ivPhoto.setImageResource(R.drawable.ic_gallery_default);
                break;
            case ERROR:
                galleryView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                break;
            case PHOTO:
                ivInfo.setImageResource(R.drawable.ic_info_default);
                ivNotebook.setImageResource(R.drawable.ic_notebook_default);
                ivPhoto.setImageResource(R.drawable.ic_gallery_selected);
                isPhotoStored = isPhotoStored(geoCache.getId());
                webView.setVisibility(View.GONE);
                galleryView.setVisibility(View.VISIBLE);
                break;
        }
        pageState = state;
    }

    private boolean refresh = false;

    private void refresh() {
        switch (pageState) {
            case INFO:
                info = null;
                break;
            case NOTEBOOK:
                notebook = null;
                break;
            case ERROR:
                pageState = PageState.INFO;
                break;
            case PHOTO:
                controller.getDbManager().deleteCacheById(geoCache.getId());
                break;
        }
        refresh = true;
        loadView(pageState);
    }

    private void loadView(PageState pageState) {
        LogManager.d(TAG, "loadWebView PageType " + pageState);

        setPageType(pageState);
        switch (pageState) {
            case INFO:
                if (info == null) {
                    controller.getApiManager().getInfo(this, DownloadInfoState.SHOW_INFO, this, geoCache.getId());
                } else {
                    webView.loadDataWithBaseURL(GeocachingSuApiManager.HTTP_PDA_GEOCACHING_SU, info, "text/html", GeocachingSuApiManager.UTF8_ENCODING, null);
                }
                break;
            case NOTEBOOK:
                if (notebook == null) {
                    controller.getApiManager().getInfo(this, DownloadInfoState.SHOW_NOTEBOOK, this, geoCache.getId());
                } else {
                    webView.loadDataWithBaseURL(GeocachingSuApiManager.HTTP_PDA_GEOCACHING_SU, notebook, "text/html", GeocachingSuApiManager.UTF8_ENCODING, null);
                }
                break;
            case PHOTO:
                isPhotoStored = isPhotoStored(geoCache.getId());
                if (isPhotoStored) {
                    if (galleryAdapter == null) {
                        galleryAdapter = new GalleryImageAdapter(this, geoCache.getId());
                        galleryView.setAdapter(galleryAdapter);
                    } else {
                        galleryAdapter.notifyDataSetChanged();
                    }
                } else if (refresh) {
                    controller.getApiManager().getPhotos(context, InfoActivity.this, InfoActivity.this.geoCache.getId());
                    refresh = false;
                } else {
                    askSavePicture();
                }
                break;
            case ERROR:
                webView.loadData("<?xml version='1.0' encoding='utf-8'?><center>" + errorMessage + "</center>", "text/html", GeocachingSuApiManager.UTF8_ENCODING);// TODO
                break;
        }
    }

    public void onFavoritesStarClick(View v) {
        if (cbFavoriteCache.isChecked()) {
            if (notebook == null) {
                if (controller.getPreferencesManager().getDownloadNoteBookAlways()) {
                    {
                        controller.getApiManager().getInfo(this, DownloadInfoState.SAVE_CACHE_NOTEBOOK, this, geoCache.getId());
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
            controller.getDbManager().updateInfoText(geoCache.getId(), info);
            controller.getDbManager().updateNotebookText(geoCache.getId(), notebook);
        } else {
            isCacheStored = true;
            controller.getDbManager().addGeoCache(geoCache, info, notebook);
        }
    }

    private void deleteCache() {
        isCacheStored = false;
        controller.getCheckpointManager(geoCache.getId()).clear();
        controller.getDbManager().deleteCacheById(geoCache.getId());
        if (galleryAdapter != null) {
            galleryAdapter.notifyDataSetChanged();
        }
    }

    public void showInfo(String info) {
        this.info = info;
         if (isCacheStored) {
            saveCache();   //refresh
        }
        loadView(PageState.INFO);
    }

    public void showNotebook(String notebook) {
        this.notebook = notebook;
        if (isCacheStored) {
            saveCache();   //refresh
        }
        loadView(PageState.NOTEBOOK);
    }

    public void showPhoto() {
        loadView(PageState.PHOTO);
    }

    public void showErrorMessage(int stringId) {
        pageState = PageState.ERROR;
        errorMessage = getString(stringId);
        loadView(pageState);
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
        isCacheStored = controller.getDbManager().isCacheStored(geoCache.getId());
        if (!isCacheStored) {
            cbFavoriteCache.setChecked(true);
            if ((notebook == null) && controller.getPreferencesManager().getDownloadNoteBookAlways()) {
                controller.getApiManager().getInfo(this, DownloadInfoState.SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP, this, geoCache.getId());
                return;
            }
        }
        saveCache();
        NavigationManager.startSearchMapActivity(this, geoCache);
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActvity(this);
    }

    public void onInfoClick(View v) {
        loadView(PageState.INFO);
    }

    public void onNotebookClick(View v) {
        loadView(PageState.NOTEBOOK);
    }

    public void onPhotoClick(View v) {
        loadView(PageState.PHOTO);
    }

    private boolean isPhotoStored(int cacheId) {
        File dir = new File(Environment.getExternalStorageDirectory(), String.format(this.getString(R.string.cache_directory), cacheId));
        String[] imageNames = dir.list();
        return (imageNames != null) && (imageNames.length != 0);
    }

    private void askSavePicture() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.ask_download_photos)).setPositiveButton(context.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                controller.getApiManager().getPhotos(context, InfoActivity.this, InfoActivity.this.geoCache.getId());
            }
        }).setNegativeButton(context.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}