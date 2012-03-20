package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.graphics.Picture;
import android.webkit.WebView.PictureListener;

public class NotebookFragment extends AbstractWebViewFragment {
    @Override
    protected InfoViewModel.WebViewState getFragmentState() {
        return infoViewModel.getInfoState(1);
    }    
}