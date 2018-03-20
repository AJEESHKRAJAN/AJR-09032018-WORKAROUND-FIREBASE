package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityAdmin;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog
 * Created by ajesh on 20-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class NewDepartmentDialog extends DialogFragment {
    private static final String logName = "FIREB-DIALOG-DEPT-NEW-";

    //Vars
    private ValidationHelper theHelper;
    //widgets
    private EditText mNewDepartment;
    private TextView mConfirmDialog;

    public NewDepartmentDialog() {
        LogHelper.LogThreadId(logName, "NewDepartmentDialog : Initiated");
        theHelper = new ValidationHelper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.dialog_add_department, container, false);

        mNewDepartment = theView.findViewById(R.id.input_new_department);
        mConfirmDialog =  theView.findViewById(R.id.dialogConfirm);

        LogHelper.LogThreadId(logName, "NewDepartmentDialog : View is set");

        mConfirmDialog.setOnClickListener(confirmSelectedDialog());
        return theView;
    }

    private View.OnClickListener confirmSelectedDialog() {
        View.OnClickListener theListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!theHelper.isEmpty(mNewDepartment.getText().toString())){
                    LogHelper.LogThreadId(logName, "NewDepartmentDialog :onClick: adding new department to the list.");
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    reference
                            .child(getString(R.string.dbnode_departments))
                            .child(mNewDepartment.getText().toString())
                            .setValue(mNewDepartment.getText().toString());

                    LogHelper.LogThreadId(logName, "NewDepartmentDialog : New department has been added.");

                    getDialog().dismiss();

                    LogHelper.LogThreadId(logName, "NewDepartmentDialog : Returning back to Admin Activity");
                    ((ActivityAdmin)getActivity()).getDepartments();
                }
            }
        };
        return theListener;
    }
}
