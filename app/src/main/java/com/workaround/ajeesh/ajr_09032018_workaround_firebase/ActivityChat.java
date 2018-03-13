package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseUser;

public class ActivityChat extends AppCompatActivity {
    private static final String TAG = "FIREB-ACT-CHAT";

    //Firebase
    private FirebaseUser theFireBaseUser;

    //widgets
    private ListView mListView;
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mListView = findViewById(R.id.listView);
        mFab = findViewById(R.id.fob);
    }
}
