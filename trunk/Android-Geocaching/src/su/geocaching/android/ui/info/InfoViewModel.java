package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.DbManager;
import su.geocaching.android.model.GeoCache;

public class InfoViewModel {
    private static final String TAG = InfoViewModel.class.getCanonicalName();
    
    private int geoCacheId;
    private DbManager dbManager;
    
    private int selectedTabIndex;
    
    private WebViewState infoState0;
    private WebViewState infoState1;
    private WebViewState infoState2;
    
    public InfoViewModel() {
        dbManager = Controller.getInstance().getDbManager();
    }
    
    public synchronized void SetGeoCache(int geoCacheId) {
        if (this.geoCacheId != geoCacheId) {
            this.geoCacheId = geoCacheId;
            
            this.infoState0 = new WebViewState();
            this.infoState1 = new WebViewState();
            this.infoState2 = new WebViewState();
            
            if (dbManager.isCacheStored(geoCacheId)) {
                this.infoState0.setText(dbManager.getCacheInfoById(geoCacheId));
                this.infoState1.setText(dbManager.getCacheNotebookTextById(geoCacheId));
                this.infoState2.setText("<center>ФОТОГРАФИИ</center>");
            }
            
            setSelectedTabIndex(0);
        }
    }
    
    public WebViewState getInfoState(int num) {
        if (num == 0) return infoState0;
        if (num == 1) return infoState1;
        return infoState2;
    }
    
    public int getSelectedTabIndex()
    {
        return selectedTabIndex;
    }

    public void setSelectedTabIndex(int selectedTabIndex)
    {
        this.selectedTabIndex = selectedTabIndex;
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
