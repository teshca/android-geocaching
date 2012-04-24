package su.geocaching.android.ui.info;

public class PhotoFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewState getFragmentState() {
        return infoViewModel.getPhotosState();
    }

    @Override
    protected void BeginLoadData() {
        // TODO Auto-generated method stub
        
    }    
}