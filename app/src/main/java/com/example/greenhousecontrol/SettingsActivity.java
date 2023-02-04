package com.example.greenhousecontrol;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(
                SettingsActivity.this);

        NumberPicker tempNum = findViewById(R.id.tempNum);
        tempNum.setMinValue(25);
        tempNum.setMaxValue(35);
        tempNum.setWrapSelectorWheel(false);
        tempNum.setValue(sharedPreferences.getInt("minTemperature", 30));

        Button saveBtn = findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("minTemperature", tempNum.getValue());
                editor.commit();
                finish();
            }
        });
    }
}