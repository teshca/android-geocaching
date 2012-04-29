package su.geocaching.android.ui.info;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.AdvancedDownloadInfoTask;
import su.geocaching.android.controller.apimanager.AdvancedDownloadNotebookTask;
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
    
    private int selectedTabIndex;
    
    private WebViewState infoState;
    private WebViewState notebookState;
    private WebViewState photosState;
    
    private AdvancedInfoActivity activity;
    
    public InfoViewModel() {
        dbManager = Controller.getInstance().getDbManager();
    }
    
    public synchronized void SetGeoCache(int geoCacheId) {
        if (this.geoCacheId != geoCacheId) {
            this.geoCacheId = geoCacheId;
            
            this.infoState = new WebViewState(INFO_TAB_INDEX);
            this.notebookState = new WebViewState(NOTEBOOK_TAB_INDEX);
            this.photosState = new WebViewState(PHOTOS_TAB_INDEX);
            
            if (isCacheStored()) {
                this.infoState.setText(dbManager.getCacheInfoById(geoCacheId));
                this.notebookState.setText(dbManager.getCacheNotebookTextById(geoCacheId));
                this.photosState.setText("<center>ФОТОГРАФИЯ</center>");
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
    }

    public synchronized void BeginLoadInfo() {
        if (isTaskActive(downloadInfoTask)) return;
        
        downloadInfoTask = new AdvancedDownloadInfoTask(this);
        downloadInfoTask.execute();
        if (activity != null) {
            activity.showInfoProgressBar();
            activity.hideInfoErrorMessage();
        }
    }
    
    public synchronized void BeginLoadNotebook() {
        if (isTaskActive(downloadNotebookTask)) return;
        
        downloadNotebookTask = new AdvancedDownloadNotebookTask(this);
        downloadNotebookTask.execute();
        if (activity != null) {
            activity.showNotebookProgressBar();
            activity.hideNotebookErrorMessage();
        }
    }
    
    public synchronized void geocacheInfoDownloaded(String result) {
        this.infoState.setText(result);
        if (activity != null) {
            activity.setInfoText(result);
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
            activity.setNotebookText(result);
            activity.hideNotebookProgressBar();
        }        
    }

    public void geocacheNotebookDownloadFailed() {
        if (activity != null) {
            activity.hideNotebookProgressBar();
            activity.showNotebookErrorMessage();
        }
    }    
    
    public boolean isCacheStored()  {
        return dbManager.isCacheStored(geoCacheId);        
    }
    
    public WebViewState getInfoState() {
        return infoState;
    }
    
    public WebViewState getNotebookState() {
        return notebookState;
    }

    public WebViewState getPhotosState() {
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
    }
    
    private static boolean isTaskActive(AsyncTask task) {
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

    public synchronized void unregisterActivity(AdvancedInfoActivity activity) {
        if (this.activity == null) {
            LogManager.e(TAG, "Attempt to unregister activity while activity is null");
        }
        this.activity = null;
    }    
    
    public static class WebViewState
    {
        private int index;               
        public WebViewState(int index){
           this.index = index; 
        }
        public int getIndex() {
            return this.index;
        }
        
        private int scrollY;
        private float scale;
        private int width;
        private String text;
        
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
}
