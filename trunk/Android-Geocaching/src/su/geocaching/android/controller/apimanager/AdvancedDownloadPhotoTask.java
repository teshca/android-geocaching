package su.geocaching.android.controller.apimanager;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.info.GeoCachePhotoViewModel;

/**
 * This AsyncTask for downloading notebook of geocache
 *
 * @author Nikita Bumakov
 */
public class AdvancedDownloadPhotoTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = AdvancedDownloadPhotoTask.class.getCanonicalName();

    private GeoCachePhotoViewModel cachePhotoViewModel;

    public AdvancedDownloadPhotoTask(GeoCachePhotoViewModel cachePhotoViewModel) {
        this.cachePhotoViewModel = cachePhotoViewModel;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return Controller.getInstance().getApiManager().downloadPhoto(this.cachePhotoViewModel.getGeoCacheId(), this.cachePhotoViewModel.getRemoteUrl());
    }

    @Override
    protected void onPostExecute(Boolean success) {
        LogManager.d(TAG, "onPostExecute");

        if (!success) {
            this.cachePhotoViewModel.geocachePhotoDownloadFailed();
            return;
        }

        this.cachePhotoViewModel.geocachePhotoDownloaded();
    }
}
