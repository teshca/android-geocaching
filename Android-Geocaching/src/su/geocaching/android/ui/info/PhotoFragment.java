package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class PhotoFragment extends Fragment implements IInfoFragment {
    
    private InfoViewModel infoViewModel;
    private GalleryView galleryView;
    private GalleryImageAdapter galleryAdapter;   
    
    private ProgressBar progressBar;
    private View errorMessage;
    private ImageButton refreshButton;
   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_photo_gallery_fragment, container, false);
        
        progressBar = (ProgressBar) v.findViewById(R.id.info_progress_bar);
        errorMessage = (View) v.findViewById(R.id.info_error_panel);
        refreshButton = (ImageButton) v.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                infoViewModel.beginLoadPhotoUrls();
            }}
        );
        
        infoViewModel = Controller.getInstance().getInfoViewModel();
        galleryView = (GalleryView)v.findViewById(R.id.galleryView);
        galleryAdapter = new GalleryImageAdapter(this.getActivity(), Controller.getInstance().getInfoViewModel().getGeoCachceId());
        galleryView.setAdapter(galleryAdapter);
        return v;
    }
    
    @Override 
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (infoViewModel.getSelectedTabIndex() == infoViewModel.getPhotosState().getIndex()) {
            onNavigatedTo();
        }
    } 

    @Override
    public void onResume() {
        super.onResume();

        galleryAdapter.updateImageList();
        galleryAdapter.notifyDataSetChanged();
        
        //TODO No photos message
/*
        if (favoriteGeoCachesAdapter.isEmpty()) {
            tvNoCache.setVisibility(View.VISIBLE);
            actionSearch.setVisibility(View.GONE);
            actionSort.setVisibility(View.GONE);
        } else {
            tvNoCache.setVisibility(View.GONE);
            actionSearch.setVisibility(View.VISIBLE);
            actionSort.setVisibility(View.VISIBLE);
        }
        */
    }

    @Override
    public void onPause() {
        super.onPause();
        galleryAdapter.clear();
    }    
    
    @Override
    public void onNavigatedTo() {
        if (infoViewModel.getPhotosState().getPhotoUrls() == null) {
            infoViewModel.beginLoadPhotoUrls();
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
    }
}