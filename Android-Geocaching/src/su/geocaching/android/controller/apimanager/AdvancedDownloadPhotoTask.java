package su.geocaching.android.controller.apimanager;


import java.net.URL;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.info.GeoCachePhotoViewModel;
import su.geocaching.android.ui.info.InfoViewModel;

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
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return Controller.getInstance().getApiManager().downloadPhoto(this.cachePhotoViewModel.getGeoCachceId(), this.cachePhotoViewModel.getRemoteUrl());
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