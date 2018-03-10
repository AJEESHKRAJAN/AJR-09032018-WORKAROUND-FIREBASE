package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog.ResendVerificationEmailDialog;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

public class ActivityMain extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-MAIN";
    private EditText mEmail, mPassword;
    private TextView mRegister, mForgotPassword, mResendVerificationEmail;
    private Button mSignIn;
    private ProgressBar mProgressBar;
    private ValidationHelper helper;

    private FirebaseAuth.AuthStateListener mFireBaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogHelper.LogThreadId(logName, "Login Activity initiated.");

        SetupFirebaseAuthListener();

        helper = new ValidationHelper();
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mProgressBar = findViewById(R.id.progressBar);
        mForgotPassword = findViewById(R.id.forgot_password);
        mResendVerificationEmail = findViewById(R.id.resend_verification_email);
        mRegister = findViewById(R.id.link_register);
        mSignIn = findViewById(R.id.email_sign_in_button);
        LogHelper.LogThreadId(logName, "Widgets registered..");

        mSignIn.setOnClickListener(SignInListener());
        mRegister.setOnClickListener(RegisterLinkListener());
        mForgotPassword.setOnClickListener(ForgotPasswordListener());
        mResendVerificationEmail.setOnClickListener(ResendVerEmailListener());
        LogHelper.LogThreadId(logName, "All Listeners processed.");

    }


    private View.OnClickListener SignInListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!helper.IsEmpty(mEmail.getText().toString()) && !helper.IsEmpty(mPassword.getText().toString())) {
                    LogHelper.LogThreadId(logName, "Sign in triggered...");
                    showDialog();
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    LogHelper.LogThreadId(logName, "Sign in status..." + task.isSuccessful());
                                    hideDialog();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(ActivityMain.this, "Failed to log in.", Toast.LENGTH_LONG).show();
                                }
                            });
                    hideDialog();
                } else {
                    Toast.makeText(ActivityMain.this, "Email / Password cannot be blank.", Toast.LENGTH_LONG).show();
                }
            }
        };
        return listener;
    }

    private View.OnClickListener RegisterLinkListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent theIntent = new Intent(ActivityMain.this, ActivityRegister.class);
                startActivity(theIntent);
            }
        };
        return listener;
    }

    private View.OnClickListener ResendVerEmailListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ResendVerificationEmailDialog dialog = new ResendVerificationEmailDialog();
                dialog.show(getSupportFragmentManager(), "dialog_resend_verification_email");
            }
        };
        return listener;
    }

    private View.OnClickListener ForgotPasswordListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
        return listener;
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

    private void SetupFirebaseAuthListener() {
        mFireBaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    if (currentUser.isEmailVerified()) {
                        LogHelper.LogThreadId(logName, "onAuthStateChanged - Signed in as " + currentUser.getUid());
                        Toast.makeText(ActivityMain.this, "Authenticated with " + currentUser.getEmail(), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ActivityMain.this, "Check the verification email sent to your registered email address.",
                                Toast.LENGTH_LONG).show();
                        LogHelper.LogThreadId(logName, "onAuthStateChanged - Email not verified for the user " + currentUser.getUid());
                    }

                } else {
                    LogHelper.LogThreadId(logName, "onAuthStateChanged - Signed out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mFireBaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFireBaseAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mFireBaseAuthListener);
        }

    }
}
