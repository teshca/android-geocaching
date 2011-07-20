package su.geocaching.android.controller.apimanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.UncaughtExceptionsHandler;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.info.InfoActivity;

/**
 * This AsyncTask for downloading photo of geocache
 *
 * @author Nikita Bumakov
 */
public class DownloadPhotoTask extends AsyncTask<URL, Void, Void> {

    private static final String TAG = DownloadPhotoTask.class.getCanonicalName();
    private static final int neededFreeSdSpace = 1572864; // bytes 1,5mb //TODO we mast get real space

    private int cacheId;
    private boolean externalStorageAvailable, externalStorageWriteable, enoughFreeSpace;

    private Context context;
    private ProgressDialog progressDialog;
    private InfoActivity infoActivity;

    public DownloadPhotoTask(Context context, InfoActivity infoActivity, int cacheId) {
        this.context = context;
        this.infoActivity = infoActivity;
        this.cacheId = cacheId;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");

        checkSDCard();
        if (externalStorageAvailable && externalStorageWriteable && enoughFreeSpace) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.download_photo_message));
            progressDialog.show();
        }
    }

    private void checkSDCard() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize();
        enoughFreeSpace = sdAvailSize >= neededFreeSdSpace;

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
                    Uri uri = prepareFile(url);
                    boolean success = false;
                    for (int attempt = 0; attempt < 5 && !success; attempt++)
                        try {
                            success = downloadAndSavePhoto(url, uri);
                        } catch (IOException e) {
                            LogManager.e(TAG, e.getMessage(), e);
                        }
                }
            }
        }
        return null;
    }

    private Uri prepareFile(URL photoURL) {
        File sdImageMainDirectory = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.main_directory));
        File sdImagePhotoDirectory = new File(sdImageMainDirectory, context.getString(R.string.photo_directory));
        File sdImageCacheDirectory = new File(sdImagePhotoDirectory, Integer.toString(cacheId));
        sdImageCacheDirectory.mkdirs();  //TODO we should catch exceptions and correctly process situation when folder don't created

        String filename = photoURL.toString().substring(photoURL.toString().lastIndexOf('/') + 1);
        File outputFile = new File(sdImageCacheDirectory, filename);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, outputFile.toString());
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        return context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private boolean downloadAndSavePhoto(URL from, Uri where) throws IOException {

        if (where == null) {
            // TODO
            return false;
        }

        OutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        try {
            URLConnection connection = from.openConnection();
            inputStream = new BufferedInputStream(connection.getInputStream(), 1024);
            outputStream = context.getContentResolver().openOutputStream(where);
            int size;
            byte[] buffer = new byte[1024];
            while ((size = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, size);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            LogManager.e(TAG, e.getMessage(), e);
            return false;
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return true;
    }

    @Override
    protected void onPostExecute(Void result) {

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (!enoughFreeSpace && externalStorageAvailable && externalStorageWriteable) {
            infoActivity.showErrorMessage(R.string.no_free_space);
            return;
        } else
        if (!externalStorageAvailable) {
            infoActivity.showErrorMessage(R.string.insert_sd_card);
            return;
        } else
        if (!externalStorageWriteable) {
            infoActivity.showErrorMessage(R.string.impossible_to_save_img);
            return;
        }

        infoActivity.showPhoto();
        super.onPostExecute(result);
    }
}
