package su.geocaching.android.model;

import java.io.File;
import java.net.URL;
import android.net.Uri;

public class GeoCachePhoto {
    
    public GeoCachePhoto(URL remoteURL) {
        this.remoteUrl = remoteURL;
    }
    
    private URL remoteUrl;
    private File file;
    
    public URL getRemoteUrl() {
        return remoteUrl;
    }
    
    //TODO: implement get/set
    public Uri localUri;

    public File getFile() {
        return file;
    }    
}
