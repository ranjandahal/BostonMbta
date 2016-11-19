package edu.umb.subway;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Created by Bikesh on 11/12/2016.
 */

public class DBHandlerMbta extends SQLiteOpenHelper {

    //Defining the database and Table
    private static final int DATABASE_VERSION = 1; //Database version
    private static final String DATABASE_NAME = "db_mbta"; //Datbase Name
    private static final String DATABASE_TABLE_STATION = "tblStation"; //Databse Table Name
    private static final String DATABASE_TABLE_FAVORITE = "tblFavorite"; //Databse Table Name

    //Defining the column names for the table
    private static final String sid= "stationID";
    private static final String sname = "stationName";
    private static final String slatitude = "lat";
    private static final String slongitude= "long";
    private static final String sRank = "rank";
    private static final String sColor= "color";
    private static final String sRoute= "route";
    private static final String sDestination= "route";
    private static final String sZoomLevel = "zoomLevel";
//    private static final String smessage= "message";
//    private static final String sfavorite= "favorite";

    //
    public DBHandlerMbta(Context context){
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //onUpgrade(db, 1, 2);
        try {
            String table = "CREATE TABLE " + DATABASE_TABLE_STATION + "("
                    + sid + " STRING, "
                    + sname + " TEXT, "
                    + slatitude + " REAL, "
                    + slongitude + " REAL, "
                    + sRank + " INTEGER, "
                    + sColor + " TEXT, "
                    + sRoute + " TEXT, "
                    + sZoomLevel + " REAL, "
                    + "PRIMARY KEY (" + sid + "," + sColor + "," + sRoute + ")"
                    + " );";

            db.execSQL(table);

            table = "CREATE TABLE " + DATABASE_TABLE_FAVORITE + "("
                    + sid + " STRING, "
                    + sname + " TEXT, "
                    + sColor + " TEXT, "
                    + sDestination + " TEXT, "
                    + "PRIMARY KEY (" + sid + "," + sColor + "," + sname + ")"
                    + " );";

            db.execSQL(table);

        }catch (Exception ex){}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DROP OLDER TABLE IF EXISTS
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_STATION);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FAVORITE);

        // CREATE TABLE AGAIN
        onCreate(db);
    }

//********** DATABASE OPERATIONS:  ADD, EDIT, DELETE
    //Adding new Station
    public void addStation(String stId, String name, Double lat,
                           Double lon, Integer rank, String color, String route, float zoomLevel) {
        SQLiteDatabase myAddStationdb = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(sid, stId);
            values.put(sname, name);
            values.put(slatitude, lat);
            values.put(slongitude, lon);
            values.put(sRank, rank);
            values.put(sColor, color);
            values.put(sRoute, route);
            values.put(sZoomLevel, zoomLevel);
            // Inserting Row
            myAddStationdb.insert(DATABASE_TABLE_STATION, null, values);
        }catch(Exception ex) {
            Log.v("Insert Error", ex.getMessage());
        }
        finally {
            myAddStationdb.close(); // Closing database connection
        }
    }

    public void addFavorite(String stId, String name, String color) {
        SQLiteDatabase myAddStationdb = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();

            values.put(sid, stId);
            values.put(sname, name);
            values.put(sColor, color);
            // Inserting Row
            myAddStationdb.insert(DATABASE_TABLE_FAVORITE, null, values);
        }catch(Exception ex) {
            Log.v("Insert Error", ex.getMessage());
        }
        finally {
            myAddStationdb.close(); // Closing database connection
        }
    }

    //Reading Records
    public Stations getStation(int pSid) {
        SQLiteDatabase myGetStationdb = this.getReadableDatabase();
        Cursor cursor = myGetStationdb.query(
                DATABASE_TABLE_STATION, new String[]
                        {sid,sname, slatitude,slongitude, sRank, sColor, sRoute, sZoomLevel}, sid + " = ?",
                new String[] { String.valueOf(pSid) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Stations desireStation = new Stations
                (
                    cursor.getString(0), cursor.getString(1),
                    Double.parseDouble(cursor.getString(2)),
                    Double.parseDouble(cursor.getString(3)),
                    Integer.parseInt(cursor.getString(4)),
                    cursor.getString(5), cursor.getString(6),
                    cursor.getFloat(7)
                );
        // return station
        return desireStation;
    }

    public Stations getFavoriteStation(int pSid) {
        SQLiteDatabase myGetStationdb = this.getReadableDatabase();
        Cursor cursor = myGetStationdb.query(
                DATABASE_TABLE_STATION, new String[]
                        {sid,sname, sColor}, sid + " = ?",
                new String[] { String.valueOf(pSid) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Stations desireStation = new Stations
                (
                        cursor.getString(0), cursor.getString(1),
                        Double.parseDouble(cursor.getString(2)),
                        Double.parseDouble(cursor.getString(3)),
                        Integer.parseInt(cursor.getString(4)),
                        cursor.getString(5), cursor.getString(6),
                        cursor.getFloat(7)
                );
        // return station
        return desireStation;
    }

        // Information about all Station
    public List<Stations> getAllStation(String color) {
        List<Stations> stationsList = new ArrayList<Stations>();

        // Select All Query
        String selectQuery = "none";
        if (color.equals("none") || color.length() == 0)
            selectQuery = "SELECT * FROM " + DATABASE_TABLE_STATION + " ORDER BY RANK";
        else
            selectQuery = "SELECT * FROM " + DATABASE_TABLE_STATION + " WHERE color = '" + color + "' ORDER BY RANK";

        SQLiteDatabase allStationdb = this.getReadableDatabase(); //.getWritableDatabase();
        Cursor cursor = allStationdb.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                stationsList.add(new Stations(cursor.getString(0),cursor.getString(1),
                          Double.parseDouble(cursor.getString(2)), Double.parseDouble(cursor.getString(3)),
                          Integer.parseInt(cursor.getString(4)), cursor.getString(5), cursor.getString(6),
                        cursor.getFloat(7)));
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
        values.put(sColor,stations.getColor());
        values.put(sRoute,stations.getRoute());
    // updating row
        return updateStationdb.update(DATABASE_TABLE_STATION, values, sid + " = ?",
                new String[]{String.valueOf(stations.getStationID())});
    }

    //Deleting a station
    public void deleteStation(Stations stations) {
        SQLiteDatabase deleteStationdb = this.getWritableDatabase();
        deleteStationdb.delete(DATABASE_TABLE_STATION, sid + " = ?",
                new String[] { String.valueOf(stations.getStationID()) });
        deleteStationdb.close();
    }

    public void initialSetup() {
        if(getAllStation("none").isEmpty()) {
            //Blue lines
            addStation("place-wondl", "Wonderland", 42.41342, -70.991648, 1, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-rbmnl", "Revere Beach", 42.40784254, -70.99253321, 2, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-bmmnl", "Beachmont", 42.39754234, -70.99231944, 3, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-sdmnl", "Suffolk Downs", 42.39050067, -70.99712259, 4, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-orhte", "Orient Heights", 42.386867, -71.004736, 5, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-wimnl", "Wood Island", 42.3796403, -71.02286539, 6, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-aport", "Airport", 42.374262, -71.030395, 7, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-mvbcl", "Maverick", 42.36911856, -71.03952958, 8, "blue", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-aqucl", "Aquarium", 42.359784, -71.051652, 9, "blue", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-state", "State Street", 42.358978, -71.057598, 10, "blue,orange", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-gover", "Government Center", 42.359705, -71.059215, 11, "blue,green", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-bomnl", "Bowdoin", 42.361365, -71.062037, 12, "blue", "none", ZoomLevels.LEVEL_THREE_ZOOM);

            //Orange lines
            addStation("place-ogmnl", "Oak Grove", 42.43668, -71.071097, 1, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-mlmnl", "Malden", 42.426632, -71.07411, 2, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-welln", "Wellington", 42.40237, -71.077082, 3, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-astao", "Assembly", 42.392811, -71.077257, 4, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-sull", "Sullivan Square", 42.383975, -71.076994, 5, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-ccmnl", "Community College", 42.373622, -71.069533, 6, "orange", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-north", "North Station", 42.365577, -71.06129, 7, "orange,green", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-haecl", "Haymarket", 42.363021, -71.05829, 8, "orange,green", "none", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-state", "State Street", 42.358978, -71.057598, 9, "orange,blue", "none", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-dwnxg", "Downtown Crossing", 42.355518, -71.060225, 10, "orange,red", "none", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-chncl", "Chinatown", 42.352547, -71.062752, 11, "orange", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-tumnl", "Tufts Medical Center", 42.349662, -71.063917, 12, "orange", "none", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-bbsta", "Back Bay", 42.34735, -71.075727, 13, "orange", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-masta", "Massachusetts Avenue", 42.341512, -71.083423, 14, "orange", "none", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-rugg", "Ruggles", 42.336377, -71.088961, 15, "orange", "none", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-rcmnl", "Roxbury Crossing", 42.331397, -71.095451, 16, "orange", "none", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-jaksn", "Jackson Square", 42.323132, -71.099592, 17, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-sbmnl", "Stony Brook", 42.317062, -71.104248, 18, "orange", "none", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-grnst", "Green Street", 42.310525, -71.107414, 19, "orange", "none", ZoomLevels.MIN_ZOOM);
            addStation("place-forhl", "Forest Hills", 42.300523, -71.113686, 20, "orange", "none", ZoomLevels.MIN_ZOOM);

            //Red Lines
            addStation("place-alfcl", "Alewife", 42.395428, -71.142483, 1, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-davis", "Davis", 42.39674, -71.121815, 2, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-portr", "Porter", 42.3884, -71.119149, 3, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-harsq", "Harvard", 42.373362, -71.118956, 4, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-cntsq", "Central", 42.365486, -71.103802, 5, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-knncl", "Kendall/MIT", 42.36249079, -71.08617653, 6, "red", "A", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-chmnl", "Charles/MGH", 42.361166, -71.070628, 7, "red", "A", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-pktrm", "Park Street", 42.35639457, -71.0624242, 8, "red,green", "A", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-dwnxg", "Downtown Crossing", 42.355518, -71.060225, 9, "red,orange", "A", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-sstat", "South Station", 42.352271, -71.055242, 10, "red", "A", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-brdwy", "Broadway", 42.342622, -71.056967, 11, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-andrw", "Andrew", 42.330154, -71.057655, 12, "red", "A", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-jfk", "JFK/UMASS", 42.320685, -71.052391, 13, "red", "AB", ZoomLevels.MIN_ZOOM);

            //Ashmont line
            addStation("place-shmnl", "Savin Hill", 42.31129, -71.053331, 14, "red", "A", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-fldcr", "Fields Corner", 42.300093, -71.061667, 15, "red", "A", ZoomLevels.MIN_ZOOM);
            addStation("place-smmnl", "Shawmut", 42.29312583, -71.06573796, 16, "red", "A", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-asmnl", "Ashmont", 42.284652, -71.064489, 17, "red", "A", ZoomLevels.MIN_ZOOM);

            //Braintree line
            addStation("place-nqncy", "North Quincy", 42.275275, -71.029583, 18, "red", "B", ZoomLevels.MIN_ZOOM);
            addStation("place-wlsta", "Wollaston", 42.2665139, -71.0203369, 19, "red", "B", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-qnctr", "Quincy Center", 42.251809, -71.005409, 20, "red", "B", ZoomLevels.MIN_ZOOM);
            addStation("place-qamnl", "Quincy Adams", 42.233391, -71.007153, 21, "red", "B", ZoomLevels.MIN_ZOOM);
            addStation("place-brntn", "Braintree", 42.2078543, -71.0011385, 22, "red", "B", ZoomLevels.MIN_ZOOM);

            //Green Line
            addStation("place-lech", "Lechmere", 42.370772, -71.076536, 1, "green", "B", ZoomLevels.MIN_ZOOM);
            addStation("place-spmnl", "Science Park", 42.366664, -71.067666, 2, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-north", "North Station", 42.365577, -71.06129, 3, "green,orange", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-haecl", "Haymarket", 42.363021, -71.05829, 4, "green,orange", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-gover", "Government Center", 42.359705, -71.059215, 5, "green,blue", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-pktrm", "Park Street", 42.35639457, -71.0624242, 6, "green,red", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-boyls", "Boylston", 42.35302, -71.06459, 7, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-armnl", "Arlington", 42.351902, -71.070893, 8, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-coecl", "Copley", 42.349974, -71.077447, 9, "green", "BE", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-hymnl", "Hynes Convention Center", 42.347888, -71.087903, 10, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-kencl", "Kenmore", 42.348949, -71.095169, 11, "green", "BCD", ZoomLevels.LEVEL_TWO_ZOOM);

            //B line
            addStation("place-bland", "Blandford Street", 42.349293, -71.100258, 12, "green", "B", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-buest", "Boston Univ East", 42.349735, -71.103889, 13, "green", "B", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-bucen", "Boston Univ Central", 42.350082, -71.106865, 14, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-buwst", "Boston Univ West", 42.350941, -71.113876, 15, "green", "B", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-stplb", "Saint Paul Street", 42.3512, -71.116104, 16, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-plsgr", "Pleasant Street", 42.351521, -71.118889, 17, "green", "B", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-babck", "Babcock Street ", 42.35182, -71.12165, 18, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-brico", "Packards Corner", 42.351967, -71.125031, 19, "green", "B", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-harvd", "Harvard Avenue", 42.350243, -71.131355, 20, "green", "B", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-grigg", "Griggs Street", 42.348545, -71.134949, 21, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-alsgr", "Allston Street", 42.348701, -71.137955, 22, "green", "B", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-wrnst", "Warren Street", 42.348343, -71.140457, 23, "green", "B", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-wascm", "Washington Street", 42.343864, -71.142853, 24, "green", "B", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-sthld", "Sutherland Road", 42.341614, -71.146202, 25, "green", "B", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-chswk", "Chiswick Road", 42.340805, -71.150711, 26, "green", "B", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-chill", "Chestnut Hill Avenue", 42.338169, -71.15316, 27, "green", "B", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-sougr", "South Street", 42.3396, -71.157661, 28, "green", "B", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-lake", "Boston College", 42.340081, -71.166769, 29, "green", "B", ZoomLevels.LEVEL_ONE_ZOOM);

            //C line
            addStation("place-smary", "Saint Mary Street", 42.345974, -71.107353, 30, "green", "C", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-hwsst", "Hawes Street", 42.344906, -71.111145, 31, "green", "C", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-kntst", "Kent Street", 42.344074, -71.114197, 32, "green", "C", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-stpul", "Saint Paul Street", 42.343327, -71.116997, 33, "green", "C", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-cool", "Coolidge Corner", 42.342116, -71.121263, 34, "green", "C", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-sumav", "Summit Avenue", 42.34111, -71.12561, 35, "green", "C", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-bndhl", "Brandon Hall", 42.340023, -71.129082, 36, "green", "C", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-fbkst", "Fairbanks Street", 42.339725, -71.131073, 37, "green", "C", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-bcnwa", "Washington Square", 42.339394, -71.13533, 38, "green", "C", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-tapst", "Tappan Street", 42.338459, -71.138702, 39, "green", "C", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-denrd", "Dean Road", 42.337807, -71.141853, 40, "green", "C", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-engav", "Englewood Avenue", 42.336971, -71.14566, 41, "green", "C", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-clmnl", "Cleveland Circle", 42.336142, -71.149326, 42, "green", "C", ZoomLevels.LEVEL_ONE_ZOOM);

            //D line
            addStation("place-fenwy", "Fenway", 42.345394, -71.104187, 43, "green", "D", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-longw", "Longwood", 42.341145, -71.110451, 44, "green", "D", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-bvmnl", "Brookline Village", 42.332774, -71.116296, 45, "green", "D", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-brkhl", "Brookline Hills", 42.331333, -71.126999, 46, "green", "D", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-bcnfd", "Beaconsfield", 42.335846, -71.140823, 46, "green", "D", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-rsmnl", "Reservoir", 42.335027, -71.148952, 47, "green", "D", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-chhil", "Chestnut Hill", 42.326653, -71.165314, 48, "green", "D", ZoomLevels.MIN_ZOOM);
            addStation("place-newto", "Newton Centre", 42.329391, -71.192429, 49, "green", "D", ZoomLevels.MIN_ZOOM);
            addStation("place-newtn", "Newton Highlands", 42.321735, -71.206116, 50, "green", "D", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-eliot", "Eliot", 42.319023, -71.216713, 51, "green", "D", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-waban", "Waban", 42.325943, -71.230728, 52, "green", "D", ZoomLevels.LEVEL_ONE_ZOOM);
            addStation("place-woodl", "Woodland", 42.333374, -71.244301, 53, "green", "D", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-river", "Riverside", 42.337059, -71.251742, 54, "green", "D", ZoomLevels.MIN_ZOOM);
            //E line
            addStation("place-prmnl", "Prudential", 42.34557, -71.081696, 55, "green", "E", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-symcl", "Symphony", 42.342687, -71.085056, 56, "green", "E", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-nuniv", "Northeastern University", 42.340401, -71.088806, 57, "green", "E", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-mfa", "Museum of Fine Arts", 42.337711, -71.095512, 58, "green", "E", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-lngmd", "Longwood Medical Area", 42.33596, -71.100052, 59, "green", "E", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-brmnl", "Brigham Circle", 42.334229, -71.104609, 60, "green", "E", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-fenwd", "Fenwood Road", 42.333706, -71.105728, 61, "green", "E", ZoomLevels.LEVEL_THREE_ZOOM);
            addStation("place-mispk", "Mission Park", 42.333195, -71.109756, 62, "green", "E", ZoomLevels.LEVEL_TWO_ZOOM);
            addStation("place-rvrwy", "Riverway", 42.331684, -71.111931, 63, "green", "E", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-bckhl", "Back of the Hill", 42.330139, -71.111313, 64, "green", "E", ZoomLevels.LEVEL_FOUR_ZOOM);
            addStation("place-hsmnl", "Heath Street", 42.328681, -71.110559, 65, "green", "E", ZoomLevels.LEVEL_TWO_ZOOM);
        }
    }

    public Object[] getStationsArray(List<Stations> stationsArrayList){
        //String[] stationNames = new String[stationsArrayList.size()]; // = new StringBuilder();
        List<Stations> stations = new ArrayList<>();
        Set<Stations> stationSet = new HashSet<Stations>();
        Set<String> st = new HashSet<>();
        Map<String,String> stationMap = new HashMap<>();
        for (int count = 0; count < stationsArrayList.size(); count++ ){//Stations st: stationsArrayList) {
            //stationNames[count] = stationsArrayList.get(count).getName();
            /*for (Stations st: stations) {
                if(stationsArrayList.get(count).getName().equalsIgnoreCase(st.getName()))
                    stations.add(stationsArrayList.get(count));
            }*/
            stationSet.add(stationsArrayList.get(count));
            st.add(stationsArrayList.get(count).getName());
            //stationMap.put(stationsArrayList.get(count).getColor(), stationsArrayList.get(count).getName());
        }
        //return  stationMap;
        return st.toArray();
        //return stationNames;
    }
}
