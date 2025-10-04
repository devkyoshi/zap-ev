package com.ead.zap.database.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ead.zap.database.AppDatabaseHelper;
import com.ead.zap.models.EVOwner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object for EV Owner operations
 * Handles CRUD operations for EV Owner data in local SQLite database
 */
public class EVOwnerDAO {
    private AppDatabaseHelper dbHelper;
    
    public EVOwnerDAO(Context context) {
        this.dbHelper = AppDatabaseHelper.getInstance(context);
    }
    
    /**
     * Insert or update EV Owner
     */
    public long insertOrUpdateEVOwner(EVOwner evOwner) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(AppDatabaseHelper.KEY_ID, evOwner.getId());
        values.put(AppDatabaseHelper.KEY_EV_OWNER_NIC, evOwner.getNic());
        values.put(AppDatabaseHelper.KEY_EV_OWNER_FIRST_NAME, evOwner.getFirstName());
        values.put(AppDatabaseHelper.KEY_EV_OWNER_LAST_NAME, evOwner.getLastName());
        values.put(AppDatabaseHelper.KEY_EV_OWNER_EMAIL, evOwner.getEmail());
        values.put(AppDatabaseHelper.KEY_EV_OWNER_PHONE, evOwner.getPhoneNumber());
        values.put(AppDatabaseHelper.KEY_EV_OWNER_IS_ACTIVE, evOwner.isActive() ? 1 : 0);
        
        if (evOwner.getLastLogin() != null) {
            values.put(AppDatabaseHelper.KEY_EV_OWNER_LAST_LOGIN, evOwner.getLastLogin().getTime());
        }
        
        if (evOwner.getCreatedAt() != null) {
            values.put(AppDatabaseHelper.KEY_CREATED_AT, evOwner.getCreatedAt().getTime());
        }
        
        if (evOwner.getUpdatedAt() != null) {
            values.put(AppDatabaseHelper.KEY_UPDATED_AT, evOwner.getUpdatedAt().getTime());
        } else {
            values.put(AppDatabaseHelper.KEY_UPDATED_AT, System.currentTimeMillis());
        }
        
        // Use INSERT OR REPLACE to handle updates
        long result = db.insertWithOnConflict(
            AppDatabaseHelper.TABLE_EV_OWNERS, 
            null, 
            values, 
            SQLiteDatabase.CONFLICT_REPLACE
        );
        
        // Insert vehicle details if any
        if (evOwner.getVehicleDetails() != null && !evOwner.getVehicleDetails().isEmpty()) {
            VehicleDAO vehicleDAO = new VehicleDAO(dbHelper.getReadableDatabase().getPath());
            for (EVOwner.VehicleDetail vehicle : evOwner.getVehicleDetails()) {
                // You'll need to implement insertVehicle in VehicleDAO
                // vehicleDAO.insertVehicle(evOwner.getId(), vehicle);
            }
        }
        
        return result;
    }
    
    /**
     * Get EV Owner by ID
     */
    public EVOwner getEVOwnerById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + AppDatabaseHelper.TABLE_EV_OWNERS + 
                           " WHERE " + AppDatabaseHelper.KEY_ID + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{id});
        
        EVOwner evOwner = null;
        if (cursor.moveToFirst()) {
            evOwner = createEVOwnerFromCursor(cursor);
        }
        
        cursor.close();
        return evOwner;
    }
    
    /**
     * Get EV Owner by NIC
     */
    public EVOwner getEVOwnerByNIC(String nic) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + AppDatabaseHelper.TABLE_EV_OWNERS + 
                           " WHERE " + AppDatabaseHelper.KEY_EV_OWNER_NIC + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{nic});
        
        EVOwner evOwner = null;
        if (cursor.moveToFirst()) {
            evOwner = createEVOwnerFromCursor(cursor);
        }
        
        cursor.close();
        return evOwner;
    }
    
    /**
     * Get all EV Owners
     */
    public List<EVOwner> getAllEVOwners() {
        List<EVOwner> evOwners = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + AppDatabaseHelper.TABLE_EV_OWNERS + 
                           " ORDER BY " + AppDatabaseHelper.KEY_EV_OWNER_FIRST_NAME;
        
        Cursor cursor = db.rawQuery(selectQuery, null);
        
        if (cursor.moveToFirst()) {
            do {
                EVOwner evOwner = createEVOwnerFromCursor(cursor);
                evOwners.add(evOwner);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return evOwners;
    }
    
    /**
     * Delete EV Owner
     */
    public int deleteEVOwner(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        
        return db.delete(AppDatabaseHelper.TABLE_EV_OWNERS,
                        AppDatabaseHelper.KEY_ID + " = ?",
                        new String[]{id});
    }
    
    /**
     * Clear all EV Owner data
     */
    public void clearAllEVOwners() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(AppDatabaseHelper.TABLE_EV_OWNERS, null, null);
    }
    
    /**
     * Create EVOwner object from cursor
     */
    private EVOwner createEVOwnerFromCursor(Cursor cursor) {
        EVOwner evOwner = new EVOwner();
        
        evOwner.setId(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_ID)));
        evOwner.setNic(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_NIC)));
        evOwner.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_FIRST_NAME)));
        evOwner.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_LAST_NAME)));
        evOwner.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_EMAIL)));
        evOwner.setPhoneNumber(cursor.getString(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_PHONE)));
        evOwner.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_IS_ACTIVE)) == 1);
        
        long lastLoginTime = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_EV_OWNER_LAST_LOGIN));
        if (lastLoginTime > 0) {
            evOwner.setLastLogin(new Date(lastLoginTime));
        }
        
        long createdAtTime = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_CREATED_AT));
        if (createdAtTime > 0) {
            evOwner.setCreatedAt(new Date(createdAtTime));
        }
        
        long updatedAtTime = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabaseHelper.KEY_UPDATED_AT));
        if (updatedAtTime > 0) {
            evOwner.setUpdatedAt(new Date(updatedAtTime));
        }
        
        return evOwner;
    }
}