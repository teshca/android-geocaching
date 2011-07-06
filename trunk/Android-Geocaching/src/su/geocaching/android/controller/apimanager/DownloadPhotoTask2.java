package su.geocaching.android.controller.apimanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.widget.Toast;

public class DownloadPhotoTask2 extends AsyncTask<URL, Void, Void> {

    private static final String TAG = DownloadPhotoTask2.class.getCanonicalName();

    private int cacheId;
    private Context context;
    private ProgressDialog progressDialog;

    // TODO this must be refactored
    private boolean mExternalStorageAvailable;
    private boolean mExternalStorageWriteable;

    private double megaAvailable;
    private static final double neededFreeSdSpace = 1.5; // megabytes

    private static final String NO_FREE_SPACE_MESSAGE = "Недостаточно места на SD карте";
    private static final String IMPOSSIBLE_TO_SAVE_IMG_MESSAGE = "Невозможно сохранить фотографии";
    private static final String INSERT_SD_CARD_MESSAGE = "Вставте SD карту";

    public DownloadPhotoTask2(Context context, int cacheId) {
        this.cacheId = cacheId;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");

        String messageProgress = context.getString(R.string.download_photo_message);
        checkSDCard();
        progressDialog = new ProgressDialog(context);
        if (mExternalStorageAvailable && mExternalStorageWriteable && megaAvailable >= neededFreeSdSpace) {
            progressDialog.setMessage(messageProgress);
            progressDialog.show();
        } else if (mExternalStorageAvailable) {
            Toast.makeText(context, IMPOSSIBLE_TO_SAVE_IMG_MESSAGE, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, INSERT_SD_CARD_MESSAGE, Toast.LENGTH_LONG).show();
        }
    }

    private void checkSDCard() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks() * (double) stat.getBlockSize(); // TODOsdAvailSize=0.0 !??
        // One binary megabyte equals 1,048,576 bytes.
        megaAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            LogManager.v(TAG, "SD Card is available for read and write " + mExternalStorageAvailable + mExternalStorageWriteable);
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
            LogManager.v(TAG, "SD Card is available for read " + mExternalStorageAvailable);
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
            LogManager.v(TAG, "Please insert a SD Card to save your image " + mExternalStorageAvailable + mExternalStorageWriteable);
        }
    }

    @Override
    protected Void doInBackground(URL... params) {
        if (mExternalStorageAvailable && mExternalStorageWriteable && megaAvailable >= neededFreeSdSpace) {
            if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
                for (URL url : params) {
                    Bitmap bitmap = downloadBitmap(url.toString());
                    saveFile(bitmap, url.getFile());
                }
            }
        }

        return null;
    }

    static Bitmap downloadBitmap(String url) {
        final DefaultHttpClient client = new DefaultHttpClient();
        final HttpGet getRequest = new HttpGet(url);

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LogManager.e(TAG, "Error " + statusCode + " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
            if (entity != null) {
                InputStream inputStream = null;
                Bitmap bitmap = null;
                try {
                    inputStream = bufHttpEntity.getContent();
                    // long a = bufHttpEntity.getContentLength();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } catch (Exception e) {
                    getRequest.abort();
                    LogManager.e(TAG, "Error while decoding bitmap ");

                } finally {
                    if (inputStream != null) {

                        inputStream.close();
                    }
                    entity.consumeContent();
                    /*
                     * if (bitmap == null) { BitmapFactory.Options o2 = new BitmapFactory.Options(); o2.inSampleSize = 2; return BitmapFactory.decodeStream(inputStream, null, o2); }
                     */
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            LogManager.e(TAG, "Error while retrieving bitmap from " + url + e.toString());
        }
        return null;
    }

    private void saveFile(Bitmap bitmap, String name) {
        String filename = name;
        ContentValues values = new ContentValues();
        File sdImageMainDirectory = new File(Environment.getExternalStorageDirectory(), context.getString(R.string.main_directory));
        sdImageMainDirectory.mkdirs();
        File sdImagePhotoDirectory = new File(sdImageMainDirectory, context.getString(R.string.photo_directory));
        sdImagePhotoDirectory.mkdirs();
        File sdImageCacheDirectory = new File(sdImagePhotoDirectory, String.format("%d", cacheId));
        sdImageCacheDirectory.mkdirs();
        int dotIndex = name.indexOf(".");
        String nameWithoutExtent = name.substring(0, dotIndex);
        String id = String.format("%d%s", cacheId, nameWithoutExtent);
        File outputFile = new File(sdImageCacheDirectory, filename);
        values.put(MediaStore.MediaColumns.DATA, outputFile.toString());
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns._ID, id);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
        Uri uri = context.getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

        values);
        try {
            OutputStream outStream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (megaAvailable < neededFreeSdSpace && mExternalStorageAvailable && mExternalStorageWriteable) {
            Toast.makeText(context, NO_FREE_SPACE_MESSAGE, Toast.LENGTH_LONG).show();
        }
        progressDialog.dismiss();
        super.onPostExecute(result);
    }
}
