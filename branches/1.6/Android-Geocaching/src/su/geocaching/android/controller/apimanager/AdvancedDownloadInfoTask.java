package su.geocaching.android.controller.apimanager;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.info.InfoViewModel;

/**
 * This AsyncTask for downloading info of geocache
 *
 * @author Nikita Bumakov
 */
public class AdvancedDownloadInfoTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = AdvancedDownloadInfoTask.class.getCanonicalName();

    private InfoViewModel infoViewModel;


    public AdvancedDownloadInfoTask(InfoViewModel infoViewModel) {
        this.infoViewModel = infoViewModel;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String cacheInfo = Controller.getInstance().getApiManager().getInfo(this.infoViewModel.getGeoCachceId());
        if (cacheInfo != null) {
            cacheInfo = CheckpointManager.insertCheckpointsLink(cacheInfo);
        }
        return cacheInfo;
    }

    @Override
    protected void onPostExecute(String result) {
        LogManager.d(TAG, "onPreExecute");

        if (result == null) {
            this.infoViewModel.geocacheInfoDownloadFailed();
            return;
        }

        this.infoViewModel.geocacheInfoDownloaded(result);
    }
}
