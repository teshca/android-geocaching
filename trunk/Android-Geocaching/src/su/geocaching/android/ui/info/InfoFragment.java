package su.geocaching.android.ui.info;

import android.os.Bundle;

public class InfoFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewTabState getFragmentState() {
        return infoViewModel.getInfoState();
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (infoViewModel.getSelectedTabIndex() == infoViewModel.getInfoState().getIndex()) {
            onNavigatedTo();
        }
    }     

    @Override
    protected void BeginLoadData() {
        infoViewModel.beginLoadInfo();        
    }
}