package su.geocaching.android.controller.managers;

import java.io.File;
import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;

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
    private static final int DATABASE_VERSION = 4;
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
    private static final String CACHE_ID = "cache_id";
    private static final String CHECKPOINT_ID = "checkpoint_id";

    private SQLiteDatabase db;
    private Context context;

    private static final String SQL_CREATE_DATABASE_TABLE = String.format("CREATE TABLE %s (%s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s STRING, %s STRING, %s STRING);",
            DATABASE_NAME_TABLE, COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, COLUMN_WEB_TEXT, COLUMN_NOTEBOOK_TEXT, COLUMN_USER_NOTES);
    private static final String SQL_CREATE_DATABASE_CHECKPOINT_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER);", DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_ID, CACHE_ID,
            CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS);

    /**
     * @param context this activivty
     */
    public DbManager(Context context) {
        super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
        this.context = context;
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
                db.execSQL(String.format("ALTER TABLE %s ADD %s string;", DATABASE_NAME_TABLE, COLUMN_NOTEBOOK_TEXT));
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
                db.execSQL(String.format("ALTER TABLE %s ADD %s string;", DATABASE_NAME_TABLE, COLUMN_USER_NOTES));
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
    public void addGeoCache(GeoCache geoCacheForAdd, String webText, String webNotebookText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, geoCacheForAdd.getId());
        values.put(COLUMN_NAME, geoCacheForAdd.getName());
        values.put(COLUMN_STATUS, geoCacheForAdd.getStatus().ordinal());
        values.put(COLUMN_TYPE, geoCacheForAdd.getType().ordinal());
        values.put(COLUMN_LAT, geoCacheForAdd.getLocationGeoPoint().getLatitudeE6());
        values.put(COLUMN_LON, geoCacheForAdd.getLocationGeoPoint().getLongitudeE6());
        values.put(COLUMN_WEB_TEXT, webText);
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
    public ArrayList<GeoCache> getArrayGeoCache() {
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
        deletePhotos(id);
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
    }


  public void deletePhotos(int cacheId) {
    File images = new File(Environment.getExternalStorageDirectory(), String.format(context.getString(R.string.cache_directory), cacheId));
    if (images == null || images.list() == null) {
      // TODO need some message
      return;
    }

    for (File f : images.listFiles()) {
      try {
        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.format("%s=\"%s\"", MediaStore.Images.Media.DATA, f.toString()), null);
      } catch (Exception e) {
        Log.e(TAG, e.getMessage(), e);
      }
    }
    images.delete();
  }
}
