package su.geocaching.android.ui.info;

import android.graphics.Picture;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.apimanager.GeocachingSuApiManager;
import su.geocaching.android.model.GeoPoint;
import su.geocaching.android.ui.R;

public abstract class AbstractWebViewFragment extends SherlockFragment implements IInfoFragment {

    private WebView webView;
    private ProgressBar progressBar;
    private View errorMessage;
    private ImageButton refreshButton;

    protected InfoViewModel infoViewModel;
    private InfoViewModel.WebViewTabState state;

    // we need this counter in order to restore WebView scroll when initial scale is not 100%
    private int onNewPictureCounter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        infoViewModel = Controller.getInstance().getInfoViewModel();
        state = getFragmentState();
        setHasOptionsMenu(android.os.Build.VERSION.SDK_INT >= 11);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateText();
        if (infoViewModel.getSelectedTabIndex() == state.getIndex()) {
            onNavigatedTo();
        }
    }

    protected abstract InfoViewModel.WebViewTabState getFragmentState();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_web_view_fragment, container, false);
        webView = (WebView) v.findViewById(R.id.info_web_brouse);

        progressBar = (ProgressBar) v.findViewById(R.id.info_progress_bar);
        errorMessage = (View) v.findViewById(R.id.info_error_panel);
        refreshButton = (ImageButton) v.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                BeginLoadData();
            }
        }
        );

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= 11) {
            webView.getSettings().setDisplayZoomControls(false);
        }
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
                String urlInfo = String.format(GeocachingSuApiManager.LINK_INFO_TEXT, infoViewModel.getGeoCachceId());
                String urlPhoto = String.format(GeocachingSuApiManager.LINK_PHOTO_PAGE, infoViewModel.getGeoCachceId());

                if (urlInfo.contains(url)) {
                    ((AdvancedInfoActivity) getActivity()).navigateToInfoTab();
                    return true;
                }

                if (urlNotebook.contains(url)) {
                    ((AdvancedInfoActivity) getActivity()).naviagteToNotebookTab();
                    return true;
                }

                if (urlPhoto.contains(url)) {
                    ((AdvancedInfoActivity) getActivity()).naviagteToPhotosTab();
                    return true;
                }

                if (url.startsWith("geo:")) {
                    String[] coordinates = url.split("[^0-9]+");
                    int lat = Integer.parseInt(coordinates[1]);
                    int lng = Integer.parseInt(coordinates[2]);
                    ((AdvancedInfoActivity) getActivity()).openCheckpointDialog(GeoPoint.fromE6(lat, lng));
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

    public void onNavigatedTo() {
        if (state.getText() == null) {
            BeginLoadData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.web_view_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_web_view_search:
                webView.showFindDialog(null, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected abstract void BeginLoadData();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        state.setScale(webView.getScale());
        state.setScrollY(webView.getScrollY());
        state.setWidth(webView.getWidth());

        webView.destroy();
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    public void showErrorMessage() {
        errorMessage.setVisibility(View.VISIBLE);
    }

    public void hideErrorMessage() {
        errorMessage.setVisibility(View.GONE);
    }

    public void updateText() {
        setWebViewData(state.getText());
    }

    private void setWebViewData(String data) {
        // TODO: Try to contact server side developers in order to add this style on server
        if (data != null) {
            data = data.replace("<body>", "<body style='word-wrap: break-word'>");
        }
        webView.loadDataWithBaseURL(GeocachingSuApiManager.HTTP_PDA_GEOCACHING_SU, data, "text/html", GeocachingSuApiManager.UTF8_ENCODING, null);
    }
}