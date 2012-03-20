package su.geocaching.android.ui.info;

public class InfoFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewState getFragmentState() {
        return infoViewModel.getInfoState(0);
    }
}