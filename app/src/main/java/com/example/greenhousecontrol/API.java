package com.example.greenhousecontrol;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class API {


    public static float getTemperature(int sensorId)
    {
        HttpURLConnection connection = null;
        String jsonData = "";
        try {
            //Create connection
            URL url = new URL("https://dt.miet.ru/ppo_it/api/temp_hum/" + sensorId);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            if (connection.getResponseCode() > 299)
                return -1;

            try (final BufferedReader in =
                         new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                final StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null)
                {
                    content.append(inputLine);
                }
                jsonData =  content.toString();
            }
            catch (final Exception ex)
            {
                ex.printStackTrace();
                return -1;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
        SensorReading reading = Json.importFromJSON(jsonData);
        return reading.getTemperature();
    }

    public static boolean setWindowState(int state)
    {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL("https://dt.miet.ru/ppo_it/api/fork_drive?state=" + state);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            connection.setConnectTimeout(3000);
            if (connection.getResponseCode() > 299)
                return false;
        }catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
        return true;
    }
}

