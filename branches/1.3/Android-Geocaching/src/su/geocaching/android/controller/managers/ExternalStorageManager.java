package su.geocaching.android.controller.managers;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

public class ExternalStorageManager {

    private static final String TAG = ExternalStorageManager.class.getCanonicalName();
    private static final String APPLICATION_DIRECTORY = "/Android Geocaching.su";
    private static final String PHOTOS_DIRECTORY = APPLICATION_DIRECTORY + "/photos";
    private static final String CACHE_PHOTOS_DIRECTORY = PHOTOS_DIRECTORY + "/%d";
    private Context context;

    public ExternalStorageManager(Context context){
        this.context = context;
    }
    
    public void deletePhotos(int cacheId) {
        File photosDirectory = getPhotosDirectory(cacheId);
        deleteMediaDirectory(photosDirectory);
    }

    public void deleteAllPhotos() {
        File photosDirectory = new File(Environment.getExternalStorageDirectory(), PHOTOS_DIRECTORY);
        deleteMediaDirectory(photosDirectory);
    }

    private void deleteMediaDirectory(File photosDirectory) {
        try {
            context.getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    String.format("%s like '%s%%'",  MediaStore.Images.Media.DATA, photosDirectory.getAbsolutePath()),
                    null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        deleteRecursive(photosDirectory);
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public boolean hasPhotos(int cacheId) {
        File[] photos = getPhotos(cacheId);
        return (photos != null) && (photos.length != 0);
    }

    public File[] getPhotos(int cacheId) {
        File imagesDirectory = getPhotosDirectory(cacheId);
        FilenameFilter imageFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".png"));
            }
        };
        return imagesDirectory.listFiles(imageFilter);
    }
    
    private File getPhotosDirectory(int cacheId)
    {
        return new File(Environment.getExternalStorageDirectory(), String.format(CACHE_PHOTOS_DIRECTORY, cacheId));
    }

    public Uri preparePhotoFile(String fileName, int cacheId) {
        File imagesDirectory = getPhotosDirectory(cacheId);
        if (!imagesDirectory.exists())
        {
            boolean created = imagesDirectory.mkdirs();
            //TODO we should check created and correctly process situation when folder don't created
        }
        File outputFile = new File(imagesDirectory, fileName);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outputFile.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        return context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}
