package su.geocaching.android.ui.info;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoFragment extends Fragment implements IInfoFragment {
    
    private GalleryView galleryView;
    private GalleryImageAdapter galleryAdapter;   
   
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.info_photo_gallery_fragment, container, false);
        galleryView = (GalleryView)v.findViewById(R.id.galleryView);
        galleryAdapter = new GalleryImageAdapter(this.getActivity(), Controller.getInstance().getInfoViewModel().getGeoCachceId());
        galleryView.setAdapter(galleryAdapter);
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();

        galleryAdapter.updateImageList();
        galleryAdapter.notifyDataSetChanged();
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
        
    }
}