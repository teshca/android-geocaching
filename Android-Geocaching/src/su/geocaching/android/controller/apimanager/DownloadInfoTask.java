package su.geocaching.android.controller.apimanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.InfoActivity2;
import su.geocaching.android.ui.InfoActivity2.PageState;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class DownloadInfoTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DownloadPageTask.class.getCanonicalName();

    private int cacheId;
    private PageState pageType;
    private ProgressDialog progressDialog;
    private Context context;
    private InfoActivity2 infoActibity;

    public DownloadInfoTask(Context context, int cacheId, InfoActivity2 infoActibity, PageState pageType) {
        this.pageType = pageType;
        this.cacheId = cacheId;
        this.context = context;
        this.infoActibity = infoActibity;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.downloading));// TODO MessageProgress
        progressDialog.show();
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String result = null;

        if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
            try {
                result = getWebText(cacheId);
            } catch (IOException e) {
                LogManager.e(TAG, "IOException getWebText", e);
            }
        } else {
            pageType = PageState.NO_INTERNET;
        }
        return result == null ? "" : result;
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];

        String formatLink;
        switch (pageType) {
            case INFO:
                formatLink = ApiManager.LINK_INFO_CACHE;
                break;
            case NOTEBOOK:
                formatLink = ApiManager.LINK_NOTEBOOK_TEXT;
                break;
            default:
                formatLink = ApiManager.LINK_INFO_CACHE;
                break;
        }

        URL url = new URL(String.format(formatLink, id));
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), ApiManager.CP1251_ENCODING));

        html.append(in.readLine());
        html.append(in.readLine());
        String endLine = in.readLine();
        if (endLine != null) {
            html.append(in.readLine().replace(ApiManager.CP1251_ENCODING, ApiManager.UTF8_ENCODING));
        }
        // TODO!!
        /*
         * 07-04 08:58:20.968: ERROR/AndroidRuntime(595): Caused by: java.lang.NullPointerException 07-04 08:58:20.968: ERROR/AndroidRuntime(595): at
         * su.geocaching.android.controller.apimanager.DownloadInfoTask.getWebText(DownloadInfoTask.java:81) 07-04 08:58:20.968: ERROR/AndroidRuntime(595): at
         * su.geocaching.android.controller.apimanager.DownloadInfoTask.doInBackground(DownloadInfoTask.java:49) 07-04 08:58:20.968: ERROR/AndroidRuntime(595): at
         * su.geocaching.android.controller.apimanager.DownloadInfoTask.doInBackground(DownloadInfoTask.java:1) 07-04 08:58:20.968: ERROR/AndroidRuntime(595): at
         * android.os.AsyncTask$2.call(AsyncTask.java:185) 07-04 08:58:20.968: ERROR/AndroidRuntime(595): at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:256)
         */

        int size;
        while ((size = in.read(buffer)) != -1) {
            html.append(buffer, 0, size);
        }
        return html.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        LogManager.d(TAG, "TestTime onPreExecute - Stop");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        infoActibity.setInfo(pageType, result);
        super.onPostExecute(result);
    }
}
