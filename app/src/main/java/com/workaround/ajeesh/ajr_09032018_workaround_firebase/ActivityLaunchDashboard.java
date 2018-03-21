package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.UniversalImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.Chatroom;

public class ActivityLaunchDashboard extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-DASHBOARD";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String userId, userName, userEmail;
    private Uri userProfileUri;
    private FirebaseUser user;
    public static boolean isActivityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        user = FirebaseAuth.getInstance().getCurrentUser();
        setupFirebaseAuth();
        LogHelper.LogThreadId(logName, "Activity Dashboard has been initiated.");

        //getUserDetails();
        setUserDetails();
        getPendingIntent();
        initUniversalImageLoader();
        InitializeFirebaseMessagingToken();

        FloatingActionButton fab = findViewById(R.id.fabDashboard);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Employee Dashboard - More to come...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void InitializeFirebaseMessagingToken() {
        LogHelper.LogThreadId(logName, "InitializeFirebaseMessagingToken - Called");
        String token = FirebaseInstanceId.getInstance().getToken();
        LogHelper.LogThreadId(logName, "InitializeFirebaseMessagingToken - Token : " + token);

        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        LogHelper.LogThreadId(logName, "sendRegistrationToServer - Initiated");

        DatabaseReference theFirebaseDb = FirebaseDatabase.getInstance().getReference();

        theFirebaseDb.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(token);
        LogHelper.LogThreadId(logName, "sendRegistrationToServer - The token has been registered from the launch dashboard activity");
    }

    private void getUserDetails() {
        LogHelper.LogThreadId(logName, "Retrieving user details from Firebase.");

        userId = user.getUid();
        userName = user.getDisplayName();
        userEmail = user.getEmail();
        userProfileUri = user.getPhotoUrl();

        LogHelper.LogThreadId(logName, "User Details : \n" +
                "User ID : " + userId + "\n" +
                "User Name : " + userName + "\n" +
                "User Email : " + userEmail + "\n" +
                "User Profile URL : " + userProfileUri + "\n");
    }

    private void setUserDetails() {
        if (user != null) {
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName("Ajeesh K Rajan")
                    .setPhotoUri(Uri.parse("https://www.gstatic.com/webp/gallery3/2_webp_ll.png"))
                    .build();

            user.updateProfile(profileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            LogHelper.LogThreadId(logName, "User profile is updated successfully.");
                            getUserDetails();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            LogHelper.LogThreadId(logName, "Failed to update user profile");
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        boolean handled = true;
        switch (id) {
            case R.id.actionSignOut: {
                signOut();
                break;
            }
            case R.id.actionAccountSettings: {
                Intent theIntent = new Intent(this, ActivitySettings.class);
                startActivity(theIntent);
                break;
            }
            case R.id.actionChat: {
                Intent theIntent = new Intent(this, ActivityChat.class);
                startActivity(theIntent);
                break;
            }
            case R.id.actionAdmin: {
                Intent theIntent = new Intent(this, ActivityAdmin.class);
                startActivity(theIntent);
                break;
            }
            default:
                handled = super.onOptionsItemSelected(item);
                break;
        }
        return handled;
    }


    private void getPendingIntent(){
        LogHelper.LogThreadId(logName, "getPendingIntent: checking for pending intents.");

        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_chatroom))){
            LogHelper.LogThreadId(logName,  "getPendingIntent: pending intent detected.");

            //get the chatroom
            Chatroom chatroom = intent.getParcelableExtra(getString(R.string.intent_chatroom));
            //navigate to the chatoom
            Intent chatroomIntent = new Intent(ActivityLaunchDashboard.this, ActivityChatroom.class);
            chatroomIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
            startActivity(chatroomIntent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    /**
     * Sign out the current user
     */
    private void signOut() {
        LogHelper.LogThreadId(logName, "signOut: signing out");
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(ActivityLaunchDashboard.this, ActivityMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setupFirebaseAuth() {
        LogHelper.LogThreadId(logName, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {

                    LogHelper.LogThreadId(logName, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    LogHelper.LogThreadId(logName, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ActivityLaunchDashboard.this, ActivityMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }


    private void checkAuthenticationState() {
        LogHelper.LogThreadId(logName, "Checking Authentication State");

        if (user == null) {
            LogHelper.LogThreadId(logName, "User logged out from activity " + this.toString());

            Intent theIntent = new Intent(ActivityLaunchDashboard.this, ActivityMain.class);
            theIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(theIntent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + this.getTitle() + " activity.");
        }
    }

    private void initUniversalImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }
}
