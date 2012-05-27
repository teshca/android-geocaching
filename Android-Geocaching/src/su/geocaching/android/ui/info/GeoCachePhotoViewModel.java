package su.geocaching.android.ui.info;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import su.geocaching.android.controller.Controller;
import android.net.Uri;

public class GeoCachePhotoViewModel {
    
    public GeoCachePhotoViewModel(URL remoteURL, int geoCacheId) {
        this.remoteUrl = remoteURL;
        this.localUri = Controller.getInstance().getExternalStorageManager().getLocalPhotoUri(remoteURL, geoCacheId);         
    }
    
    private List<PhotoDownloadingChangedEventListener> _listeners = new ArrayList<PhotoDownloadingChangedEventListener>();
    public synchronized void addPhotoDownloadingChangedEventListener(PhotoDownloadingChangedEventListener listener)  {
        _listeners.add(listener);
    }
    public synchronized void removePhotoDownloadingChangedEventListener(PhotoDownloadingChangedEventListener listener)   {
        _listeners.remove(listener);
    }
    
    private synchronized void fireIsDownloadingChanged() {
        for (PhotoDownloadingChangedEventListener _listener : _listeners) {
            _listener.onPhotoDownloadingChanged();
        }
    }
    
    private URL remoteUrl;
    public URL getRemoteUrl() {
        return remoteUrl;
    }
    
    private Uri localUri;
    public Uri getLocalUri() {
        return localUri;
    }
    
    private boolean hasErrors;
    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;        
    }
    public boolean HasErrors() {
        return hasErrors;
    }
    
    private boolean isDownloading;
    public void setIsDownloading(boolean isDownloading) {
        if (this.isDownloading != isDownloading) {
            this.isDownloading = isDownloading;   
            fireIsDownloadingChanged();
        }        
    }
    public boolean IsDownloading() {
        return isDownloading;
    }
    public void BeginLoadPhoto() {
        // TODO Auto-generated method stub
        
    }    
}
