package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.BuildConfig;
import com.udacity.stockhawk.R;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = BuildConfig.APPLICATION_ID + ".SYMBOL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_SYMBOL)) {
            setTitle(intent.getStringExtra(EXTRA_SYMBOL));
        }
    }
}
