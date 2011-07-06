package su.geocaching.android.controller.apimanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.widget.Toast;

public class DownloadPhotoTask extends AsyncTask<URL, Void, Void> {

    private static final String TAG = DownloadPhotoTask.class.getCanonicalName();
    private static final int neededFreeSdSpace = 1572864; // bytes 1,5mb

    private int cacheId;
    private boolean externalStorageAvailable;
    private boolean externalStorageWriteable;
    private boolean enoughFreeSpace;

    private Context context;
    private ProgressDialog progressDialog;

    public DownloadPhotoTask(Context context, int cacheId) {
        this.cacheId = cacheId;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");

        checkSDCard();
        if (externalStorageAvailable && externalStorageWriteable && enoughFreeSpace) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.download_photo_message));
            progressDialog.show();
        } else if (externalStorageAvailable) {
            Toast.makeText(context, context.getString(R.string.impossible_to_save_img), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.insert_sd_card), Toast.LENGTH_LONG).show();
        }
    }

    private void checkSDCard() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        enoughFreeSpace = sdAvailSize > neededFreeSdSpace;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            externalStorageAvailable = externalStorageWriteable = true;
            LogManager.v(TAG, "SD Card is available for read and write " + externalStorageAvailable + externalStorageWriteable);
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
            externalStorageWriteable = false;
            LogManager.v(TAG, "SD Card is available for read " + externalStorageAvailable);
        } else {
            externalStorageAvailable = externalStorageWriteable = false;
            LogManager.v(TAG, "Please insert a SD Card to save your image " + externalStorageAvailable + externalStorageWriteable);
        }
    }

    @Override
    protected Void doInBackground(URL... params) {
        if (externalStorageAvailable && externalStorageWriteable && enoughFreeSpace) {
            if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
                for (URL url : params) {
                    try {
                        downloadAndSavePhoto(url);
                    } catch (IOException e) {
                        LogManager.e(TAG, e.getMessage(), e);
                    }
                }
            }
        }
        return null;
    }

    private void downloadAndSavePhoto(URL photoURL) throws IOException {

        String filename = photoURL.toString().substring(photoURL.toString().lastIndexOf('/') + 1);
        ContentValues values = new ContentValues();
        File sdImageMainDirectory = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.main_directory));
        sdImageMainDirectory.mkdirs();
        File sdImagePhotoDirectory = new File(sdImageMainDirectory, context.getString(R.string.photo_directory));
        sdImagePhotoDirectory.mkdirs();
        File sdImageCacheDirectory = new File(sdImagePhotoDirectory, String.format("%d", cacheId));
        sdImageCacheDirectory.mkdirs();
        int dotIndex = filename.indexOf(".");
        String nameWithoutExtent = filename.substring(0, dotIndex);
        String id = String.format("%d%s", cacheId, nameWithoutExtent);
        File outputFile = new File(sdImageCacheDirectory, filename);
        values.put(MediaStore.MediaColumns.DATA, outputFile.toString());
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns._ID, id);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        Uri uri = context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        OutputStream outStream = null;
        BufferedInputStream bis = null;
        try {
            URLConnection conection = photoURL.openConnection();
            bis = new BufferedInputStream(conection.getInputStream(), 1024);
            outStream = context.getContentResolver().openOutputStream(uri);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = bis.read(buffer)) != -1) {
                outStream.write(buffer, 0, size);
            }
            outStream.close();
            bis.close();
        } catch (FileNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
        } finally {
            if (outStream != null) {
                outStream.close();
            }
            if (bis != null) {
                bis.close();
            }

        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (!enoughFreeSpace && externalStorageAvailable && externalStorageWriteable) {
            Toast.makeText(context, context.getString(R.string.no_free_space), Toast.LENGTH_LONG).show();
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onPostExecute(result);
    }
}
