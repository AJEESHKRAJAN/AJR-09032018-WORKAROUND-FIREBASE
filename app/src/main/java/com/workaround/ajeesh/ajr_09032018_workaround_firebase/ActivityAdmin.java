package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

public class ActivityAdmin extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-ADMIN";

    //Firebase
    private FirebaseUser theFireBaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        theFireBaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (theFireBaseUser != null) {
            checkAuthenticationState();
        }
    }

    private void checkAuthenticationState() {
        LogHelper.LogThreadId(logName, "Checking Authentication State");

        if (theFireBaseUser == null) {
            LogHelper.LogThreadId(logName, "User logged out from activity " + this.toString());

            Intent theIntent = new Intent(ActivityAdmin.this, ActivityMain.class);
            theIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(theIntent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + this.getTitle() + " activity.");
        }
    }
}
