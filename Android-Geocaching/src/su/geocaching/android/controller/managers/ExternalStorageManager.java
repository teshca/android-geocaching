package su.geocaching.android.controller.managers;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
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
    
    public boolean isExternalStorageAvailable() {        
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }    
    
    private File getExternalFilesDir() {      
        if (android.os.Build.VERSION.SDK_INT >= 8) {
            return context.getExternalFilesDir(null);
        } else{
            // e.g. "<sdcard>/Android/data/<package_name>/files/"
            final File extCacheDir = new File(Environment.getExternalStorageDirectory(),
                "/Android/data/" + context.getApplicationInfo().packageName + "/files/");
            extCacheDir.mkdirs();
            return extCacheDir;
        }        
    } 
    
    private File getBasePhotosDir() {
        return new File(getExternalFilesDir(), "/photos");
    }
    
    public ExternalStorageManager(Context context){
        this.context = context;
        updatePhotoCacheDirectory();
        prunePhotoCache();
    }
    
    private void updatePhotoCacheDirectory() {
        File oldPhotosDirectory = new File(Environment.getExternalStorageDirectory(), OLD_PHOTOS_DIRECTORY);
        // this only executes once after update
        if (oldPhotosDirectory.exists()) {
            oldPhotosDirectory.renameTo(getBasePhotosDir());
            oldPhotosDirectory.getParentFile().delete();
            // now we need to update database. add information about photos that already downloaded
            File photosDirectory = getBasePhotosDir();
            File[] cacheDirs = photosDirectory.listFiles();
            if (cacheDirs != null) {
                for (File cacheDir : cacheDirs) {
                    try {
                        int cacheId = Integer.parseInt(cacheDir.getName());
                        if (Controller.getInstance().getDbManager().isCacheStored(cacheId)) {
                            Collection<URL> photoUrls = new LinkedList<URL>();
                            File[] photoFiles = cacheDir.listFiles();
                            if (photoFiles != null) {
                                for (File photoFile : photoFiles) {
                                    String fileName = photoFile.getName();
                                    String subfolder = fileName.startsWith(Integer.toString(cacheId)) ? "caches/" : "areas/";
                                    try {
                                        photoUrls.add(new URL("http://www.geocaching.su/photos/" + subfolder + photoFile.getName()));
                                    } catch (MalformedURLException e) {
                                        LogManager.e(TAG, e);
                                    }
                                }
                            }
                            Controller.getInstance().getDbManager().updatePhotos(cacheId, photoUrls);
                        } else {
                            deletePhotos(cacheId);
                        }
                    } catch(NumberFormatException e) {
                        LogManager.e(TAG, e);
                    }
                }
            }
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
        if (fileOrDirectory.isDirectory()) {
            File[] children = fileOrDirectory.listFiles();
            if (children != null) {
                for (File child : children)
                    deleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }

    public Collection<Uri> getPhotos(int cacheId) {
        final File imagesDirectory = getPhotosDirectory(cacheId);
        final File[] photoFiles = imagesDirectory.listFiles(imageFilter); 
        final LinkedList<Uri> photosUrls = new LinkedList<Uri>();
        if (photoFiles != null) {
            for (File f : photoFiles) {
                photosUrls.add(Uri.fromFile(f));
            }            
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
        File[] photoFiles = photosDirectory.listFiles();
        if (photoFiles != null) {
            for (File file : photoFiles) {
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
    }
    
    //TODO: Remove?
    /*
    public Uri addFileToMediaFirectory(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.MediaColumns.TITLE, file.getName());
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        
        values.put(Images.ImageColumns.BUCKET_ID, file.getParent().hashCode());
        values.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getParent().getName());
        
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
