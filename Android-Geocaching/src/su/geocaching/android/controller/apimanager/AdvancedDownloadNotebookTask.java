package su.geocaching.android.controller.apimanager;


import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.CheckpointManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.info.InfoViewModel;

/**
 * This AsyncTask for downloading notebook of geocache
 *
 * @author Nikita Bumakov
 */
public class AdvancedDownloadNotebookTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = AdvancedDownloadNotebookTask.class.getCanonicalName();

    private InfoViewModel infoViewModel;

    public AdvancedDownloadNotebookTask(InfoViewModel infoViewModel) {
        this.infoViewModel = infoViewModel;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");
    }

    @Override
    protected String doInBackground(Void... arg0) {
        String cacheNotebook = Controller.getInstance().getApiManager().getNotebook(this.infoViewModel.getGeoCachceId());
        if (cacheNotebook != null) {
            cacheNotebook = CheckpointManager.insertCheckpointsLink(cacheNotebook);
        }
        return cacheNotebook;
    }

    @Override
    protected void onPostExecute(String result) {
        LogManager.d(TAG, "onPreExecute");

        if (result == null) {
            this.infoViewModel.geocacheNotebookDownloadFailed();
            return;
        }

        this.infoViewModel.geocacheNotebookDownloaded(result);
    }
}
