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

/**
 * This class contains method for working with database.
 * 
 * @author Alekseenko Vladimir
 * 
 */
public class DbManager extends SQLiteOpenHelper {

	// Name, version and name table
	public static final String DATABASE_NAME_BASE = "CacheBase.db";
	private static final String DATABASE_NAME_TABLE = "cache";
	private static final int DATABASE_VERSION = 2;
	// Name column database
	private static final String COLUMN_ID = "cid";
	private static final String COLUMN_TYPE = "type";
	private static final String COLUMN_WEB_TEXT = "text";
	private static final String COLUMN_NOTEBOOK_TEXT = "notetext";
	private static final String COLUMN_LON = "longtitude";
	private static final String COLUMN_LAT = "lantitude";
	private static final String COLUMN_NAME = "name";
	private static final String COLUMN_STATUS = "status";
	private SQLiteDatabase db = null;

	private static final String SQL_CREATE_DATABASE_TABLE = String.format("create table %s (%s integer, %s string, %s integer,%s integer, %s integer, %s integer, %s string, %s string);",
			DATABASE_NAME_TABLE, COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, COLUMN_WEB_TEXT, COLUMN_NOTEBOOK_TEXT);

	/**
	 * @param context
	 *            this activivty
	 */
	public DbManager(Context context) {
		super(context, DATABASE_NAME_BASE, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_DATABASE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * Method for open database
	 */
	public void openDB() {
		Log.d("DataBase", "Open");
		db = this.getWritableDatabase();
	}

	/**
	 * @return ArrayList GeoCaches in database. Null if in database haven't GeoCache
	 */
	public ArrayList<GeoCache> getArrayGeoCache() {
		ArrayList<GeoCache> exitCollection = new ArrayList<GeoCache>();
		Cursor cur = db.rawQuery(String.format("select %s,%s,%s,%s,%s,%s from %s", COLUMN_ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE), null);

		if (cur.getCount() == 0) {
			cur.close();
			return null;
		}

		cur.moveToFirst();

		for (int i = 0; i < cur.getCount(); i++) {

			GeoCache geocache = new GeoCache();
			geocache.setId(cur.getInt(cur.getColumnIndex(COLUMN_ID)));
			geocache.setName(cur.getString(cur.getColumnIndex(COLUMN_NAME)));
			geocache.setStatus(GeoCacheStatus.values()[cur.getInt(cur.getColumnIndex(COLUMN_STATUS))]);
			// switch (cur.getInt(cur.getColumnIndex(COLUMN_STATUS))) {
			// case 0:
			// geocache.setStatus(GeoCacheStatus.VALID);
			// break;
			// case 1:
			// geocache.setStatus(GeoCacheStatus.NOT_VALID);
			// break;
			// case 2:
			// geocache.setStatus(GeoCacheStatus.NOT_CONFIRMED);
			// break;
			// }

			geocache.setLocationGeoPoint(new GeoPoint(cur.getInt(cur.getColumnIndex(COLUMN_LAT)), cur.getInt(cur.getColumnIndex(COLUMN_LON))));

			geocache.setType(GeoCacheType.values()[cur.getInt(cur.getColumnIndex(COLUMN_TYPE))]);
			// switch (cur.getInt(cur.getColumnIndex(COLUMN_TYPE))) {
			// case 0:
			// geocache.setType(GeoCacheType.TRADITIONAL);
			// break;
			// case 1:
			// geocache.setType(GeoCacheType.VIRTUAL);
			// break;
			// case 2:
			// geocache.setType(GeoCacheType.STEP_BY_STEP);
			// break;
			// case 3:
			// geocache.setType(GeoCacheType.EXTREME);
			// break;
			// case 4:
			// geocache.setType(GeoCacheType.EVENT);
			// break;
			// }
			exitCollection.add(geocache);
			cur.moveToNext();
		}

		cur.close();
		return exitCollection;
	}

	/**
	 * Method for close database
	 */
	public void closeDB() {
		Log.d("Database", "close");
		db.close();
	}

	/**
	 * @param geoCacheForAdd
	 *            GeoCache for add in database
	 * @param webText
	 *            html text for description GeoCache
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
		if (webNotebookText != null && webNotebookText != "")
			values.put(COLUMN_NOTEBOOK_TEXT, webNotebookText);
		db.insert(DATABASE_NAME_TABLE, null, values);
	}

	/**
	 * @param id
	 *            ID geocache for delete from database
	 */
	public void deleteCacheById(int id) {
		this.db.execSQL(String.format("delete from %s where %s=%s;", DATABASE_NAME_TABLE, COLUMN_ID, id + ""));
	}

	/**
	 * @param id
	 *            ID GeoCache for taking his html description
	 * @return String if GeoCache in database. Empty string if in database haven't GeoCache
	 */
	public String getWebTextById(int id) {
		String exitString = "";
		Cursor c = db.rawQuery(String.format("select %s from %s where %s=%s", COLUMN_WEB_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id + ""), null);
		if (c.getCount() == 0) {
			c.close();
			return null;
		}
		c.moveToFirst();
		exitString = c.getString(0);
		c.close();
		return exitString;
	}

	public String getWebNotebookTextById(int id) {
		String exitString = "";
		Cursor c = db.rawQuery(String.format("select %s from %s where %s=%s", COLUMN_NOTEBOOK_TEXT, DATABASE_NAME_TABLE, COLUMN_ID, id + ""), null);
		if (c != null) {
			if (c.getCount() == 0) {
				c.close();
				return null;
			}
			c.moveToFirst();
			if (c.getString(0) != null)
				exitString = c.getString(0);
			else
				exitString = null;
			c.close();
			return exitString;
		}
		return null;
	}

	/**
	 * @param id
	 *            ID GeoCache for taking from database
	 * @return GeoCache if database have GeoCache. Null if database haven't GeoCache
	 */
	public GeoCache getCacheByID(int id) {
		Cursor c = db.rawQuery(String.format("select %s,%s,%s,%s,%s from %s where %s=%s", COLUMN_NAME, COLUMN_TYPE, COLUMN_STATUS, COLUMN_LAT, COLUMN_LON, DATABASE_NAME_TABLE, COLUMN_ID, id + ""),
				null);
		if (c.getCount() == 0) {
			c.close();
			return null;
		}
		c.moveToFirst();
		GeoCache exitCache = new GeoCache();
		exitCache.setId(id);

		exitCache.setName(c.getString(c.getColumnIndex(COLUMN_NAME)));
		exitCache.setLocationGeoPoint(new GeoPoint(c.getInt(c.getColumnIndex(COLUMN_LAT)), c.getInt(c.getColumnIndex(COLUMN_LON))));

		exitCache.setStatus(GeoCacheStatus.values()[c.getInt(c.getColumnIndex(COLUMN_STATUS))]);
		// switch (c.getInt(c.getColumnIndex(COLUMN_STATUS))) {
		// case 1:
		// exitCache.setStatus(GeoCacheStatus.VALID);
		// break;
		//
		// case 2:
		// exitCache.setStatus(GeoCacheStatus.NOT_VALID);
		// break;
		//
		// case 3:
		// exitCache.setStatus(GeoCacheStatus.NOT_CONFIRMED);
		// break;
		// default:
		// exitCache.setStatus(GeoCacheStatus.NOT_VALID);
		// break;
		// }

		exitCache.setType(GeoCacheType.values()[c.getInt(c.getColumnIndex(COLUMN_TYPE))]);
		//
		// switch (c.getInt(c.getColumnIndex(COLUMN_TYPE))) {
		// case 1:
		// exitCache.setType(GeoCacheType.TRADITIONAL);
		// break;
		// case 2:
		// exitCache.setType(GeoCacheType.VIRTUAL);
		// break;
		// case 3:
		// exitCache.setType(GeoCacheType.STEP_BY_STEP);
		// break;
		// case 4:
		// exitCache.setType(GeoCacheType.EXTREME);
		// break;
		// case 5:
		// exitCache.setType(GeoCacheType.EVENT);
		// break;
		// default:
		// exitCache.setType(GeoCacheType.TRADITIONAL);
		// break;
		// }

		c.close();
		return exitCache;
	}
}
