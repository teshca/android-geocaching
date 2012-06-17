package su.geocaching.android.controller.managers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;

/**
 * This class contains method for working with database.
 *
 * @author Alekseenko Vladimir
 */
public class DbManager extends SQLiteOpenHelper {
    private static final String TAG = DbManager.class.getCanonicalName();

    // Name, version and name table
    private static final String DATABASE_NAME_BASE = "CacheBase.db";
    private static final String DATABASE_NAME_TABLE = "cache";
    private static final String DATABASE_CHECKPOINT_NAME_TABLE = "chekpoints";
    private static final int DATABASE_VERSION = 5;
    // Name column database
    private static final String COLUMN_ID = "cid";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_WEB_TEXT = "text";
    private static final String COLUMN_NOTEBOOK_TEXT = "notetext";
    private static final String COLUMN_LON = "longtitude";
    private static final String COLUMN_LAT = "lantitude";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_USER_NOTES = "user_notes";
    private static final String COLUMN_PHOTOS = "photos";
    private static final String CACHE_ID = "cache_id";
    private static final String CHECKPOINT_ID = "checkpoint_id";
    
    private static final String PHOTO_URL_DEVIDER = "; ";

    private SQLiteDatabase db;

    private static final String SQL_CREATE_DATABASE_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s STRING, %s STRING, %s STRING, %s STRING);",
            DATABASE_NAME_TABLE, COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, COLUMN_WEB_TEXT, COLUMN_NOTEBOOK_TEXT, COLUMN_USER_NOTES, COLUMN_PHOTOS);
    private static final String SQL_CREATE_DATABASE_CHECKPOINT_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER);", 
            DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_ID, CACHE_ID, CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS);

    public DbManager(Context context) {
        super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            // Create tables
            db.execSQL(SQL_CREATE_DATABASE_TABLE);
            db.execSQL(SQL_CREATE_DATABASE_CHECKPOINT_TABLE);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            LogManager.e(TAG, e.toString(), e);
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.beginTransaction();
            try {
                db.execSQL(String.format("ALTER TABLE %s ADD %s STRING;", DATABASE_NAME_TABLE, COLUMN_NOTEBOOK_TEXT));
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                LogManager.e(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion < 3) {
            db.beginTransaction();
            try {
                db.execSQL(SQL_CREATE_DATABASE_CHECKPOINT_TABLE);
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                LogManager.e(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion < 4) {
            db.beginTransaction();
            try {
                db.execSQL(String.format("ALTER TABLE %s ADD %s STRING;", DATABASE_NAME_TABLE, COLUMN_USER_NOTES));
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                LogManager.e(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
            }
        }
        if (oldVersion < 5) {
            db.beginTransaction();
            try {
                db.execSQL(String.format("ALTER TABLE %s ADD %s STRING;", DATABASE_NAME_TABLE, COLUMN_PHOTOS));
                db.setTransactionSuccessful();
            } catch (SQLException e) {
                LogManager.e(TAG, e.toString(), e);
            } finally {
                db.endTransaction();
            }
        }        
    }

    /**
     * @param geoCacheForAdd  GeoCache for add in database
     * @param webText         html text for description GeoCache
     * @param webNotebookText text for web notebook
     */
    public void addGeoCache(GeoCache geoCacheForAdd, String webText, String webNotebookText, Collection<URL> photos) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, geoCacheForAdd.getId());
        values.put(COLUMN_NAME, geoCacheForAdd.getName());
        values.put(COLUMN_STATUS, geoCacheForAdd.getStatus().ordinal());
        values.put(COLUMN_TYPE, geoCacheForAdd.getType().ordinal());
        values.put(COLUMN_LAT, geoCacheForAdd.getLocationGeoPoint().getLatitudeE6());
        values.put(COLUMN_LON, geoCacheForAdd.getLocationGeoPoint().getLongitudeE6());
        values.put(COLUMN_WEB_TEXT, webText);
        if (photos != null) {
            values.put(COLUMN_PHOTOS, TextUtils.join(PHOTO_URL_DEVIDER, photos));
        }
        if (webNotebookText != null) {
            values.put(COLUMN_NOTEBOOK_TEXT, webNotebookText);
        }
        db.insert(DATABASE_NAME_TABLE, null, values);
    }

    /**
     * @param checkpoint GeoCache for add in database
     */
    public void addCheckpointGeoCache(GeoCache checkpoint, int cacheId) {
        LogManager.d(TAG, "addCheckpointGeoCache " + checkpoint.getId());
        ContentValues values = new ContentValues();
        values.put(CACHE_ID, cacheId);
        values.put(CHECKPOINT_ID, checkpoint.getId());
        values.put(COLUMN_NAME, checkpoint.getName());
        values.put(COLUMN_LAT, checkpoint.getLocationGeoPoint().getLatitudeE6());
        values.put(COLUMN_LON, checkpoint.getLocationGeoPoint().getLongitudeE6());
        values.put(COLUMN_STATUS, checkpoint.getStatus().ordinal());

        db.insert(DATABASE_CHECKPOINT_NAME_TABLE, null, values);
    }

    /**
     * @param id ID GeoCache for taking from database
     * @return GeoCache if database have GeoCache. Null if database haven't GeoCache
     */
    public GeoCache getCacheByID(int id) {
        Cursor cur = db.rawQuery(String.format("select %s,%s,%s,%s,%s from %s where %s=%d", COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE, COLUMN_ID, id), null);
        if (cur.getCount() == 0) {
            cur.close();
            return null;
        }
        cur.moveToFirst();
        GeoCache cache = new GeoCache();
        cache.setId(id);

        cache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
        cache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));
        cache.setStatus(GeoCacheStatus.values()[cur.getInt(cur.getColumnIndex(COLUMN_STATUS))]);
        cache.setType(GeoCacheType.values()[cur.getInt(cur.getColumnIndex(COLUMN_TYPE))]);

        cur.close();
        return cache;
    }

    /**
     * @return ArrayList GeoCaches in database. Null if in database haven't GeoCache
     */
    public ArrayList<GeoCache> getFavoritesGeoCaches() {
        ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
        Cursor cur = db.rawQuery(String.format("select %s,%s,%s,%s,%s,%s from %s", COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE), null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {

            GeoCache geocache = new GeoCache();
            geocache.setId(cur.getInt(cur.getColumnIndex(COLUMN_ID)));
            geocache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            geocache.setStatus(GeoCacheStatus.values()[cur.getInt(cur.getColumnIndex(COLUMN_STATUS))]);
            geocache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));
            geocache.setType(GeoCacheType.values()[cur.getInt(cur.getColumnIndex(COLUMN_TYPE))]);
            exitCollection.add(geocache);

            cur.moveToNext();
        }

        cur.close();
        return exitCollection;
    }

    /**
     * @param id id of GeoCache
     * @return list of checkpoints corresponding to a given cache
     */
    public ArrayList<GeoCache> getCheckpointsArrayById(int id) {
        LogManager.d(TAG, "getCheckpointsArrayById " + id);
        ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
        Cursor cursor = db.rawQuery(
                String.format("SELECT %s,%s,%s,%s,%s FROM %s WHERE %s=%d", CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS, DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id), null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            GeoCache geocache = new GeoCache();
            geocache.setId(cursor.getInt(cursor.getColumnIndex(CHECKPOINT_ID)));
            geocache.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            geocache.setLocationGeoPoint(new GeoPoint(cursor.getInt(cursor.getColumnIndex(COLUMN_LAT)), cursor.getInt(cursor.getColumnIndex(COLUMN_LON))));
            geocache.setType(GeoCacheType.CHECKPOINT);
            geocache.setStatus(GeoCacheStatus.values()[cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS))]);
            exitCollection.add(geocache);

            cursor.moveToNext();
        }

        cursor.close();
        return exitCollection;
    }

    /**
     * @param id ID GeoCache for taking his html description
     * @return String if GeoCache in database. Empty string if in database haven't GeoCache
     */
    public String getCacheInfoById(int id) {
        String exitString = null;
        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_WEB_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            exitString = cursor.getString(cursor.getColumnIndex(COLUMN_WEB_TEXT));
        }
        cursor.close();

        return exitString;
    }

    public String getCacheNotebookTextById(int id) {
        String exitString = null;
        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_NOTEBOOK_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            exitString = cursor.getString(cursor.getColumnIndex(COLUMN_NOTEBOOK_TEXT));
        }
        cursor.close();

        return exitString;
    }

    public String getNoteById(int id) {
        String exitString = null;
        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_USER_NOTES, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            exitString = cursor.getString(cursor.getColumnIndex(COLUMN_USER_NOTES));
        }
        cursor.close();
        return exitString;
    }
    
    public Collection<URL> getCachePhotosById(int id) {
        ArrayList<URL> photosUrl = null;

        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_PHOTOS, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            String photos = cursor.getString(cursor.getColumnIndex(COLUMN_PHOTOS));
            if (photos != null) {
                photosUrl = new ArrayList<URL>();
                for (String url : photos.split(PHOTO_URL_DEVIDER)) {
                    try {
                        photosUrl.add(new URL(url));
                    } catch (MalformedURLException e) {
                        LogManager.e(TAG, e);
                    }
                }
            }
        }
        cursor.close();        

        return photosUrl;
    }

    /**
     * @param cacheId      id of Searching GeoCache
     * @param checkpointId id of checkpoint
     * @param status       checkpoint status
     */
    public void updateCheckpointCacheStatus(int cacheId, int checkpointId, GeoCacheStatus status) {
        db.execSQL(String.format("UPDATE %s SET %s=%d WHERE %s=%d AND %s=%d", DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_STATUS, status.ordinal(), CACHE_ID, cacheId, CHECKPOINT_ID, checkpointId));
    }

    /**
     * Remove geocache from DB, also remove all checkpoints corresponding to a geocache
     *
     * @param id ID geocache for delete from database
     */
    public void deleteCacheById(int id) {
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_NAME_TABLE, COLUMN_ID, id));
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id));
        Controller.getInstance().getExternalStorageManager().deletePhotos(id);
    }

    /**
     * @param cacheId id of parent geocache
     * @param checkpointId checkpoint id for delete from database
     */
    public void deleteCheckpointCache(int cacheId, int checkpointId) {
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d AND %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, cacheId, CHECKPOINT_ID, checkpointId));
    }

    public void deleteCheckpointCache(int id) {
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id));
    }

    public void updateNotebookText(int cacheId, String htmlNotebookText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTEBOOK_TEXT, htmlNotebookText);
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
    }

    public void updateInfoText(int cacheId, String htmlInfoText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEB_TEXT, htmlInfoText);
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
    }

    public void updateNotes(int cacheId, String note) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NOTES, note);
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
    }
    
    public void updatePhotos(int cacheId, Collection<URL> photos) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTOS, TextUtils.join(PHOTO_URL_DEVIDER, photos));
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
    }

    public boolean isCacheStored(int id) {
        String[] selectionArgs = new String[]{Integer.toString(id)};
        Cursor cursor = db.query(DATABASE_NAME_TABLE, null, COLUMN_ID + "=?", selectionArgs, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        LogManager.d(TAG, "isCacheStored id=" + id + " " + (count > 0));
        return count > 0;
    }

    public void clearDB() {
        LogManager.d(TAG, "clearDB");
        db.delete(DATABASE_NAME_TABLE, null, null);
        db.delete(DATABASE_CHECKPOINT_NAME_TABLE, null, null);
        Controller.getInstance().getExternalStorageManager().deleteAllPhotos();
    }
}
