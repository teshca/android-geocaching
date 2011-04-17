package su.geocaching.android.model.datastorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.ui.GeoCacheInfoActivity.PageType;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

public class DownloadPageTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DownloadPageTask.class.getCanonicalName();
    private static final String LINK_INFO_CACHE = "http://pda.geocaching.su/cache.php?cid=%d&mode=0";
    private static final String LINK_NOTEBOOK_TEXT = "http://pda.geocaching.su/note.php?cid=%d&mode=0";
    private static final String HTTP_PDA_GEOCACHING_SU = "http://pda.geocaching.su/";
    private static final String HTML_ENCODING = "UTF-8";

    private PageType pageType;
    private boolean isCacheStoredInDataBase;
    private ProgressDialog progressDialog;
    private int cacheId;
    private Context context;
    private WebView webView;
    private String downloadedText;
    private int scroolX, scroolY;
    private String messageProgress;
    private String linkPage;
    private String messageWebView;

    public DownloadPageTask(Context context, int cacheId, int scroolX, int scroolY, WebView webView, String downloadedText, PageType pageType) {
        isCacheStoredInDataBase = Controller.getInstance().getDbManager().isCacheStored(cacheId);
        this.pageType = pageType;
        this.downloadedText = downloadedText;
        this.cacheId = cacheId;
        this.scroolX = scroolX;
        this.scroolY = scroolY;
        this.context = context;
        this.webView = webView;
        getNeedStringSet();
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");
        if (isCacheStoredInDataBase && downloadedText == null) {
            downloadedText = dataBaseGetWebText(pageType);
        }
        if (downloadedText == null || downloadedText.equals("")) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(messageProgress);
            progressDialog.show();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = null;
        if (downloadedText == null || downloadedText.equals("")) {
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
                res = Controller.getInstance().getDbManager().getWebTextById(cacheId);
                break;
            }
            case NOTEBOOK: {
                res = Controller.getInstance().getDbManager().getWebNotebookTextById(cacheId);
                break;
            }
        }
        return res;
    }

    private void updateTextInDataBase(PageType pageType, String text) {
        switch (pageType) {
            case INFO: {
                Controller.getInstance().getDbManager().updateInfoText(cacheId, text);
                break;
            }
            case NOTEBOOK: {
                Controller.getInstance().getDbManager().ubdateNotebookText(cacheId, text);
                break;

            }
        }
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        URL url = null;
        url = new URL(String.format(linkPage, id));
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
            if (result == null || result.equals("")) {
                webView.loadData("<?xml version='1.0' encoding='utf-8'?><center>" + messageWebView + "</center>", "text/html", HTML_ENCODING);
            } else {
                webView.loadDataWithBaseURL(HTTP_PDA_GEOCACHING_SU, result, "text/html", HTML_ENCODING, null);
            }
          /*  webView.postDelayed(new Runnable() {
                @Override
                public void run() {
                   // webView.scrollTo(scroolX, scroolY);
                }

            }, 1000);*/
        }
        if (downloadedText == null || downloadedText.equals("")) {
            progressDialog.dismiss();
        }
        super.onPostExecute(result);
    }

    private void getNeedStringSet() {
        switch (pageType) {
            case INFO: {
                messageProgress = context.getString(R.string.download_info);
                linkPage = LINK_INFO_CACHE;
                messageWebView = context.getString(R.string.info_not_save_but_cache_in_DB);
                break;
            }
            case NOTEBOOK: {
                messageProgress = context.getString(R.string.download_notebook);
                linkPage = LINK_NOTEBOOK_TEXT;
                messageWebView = context.getString(R.string.notebook_geocache_not_internet_and_not_in_DB);
                break;
            }
        }
    }
}
