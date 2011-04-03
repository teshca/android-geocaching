package su.geocaching.android.model.datastorage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.ui.GeoCacheInfoActivity;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.WebView;

public class DownloadWebNotebookTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DownloadWebNotebookTask.class.getCanonicalName();
    public static final String HTML_ENCODING = "UTF-8";

    private DbManager dbManager;
    private boolean isCacheStoredInDataBase;
    private String notebookText;
    private int cacheId;
    private Context context;
    private ProgressDialog progressDialog;
    private WebView webView;

    private int scroolX, scroolY;

    public DownloadWebNotebookTask(Context context, int cacheId, int scroolX, int scroolY, WebView webView, String notebookText) {
        Controller controller = Controller.getInstance();
        dbManager = controller.getDbManager();
        isCacheStoredInDataBase = dbManager.isCacheStored(cacheId);
        this.notebookText = notebookText;
        this.cacheId = cacheId;
        this.scroolX = scroolX;
        this.scroolY = scroolY;
        this.context = context;
        this.webView = webView;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");
        if (!isCacheStoredInDataBase)
        {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.download_notebook));
            progressDialog.show();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = null;
        if (notebookText == null) {

            if (isCacheStoredInDataBase) {
                result = dbManager.getWebNotebookTextById(cacheId);
            }
            if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
                if (result == null || result.equals("")) {
                    try {
                        result = getWebText(cacheId);
                        if (isCacheStoredInDataBase) {
                            dbManager.ubdateNotebookText(cacheId, result);
                        }
                    } catch (IOException e) {
                        LogManager.e(TAG, "IOException getWebText", e);
                    }
                }
            }
            return result == null ? "" : result;
        } else {
            return notebookText;
        }
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        URL url = new URL(String.format("http://pda.geocaching.su/note.php?cid=%d&mode=0", id));
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
                webView.loadData("<?xml version='1.0' encoding='utf-8'?><center>" + context.getString(R.string.notebook_geocache_not_internet_and_not_in_DB) + "</center>", "text/html", HTML_ENCODING);
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
        if (!isCacheStoredInDataBase) {
            progressDialog.dismiss();
        }
        super.onPostExecute(result);
    }
}
