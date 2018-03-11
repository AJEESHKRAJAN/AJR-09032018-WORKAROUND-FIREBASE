package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;

public class ActivityRegister extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-REG";
    private ValidationHelper helper;

    private EditText mEmail, mPassword, mConfirmPassword;
    private Button mRegister;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        helper = new ValidationHelper();

        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mRegister = findViewById(R.id.btn_register);
        mProgressBar = findViewById(R.id.progressBar);

        LogHelper.LogThreadId(logName, "All instances in Register user has been created.");
        mRegister.setOnClickListener(RegisterUser());
        hideSoftKeyboard();
    }

    private View.OnClickListener RegisterUser() {
        LogHelper.LogThreadId(logName, "Register User - OnCLick listener.");
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!helper.isEmpty(mEmail.getText().toString()) && !helper.isEmpty(mPassword.getText().toString()) && !helper.isEmpty(mConfirmPassword.getText().toString())) {
                    if (helper.isValidDomain(mEmail.getText().toString())) {
                        if (helper.doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                            LogHelper.LogThreadId(logName, "All conditions met. Good to register in Firebase now.");
                            registerNewUser(mEmail.getText().toString(), mPassword.getText().toString());
                        } else {
                            Toast.makeText(ActivityRegister.this, "Password doesn't match.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ActivityRegister.this, "Invalid Domain. Please contact your supervisor..", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ActivityRegister.this, "All fields are mandatory. Don't leave it blank.", Toast.LENGTH_LONG).show();
                }
            }
        };
        return listener;
    }

    private void registerNewUser(final String email, String password) {
        showDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                LogHelper.LogThreadId(logName, "Firebase :  Auth : OnComplete Override method.");
                if (task.isSuccessful()) {
                    LogHelper.LogThreadId(logName, "Is User Creation Successful - " + task.isSuccessful());
                    LogHelper.LogThreadId(logName, "User Creation for : " + FirebaseAuth.getInstance().getCurrentUser().getUid());

                    SendVerificationEmail();


                    User user = new User();
                    user.setName(email.substring(0, email.indexOf('@')));
                    user.setPhone("9790793380");
                    user.setProfile_image("");
                    user.setSecurity_level("1");
                    user.setUser_id(FirebaseAuth.getInstance().getCurrentUser().getUid());

                    FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbnode_users))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    LogHelper.LogThreadId(logName, "New user has been added to firebase database.");

                                    FirebaseAuth.getInstance().signOut();
                                    redirectLoginScreen();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ActivityRegister.this, "Failed to add user in firebase database"
                                            , Toast.LENGTH_SHORT).show();
                                    FirebaseAuth.getInstance().signOut();
                                    redirectLoginScreen();
                                }
                            });
                }
                if (!task.isSuccessful()) {
                    Toast.makeText(ActivityRegister.this, "Cannot Create new user", Toast.LENGTH_LONG).show();
                }
                hideDialog();
            }
        });
    }

    private void SendVerificationEmail() {
        LogHelper.LogThreadId(logName, "Send a verification email to the user.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivityRegister.this, "Sent verification email", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ActivityRegister.this, "Couldn't send verification email", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    /**
     * Redirects the user to the login screen
     */
    private void redirectLoginScreen() {
        LogHelper.LogThreadId(logName, "redirectLoginScreen: redirecting to login screen.");

        Intent intent = new Intent(this, ActivityMain.class);
        startActivity(intent);
        finish();
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
