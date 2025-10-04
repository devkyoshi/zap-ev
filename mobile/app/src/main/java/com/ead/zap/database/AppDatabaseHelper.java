package com.ead.zap.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite Database helper for local data storage
 * Manages database creation and versioning
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {
    
    // Database Info
    private static final String DATABASE_NAME = "zap_ev_database.db";
    private static final int DATABASE_VERSION = 1;
    
    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EV_OWNERS = "ev_owners";
    public static final String TABLE_VEHICLES = "vehicles";
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_CHARGING_STATIONS = "charging_stations";
    
    // Common column names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_AT = "created_at";
    public static final String KEY_UPDATED_AT = "updated_at";
    
    // User table columns
    public static final String KEY_USER_USERNAME = "username";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_ROLE = "role";
    public static final String KEY_USER_IS_ACTIVE = "is_active";
    public static final String KEY_USER_LAST_LOGIN = "last_login";
    
    // EV Owner table columns
    public static final String KEY_EV_OWNER_NIC = "nic";
    public static final String KEY_EV_OWNER_FIRST_NAME = "first_name";
    public static final String KEY_EV_OWNER_LAST_NAME = "last_name";
    public static final String KEY_EV_OWNER_EMAIL = "email";
    public static final String KEY_EV_OWNER_PHONE = "phone_number";
    public static final String KEY_EV_OWNER_IS_ACTIVE = "is_active";
    public static final String KEY_EV_OWNER_LAST_LOGIN = "last_login";
    
    // Vehicle table columns
    public static final String KEY_VEHICLE_OWNER_ID = "owner_id";
    public static final String KEY_VEHICLE_MAKE = "make";
    public static final String KEY_VEHICLE_MODEL = "model";
    public static final String KEY_VEHICLE_LICENSE_PLATE = "license_plate";
    public static final String KEY_VEHICLE_YEAR = "year";
    
    // Booking table columns (for offline caching)
    public static final String KEY_BOOKING_REMOTE_ID = "remote_id";
    public static final String KEY_BOOKING_USER_ID = "user_id";
    public static final String KEY_BOOKING_STATION_ID = "station_id";
    public static final String KEY_BOOKING_STATION_NAME = "station_name";
    public static final String KEY_BOOKING_STATION_ADDRESS = "station_address";
    public static final String KEY_BOOKING_RESERVATION_DATE = "reservation_date";
    public static final String KEY_BOOKING_RESERVATION_TIME = "reservation_time";
    public static final String KEY_BOOKING_DURATION = "duration";
    public static final String KEY_BOOKING_TOTAL_COST = "total_cost";
    public static final String KEY_BOOKING_STATUS = "status";
    public static final String KEY_BOOKING_QR_CODE = "qr_code";
    
    // Charging Station table columns (for offline caching)
    public static final String KEY_STATION_REMOTE_ID = "remote_id";
    public static final String KEY_STATION_NAME = "name";
    public static final String KEY_STATION_ADDRESS = "address";
    public static final String KEY_STATION_LATITUDE = "latitude";
    public static final String KEY_STATION_LONGITUDE = "longitude";
    public static final String KEY_STATION_AVAILABLE_SLOTS = "available_slots";
    public static final String KEY_STATION_TOTAL_SLOTS = "total_slots";
    public static final String KEY_STATION_COST_PER_HOUR = "cost_per_hour";
    public static final String KEY_STATION_IS_ACTIVE = "is_active";
    
    private static AppDatabaseHelper instance;
    
    public static synchronized AppDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_USER_USERNAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT,"
                + KEY_USER_ROLE + " TEXT,"
                + KEY_USER_IS_ACTIVE + " INTEGER,"
                + KEY_USER_LAST_LOGIN + " INTEGER,"
                + KEY_CREATED_AT + " INTEGER,"
                + KEY_UPDATED_AT + " INTEGER"
                + ")";
        
        // Create EV Owners table
        String CREATE_EV_OWNERS_TABLE = "CREATE TABLE " + TABLE_EV_OWNERS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_EV_OWNER_NIC + " TEXT UNIQUE,"
                + KEY_EV_OWNER_FIRST_NAME + " TEXT,"
                + KEY_EV_OWNER_LAST_NAME + " TEXT,"
                + KEY_EV_OWNER_EMAIL + " TEXT,"
                + KEY_EV_OWNER_PHONE + " TEXT,"
                + KEY_EV_OWNER_IS_ACTIVE + " INTEGER,"
                + KEY_EV_OWNER_LAST_LOGIN + " INTEGER,"
                + KEY_CREATED_AT + " INTEGER,"
                + KEY_UPDATED_AT + " INTEGER"
                + ")";
        
        // Create Vehicles table
        String CREATE_VEHICLES_TABLE = "CREATE TABLE " + TABLE_VEHICLES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_VEHICLE_OWNER_ID + " TEXT,"
                + KEY_VEHICLE_MAKE + " TEXT,"
                + KEY_VEHICLE_MODEL + " TEXT,"
                + KEY_VEHICLE_LICENSE_PLATE + " TEXT,"
                + KEY_VEHICLE_YEAR + " INTEGER,"
                + "FOREIGN KEY(" + KEY_VEHICLE_OWNER_ID + ") REFERENCES " 
                + TABLE_EV_OWNERS + "(" + KEY_ID + ")"
                + ")";
        
        // Create Bookings table (for offline caching)
        String CREATE_BOOKINGS_TABLE = "CREATE TABLE " + TABLE_BOOKINGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_BOOKING_REMOTE_ID + " TEXT,"
                + KEY_BOOKING_USER_ID + " TEXT,"
                + KEY_BOOKING_STATION_ID + " TEXT,"
                + KEY_BOOKING_STATION_NAME + " TEXT,"
                + KEY_BOOKING_STATION_ADDRESS + " TEXT,"
                + KEY_BOOKING_RESERVATION_DATE + " INTEGER,"
                + KEY_BOOKING_RESERVATION_TIME + " INTEGER,"
                + KEY_BOOKING_DURATION + " INTEGER,"
                + KEY_BOOKING_TOTAL_COST + " REAL,"
                + KEY_BOOKING_STATUS + " TEXT,"
                + KEY_BOOKING_QR_CODE + " TEXT,"
                + KEY_CREATED_AT + " INTEGER,"
                + KEY_UPDATED_AT + " INTEGER"
                + ")";
        
        // Create Charging Stations table (for offline caching)
        String CREATE_STATIONS_TABLE = "CREATE TABLE " + TABLE_CHARGING_STATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_STATION_REMOTE_ID + " TEXT,"
                + KEY_STATION_NAME + " TEXT,"
                + KEY_STATION_ADDRESS + " TEXT,"
                + KEY_STATION_LATITUDE + " REAL,"
                + KEY_STATION_LONGITUDE + " REAL,"
                + KEY_STATION_AVAILABLE_SLOTS + " INTEGER,"
                + KEY_STATION_TOTAL_SLOTS + " INTEGER,"
                + KEY_STATION_COST_PER_HOUR + " REAL,"
                + KEY_STATION_IS_ACTIVE + " INTEGER,"
                + KEY_CREATED_AT + " INTEGER,"
                + KEY_UPDATED_AT + " INTEGER"
                + ")";
        
        // Execute table creation
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_EV_OWNERS_TABLE);
        db.execSQL(CREATE_VEHICLES_TABLE);
        db.execSQL(CREATE_BOOKINGS_TABLE);
        db.execSQL(CREATE_STATIONS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHARGING_STATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VEHICLES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EV_OWNERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        
        // Create tables again
        onCreate(db);
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }
}