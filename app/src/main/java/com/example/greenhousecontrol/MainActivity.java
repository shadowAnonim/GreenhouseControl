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
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private LineChart[] charts = new LineChart[5];
    private Button windowBtn;
    private TableLayout tl;
    private ArrayList<String[]> tableData = new ArrayList();
    private boolean windowOpen;
    private ArrayList<Entry>[] entries = new ArrayList[5];
    private int[] lineColors = new int[] {Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.BLACK};
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

        tl = findViewById(R.id.table);
        tableData.add(new String[] { "Время", "Номер", "Показания"});
        


        windowBtn = findViewById(R.id.windowBtn);
        windowBtn.setOnClickListener(windowBtnClick);
        windowOpen = sharedPreferences.getBoolean("windowOpen", false);
        windowBtn.setText("Форточка " + (windowOpen ? "открыта" : "закрыта"));
        windowBtn.setBackgroundColor(windowOpen ? Color.GREEN : Color.RED);

        charts[0] = findViewById(R.id.chartAvg);
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
                    float temp = API.getTemperature(i + 1);;
                    if (temp == -1)
                    {
                        showError();
                        return;
                    }
                    tempSum += temp;
                    entries[i].add(new Entry(0, temp));
                    tableData.add(1, new String[] {Calendar.getInstance().getTime().toString(),
                            String.valueOf(i + 1), String.valueOf(temp)});
                    if (entries[i].size() > maxEntries)
                    {
                        entries[i].remove(entries[i].get(0));
                    }
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
            if (entries[4].size() > maxEntries)
                entries[4].remove(entries[4].get(0));
            drawCharts();
            drawTables();

            handler.postDelayed(updateData, 4000);
        }
    };

    void drawCharts()
    {
        try {
            LineData data = new LineData();
            for (int i = 0; i < 5; i++) {
                LineDataSet dataset = new LineDataSet(entries[i], i == 4 ? "Средняя температура" : "Датчик " + (i + 1));
                dataset.setLineWidth(i == 4 ? 3 : 1);
                dataset.setColor(lineColors[i]);
                data.addDataSet(dataset);
                charts[0].invalidate();
            }
            charts[0].setData(data);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    void drawTables()
    {
        tl.removeAllViews();

        for (String[] line: tableData) {
            TableRow tr = new TableRow(this);
            tr.setBackgroundColor(Color.BLACK);
            tr.setPadding(0, 0, 0, 2); //Border between rows

            TableRow.LayoutParams llp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 0, 2, 0);//2px right-margin
            for (String s: line) {
                LinearLayout cell = new LinearLayout(this);
                cell.setBackgroundColor(Color.WHITE);
                cell.setLayoutParams(llp);//2px border on the right for the cell
                TextView tv = new TextView(this);
                tv.setText(s);
                tv.setPadding(0, 0, 4, 3);
                cell.addView(tv);
                tr.addView(cell);
            }
            tl.addView(tr);
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