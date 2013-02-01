package su.geocaching.android.ui.info;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockFragment;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.info.InfoViewModel.PhotosTabState;

public class PhotoFragment extends SherlockFragment implements IInfoFragment {

    private InfoViewModel infoViewModel;
    private GalleryView galleryView;
    private AdvancedGalleryImageAdapter galleryAdapter;

    private ProgressBar progressBar;
    private View errorMessage;
    private ImageButton refreshButton;
    private TextView noPhotosTextView;
    private View sdCardErrorMessage;

    private View trafficWarning;
    private Button downloadButton;
    private CheckBox downloadAlways;

    private PhotosTabState state;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_photo_gallery_fragment, container, false);

        progressBar = (ProgressBar) v.findViewById(R.id.info_progress_bar);
        errorMessage = v.findViewById(R.id.info_error_panel);
        noPhotosTextView = (TextView) v.findViewById(R.id.info_no_photos_text);
        refreshButton = (ImageButton) v.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                infoViewModel.beginLoadPhotoUrls();
            }
        }
        );

        trafficWarning = v.findViewById(R.id.info_traffic_warning_panel);
        downloadAlways = (CheckBox) v.findViewById(R.id.downloadAlways);
        downloadButton = (Button) v.findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                infoViewModel.beginLoadPhotoUrls();

                Controller.getInstance().getPreferencesManager().setDownloadPhotosAlways(downloadAlways.isChecked());
                trafficWarning.setVisibility(View.GONE);
            }
        }
        );

        sdCardErrorMessage = v.findViewById(R.id.info_sd_card_error_panel);

        infoViewModel = Controller.getInstance().getInfoViewModel();
        state = infoViewModel.getPhotosState();

        galleryView = (GalleryView) v.findViewById(R.id.galleryView);
        galleryAdapter = new AdvancedGalleryImageAdapter(this.getActivity(), Controller.getInstance().getInfoViewModel());
        galleryView.setAdapter(galleryAdapter);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (infoViewModel.getSelectedTabIndex() == state.getIndex()) {
            onNavigatedTo();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onNavigatedTo() {
        if (galleryView.getVisibility() == View.VISIBLE ||
                noPhotosTextView.getVisibility() == View.VISIBLE) {
            return;
        }

        trafficWarning.setVisibility(View.GONE);
        sdCardErrorMessage.setVisibility(View.GONE);
        galleryView.setVisibility(View.GONE);
        noPhotosTextView.setVisibility(View.GONE);
        errorMessage.setVisibility(View.GONE);

        if (Controller.getInstance().getExternalStorageManager().isExternalStorageAvailable()) {
            if (state.getPhotos() == null && !infoViewModel.isPhotoUrlsLoading()) {
                if (Controller.getInstance().getPreferencesManager().getDownloadPhotosAlways() ||
                        Controller.getInstance().getConnectionManager().isWifiConnected()) {
                    infoViewModel.beginLoadPhotoUrls();
                } else {
                    trafficWarning.setVisibility(View.VISIBLE);
                }
            } else {
                updatePhotosList();
            }
        } else {
            sdCardErrorMessage.setVisibility(View.VISIBLE);
        }
    }

    public void updatePhotosList() {
        if (state.getPhotos() == null) {
            galleryView.setVisibility(View.GONE);
            noPhotosTextView.setVisibility(View.GONE);
        } else if (state.getPhotos().isEmpty()) {
            galleryView.setVisibility(View.GONE);
            noPhotosTextView.setVisibility(View.VISIBLE);
        } else {
            galleryView.setVisibility(View.VISIBLE);
            noPhotosTextView.setVisibility(View.GONE);
            galleryAdapter.updateImageList();
        }
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

        //also hide traffic warning in case option menu 'refresh' was clicked
        // while traffic warning is visible
        trafficWarning.setVisibility(View.GONE);
    }
}