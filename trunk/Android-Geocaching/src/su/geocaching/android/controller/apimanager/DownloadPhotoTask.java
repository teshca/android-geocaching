package su.geocaching.android.controller.apimanager;

import java.io.*;
import java.net.URL;

import android.app.ProgressDialog;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
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

    private ProgressDialog progressDialog;
    private InfoActivity infoActivity;

    public DownloadPhotoTask(InfoActivity infoActivity, int cacheId) {
        this.infoActivity = infoActivity;
        this.cacheId = cacheId;
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionsHandler());
    }

    @Override
    protected void onPreExecute() {
        checkSDCard();
        if (externalStorageAvailable && externalStorageWriteable && enoughFreeSpace) {
            progressDialog = new ProgressDialog(infoActivity);
            progressDialog.setMessage(infoActivity.getString(R.string.download_photo_message));
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
            LogManager.w(TAG, "SD Card is available for read and write " + externalStorageAvailable + externalStorageWriteable);
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            externalStorageAvailable = true;
            externalStorageWriteable = false;
            LogManager.w(TAG, "SD Card is available for read " + externalStorageAvailable);
        } else {
            externalStorageAvailable = externalStorageWriteable = false;
            LogManager.w(TAG, "Please insert a SD Card to save your image " + externalStorageAvailable + externalStorageWriteable);
        }
    }

    @Override
    protected Void doInBackground(URL... params) {
        if (externalStorageAvailable && externalStorageWriteable && enoughFreeSpace) {
            if (Controller.getInstance().getConnectionManager().isActiveNetworkConnected()) {
                for (URL url : params) {
                    String fileName = url.getPath().substring(url.getPath().lastIndexOf("/"));
                    Uri uri = Controller.getInstance().getExternalStorageManager().preparePhotoFile(fileName, cacheId);
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

    private boolean downloadAndSavePhoto(URL from, Uri where) throws IOException {

        if (where == null) {
            // TODO
            return false;
        }

        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(from.toString());  //TODO overhead
        OutputStream outputStream = null;

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                LogManager.w(TAG, "Error " + statusCode + " while retrieving bitmap from " + from.toString());
                return false;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    outputStream = infoActivity.getContentResolver().openOutputStream(where);
                    inputStream = new BufferedInputStream(new FlushedInputStream(entity.getContent()), 1024);
                    outputStream = infoActivity.getContentResolver().openOutputStream(where);
                    int size;
                    byte[] buffer = new byte[1024];
                    while ((size = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, size);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or IllegalStateException
            getRequest.abort();
            LogManager.e(TAG, "Error while retrieving bitmap from " + from.toString(), e);
        } finally {
            if (client != null) {
                client.close();
            }
            if (outputStream != null) {
                outputStream.close();
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
        } else if (!externalStorageAvailable) {
            infoActivity.showErrorMessage(R.string.insert_sd_card);
            return;
        } else if (!externalStorageWriteable) {
            infoActivity.showErrorMessage(R.string.impossible_to_save_img);
            return;
        }

        infoActivity.notifyPhotoDownloaded();
        super.onPostExecute(result);
    }

    public static class FlushedInputStream extends FilterInputStream {
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
}
