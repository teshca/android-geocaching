package su.geocaching.android.ui.info;

public class InfoFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewState getFragmentState() {
        return infoViewModel.getInfoState();
    }

    @Override
    protected void BeginLoadData() {
        infoViewModel.BeginLoadInfo();        
    }
}