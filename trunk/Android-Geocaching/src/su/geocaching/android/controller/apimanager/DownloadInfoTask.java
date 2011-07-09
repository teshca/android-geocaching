package su.geocaching.android.controller.apimanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.InfoActivity;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * @author Nikita Bumakov
 */
public class DownloadInfoTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = DownloadInfoTask.class.getCanonicalName();

    public enum DownloadInfoState {
        ERROR, SHOW_INFO, SHOW_NOTEBOOK, SAVE_CACHE_NOTEBOOK, SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP, DOWNLOAD_PHOTO_PAGE
    }

    private int cacheId;
    private DownloadInfoState state;
    private Context context;
    private ProgressDialog progressDialog;
    private InfoActivity infoActibity;
    private URL downloadUrl;

    public DownloadInfoTask(Context context, int cacheId, InfoActivity infoActibity, DownloadInfoState state) {
        this.state = state;
        this.cacheId = cacheId;
        this.context = context;
        this.infoActibity = infoActibity;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");

        String progressMessage = "";
        try {
            switch (state) {
                case SHOW_INFO:
                    progressMessage = context.getString(R.string.download_info);
                    downloadUrl = new URL(String.format(ApiManager.LINK_INFO_CACHE, cacheId));
                    break;
                case SHOW_NOTEBOOK:
                case SAVE_CACHE_NOTEBOOK:
                case SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP:
                    progressMessage = context.getString(R.string.download_notebook);
                    downloadUrl = new URL(String.format(ApiManager.LINK_NOTEBOOK_TEXT, cacheId));
                    break;
                case DOWNLOAD_PHOTO_PAGE:
                    downloadUrl = new URL(String.format(ApiManager.LINK_PHOTO_PAGE, cacheId));
                    break;
            }
        } catch (IOException e) {
            LogManager.e(TAG, "IOException getWebText", e);
        }

        if (context != null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(progressMessage);
            progressDialog.show();
        }
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
            state = DownloadInfoState.ERROR;
        }
        return result;
    }

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader in = new BufferedReader(new InputStreamReader(downloadUrl.openStream(), ApiManager.CP1251_ENCODING));

        int size;
        while ((size = in.read(buffer)) != -1) {
            html.append(buffer, 0, size);
        }
        return html.toString().replace(ApiManager.CP1251_ENCODING, ApiManager.UTF8_ENCODING);
    }

    @Override
    protected void onPostExecute(String result) {
        LogManager.d(TAG, "TestTime onPreExecute - Stop");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        switch (state) {
            case SHOW_INFO:
                infoActibity.showInfo(result);
                break;
            case SHOW_NOTEBOOK:
                infoActibity.showNotebook(result);
                break;
            case SAVE_CACHE_NOTEBOOK:
                infoActibity.saveNotebook(result);
                break;
            case SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP:
                infoActibity.saveNotebookAndGoToMap(result);
                break;
            case ERROR:
                infoActibity.showErrorMessage(R.string.info_geocach_not_internet_and_not_in_DB);
                break;
            default:
                break;
        }

        super.onPostExecute(result);
    }
}
