package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Picture;
import android.webkit.WebView.PictureListener;

public abstract class AbstractWebViewFragment extends Fragment {      
    private WebView webView;
    protected InfoViewModel infoViewModel;
    private InfoViewModel.WebViewState state;
    
    // we need this counter in order to restore WebView scroll when initial scale is not 100%
    private int onNewPictureCounter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoViewModel = Controller.getInstance().getInfoViewModel();
        state = getFragmentState();
    }
    
    protected abstract InfoViewModel.WebViewState getFragmentState();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_info_fragment, container, false);
        webView = (WebView) v.findViewById(R.id.info_web_brouse);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.setHorizontalScrollBarEnabled(false);
        
        webView.setInitialScale((int) (state.getScale() * 100));
        
        onNewPictureCounter = 0;
        webView.setPictureListener(new PictureListener() {
            @Override
            public void onNewPicture(WebView view, Picture picture) {    
                onNewPictureCounter++;
                if (state.getScale() == 1 || onNewPictureCounter == 2) {
                    webView.setPictureListener(null);
                }
                
                int scrollY = state.getScrollY();
                if (scrollY != 0) {
                    scrollY = scrollY * state.getWidth() / view.getWidth();
                    view.scrollTo(0, scrollY);
                }
            }
        }); 
        
        WebViewClient webViewClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {                
                String urlNotebook = String.format(GeocachingSuApiManager.LINK_NOTEBOOK_TEXT, infoViewModel.getGeoCachceId());
                String urlInfo = String.format(GeocachingSuApiManager.LINK_INFO_CACHE, infoViewModel.getGeoCachceId());
                String urlPhoto = String.format(GeocachingSuApiManager.LINK_PHOTO_PAGE, infoViewModel.getGeoCachceId());

                if (urlInfo.contains(url)) {
                    loadView(PageState.INFO);
                    return true;
                }

                if (urlNotebook.contains(url)) {
                    loadView(PageState.NOTEBOOK);
                    return true;
                }

                if (urlPhoto.contains(url)) {
                    loadView(PageState.PHOTO);
                    return true;
                }
                if (url.contains("geo:")) {
                    if (!isCacheStored) {
                        Toast.makeText(InfoActivity.this, R.string.ask_add_cache_in_db, Toast.LENGTH_LONG).show();
                        return true;
                    }

                    String[] coordinates = url.split("[^0-9]++");
                    int lat = Integer.parseInt(coordinates[1]);
                    int lng = Integer.parseInt(coordinates[2]);
                    GeoCache checkpoint = new GeoCache();
                    checkpoint.setId(geoCache.getId());
                    checkpoint.setType(GeoCacheType.CHECKPOINT);
                    checkpoint.setLocationGeoPoint(new GeoPoint(lat, lng));
                    NavigationManager.startCreateCheckpointActivity(InfoActivity.this, checkpoint);
                    return true;
                }

                return false;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                shouldOverrideUrlLoading(view, url);
            }
        };

        webView.setWebViewClient(webViewClient);        
        
        return v;
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setWebViewData(state.getText());            
    }
      
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        state.setScale(webView.getScale());
        state.setScrollY(webView.getScrollY());
        state.setWidth(webView.getWidth());
    }
    
    private void setWebViewData(String data) {
        webView.loadDataWithBaseURL(GeocachingSuApiManager.HTTP_PDA_GEOCACHING_SU, data, "text/html", GeocachingSuApiManager.UTF8_ENCODING, null);
    } 
}