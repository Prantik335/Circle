package com.prantik.circle.Activities;

import androidx.appcompat.app.AppCompatActivity;

import com.prantik.circle.R;

import android.os.Bundle;

public class StatusesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statuses);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Statuses");
        }
    }
}