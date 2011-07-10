package su.geocaching.android.controller.managers;

import java.util.ArrayList;

import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.maps.GeoPoint;

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

    private SQLiteDatabase db = null;

    private static final String SQL_CREATE_DATABASE_TABLE = String.format("CREATE TABLE %s (%s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER, %s INTEGER, %s STRING, %s STRING, %s STRING);",
            DATABASE_NAME_TABLE, COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, COLUMN_WEB_TEXT, COLUMN_NOTEBOOK_TEXT, COLUMN_USER_NOTES);
    private static final String SQL_CREATE_DATABASE_CHECKPOINT_TABLE = String.format(
            "CREATE TABLE %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s STRING, %s INTEGER, %s INTEGER, %s INTEGER);", DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_ID, CACHE_ID,
            CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS);

    /**
     * @param context
     *            this activivty
     */
    public DbManager(Context context) {
        super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
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
     * @param geoCacheForAdd
     *            GeoCache for add in database
     * @param webText
     *            html text for description GeoCache
     * @param webNotebookText
     *            text for web notebook
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
        openWritableDB();
        db.insert(DATABASE_NAME_TABLE, null, values);
        closeDB();
    }

    /**
     * @param checkpoint
     *            GeoCache for add in database
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

        openWritableDB();
        db.insert(DATABASE_CHECKPOINT_NAME_TABLE, null, values);
        closeDB();
    }

    /**
     * @param id
     *            ID GeoCache for taking from database
     * @return GeoCache if database have GeoCache. Null if database haven't GeoCache
     */
    public GeoCache getCacheByID(int id) {
        openReadableDB();
        Cursor cur = db.rawQuery(String.format("select %s,%s,%s,%s,%s from %s where %s=%d", COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE, COLUMN_ID, id), null);
        if (cur.getCount() == 0) {
            cur.close();
            closeDB();
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
        closeDB();
        return cache;
    }

    /**
     * @return ArrayList GeoCaches in database. Null if in database haven't GeoCache
     */
    public ArrayList<GeoCache> getArrayGeoCache() {
        ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
        openReadableDB();
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
        closeDB();
        return exitCollection;
    }

    /**
     * @param id
     *            id of GeoCache
     * @return list of checkpoints corresponding to a given cache
     */
    public ArrayList<GeoCache> getCheckpointsArrayById(int id) {
        LogManager.d(TAG, "getCheckpointsArrayById " + id);
        ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
        openReadableDB();
        Cursor cur = db.rawQuery(
                String.format("SELECT %s,%s,%s,%s,%s FROM %s WHERE %s=%d", CHECKPOINT_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_STATUS, DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id), null);

        cur.moveToFirst();

        while (!cur.isAfterLast()) {
            GeoCache geocache = new GeoCache();
            geocache.setId(cur.getInt(cur.getColumnIndex(CHECKPOINT_ID)));
            geocache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
            geocache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));
            geocache.setType(GeoCacheType.CHECKPOINT);
            geocache.setStatus(GeoCacheStatus.values()[cur.getInt(cur.getColumnIndex(COLUMN_STATUS))]);
            exitCollection.add(geocache);

            cur.moveToNext();
        }

        cur.close();
        closeDB();
        return exitCollection;
    }

    /**
     * @param id
     *            ID GeoCache for taking his html description
     * @return String if GeoCache in database. Empty string if in database haven't GeoCache
     */
    public String getCacheInfoById(int id) {
        String exitString = null;
        openReadableDB();
        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_WEB_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            exitString = cursor.getString(cursor.getColumnIndex(COLUMN_WEB_TEXT));
        }
        cursor.close();
        closeDB();

        return exitString;
    }

    public String getCacheNotebookTextById(int id) {
        String exitString = null;
        openReadableDB();
        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_NOTEBOOK_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            exitString = cursor.getString(cursor.getColumnIndex(COLUMN_NOTEBOOK_TEXT));
        }
        cursor.close();
        closeDB();

        return exitString;
    }

    public String getNoteById(int id) {
        String exitString = null;
        openReadableDB();
        Cursor cursor = db.rawQuery(String.format("select %s from %s where %s=%d", COLUMN_USER_NOTES, DATABASE_NAME_TABLE, COLUMN_ID, id), null);

        if (cursor != null && cursor.getCount() != 0) {
            cursor.moveToFirst();
            exitString = cursor.getString(cursor.getColumnIndex(COLUMN_USER_NOTES));
        }
        cursor.close();
        closeDB();

        return exitString;
    }

    /**
     * @param cacheId
     *            id of Searching GeoCache
     * @param checkpointId
     *            id of checkpoint
     * @param status
     *            checkpoint status
     */
    public void ubdateCheckpointCacheStatus(int cacheId, int checkpointId, GeoCacheStatus status) {
        openWritableDB();
        db.execSQL(String.format("UPDATE %s SET %s=%d WHERE %s=%d AND %s=%d", DATABASE_CHECKPOINT_NAME_TABLE, COLUMN_STATUS, status.ordinal(), CACHE_ID, cacheId, CHECKPOINT_ID, checkpointId));
        closeDB();
    }

    /**
     * Remove geocache from DB, also remove all checkpoints corresponding to a geocache
     * 
     * @param id
     *            ID geocache for delete from database
     */
    public void deleteCacheById(int id) {
        openWritableDB();
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_NAME_TABLE, COLUMN_ID, id));
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id));
        closeDB();
    }

    /**
     * @param name
     *            name of checkpoint geocache
     * @param checkpointId
     *            geocache id for delete from database
     */
    public void deleteCheckpointCache(int cacheId, int checkpointId) {
        openWritableDB();
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d AND %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, cacheId, CHECKPOINT_ID, checkpointId));
        closeDB();
    }

    public void deleteCheckpointCache(int id) {
        openWritableDB();
        db.execSQL(String.format("DELETE FROM %s WHERE %s=%d;", DATABASE_CHECKPOINT_NAME_TABLE, CACHE_ID, id));
        closeDB();
    }

    public void updateNotebookText(int cacheId, String htmlNotebookText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTEBOOK_TEXT, htmlNotebookText);
        openWritableDB();
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
        closeDB();
    }

    public void updateInfoText(int cacheId, String htmlInfoText) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WEB_TEXT, htmlInfoText);
        openWritableDB();
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
        closeDB();
    }

    public void updateNotes(int cacheId, String note) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NOTES, note);
        openWritableDB();
        db.update(DATABASE_NAME_TABLE, values, COLUMN_ID + "=" + cacheId, null);
        closeDB();
    }

    public boolean isCacheStored(int id) {
        openReadableDB();
        String[] selectionArgs = new String[] { Integer.toString(id) };
        Cursor c = db.query(DATABASE_NAME_TABLE, null, COLUMN_ID + "=?", selectionArgs, null, null, null);
        int count = c.getCount();
        c.close();
        closeDB();
        LogManager.d(TAG, "isCacheStored id=" + id + " " + (count > 0));
        return count > 0;
    }

    /**
     * Method for open read-only database
     */
    private void openReadableDB() {
        LogManager.d(TAG, "openDB");
        db = getReadableDatabase();
    }

    /**
     * Method for open writable database
     */
    private void openWritableDB() {
        LogManager.d(TAG, "openDB");
        db = getWritableDatabase();
    }

    public void clearDB() {
        openWritableDB();
        LogManager.d(TAG, "clearDB");
        db.delete(DATABASE_NAME_TABLE, null, null);
        closeDB();
    }

    /**
     * Method for close database
     */
    private void closeDB() {
        LogManager.d(TAG, "closeDB");
        db.close();
    }
}
