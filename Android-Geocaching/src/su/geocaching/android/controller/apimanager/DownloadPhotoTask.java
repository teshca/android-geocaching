package su.geocaching.android.controller.apimanager;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Anatoliy Date: 25.04.11 Time: 21:29 To change this template use File | Settings | File Templates.
 */
public class DownloadPhotoTask extends AsyncTask<Void, Integer, List<Bitmap>> {

    private static final String TAG = DownloadPhotoTask.class.getCanonicalName();
    private static final String LINK_PHOTO_PAGE = "http://pda.geocaching.su/pict.php?cid=%d&mode=0";
    private boolean isImage = false;
    private String reviewImageLink;
    public static final String PARSER = "<img src=";
    public static final String LINK_PARSER = "thumbnails/";
    private List<Bitmap> photos;
    private List<URL> thumbnailsPhotoLinks;
    private List<String> fullSizePhotoLinks;
    private File mainDir;
    private File photoDir;
    private int cacheId;
    private ProgressDialog progressDialog;
    private String messageProgress;
    private Context context;
    private String[] imageNames;
    Toast imgagesToast;

    private boolean mExternalStorageAvailable = false;
    private boolean mExternalStorageWriteable = false;

    public DownloadPhotoTask(Context context, int cacheId) {
        this.cacheId = cacheId;
        this.context = context;
        this.messageProgress = "Скачивание фотографий";
    }

    @Override
    protected void onPreExecute() {
        LogManager.d(TAG, "TestTime onPreExecute - Start");

        File dir = new File(Environment.getExternalStorageDirectory(), String.format("/geocaching/photos/%d", cacheId));
        imageNames = dir.list();
        if (photos == null) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(messageProgress);
            progressDialog.show();
        }
    }

    private void checkSDCard() {
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

    // This class definition states that DownloadImageTask will take String
    // parameters, publish Integer progress updates, and return a Bitmap
    @Override
    protected List<Bitmap> doInBackground(Void... paths) {
        URL url;
        String result = null;
        photos = new ArrayList<Bitmap>();
        if (Controller.getInstance().getConnectionManager().isInternetConnected()) {
            try {
                result = getWebText(cacheId);
                thumbnailsPhotoLinks = photoLinks(result);
                try {
                    if (thumbnailsPhotoLinks != null) {
                        for (URL i : thumbnailsPhotoLinks) {
                            url = i;
                            String asd = i.toString();
                            Bitmap bitmap = downloadBitmap(asd);
                            photos.add(bitmap);
                            if (bitmap != null) {
                                LogManager.i(TAG, "Bitmap created");
                            } else {
                                thumbnailsPhotoLinks.remove(i);
                                LogManager.i(TAG, "Bitmap not created");
                            }
                        }
                    }
                    return photos;
                    /*
                     * } catch (MalformedURLException e) { Log.e(TAG, "Malformed exception: " + e.toString()); } catch (IOException e) { Log.e(TAG, "IOException: " + e.toString());
                     */
                } catch (Exception e) {
                    LogManager.e(TAG, "Exception: " + e.toString());
                }
            } catch (IOException e) {
                LogManager.e(TAG, "IOException getWebText", e);
            }
        }
        return photos;
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

    private String getWebText(int id) throws IOException {
        StringBuilder html = new StringBuilder();
        char[] buffer = new char[1024];
        URL url = null;
        url = new URL(String.format(LINK_PHOTO_PAGE, id));
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "windows-1251"));

        html.append(in.readLine());
        html.append(in.readLine());
        html.append(in.readLine().replace("windows-1251", "utf-8"));

        int size;
        while ((size = in.read(buffer)) != -1) {
            html.append(buffer, 0, size);
        }
        return html.toString();
    }

    public List<URL> photoLinks(String downloadedText) {
        thumbnailsPhotoLinks = new ArrayList<URL>();
        fullSizePhotoLinks = new ArrayList<String>();
        checkSDCard();
        if (mExternalStorageAvailable && mExternalStorageWriteable) {
            int index = downloadedText.indexOf(PARSER);
            while (index != -1) {
                int i = index + 9;
                String currentLink = "";
                if (downloadedText.charAt(i) == '"') {
                    i++;
                }
                while (downloadedText.charAt(i) != '"' && downloadedText.charAt(i) != '>') {
                    currentLink += downloadedText.charAt(i);
                    i++;
                }
                int start = currentLink.indexOf(LINK_PARSER);
                String name = currentLink.substring(start + LINK_PARSER.length(), currentLink.length());
                String link = currentLink.substring(0, start).concat(name);
                try {
                    int k = name.indexOf(".");
                    String s = name.substring(0, k);
                    String savedPhotoId = String.format("%d%s", cacheId, s);

                    if (!isPhotoDownloaded(savedPhotoId)) {
                        fullSizePhotoLinks.add(link);
                        thumbnailsPhotoLinks.add(new URL(link));
                    }
                } catch (MalformedURLException e) {
                    LogManager.e(TAG, "MalformedURLException PhotoLinks", e);
                }
                index = downloadedText.indexOf(PARSER, index + 1);
            }

        }
        return thumbnailsPhotoLinks;
    }

    @Override
    protected void onPostExecute(List<Bitmap> result) {

        for (int i = 0; i < result.size(); i++) {
           /* LogManager.e(TAG, String.format("Index = %d", i));
            LogManager.e(TAG, String.format("links.size = %d", thumbnailsPhotoLinks.size()));
            LogManager.e(TAG, String.format("asd.size = %d", fullSizePhotoLinks.size()));
            LogManager.e(TAG, String.format("result.size = %d", result.size()));*/

            String name = fullSizePhotoLinks.get(i).substring(fullSizePhotoLinks.get(i).lastIndexOf("/") + 1);
            if (result != null) {
                boolean b = hasExternalStoragePublicPicture(name);
                if (mExternalStorageAvailable && mExternalStorageWriteable) {
                    saveFile(result.get(i), name, i);
                    isImage = true;
                }
            } else {
                isImage = false;
            }
        }
        progressDialog.dismiss();
    }

    public boolean isPhotoDownloaded(String id) {
        String[] selectionArgs = new String[] { id };
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.MediaColumns._ID + "=?", selectionArgs, null);
        int count = cursor.getCount();
        cursor.close();
        LogManager.e(TAG, String.format("cursor.getCount() = %d", count));
        return count > 0;
    }

    private void saveFile(Bitmap bitmap, String name, int i) {
        String filename = name;
        ContentValues values = new ContentValues();
        File sdImageMainDirectory = new File(Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.main_directory));
        sdImageMainDirectory.mkdirs();
        mainDir = sdImageMainDirectory;
        File sdImagePhotoDirectory = new File(sdImageMainDirectory, context.getResources().getString(R.string.photo_directory));
        sdImagePhotoDirectory.mkdirs();
        photoDir = sdImagePhotoDirectory;
        File sdImageCacheDirectory = new File(sdImagePhotoDirectory, String.format("%d", cacheId));
        sdImageCacheDirectory.mkdirs();
        int d = filename.indexOf(".");
        String s = filename.substring(0, d);
        String id = String.format("%d%s", cacheId, s);
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

    private boolean hasExternalStoragePublicPicture(String name) {
        File sdImageMainDirectory = new File(Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.main_directory));
        File sdImagePhotoDirectory = new File(sdImageMainDirectory, context.getResources().getString(R.string.photo_directory));
        sdImagePhotoDirectory.mkdirs();
        File sdImageCacheDirectory = new File(sdImagePhotoDirectory, String.format("%d", cacheId));
        sdImageCacheDirectory.mkdirs();
        File file = new File(sdImageMainDirectory, name);
        if (file != null) {
            file.delete();
        }
        return file.exists();
    }

}
