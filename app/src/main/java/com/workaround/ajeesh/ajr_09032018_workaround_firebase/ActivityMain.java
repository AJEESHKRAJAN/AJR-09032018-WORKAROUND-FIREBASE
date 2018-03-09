package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ActivityMain extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView register = (TextView) findViewById(R.id.link_register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityMain.this, ActivityRegister.class);
                startActivity(intent);
            }
        });

    }


}
