package su.geocaching.android.ui.info;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.AdvancedDownloadInfoTask;
import su.geocaching.android.controller.apimanager.AdvancedDownloadNotebookTask;
import su.geocaching.android.controller.apimanager.AdvancedDownloadPhotoUrlsTask;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;

public class InfoViewModel {
    private static final String TAG = InfoViewModel.class.getCanonicalName();
        
    private static int INFO_TAB_INDEX = 0;
    private static int NOTEBOOK_TAB_INDEX = 1;
    private static int PHOTOS_TAB_INDEX = 2;
    
    private int geoCacheId;
    private GeoCache geoCache;
    private DbManager dbManager;
    
    private AdvancedDownloadInfoTask downloadInfoTask = null;
    private AdvancedDownloadNotebookTask downloadNotebookTask = null;
    private AdvancedDownloadPhotoUrlsTask downloadPhotoUrlsTask = null;
    
    private int selectedTabIndex;
    
    private WebViewTabState infoState;
    private WebViewTabState notebookState;
    private PhotosTabState photosState;
    
    private AdvancedInfoActivity activity;
    
    public InfoViewModel() {
        dbManager = Controller.getInstance().getDbManager();
    }
    
    public synchronized void setGeoCache(GeoCache geoCache) {
        if (this.geoCacheId != geoCache.getId()) {
            this.geoCacheId = geoCache.getId();
            this.geoCache = geoCache;
            
            cancelDownloadTasks();
            
            this.infoState = new WebViewTabState(INFO_TAB_INDEX);
            this.notebookState = new WebViewTabState(NOTEBOOK_TAB_INDEX);
            this.photosState = new PhotosTabState(PHOTOS_TAB_INDEX);
            
            if (isCacheStored()) {
                this.infoState.setText(dbManager.getCacheInfoById(geoCacheId));
                this.notebookState.setText(dbManager.getCacheNotebookTextById(geoCacheId));
                this.photosState.setPhotoUrls(dbManager.getCachePhotosById(geoCacheId), geoCacheId);
            }
            
            setSelectedTabIndex(INFO_TAB_INDEX);
        }
    }
    
    public GeoCache getGeoCachce() {
        return this.geoCache;
    }

    public void saveCache() {
        if (isCacheStored()) {
            dbManager.updateInfoText(this.geoCacheId, this.infoState.getText());
            dbManager.updateNotebookText(this.geoCacheId, this.notebookState.getText());
            dbManager.updatePhotos(this.geoCacheId, this.photosState.getPhotoUrls());
        } else {
            dbManager.addGeoCache(this.geoCache, this.infoState.getText(), this.notebookState.getText(), this.photosState.getPhotoUrls());
        }        
    }

    public void deleteCache() {
        // TODO:  Check if we need to clear CheckpointManager      
        //controller.getCheckpointManager(geoCache.getId()).clear();
        //Controller.getInstance().getExternalStorageManager().deletePhotos(id);
        dbManager.deleteCacheById(this.geoCacheId);
        
    }
    
    private void cancelDownloadTasks() {
        if (isTaskActive(downloadInfoTask)) {
            downloadInfoTask.cancel(true);
        }
        if (isTaskActive(downloadNotebookTask)) {
            downloadInfoTask.cancel(true);
        }
        if (isTaskActive(downloadPhotoUrlsTask)) {
            downloadPhotoUrlsTask.cancel(true);
        }
        if (this.photosState != null) {
            Collection<GeoCachePhotoViewModel> photos = this.photosState.getPhotos();
            if (photos != null) {
                for (GeoCachePhotoViewModel photo : this.photosState.getPhotos()) {
                    photo.cancelLoadPhoto();
                }            
            }
        }
    }

    public synchronized void beginLoadInfo() {
        if (isTaskActive(downloadInfoTask)) return;
        
        downloadInfoTask = new AdvancedDownloadInfoTask(this);
        downloadInfoTask.execute();
        if (activity != null) {
            activity.showInfoProgressBar();
            activity.hideInfoErrorMessage();
        }
    }
    
    public synchronized void beginLoadNotebook() {
        if (isTaskActive(downloadNotebookTask)) return;
        
        downloadNotebookTask = new AdvancedDownloadNotebookTask(this);
        downloadNotebookTask.execute();
        if (activity != null) {
            activity.showNotebookProgressBar();
            activity.hideNotebookErrorMessage();
        }
    }
    
    public synchronized void beginLoadPhotoUrls() {
        if (isTaskActive(downloadPhotoUrlsTask)) return;
        
        downloadPhotoUrlsTask = new AdvancedDownloadPhotoUrlsTask(this);
        downloadPhotoUrlsTask.execute();
        if (activity != null) {
            activity.showPhotoListProgressBar();
            activity.hidePhotoListErrorMessage();
        }       
    }    
    
    public synchronized void geocacheInfoDownloaded(String result) {
        this.infoState.setText(result);
        if (activity != null) {
            activity.updateInfoText();
            activity.hideInfoProgressBar();
        }
    }
    
    public synchronized void geocacheInfoDownloadFailed() {
        if (activity != null) {
            activity.hideInfoProgressBar();
            activity.showInfoErrorMessage();
        }
    }
    
    public void geocacheNotebookDownloaded(String result) {
        this.notebookState.setText(result);
        if (activity != null) {
            activity.updateNotebookText();
            activity.hideNotebookProgressBar();
        }        
    }

    public void geocacheNotebookDownloadFailed() {
        if (activity != null) {
            activity.hideNotebookProgressBar();
            activity.showNotebookErrorMessage();
        }
    }
    
    public void geocachePhotoListDownloaded(List<URL> result) {
        this.photosState.setPhotoUrls(result, this.geoCacheId);

        beginLoadPhotos();
        
        if (activity != null) {
            activity.updatePhotosList();
            activity.hidePhotoListProgressBar();
        }
    }
    
    public void geocachePhotoListDownloadFailed() {
        if (activity != null) {
            activity.hidePhotoListProgressBar();
            activity.showPhotoListErrorMessage();
        }        
    }
    
    public synchronized void beginLoadPhotos() {
        for (GeoCachePhotoViewModel photo : this.photosState.getPhotos()) {
            photo.beginLoadPhoto();
        }
    }        
        
    public boolean isCacheStored()  {
        return dbManager.isCacheStored(geoCacheId);        
    }
    
    public WebViewTabState getInfoState() {
        return infoState;
    }
    
    public WebViewTabState getNotebookState() {
        return notebookState;
    }

    public PhotosTabState getPhotosState() {
        return photosState;
    }
    
    public int getGeoCachceId() {
        return geoCacheId; 
    }
    
    public int getSelectedTabIndex() {
        return selectedTabIndex;
    }

    public void setSelectedTabIndex(int selectedTabIndex) {
        this.selectedTabIndex = selectedTabIndex;
    }
    
    public synchronized void registerActivity(AdvancedInfoActivity activity) {
        if (this.activity != null) {
            LogManager.e(TAG, "Attempt to register activity while activity is not null");
        }
        this.activity = activity;
        if (isTaskActive(downloadInfoTask)) {
            onShowDownloadingInfo();
        }
        if (isTaskActive(downloadNotebookTask)) {
            onShowDownloadingNotebook();
        }
        if (isTaskActive(downloadPhotoUrlsTask)) {
            onShowDownloadingPhotoUrls();
        }        
    }
    
    private static boolean isTaskActive(AsyncTask<?,?,?> task) {
        return task != null && !task.isCancelled() && (task.getStatus() != AsyncTask.Status.FINISHED);
    }

    private synchronized void onShowDownloadingInfo() {
        if (activity != null) {
            activity.showInfoProgressBar();
        }        
    }
    
    private synchronized void onShowDownloadingNotebook() {
        if (activity != null) {
            activity.showNotebookProgressBar();
        }        
    }
    
    private synchronized void onShowDownloadingPhotoUrls() {
        if (activity != null) {
            activity.showPhotoListProgressBar();
        }        
    }    

    public synchronized void unregisterActivity(AdvancedInfoActivity activity) {
        if (this.activity == null) {
            LogManager.e(TAG, "Attempt to unregister activity while activity is null");
        }
        this.activity = null;
    }    
    
    public static class WebViewTabState extends InfoTabState {       
        private int scrollY;
        private float scale;
        private int width;
        private String text;
        
        public WebViewTabState(int index) {
            super(index);
        }        
        
        public int getScrollY() {
            return this.scrollY;
        }
        public void setScrollY(int scrollY) {
            this.scrollY = scrollY;
        }
        
        public int getWidth() {
           return this.width;
        }
        public void setWidth(int width) {
            this.width = width;
        }
        
        public float getScale() {
            return this.scale;
        }
        public void setScale(float scale) {
            this.scale = scale;
        }
        
        public String getText() {
            return this.text;
        }
        public void setText(String text) {
            this.text = text;
        }           
    }  

    public static class PhotosTabState extends InfoTabState {
        
        private HashMap<URL, GeoCachePhotoViewModel> photos;
        
        public PhotosTabState(int index) {
            super(index);
        }

        public Collection<GeoCachePhotoViewModel> getPhotos() {
            return this.photos == null ? null : this.photos.values();
        }
        
        public GeoCachePhotoViewModel getPhoto(URL photoURL) {
            return this.photos.get(photoURL);
        }        
        
        public void setPhotoUrls(Collection<URL> photosUrls, int geocacheId) {
            if (photosUrls == null) {
                this.photos = null;
                return;
            }                
            this.photos = new HashMap<URL, GeoCachePhotoViewModel>();
            for (URL url : photosUrls) {
                this.photos.put(url, new GeoCachePhotoViewModel(url, geocacheId));
            }
        }
        
        public Collection<URL> getPhotoUrls() {
            return this.photos == null ? null : this.photos.keySet();
        }
    }
    
    public static class InfoTabState {
        private int index;               
        public InfoTabState(int index) {
           this.index = index; 
        }
        public int getIndex() {
            return this.index;
        }        
    }
}
