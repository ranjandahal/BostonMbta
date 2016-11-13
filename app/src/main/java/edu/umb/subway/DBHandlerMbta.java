package edu.umb.subway;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bikesh on 11/12/2016.
 */

public class DBHandlerMbta extends SQLiteOpenHelper {

    //Defining the database and Table
    private static final int DATABASE_VERSION = 1; //Database version
    private static final String DATABASE_NAME = "db_mbta"; //Datbase Name
    private static final String DATABASE_TABLE = "tblStation"; //Databse Table Name

    //Defining the column names for the table
    private static final String sid= "stationID";
    private static final String sname = "stationName";
    private static final String slatitude = "lat";
    private static final String slongitude= "long";
    private static final String sRank = "rank";

//    private static final String swebservicename = "webServiceName";
    private static final String scolor= "color";
//    private static final String smessage= "message";
//    private static final String sfavorite= "favorite";

    //
    public DBHandlerMbta(Context context){
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String table = "CREATE TABLE " + DATABASE_TABLE + "("
                +sid + " STRING PRIMARY KEY, "
                + sname + " TEXT, "
                + slatitude + " REAL, "
                +slongitude + " REAL, "
                +sRank + " INTEGER, "
//                +swebservicename +" TEXT, "
               +scolor +" TEXT "
//                +smessage +" TEXT,"
//                + sfavorite + " TEXT"
                + ")";

        db.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DROP OLDER TABLE IF EXISTS
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

        // CREATE TABLE AGAIN
        onCreate(db);
    }

//********** DATABASE OPERATIONS:  ADD, EDIT, DELETE
    //Adding new Station
    public void addStation(String stId, String name, Double lat,
                           Double lon, Integer rank, String color) {
        SQLiteDatabase myAddStationdb = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(sid, stId);
        values.put(sname, name);
        values.put(slatitude, lat);
        values.put(slongitude, lon);
        values.put(sRank, rank);
        values.put(scolor, color);
//        values.put(swebservicename, stations.getWebServiceName());
//        values.put(scolor, stations.getColor());
//        values.put(smessage, stations.getMessage());
//        values.put(sfavorite, stations.getFavorite());

        // Inserting Row
        myAddStationdb.insert(DATABASE_TABLE, null, values);
        myAddStationdb.close(); // Closing database connection
    }

    //Reading Records
    public Stations getStation(int pSid)
    {
        SQLiteDatabase myGetStationdb = this.getReadableDatabase();
        Cursor cursor = myGetStationdb.query(
                DATABASE_TABLE, new String[]
                        {sid,sname, slatitude,slongitude, sRank}, sid + " = ?",
                new String[] { String.valueOf(pSid) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Stations desireStation = new Stations
                (
                    cursor.getString(0),
                    cursor.getString(1),
                    Double.parseDouble(cursor.getString(2)),
                    Double.parseDouble(cursor.getString(3)),
                    Integer.parseInt(cursor.getString(4)),
                    cursor.getString(5)
                );
        // return station
        return desireStation;
    }

        // Information about all Station
    public List<Stations> getAllStation(String color) {
        List<Stations> stationsList = new ArrayList<Stations>();

        // Select All Query
        String selectQuery = "";
        if (color.equals("none") || color.length() == 0)
            selectQuery = "SELECT * FROM " + DATABASE_TABLE + " ORDER BY RANK";
        else
            selectQuery = "SELECT * FROM " + DATABASE_TABLE + " WHERE color = '" + color + "' ORDER BY RANK";

        SQLiteDatabase allStationdb = this.getReadableDatabase(); //.getWritableDatabase();
        Cursor cursor = allStationdb.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                stationsList.add(new Stations(cursor.getString(0),cursor.getString(1),
                          Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(3)),
                          Integer.parseInt(cursor.getString(4)), cursor.getString(5)));
                Log.v("Stop", cursor.getString(1));
            } while (cursor.moveToNext());
        }
        for (Stations st:stationsList) {
            Log.v("Stop", st.getName());
        }
        // return contact list
        return stationsList;
    }

    // Updating a station
    public int updateStation(Stations stations) {
        SQLiteDatabase updateStationdb = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(sid, stations.getStationID());
        values.put(sname, stations.getName());
        values.put(slatitude, stations.getLat());
        values.put(slongitude, stations.getLng());
        values.put(sRank,stations.getRank());
        values.put(scolor,stations.getColor());
//        values.put(swebservicename, stations.getWebServiceName());
//        values.put(scolor, stations.getColor());
//        values.put(smessage, stations.getMessage());
//        values.put(sfavorite, stations.getFavorite());

    // updating row
        return updateStationdb.update(DATABASE_TABLE, values, sid + " = ?",
                new String[]{String.valueOf(stations.getStationID())});
    }

    //Deleting a station
    public void deleteStation(Stations stations) {
        SQLiteDatabase deleteStationdb = this.getWritableDatabase();
        deleteStationdb.delete(DATABASE_TABLE, sid + " = ?",
                new String[] { String.valueOf(stations.getStationID()) });
        deleteStationdb.close();
    }

    public void initialSetup()
    {
        if(getAllStation("").isEmpty()) {
            addStation("place-wondl", "Wonderland Station", 42.41342, -70.991648, 1, "blue");
            addStation("place-rbmnl", "Revere Beach Station", 42.40784254, -70.99253321, 2, "blue");
            addStation("place-bmmnl", "Beachmont Station", 42.39754234, -70.99231944, 3, "blue");
            addStation("place-sdmnl", "Suffolk Downs Station", 42.39050067, -70.99712259, 4, "blue");
            addStation("place-orhte", "Orient Heights Station", 42.386867, -71.004736, 5, "blue");
            addStation("place-wimnl", "Wood Island Station", 42.3796403, -71.02286539, 6, "blue");
            addStation("place-aport", "Airport Station", 42.374262, -71.030395, 7, "blue");
            addStation("place-mvbcl", "Maverick Station", 42.36911856, -71.03952958, 8, "blue");
            addStation("place-aqucl", "Aquarium Station", 42.359784, -71.051652, 9, "blue");
            addStation("place-state", "State St. Station", 42.358978, -71.057598, 10, "blue");
            addStation("place-gover", "Government Center Station", 42.359705, -71.059215, 11, "blue");
            addStation("place-bomnl", "Bowdoin Station", 42.361365, -71.062037, 12, "blue");
        }
    }
}
