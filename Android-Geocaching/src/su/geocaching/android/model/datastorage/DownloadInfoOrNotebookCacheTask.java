package su.geocaching.android.model.datastorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.ui.GeoCacheInfoActivity;
import su.geocaching.android.ui.GeoCacheInfoActivity.PageType;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

public class DownloadInfoOrNotebookCacheTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DownloadInfoOrNotebookCacheTask.class.getCanonicalName();
    public static final String HTML_ENCODING = "UTF-8";
    private static final String LINK_INFO_CACHE = "http://pda.geocaching.su/cache.php?cid=%d&mode=0";
    private static final String LINK_NOTEBOOK_TEXT = "http://pda.geocaching.su/note.php?cid=%d&mode=0";

    private PageType pageType;
    private DbManager dbManager;
    private boolean isCacheStoredInDataBase;
    private ProgressDialog progressDialog;
    private int cacheId;
    private Context context;
    private WebView webView;
    private String downloadedText;
    private int scroolX, scroolY;
    private String[] needStringsSet;

    public DownloadInfoOrNotebookCacheTask(Context context, int cacheId, int scroolX, int scroolY, WebView webView, String downloadedText, PageType pageType) {
        Controller controller = Controller.getInstance();
        dbManager = controller.getDbManager();
        isCacheStoredInDataBase = dbManager.isCacheStored(cacheId);
        this.pageType = pageType;
        this.downloadedText = downloadedText;
        this.cacheId = cacheId;
        this.scroolX = scroolX;
        this.scroolY = scroolY;
        this.context = context;
        this.webView = webView;
        needStringsSet = new String[3];
        needStringsSet = getNeedStringSet();
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");
        if (isCacheStoredInDataBase && downloadedText == null) {
            downloadedText = dataBaseGetWebText(pageType);
        }
        if (!isCacheStoredInDataBase || downloadedText == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(needStringsSet[0]);
            progressDialog.show();
            downloadedText = null;
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = null;
        if (downloadedText == null) {
            if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
                    try {
                        result = getWebText(cacheId);
                    } catch (IOException e) {
                        LogManager.e(TAG, "IOException getWebText", e);
                    }
                if (isCacheStoredInDataBase) {
                    updateTextInDataBase(pageType, result);
                }
            }
            return result == null ? "" : result;
        }
        result = downloadedText;
        return downloadedText;
    }

    private String dataBaseGetWebText(PageType pageType) {
        String res = null;
        switch (pageType) {
            case INFO: {
                res = dbManager.getWebTextById(cacheId);
                break;

            }
            case NOTEBOOK: {
                res = dbManager.getWebNotebookTextById(cacheId);
                break;
            }
        }
        return res;
    }

    private void updateTextInDataBase(PageType pageType, String text) {
        switch (pageType) {
            case INFO: {
                dbManager.updateInfoText(cacheId, text);
                break;
            }
            case NOTEBOOK: {
                dbManager.ubdateNotebookText(cacheId, text);
                break;

            }
        }
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        URL url = null;
        url = new URL(String.format(needStringsSet[1], id));
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "windows-1251"));

        html.append(in.readLine());
        html.append(in.readLine());
        html.append(in.readLine().replace("windows-1251", "utf-8"));

        int size;
        while ((size = in.read(buffer)) != -1) {
            html.append(buffer, 0, size);
        }
        return html.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        LogManager.d(TAG, "TestTime onPreExecute - Stop");
        if (webView != null) {
            if (result.equals("")) {
                webView.loadData("<?xml version='1.0' encoding='utf-8'?><center>" + needStringsSet[2] + "</center>", "text/html", HTML_ENCODING);
            } else {
                webView.loadDataWithBaseURL(GeoCacheInfoActivity.HTTP_PDA_GEOCACHING_SU, result, "text/html", GeoCacheInfoActivity.HTML_ENCODING, null);
            }
            webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    webView.scrollTo(scroolX, scroolY);
                }

            }, 1000);
        }
        if (!isCacheStoredInDataBase || downloadedText == null) {
            progressDialog.dismiss();
        }
        super.onPostExecute(result);
    }

    private String[] getNeedStringSet() {
        String[] needStringsSet = new String[3];
        switch (pageType) {
            case INFO: {
                needStringsSet[0] = context.getString(R.string.download_info);
                needStringsSet[1] = LINK_INFO_CACHE;
                needStringsSet[2] = context.getString(R.string.info_not_save_but_cache_in_DB);
                break;
            }
            case NOTEBOOK: {
                needStringsSet[0] = context.getString(R.string.download_notebook);
                needStringsSet[1] = LINK_NOTEBOOK_TEXT;
                context.getString(R.string.notebook_geocache_not_internet_and_not_in_DB);
                break;
            }
        }
        return needStringsSet;
    }
}
