package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

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
                if (!helper.IsEmpty(mEmail.getText().toString()) && !helper.IsEmpty(mPassword.getText().toString()) && !helper.IsEmpty(mConfirmPassword.getText().toString())) {
                    if (helper.isValidDomain(mEmail.getText().toString())) {
                        if (helper.doStringsMatch(mPassword.getText().toString(), mConfirmPassword.getText().toString())) {
                            LogHelper.LogThreadId(logName, "All conditions met. Good to register in Firebase now.");
                            registerNewUser(mEmail.getText().toString(), mPassword.getText().toString());
                        } else {
                            Toast.makeText(ActivityRegister.this, "Invalid Domain. Please contact your supervisor..", Toast.LENGTH_LONG).show();
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
