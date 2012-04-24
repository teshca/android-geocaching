package su.geocaching.android.ui.info;

import android.os.AsyncTask;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.AdvancedDownloadInfoTask;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.controller.managers.LogManager;

public class InfoViewModel {
    private static final String TAG = InfoViewModel.class.getCanonicalName();
    
    private int geoCacheId;
    private DbManager dbManager;
    
    private AdvancedDownloadInfoTask downloadInfoTask = null;
    
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
            
            this.infoState = new WebViewState();
            this.notebookState = new WebViewState();
            this.photosState = new WebViewState();
            
            if (isCacheStored()) {
                this.infoState.setText(dbManager.getCacheInfoById(geoCacheId));
                this.notebookState.setText(dbManager.getCacheNotebookTextById(geoCacheId));
                this.photosState.setText("<center>ФОТОГРАФ�?�?</center>");
            }
            
            setSelectedTabIndex(0);
        }
    }
    
    public synchronized void BeginLoadInfo() {
        downloadInfoTask = new AdvancedDownloadInfoTask(this);
        downloadInfoTask.execute();
        if (activity != null) {
            activity.showInfoProgressBar();
        }
    }
    
    public synchronized void BeginLoadNotebook() {
        // TODO Auto-generated method stub        
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
        if (downloadInfoTask != null && !downloadInfoTask.isCancelled() && (downloadInfoTask.getStatus() != AsyncTask.Status.FINISHED)) {
            onShowDownloadingInfo();
        }
    }

    private synchronized void onShowDownloadingInfo() {
        if (activity != null) {
            activity.showInfoProgressBar();
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
