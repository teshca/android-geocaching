package su.geocaching.android.ui.info;

public class InfoFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewTabState getFragmentState() {
        return infoViewModel.getInfoState();
    } 

    @Override
    protected void BeginLoadData() {
        infoViewModel.beginLoadInfo();        
    }
}