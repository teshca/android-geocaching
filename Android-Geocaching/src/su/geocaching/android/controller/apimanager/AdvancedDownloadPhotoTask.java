package su.geocaching.android.controller.apimanager;


import java.net.URL;

import android.net.Uri;
import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.info.InfoViewModel;

/**
 * This AsyncTask for downloading notebook of geocache
 *
 * @author Nikita Bumakov
 */
public class AdvancedDownloadPhotoTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = AdvancedDownloadPhotoTask.class.getCanonicalName();

    private InfoViewModel infoViewModel;
    private URL remotePhotoUrl;
    
    public AdvancedDownloadPhotoTask(InfoViewModel infoViewModel, URL remotePhotoUrl) {
        this.infoViewModel = infoViewModel;
        this.remotePhotoUrl = remotePhotoUrl;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return Controller.getInstance().getApiManager().downloadPhoto(this.infoViewModel.getGeoCachceId(), remotePhotoUrl);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        LogManager.d(TAG, "onPostExecute");
        
        if (!success) {
            this.infoViewModel.geocachePhotoDownloadFailed(remotePhotoUrl);
            return;
        }
        
        this.infoViewModel.geocachePhotoDownloaded(remotePhotoUrl);        
    }
}
