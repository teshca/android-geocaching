package su.geocaching.android.model.datastorage;

import java.util.ArrayList;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class DbManager extends SQLiteOpenHelper {

    // Name, version and name table
    public static final String DATABASE_NAME_BASE = "CacheBase.db";
    private static final String DATABASE_NAME_TABLE = "cache";
    private static final int DATABASE_VERSION = 1;
    // Name column database
    private static final String COLUMN_ID = "cid";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_WEB_TEXT = "text";
    private static final String COLUMN_LON = "longtitude";
    private static final String COLUMN_LAT = "lantitude";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_STATUS = "status";
    private SQLiteDatabase db = null;

    private static final String SQL_CREATE_DATABASE_TABLE = String.format("create table %s (%s integer, %s string, %s integer,%s integer, %s integer, %s integer, %s string);", DATABASE_NAME_TABLE,
	    COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, COLUMN_WEB_TEXT);

    public DbManager(Context context) {
	super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	db.execSQL(SQL_CREATE_DATABASE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	// TODO Auto-generated method stub

    }

    public void openDB() {
	Log.d("openDB", "Begin");
	db = this.getWritableDatabase();
	Log.d("openDB", "End");
    }

    /**
     * if table empty - return null
     */
    public ArrayList<GeoCache> getArrayGeoCache() {
//	Log.d("getArrayGeoCache", "createArray");

	ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();

	Cursor cur = db.rawQuery(String.format("select %s,%s,%s,%s,%s,%s from %s", COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE), null);
//	Log.d("getArrayGeoCache", "Cursor good.+" + cur.getCount());

	if (cur.getCount() == 0) {
	    cur.close();
	    return null;
	}

	cur.moveToFirst();
//	Log.d("getArrayGeoCache", "cursor move to fist");

	for (int i = 0; i < cur.getCount(); i++) {

	    GeoCache geocache = new GeoCache();
//	    Log.d("getArrayGeoCache " + i, "set ID");
	    geocache.setId(cur.getInt(cur.getColumnIndex(COLUMN_ID)));
//	    Log.d("getArrayGeoCache " + i, "set NAME");
	    geocache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
//	    Log.d("getArrayGeoCache " + i, "set Status");
	    switch (cur.getInt(cur.getColumnIndex(COLUMN_STATUS))) {
	    case 0:
		geocache.setStatus(GeoCacheStatus.VALID);
		break;
	    case 1:
		geocache.setStatus(GeoCacheStatus.NOT_VALID);
		break;
	    case 2:
		geocache.setStatus(GeoCacheStatus.NOT_CONFIRMED);
		break;
	    }
//	    Log.d("getArrayGeoCache " + i, "setLocation");
	    geocache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));
//	    Log.d("getArrayGeoCache " + i, "setTYPE");
	    switch (cur.getInt(cur.getColumnIndex(COLUMN_TYPE))) {
	    case 0:
		geocache.setType(GeoCacheType.TRADITIONAL);
		break;
	    case 1:
		geocache.setType(GeoCacheType.VIRTUAL);
		break;
	    case 2:
		geocache.setType(GeoCacheType.STEP_BY_STEP);
		break;
	    case 3:
		geocache.setType(GeoCacheType.EXTREME);
		break;
	    case 4:
		geocache.setType(GeoCacheType.EVENT);
		break;
	    }
//	    Log.d("getArrayGeoCache " + i, "addCache");
	    exitCollection.add(geocache);
//	    Log.d("move cursor " + i, "next" + cur.getCount());
	    cur.moveToNext();
	}
	
	cur.close();
	return exitCollection;
    }

    public void closeDB() {
	Log.d("CloseDB", "Begin");
	db.close();
	Log.d("CloseDB", "End");
    }

    /**
     * 
     */
    public void addGeoCache(GeoCache cache, String web) {
	ContentValues values = new ContentValues();
	values.put(COLUMN_ID, cache.getId());
	values.put(COLUMN_NAME, cache.getName());
	values.put(COLUMN_STATUS, cache.getStatus().ordinal());
	values.put(COLUMN_TYPE, cache.getType().ordinal());
	values.put(COLUMN_LAT, cache.getLocationGeoPoint().getLatitudeE6());
	values.put(COLUMN_LON, cache.getLocationGeoPoint().getLongitudeE6());
	values.put(COLUMN_WEB_TEXT, web);
	db.insert(DATABASE_NAME_TABLE, null, values);
    }
    /**
     * 
     */
    public void deleteCacheById(int id){
	this.db.execSQL(String.format("delete from %s where %s=%s;", DATABASE_NAME_TABLE, COLUMN_ID, id + ""));
    }
    
    /**
     * if id not in BD - return null
     */
    public String getWebTextById(int id) {
	String exitString = "";
	Cursor c = db.rawQuery(String.format("select %s from %s where %s=%s", COLUMN_WEB_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id + ""), null);
	if(c.getCount()==0){
	    c.close();
	    return null;
	}
	c.moveToFirst();
	exitString = c.getString(0);
	c.close();
	return exitString;
    }

    /**
     * if Cache not in BD - return null
     */
    public GeoCache getCacheByID(int id) {
	//Log.d("getCacheById", "Begin");
	Cursor c = db.rawQuery(String.format("select %s,%s,%s,%s,%s from %s where %s=%s", COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE, COLUMN_ID, id + ""),
		null);

//	Log.d("getCacheById", "Cursor exelent");
	if (c.getCount() == 0) {
	//    Log.d("getCacheById", "Cursor = null");
	    c.close();
	    return null;
	}
//	Log.d("getCacheById", "Move cursor");
	c.moveToFirst();
	//Log.d("getCacheById", "Create Cache");
	GeoCache exitCache = new GeoCache();
	exitCache.setId(id);

	//Log.d("getCacheById", "setName Cache");
	exitCache.setName(c.getString(0));
	//Log.d("getCacheById", "Set Lant_Lont");
	exitCache.setLocationGeoPoint(new GeoPoint(c.getInt(c.getColumnIndex(COLUMN_LAT)), c.getInt(c.getColumnIndex(COLUMN_LON))));

//	Log.d("getCacheById", "Set status");
	switch (c.getInt(c.getColumnIndex(COLUMN_STATUS))) {
	case 1:
	    exitCache.setStatus(GeoCacheStatus.VALID);
	    break;

	case 2:
	    exitCache.setStatus(GeoCacheStatus.NOT_VALID);
	    break;

	case 3:
	    exitCache.setStatus(GeoCacheStatus.NOT_CONFIRMED);
	    break;
	 default:
	     exitCache.setStatus(GeoCacheStatus.NOT_VALID);
	     break;
	}

	switch (c.getInt(c.getColumnIndex(COLUMN_TYPE))) {
	case 1:
	    exitCache.setType(GeoCacheType.TRADITIONAL);
	    break;
	case 2:
	    exitCache.setType(GeoCacheType.VIRTUAL);
	    break;
	case 3:
	    exitCache.setType(GeoCacheType.STEP_BY_STEP);
	    break;
	case 4:
	    exitCache.setType(GeoCacheType.EXTREME);
	    break;
	case 5:
	    exitCache.setType(GeoCacheType.EVENT);
	    break;
	default:
	    exitCache.setType(GeoCacheType.TRADITIONAL);
	    break;
	}
	
	c.close();
	return exitCache;
    }
}
