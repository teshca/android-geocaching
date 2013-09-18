package su.geocaching.android.ui.info;

import android.net.Uri;
import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.AdvancedDownloadPhotoTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GeoCachePhotoViewModel {

    private AdvancedDownloadPhotoTask downloadPhotoTask;

    public GeoCachePhotoViewModel(URL remoteURL, int geoCacheId) {
        this.remoteUrl = remoteURL;
        this.geoCacheId = geoCacheId;
    }

    private List<GeoCachePhotoDownloadingChangedListener> _listeners = new ArrayList<GeoCachePhotoDownloadingChangedListener>();

    public synchronized void addPhotoDownloadingChangedEventListener(GeoCachePhotoDownloadingChangedListener listener) {
        _listeners.add(listener);
    }

    public synchronized void removePhotoDownloadingChangedEventListener(GeoCachePhotoDownloadingChangedListener listener) {
        _listeners.remove(listener);
    }

    private synchronized void fireIsDownloadingChanged() {
        for (GeoCachePhotoDownloadingChangedListener _listener : _listeners) {
            _listener.onPhotoDownloadingChanged();
        }
    }

    private URL remoteUrl;

    public URL getRemoteUrl() {
        return remoteUrl;
    }

    private int geoCacheId;

    public int getGeoCacheId() {
        return geoCacheId;
    }

    public Uri getLocalUri() {
        return Controller.getInstance().getExternalStorageManager().getLocalPhotoUri(remoteUrl, geoCacheId);
    }

    private boolean hasErrors;

    private void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public boolean HasErrors() {
        return hasErrors;
    }

    private boolean isDownloading;

    private void setIsDownloading(boolean isDownloading) {
        if (this.isDownloading != isDownloading) {
            this.isDownloading = isDownloading;
            fireIsDownloadingChanged();
        }
    }

    public boolean IsDownloading() {
        return isDownloading;
    }

    public void beginLoadPhoto() {
        if (isTaskActive(downloadPhotoTask)) return;

        setHasErrors(false);
        setIsDownloading(true);
        downloadPhotoTask = new AdvancedDownloadPhotoTask(this);
        downloadPhotoTask.execute();
    }

    public void geocachePhotoDownloadFailed() {
        downloadPhotoTask = null;
        setHasErrors(true);
        setIsDownloading(false);
    }

    public void geocachePhotoDownloaded() {
        downloadPhotoTask = null;
        setIsDownloading(false);
    }

    public void cancelLoadPhoto() {
        if (isTaskActive(downloadPhotoTask)) {
            downloadPhotoTask.cancel(true);
        }
    }

    private static boolean isTaskActive(AsyncTask<?, ?, ?> task) {
        return task != null && !task.isCancelled() && (task.getStatus() != AsyncTask.Status.FINISHED);
    }
}