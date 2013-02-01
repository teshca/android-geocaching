package su.geocaching.android.controller.apimanager;


import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.info.InfoViewModel;

import java.net.URL;
import java.util.List;

/**
 * This AsyncTask for downloading notebook of geocache
 *
 * @author Nikita Bumakov
 */
public class AdvancedDownloadPhotoUrlsTask extends AsyncTask<Void, Void, List<URL>> {

    private static final String TAG = AdvancedDownloadPhotoUrlsTask.class.getCanonicalName();

    private InfoViewModel infoViewModel;
    
    public AdvancedDownloadPhotoUrlsTask(InfoViewModel infoViewModel) {
        this.infoViewModel = infoViewModel;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "onPreExecute");
    }

    @Override
    protected List<URL> doInBackground(Void... arg0) {
        List<URL> photoList = Controller.getInstance().getApiManager().getPhotoList(this.infoViewModel.getGeoCachceId());   
        return photoList;
    }

    @Override
    protected void onPostExecute(List<URL> result) {
        LogManager.d(TAG, "onPostExecute");
        
        if (result == null) {
            this.infoViewModel.geocachePhotoListDownloadFailed();
            return;
        }
        
        this.infoViewModel.geocachePhotoListDownloaded(result);        
    }
}
