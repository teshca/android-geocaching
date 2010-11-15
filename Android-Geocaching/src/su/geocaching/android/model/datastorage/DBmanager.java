package su.geocaching.android.model.datastorage;

import com.google.android.maps.GeoPoint;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBmanager extends SQLiteOpenHelper {

    // Name, version and name table   
       public static final String DATABASE_NAME_BASE="CachBase.db";
       private static final String DATABASE_NAME_TABLE="cach";
       private static final int DATABASE_VERSION=1;
    //Name column database
       private static final String COLUMN_ID="cid";
       private static final String COLUMN_TYPE="type";
       private static final String COLUMN_WEB_TEXT="text";
       private static final String COLUMN_LONG="longtitude";
       private static final String COLUMN_LANT="lantitude";
       private static final String COLUMN_NAME="name";
       private static final String COLUMN_STATUS="status";
       private SQLiteDatabase db;
       
       private static final String SQL_CREATE_DATABASE_TABLE="create table "+DATABASE_NAME_TABLE+
       		" ("+COLUMN_ID+" integer, "
       		+COLUMN_NAME+" string, "
       		+COLUMN_TYPE+" integer, "
       		+COLUMN_STATUS+" integer, "
       		+COLUMN_LANT+" integer, "
       		+COLUMN_LONG+" integer, "
       		+COLUMN_WEB_TEXT+" string);";
       
       public DBmanager(Context context) {
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
       public void openDB(){
   	Log.d("openDB", "Begin");
   	db=this.getWritableDatabase();
   	Log.d("openDB", "End");
       }
       
       /*
        * Need add cause  "if Database" not
        * This cause work
        * */
       public String[] getArrayNameCach(){
	   String arrayName[];
	   Cursor cur = db.rawQuery("select "+COLUMN_NAME+" from "+DATABASE_NAME_TABLE+";", null);
	   cur.moveToFirst();
	   	arrayName=new String[cur.getCount()];
	   	for(int i=0;i<arrayName.length;i++){
	       	arrayName[i]=cur.getString(0);
	       	cur.moveToNext();
	   	}
	   return arrayName;
       }
       
       public void closeDB(){
   	db.close();
       }
       
       /*
        * No testing
        * */
       public boolean insert(GeoCache cach, String web ){
	   //TODO: Write case if cache not in DB
   	ContentValues values = new ContentValues();
   	values.put(COLUMN_ID, cach.getId());
   	values.put(COLUMN_NAME, cach.getName());
   	values.put(COLUMN_STATUS, cach.getStatus().ordinal());
   	values.put(COLUMN_TYPE, cach.getType().ordinal());
   	values.put(COLUMN_LANT, cach.getLocationGeoPoint().getLatitudeE6());
   	values.put(COLUMN_LONG, cach.getLocationGeoPoint().getLongitudeE6());
   	values.put(COLUMN_WEB_TEXT, web);
   	db.insert(DATABASE_NAME_TABLE, null, values);
   	return true;
       }
       
       /*
        * WARING - no test. May be not work
        * */
       public GeoCache getCachByID(int id){
   	Cursor c=db.rawQuery("select "+COLUMN_NAME+","+COLUMN_TYPE+","+COLUMN_STATUS+","+COLUMN_LANT+","+COLUMN_LONG+" from "+DATABASE_NAME_TABLE+" where "+COLUMN_ID+"="+id,null);
   	
   	c.moveToFirst();
   	GeoCache exitCach = new GeoCache(id);
   		
   	exitCach.setName(c.getString(0));
   	exitCach.setLocationGeoPoint(new GeoPoint(c.getInt(3), c.getInt(4)));
   	
   	if (c.getInt(2)==1) exitCach.setStatus(GeoCacheStatus.VALID);
   	if (c.getInt(2)==2) exitCach.setStatus(GeoCacheStatus.NOT_VALID);
   	if (c.getInt(2)==3) exitCach.setStatus(GeoCacheStatus.NOT_CONFIRMED);
   	
   	if(c.getInt(1)==1) exitCach.setType(GeoCacheType.TRADITIONAL);
   	if(c.getInt(1)==2) exitCach.setType(GeoCacheType.VIRTUAL);
   	if(c.getInt(1)==3) exitCach.setType(GeoCacheType.STEP_BY_STEP);
   	if(c.getInt(1)==4) exitCach.setType(GeoCacheType.EXTREME);
   	if(c.getInt(1)==5) exitCach.setType(GeoCacheType.EVENT);
   		
   	
   	return exitCach;
       }
}
