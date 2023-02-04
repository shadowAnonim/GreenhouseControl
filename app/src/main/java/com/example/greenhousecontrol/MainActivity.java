package com.example.greenhousecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LineChart[] charts = new LineChart[5];
    private Button windowBtn;
    boolean windowOpen;
    private ArrayList<Entry>[] entries = new ArrayList[5];
    private int maxEntries = 15;
    Handler handler;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        editor = sharedPreferences.edit();

        Button settingsBtn = findViewById(R.id.settings);
        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();
            }
        });

        windowBtn = findViewById(R.id.windowBtn);
        windowBtn.setOnClickListener(windowBtnClick);
        windowOpen = sharedPreferences.getBoolean("windowOpen", false);
        windowBtn.setText("Форточка " + (windowOpen ? "открыта" : "закрыта"));
        windowBtn.setBackgroundColor(windowOpen ? Color.GREEN : Color.RED);

        charts[0] = findViewById(R.id.chart);
        charts[1] = findViewById(R.id.chart1);
        charts[2] = findViewById(R.id.chart2);
        charts[3] = findViewById(R.id.chart3);
        charts[4] = findViewById(R.id.chartAvg);

        for (int i = 0; i < 5; i++)
            entries[i] = new ArrayList<Entry>();

        handler = new Handler();
        handler.postDelayed(updateData, 0);
    }

    private View.OnClickListener windowBtnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (API.setWindowState(windowOpen ? 0 : 1)){
                editor.putBoolean("windowOpen", !windowOpen);
                editor.commit();
                windowOpen = sharedPreferences.getBoolean("windowOpen", false);
                windowBtn.setText("Форточка " + (windowOpen ? "открыта" : "закрыта"));
                windowBtn.setBackgroundColor(windowOpen ? Color.GREEN : Color.RED);
            }
        }
    };

    private Runnable updateData = new Runnable() {

        public void run() {
            float tempSum = 0;
            for (int i = 0; i < 5; i++)
                for (Entry e: entries[i])
                    e.setX(e.getX() - 4);
            for (int i = 0; i < 4; i++) {
                try {
                    float temp = API.getTemperature(i + 1);
                    if (temp == -1)
                    {
                        showError();
                        return;
                    }
                    tempSum += temp;
                    entries[i].add(new Entry(0, temp));
                    if (entries[i].size() > maxEntries)
                        entries[i].remove(entries[i].get(0));
                }catch (Exception e)
                {
                    e.printStackTrace();
                    return;
                }
            }
            float avgTemp = tempSum / 4;
            if (avgTemp < sharedPreferences.getInt("minTemperature", 30))
            {
                windowBtn.setText("Форточка закрыта");
                windowBtn.setBackgroundColor(Color.RED);
                API.setWindowState(0);
                windowBtn.setEnabled(false);
            }
            else windowBtn.setEnabled(true);
            entries[4].add(new Entry(0, avgTemp));
            drawCharts();

            handler.postDelayed(updateData, 4000);
        }
    };

    void drawCharts()
    {
        try {
            for (int i = 0; i < 5; i++) {
                LineDataSet dataset = new LineDataSet(entries[i], i == 4 ? "Средняя температура" : "Датчик " + (i + 1));
                LineData data = new LineData(dataset);
                charts[i].setData(data);
                charts[i].invalidate();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    void showError()
    {
        Toast.makeText(this, "Произошла ошибка при соединении", Toast.LENGTH_LONG).show();
    }

    private void openSettings()
    {
        Intent myIntent = new Intent(this, SettingsActivity.class);
        startActivity(myIntent);
    }

}