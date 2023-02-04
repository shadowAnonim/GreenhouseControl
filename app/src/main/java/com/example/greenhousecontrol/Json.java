package com.example.greenhousecontrol;

import android.content.Context;

import com.google.gson.Gson;

import java.util.List;

public class Json {
    static String exportToJSON(Object data) {

        Gson gson = new Gson();
        return gson.toJson(data);
    }

    static SensorReading importFromJSON(String jsonData) {
        try {

            Gson gson = new Gson();
            SensorReading reading = gson.fromJson(jsonData, SensorReading.class);
            return reading;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
