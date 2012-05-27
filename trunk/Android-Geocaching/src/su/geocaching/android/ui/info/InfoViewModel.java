package su.geocaching.android.ui.info;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.AdvancedDownloadInfoTask;
import su.geocaching.android.controller.apimanager.AdvancedDownloadNotebookTask;
import su.geocaching.android.controller.apimanager.AdvancedDownloadPhotoTask;
import su.geocaching.android.controller.apimanager.AdvancedDownloadPhotoUrlsTask;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.controller.managers.LogManager;

public class InfoViewModel {
    private static final String TAG = InfoViewModel.class.getCanonicalName();
        
    private static int INFO_TAB_INDEX = 0;
    private static int NOTEBOOK_TAB_INDEX = 1;
    private static int PHOTOS_TAB_INDEX = 2;
    
    private int geoCacheId;
    private DbManager dbManager;
    
    private AdvancedDownloadInfoTask downloadInfoTask = null;
    private AdvancedDownloadNotebookTask downloadNotebookTask = null;
    private AdvancedDownloadPhotoUrlsTask downloadPhotoUrlsTask = null;
    private HashMap<URL, AdvancedDownloadPhotoTask> downloadPhotoTasks = new HashMap<URL, AdvancedDownloadPhotoTask>();
    
    private int selectedTabIndex;
    
    private WebViewTabState infoState;
    private WebViewTabState notebookState;
    private PhotosTabState photosState;
    
    private AdvancedInfoActivity activity;
    
    public InfoViewModel() {
        dbManager = Controller.getInstance().getDbManager();
    }
    
    public synchronized void SetGeoCache(int geoCacheId) {
        if (this.geoCacheId != geoCacheId) {
            this.geoCacheId = geoCacheId;
            
            this.infoState = new WebViewTabState(INFO_TAB_INDEX);
            this.notebookState = new WebViewTabState(NOTEBOOK_TAB_INDEX);
            this.photosState = new PhotosTabState(PHOTOS_TAB_INDEX);
            
            if (isCacheStored()) {
                this.infoState.setText(dbManager.getCacheInfoById(geoCacheId));
                this.notebookState.setText(dbManager.getCacheNotebookTextById(geoCacheId));
                //TODO
                //this.photosState.setText("<center>ФОТОГРАФ�?Я</center>");
            }
            
            cancelDownloadTasks();            
            
            setSelectedTabIndex(INFO_TAB_INDEX);
        }
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
        for (AdvancedDownloadPhotoTask downloadPhotoTask : downloadPhotoTasks.values()) {
            if (isTaskActive(downloadPhotoTask)) {
                downloadPhotoTask.cancel(true);
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
            AdvancedDownloadPhotoTask downloadPhotoTask = new AdvancedDownloadPhotoTask(this, photo.getRemoteUrl());
            downloadPhotoTasks.put(photo.getRemoteUrl(), downloadPhotoTask); 
            downloadPhotoTask.execute();
            photo.setHasErrors(false);
            photo.setIsDownloading(true);
        }
    }        
    
    public void geocachePhotoDownloadFailed(URL remoteURL) {
        downloadPhotoTasks.remove(remoteURL);
        GeoCachePhotoViewModel photo = photosState.getPhoto(remoteURL);
        if (photo != null) {
            photo.setHasErrors(true);
            photo.setIsDownloading(false);
        }        
    }

    public void geocachePhotoDownloaded(URL remoteURL) {
        downloadPhotoTasks.remove(remoteURL);
        GeoCachePhotoViewModel photo = photosState.getPhoto(remoteURL);
        if (photo != null) {
            photo.setIsDownloading(false);
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
        
        public void setPhotoUrls(List<URL> photosUrls, int geocacheId) {
            if (photosUrls == null) {
                this.photos = null;
                return;
            }                
            this.photos = new HashMap<URL, GeoCachePhotoViewModel>();
            for (URL url : photosUrls) {
                this.photos.put(url, new GeoCachePhotoViewModel(url, geocacheId));
            }
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
