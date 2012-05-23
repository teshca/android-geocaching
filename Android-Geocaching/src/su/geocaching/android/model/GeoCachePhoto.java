package su.geocaching.android.model;

import java.net.URL;
import android.net.Uri;

public class GeoCachePhoto {
    
    public GeoCachePhoto(URL remoteURL) {
        this.remoteUrl = remoteURL;
    }
    
    private URL remoteUrl;
    public URL getRemoteUrl() {
        return remoteUrl;
    }
    
    private Uri localUri;
    public Uri getLocalUri() {
        return localUri;
    }
    public void setLocalUri(Uri localUri) {
        this.localUri = localUri;
    }    
}
