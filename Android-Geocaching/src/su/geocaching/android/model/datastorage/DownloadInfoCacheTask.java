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

public class DownloadInfoCacheTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DownloadInfoCacheTask.class.getCanonicalName();

    private DbManager dbManager;
    private boolean isCacheStoredInDataBase;
    private ProgressDialog progressDialog;
    private int cacheId;
    private Context context;
    private WebView webView;
    private String infoText;
    private int scroolX, scroolY;

    public DownloadInfoCacheTask(Context context, int cacheId, int scroolX, int scroolY, WebView webView, String infoText) {
        Controller controller = Controller.getInstance();
        dbManager = controller.getDbManager();
        isCacheStoredInDataBase = dbManager.isCacheStored(cacheId);

        this.infoText = infoText;
        this.cacheId = cacheId;
        this.scroolX = scroolX;
        this.scroolY = scroolY;
        this.context = context;
        this.webView = webView;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");
        if (!isCacheStoredInDataBase) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.download_info));
            progressDialog.show();
        }
    }

    @Override
    protected String doInBackground(Void... params) {
        String result = "";
        if (infoText == null) {
            if (isCacheStoredInDataBase) {
                result = dbManager.getWebTextById(cacheId);
            }
            if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
                if (result.equals("")) {
                    try {
                        result = getWebText(cacheId);
                    } catch (IOException e) {
                        LogManager.e(TAG, "IOException getWebText", e);
                    }
                }
                if (isCacheStoredInDataBase) {
                    dbManager.updateInfoText(cacheId, result);
                }
            }
            return result;
        }
        return infoText;
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];

        URL url = new URL(String.format("http://pda.geocaching.su/cache.php?cid=%d&mode=0", id));
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
            webView.loadDataWithBaseURL(GeoCacheInfoActivity.HTTP_PDA_GEOCACHING_SU, result, "text/html", GeoCacheInfoActivity.HTML_ENCODING, null);
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
