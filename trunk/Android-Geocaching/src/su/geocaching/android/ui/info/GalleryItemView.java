package su.geocaching.android.ui.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;

/**
 * @author Nickolay Artamonov
 */
public class GalleryItemView extends FrameLayout implements GeoCachePhotoDownloadingChangedListener {

    private static final String TAG = GalleryItemView.class.getCanonicalName();

    private GeoCachePhotoViewModel cachePhoto;
    private final int thumbnailsPhotoSize;
    
    private ProgressBar progressBar;
    private ImageView image;
    private View errorMessage;
    
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
        // TODO: Try BitmapFactory.decodeStream with FlushedInputStream
        return BitmapFactory.decodeFile(path, scaleOptions);
    }
    
    // http://code.google.com/p/android/issues/detail?id=6066
    /*
    private static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
    */    
          
    private void updateView() {
        errorMessage.setVisibility(GONE);  
        progressBar.setVisibility(GONE);
        image.setVisibility(GONE);
        
        if (this.cachePhoto.IsDownloading()) {    
            progressBar.setVisibility(VISIBLE);
        } else {
            if (this.cachePhoto.HasErrors()) {
                errorMessage.setVisibility(VISIBLE); 
            } else {
                progressBar.setVisibility(VISIBLE);
                Handler handler = new Handler();
                Runnable r = new Runnable()
                {
                    public void run() 
                    {
                        Bitmap bitmap = scaleBitmap(cachePhoto);
                        if (bitmap != null) {
                            image.setVisibility(VISIBLE);
                            image.setImageBitmap(bitmap);    
                        } else {
                            errorMessage.setVisibility(VISIBLE);
                        }
                        progressBar.setVisibility(GONE);
                    }
                };
                handler.post(r);           
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