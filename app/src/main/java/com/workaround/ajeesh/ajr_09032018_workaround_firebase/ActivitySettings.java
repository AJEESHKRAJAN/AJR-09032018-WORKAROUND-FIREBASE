package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

public class ActivitySettings extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-SETTINGS";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private ValidationHelper helper;

    private EditText mUserName, mUserPhone, mEmail, mCurrentPassword;
    private Button mSave;
    private ProgressBar mProgressBar;
    private TextView mResetPasswordLink;
    private DatabaseReference mDbFirebaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LogHelper.LogThreadId(logName, "User Settings Activity has been initiated.");
        user = FirebaseAuth.getInstance().getCurrentUser();
        helper = new ValidationHelper();
        mDbFirebaseReference = FirebaseDatabase.getInstance().getReference();
        setupFirebaseAuth();



        mUserName = findViewById(R.id.input_user_name);
        mUserPhone = findViewById(R.id.input_user_phone);
        mEmail = findViewById(R.id.input_email);
        mCurrentPassword = findViewById(R.id.input_password);
        mSave = findViewById(R.id.btn_save);
        mProgressBar = findViewById(R.id.progressBar);
        mResetPasswordLink = findViewById(R.id.change_password);


        showCurrentUserEmail();


        mSave.setOnClickListener(SaveEmailChange());
        mResetPasswordLink.setOnClickListener(ResetPassword());
    }

    private void showCurrentUserEmail() {
        if (user != null) {
            mEmail.setText(user.getEmail());
        }
    }

    private View.OnClickListener ResetPassword() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "ResetPassword : started.");
                if (user.getEmail() != null) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    LogHelper.LogThreadId(logName, "Password reset link has been sent to registered email.");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    LogHelper.LogThreadId(logName, "Failed in resetting password.");
                                }
                            });
                } else {
                    Toast.makeText(ActivitySettings.this, "User email not found", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return listener;

    }

    private View.OnClickListener SaveEmailChange() {


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "SaveEmailChange : started.");
                if (!helper.isEmpty(mEmail.getText().toString()) && !helper.isEmpty(mCurrentPassword.getText().toString())) {
                    if (helper.isValidDomain(mEmail.getText().toString())) {
                        if (!user.getEmail().equalsIgnoreCase(mEmail.getText().toString())) {

                            LogHelper.LogThreadId(logName, "Changing registered email is triggered.");
                            showDialog();

                            AuthCredential currentCredentials = EmailAuthProvider.getCredential(user.getEmail(), mCurrentPassword.getText().toString());

                            LogHelper.LogThreadId(logName, "Re-authenticating with:  \n Registered Email " + user.getEmail()
                                    + " \n Password: " + mCurrentPassword.getText().toString());


                            user.reauthenticate(currentCredentials)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ActivitySettings.this, "Change of Email initiated", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().fetchProvidersForEmail(mEmail.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                                            if (task.isSuccessful()) {
                                                                LogHelper.LogThreadId(logName,
                                                                        "Entered email is not a part of Firebase. Good to change.");
                                                                int noOfEmailFound = task.getResult().getProviders().size();

                                                                if (noOfEmailFound == 1) {
                                                                    LogHelper.LogThreadId(logName,
                                                                            "Entered email is already in use.");
                                                                    hideDialog();
                                                                    Toast.makeText(ActivitySettings.this, "Email already in use",
                                                                            Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    LogHelper.LogThreadId(logName,
                                                                            "Entered email is all set for updating.");

                                                                    user.updateEmail(mEmail.getText().toString())
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        LogHelper.LogThreadId(logName,
                                                                                                "Email has been changed");
                                                                                        Toast.makeText(ActivitySettings.this, "Email Changed",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        hideDialog();
                                                                                        SendVerificationEmail();
                                                                                        FirebaseAuth.getInstance().signOut();
                                                                                        checkAuthenticationState();
                                                                                    }
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    LogHelper.LogThreadId(logName,
                                                                                            "Error in updating email.");
                                                                                    Toast.makeText(ActivitySettings.this,
                                                                                            "Error in updating email",
                                                                                            Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            hideDialog();
                                                            Toast.makeText(ActivitySettings.this,
                                                                    "Unable to update email", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ActivitySettings.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            /*---------Changing User name and phone number in Firebase db ------------*/
                            if (!helper.isEmpty(mUserName.getText().toString())) {

                                mDbFirebaseReference
                                        .child(getString(R.string.dbnode_users))
                                        .child(user.getUid())
                                        .child(getString(R.string.field_name))
                                        .setValue(mUserName.getText().toString());
                                LogHelper.LogThreadId(logName,
                                        "User name is updated");

                            }
                            if (!helper.isEmpty(mUserPhone.getText().toString())) {

                                mDbFirebaseReference
                                        .child(getString(R.string.dbnode_users))
                                        .child(user.getUid())
                                        .child(getString(R.string.field_phone))
                                        .setValue(mUserPhone.getText().toString());
                                LogHelper.LogThreadId(logName,
                                        "User phone is updated.");

                            }
                            Toast.makeText(ActivitySettings.this, "Please enter a new Email.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ActivitySettings.this, "Invalid Domain", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ActivitySettings.this, "Email/Password is mandate to authenticate.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return listener;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            checkAuthenticationState();
        }
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
                    Intent intent = new Intent(ActivitySettings.this, ActivityMain.class);
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

            Intent theIntent = new Intent(ActivitySettings.this, ActivityMain.class);
            theIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(theIntent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + this.toString() + " activity.");
        }
    }

    private void SendVerificationEmail() {
        LogHelper.LogThreadId(logName, "Send a verification email to the user.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivitySettings.this, "Sent verification email", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ActivitySettings.this, "Couldn't send verification email", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
