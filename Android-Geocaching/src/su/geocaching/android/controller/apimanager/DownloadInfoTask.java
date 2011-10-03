package su.geocaching.android.controller.apimanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.info.InfoActivity;

/**
 * This AsyncTask for downloading info/notebook of geocache
 *
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
    private InfoActivity infoActivity;
    private URL downloadUrl;

    public DownloadInfoTask(Context context, int cacheId, InfoActivity infoActivity, DownloadInfoState state) {
        this.state = state;
        this.cacheId = cacheId;
        this.context = context;
        this.infoActivity = infoActivity;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");

        String progressMessage = "";
        try {
            switch (state) {
                case SHOW_INFO:
                    progressMessage = context.getString(R.string.download_info);
                    downloadUrl = new URL(String.format(GeocachingSuApiManager.LINK_INFO_CACHE, cacheId));
                    break;
                case SHOW_NOTEBOOK:
                case SAVE_CACHE_NOTEBOOK:
                case SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP:
                    progressMessage = context.getString(R.string.download_notebook);
                    downloadUrl = new URL(String.format(GeocachingSuApiManager.LINK_NOTEBOOK_TEXT, cacheId));
                    break;
                case DOWNLOAD_PHOTO_PAGE:
                    downloadUrl = new URL(String.format(GeocachingSuApiManager.LINK_PHOTO_PAGE, cacheId));
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

        if (Controller.getInstance().getConnectionManager().isActiveNetworkConnected()) {
            boolean success = false;
            for (int attempt = 0; attempt < 5 && !success; attempt++)
                try {
                    result = getWebText();
                    success = true;
                } catch (IOException e) {
                    // result is null in this case
                    LogManager.e(TAG, "IOException getWebText", e);
                }
        } else {
          if (state != DownloadInfoState.SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP) {
              state = DownloadInfoState.ERROR;
          }
        }
        return result;
    }

    private String getWebText() throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        BufferedReader in = new BufferedReader(new InputStreamReader(downloadUrl.openStream(), GeocachingSuApiManager.CP1251_ENCODING));

        int size;
        while ((size = in.read(buffer)) != -1) {
            html.append(buffer, 0, size);
        }

        String resultHtml = html.toString();
        resultHtml = resultHtml.replace(GeocachingSuApiManager.CP1251_ENCODING, GeocachingSuApiManager.UTF8_ENCODING);
        resultHtml = resultHtml.replaceAll("\\r|\\n", "");
        return resultHtml;
    }

    @Override
    protected void onPostExecute(String result) {
        LogManager.d(TAG, "onPreExecute");

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (result == null) {
            LogManager.e(TAG, "The result is null");
            state = DownloadInfoState.ERROR;
        }

        switch (state) {
            case SHOW_INFO:
                result = CheckpointManager.insertCheckpointsLink(result);
                infoActivity.showInfo(result);
                break;
            case SHOW_NOTEBOOK:
                infoActivity.showNotebook(result);
                break;
            case SAVE_CACHE_NOTEBOOK:
                infoActivity.saveNotebook(result);
                break;
            case SAVE_CACHE_NOTEBOOK_AND_GO_TO_MAP:
                infoActivity.saveNotebookAndGoToMap(result);
                break;
            case ERROR:
                infoActivity.showErrorMessage(R.string.info_geocach_not_internet_and_not_in_DB);
                break;
            default:
                break;
        }
    }
}
