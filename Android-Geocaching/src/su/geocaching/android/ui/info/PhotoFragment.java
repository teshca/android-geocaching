package su.geocaching.android.ui.info;

public class PhotoFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewState getFragmentState() {
        return infoViewModel.getInfoState(2);
    }    
}