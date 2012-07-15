package su.geocaching.android.controller.managers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;

import su.geocaching.android.controller.Controller;

public class ExternalStorageManager {

    private static final String TAG = ExternalStorageManager.class.getCanonicalName();
    private static final String OLD_PHOTOS_DIRECTORY = "/Android Geocaching.su/photos";
    private Context context;
    
    private static final FilenameFilter imageFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(".jpg") || name.endsWith(".png"));
        }
    };
    
    private File getBasePhotosDir() {
        return new File(getExternalCacheDir(), "/photos");
    }
    
    private File getExternalCacheDir() {      
        if (android.os.Build.VERSION.SDK_INT >= 8) {
            return context.getExternalCacheDir();
        } else{
            // e.g. "<sdcard>/Android/data/<package_name>/cache/"
            final File extCacheDir = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/" + context.getApplicationInfo().packageName + "/cache/");
            extCacheDir.mkdirs();
            return extCacheDir;
        }        
    }      

    public ExternalStorageManager(Context context){
        this.context = context;
        updatePhotoCacheDirectory();
        prunePhotoCache();
    }
    
    private void updatePhotoCacheDirectory() {
        File oldPhotosDirectory = new File(Environment.getExternalStorageDirectory(), OLD_PHOTOS_DIRECTORY);
        if (oldPhotosDirectory.exists()) {
            oldPhotosDirectory.renameTo(getBasePhotosDir());
            oldPhotosDirectory.getParentFile().delete();
        }
    }

    public void deletePhotos(int cacheId) {
        File photosDirectory = getPhotosDirectory(cacheId);
        deleteMediaDirectory(photosDirectory);
    }

    public void deleteAllPhotos() {
        File photosDirectory = getBasePhotosDir();
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
        Collection<Uri> photos = getPhotos(cacheId);
        return !photos.isEmpty();
    }

    public Collection<Uri> getPhotos(int cacheId) {
        final File imagesDirectory = getPhotosDirectory(cacheId);
        final File[] photoFiles = imagesDirectory.listFiles(imageFilter); 
        final LinkedList<Uri> photosUrls = new LinkedList<Uri>();
        if (photoFiles == null) return photosUrls;
        
        for (File f : imagesDirectory.listFiles(imageFilter)) {
            photosUrls.add(Uri.fromFile(f));
        }
        return photosUrls;
    }
    
    private File getPhotosDirectory(int cacheId) {
        return new File(getBasePhotosDir(), Integer.toString(cacheId));
    }

    public synchronized File getPhotoFile(String fileName, int cacheId) {
        File imagesDirectory = getPhotosDirectory(cacheId);
        if (!imagesDirectory.exists()) {
            if (!imagesDirectory.mkdirs()) return null;
        }
        return new File(imagesDirectory, fileName);
    }
    
    public void prunePhotoCache() {
        File photosDirectory = getBasePhotosDir();
        for (File file : photosDirectory.listFiles()) {
            try {
                int cacheId = Integer.parseInt(file.getName());
                if (Controller.getInstance().getDbManager().isCacheStored(cacheId)) {
                    continue;
                }
                deletePhotos(cacheId);
            } catch(NumberFormatException e) {
                LogManager.w(TAG, e.getMessage());
            }            
        }        
    }
    
    //TODO: Remove?
    /*
    public Uri addFileToMediaFirectory(File file) {        
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        Uri photoUri = context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return photoUri;
    }
    */
    
    public Uri getLocalPhotoUri(URL remoteUrl, int cacheId) {
        String fileName = remoteUrl.getPath().substring(remoteUrl.getPath().lastIndexOf("/"));
        File imagesDirectory = getPhotosDirectory(cacheId);
        File photoFile = new File(imagesDirectory, fileName); 
        return Uri.fromFile(photoFile);
    }
 }
