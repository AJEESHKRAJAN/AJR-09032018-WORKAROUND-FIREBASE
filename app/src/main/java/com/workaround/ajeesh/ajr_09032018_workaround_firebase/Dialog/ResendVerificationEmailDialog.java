package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;


/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog
 * Created by ajesh on 10-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class ResendVerificationEmailDialog extends DialogFragment {
    private static final String logName = "FIREB-DIALOG-RESEND-EMAIL";
    private EditText mEmailVerification, mPasswordVerification;
    private TextView mCancel, mConfirm;

    private Context mContext;
    private ValidationHelper helper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.resend_verification_email_dialog, container, false);

        mEmailVerification = theView.findViewById(R.id.confirm_email);
        mPasswordVerification = theView.findViewById(R.id.confirm_password);
        mConfirm = theView.findViewById(R.id.dialogConfirm);
        mCancel = theView.findViewById(R.id.dialogCancel);
        mContext = getActivity();
        helper = new ValidationHelper();

        mConfirm.setOnClickListener(onDialogConfirm());
        mCancel.setOnClickListener(onDialogCancel());
        return theView;
    }


    private View.OnClickListener onDialogConfirm() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!helper.isEmpty(mEmailVerification.getText().toString()) && !helper.isEmpty(mPasswordVerification.getText().toString())) {
                    LogHelper.LogThreadId(logName, "Entered resend verification details.");
                    authenticateAndResendEmailVerification(mEmailVerification.getText().toString(), mPasswordVerification.getText().toString());
                } else {
                    Toast.makeText(mContext, "All fields are mandatory. Don't leave it blank.", Toast.LENGTH_LONG).show();
                }
            }
        };

        return listener;
    }


    private View.OnClickListener onDialogCancel() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "Cancelled resend email verification");
                getDialog().dismiss();
            }
        };
        return listener;
    }


    private void authenticateAndResendEmailVerification(String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    LogHelper.LogThreadId(logName, "User verified. All set to send verification email link");
                    SendVerificationEmail();
                    FirebaseAuth.getInstance().signOut();
                    getDialog().dismiss();
                } else {
                    LogHelper.LogThreadId(logName, "User is not registered yet.");

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext, "Invalid Credentials. \nReset your password and try again", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(mContext, "Sent verification email", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, "Couldn't send verification email", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
