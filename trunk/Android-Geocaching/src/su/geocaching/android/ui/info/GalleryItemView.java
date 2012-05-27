package su.geocaching.android.ui.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;

/**
 * @author Nickolay Artamonov
 */
public class GalleryItemView extends FrameLayout implements GeoCachePhotoDownloadingChangedListener {

    private static final String TAG = GalleryItemView.class.getCanonicalName();

    private GeoCachePhotoViewModel cachePhoto;
    private final int thumbnailsPhotoSize;
    
    ProgressBar progressBar;
    ImageView image;
    View errorMessage;
    
    public GalleryItemView(final Context context, GeoCachePhotoViewModel cachePhoto) {
        super(context);
        this.cachePhoto = cachePhoto;
        
        View.inflate(context, R.layout.info_photo_gallery_item, this);
        thumbnailsPhotoSize = context.getResources().getDimensionPixelSize(R.dimen.adapter_photo_size);
        setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.FILL_PARENT, thumbnailsPhotoSize));
        setMinimumWidth(thumbnailsPhotoSize);
        
        image = (ImageView) findViewById(R.id.photo_image);
        progressBar = (ProgressBar) findViewById(R.id.photo_progress_bar);
        errorMessage = findViewById(R.id.photo_error_panel);
        
        ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GalleryItemView.this.cachePhoto.beginLoadPhoto();
            }}
        );        
        
        LogManager.d(TAG, "onCreate");
    }
    
    public void updateGeoCachePhoto(GeoCachePhotoViewModel photo) {
        if (getWindowToken() != null) {
            this.cachePhoto.removePhotoDownloadingChangedEventListener(this);
            this.cachePhoto = photo;
            this.cachePhoto.addPhotoDownloadingChangedEventListener(this);
            updateView();            
        } else {
            this.cachePhoto = photo; 
        }
    }
    
    private Bitmap scaleBitmap(GeoCachePhotoViewModel cachePhoto) {
        BitmapFactory.Options justDecodeBoundsOptions = new BitmapFactory.Options();
        justDecodeBoundsOptions.inJustDecodeBounds = true;
        Uri localUri = cachePhoto.getLocalUri();
        if (localUri == null) {
            return null;
        }        
        String path = localUri.getPath();
        BitmapFactory.decodeFile(path, justDecodeBoundsOptions);
        if (justDecodeBoundsOptions.outHeight == -1 || justDecodeBoundsOptions.outWidth == -1) {
            return null;
        }
        int scale = Math.max(justDecodeBoundsOptions.outHeight, justDecodeBoundsOptions.outWidth) / thumbnailsPhotoSize;
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, scaleOptions);
    }   
          
    private void updateView() {
        if (this.cachePhoto.IsDownloading()) {
            errorMessage.setVisibility(GONE);            
            progressBar.setVisibility(View.VISIBLE);            
        } else {
            progressBar.setVisibility(View.GONE);
            if (this.cachePhoto.HasErrors()) {
                errorMessage.setVisibility(VISIBLE); 
            } else {
                image.setImageBitmap(scaleBitmap(cachePhoto));
            }
        }        
    }
    
    @Override
    protected void onAttachedToWindow() {
        LogManager.d(TAG, "onAttachedToWindow");
        updateView();
        this.cachePhoto.addPhotoDownloadingChangedEventListener(this);
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        LogManager.d(TAG, "onDetachedFromWindow");
        this.cachePhoto.removePhotoDownloadingChangedEventListener(this);
        super.onDetachedFromWindow();
    }

    @Override
    public void onPhotoDownloadingChanged() {
        updateView();        
    }   
}