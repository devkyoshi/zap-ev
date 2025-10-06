package com.ead.zap.models;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import android.util.Log;

import java.lang.reflect.Type;

/**
 * Custom deserializer for BookingStatus to handle integer values from API
 */
public class BookingStatusDeserializer implements JsonDeserializer<BookingStatus> {
    private static final String TAG = "BookingStatusDeserializer";

    @Override
    public BookingStatus deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        try {
            if (json.isJsonPrimitive()) {
                if (json.getAsJsonPrimitive().isNumber()) {
                    // Handle integer status values from API
                    int statusValue = json.getAsInt();
                    Log.d(TAG, "Deserializing status integer: " + statusValue);
                    return BookingStatus.fromValue(statusValue);
                } else if (json.getAsJsonPrimitive().isString()) {
                    // Handle string status values (backward compatibility)
                    String statusString = json.getAsString();
                    Log.d(TAG, "Deserializing status string: " + statusString);
                    return BookingStatus.fromString(statusString);
                }
            }
            
            Log.w(TAG, "Unexpected JSON element for status: " + json);
            return BookingStatus.PENDING; // Default fallback
            
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing BookingStatus: " + e.getMessage(), e);
            return BookingStatus.PENDING; // Default fallback
        }
    }
}