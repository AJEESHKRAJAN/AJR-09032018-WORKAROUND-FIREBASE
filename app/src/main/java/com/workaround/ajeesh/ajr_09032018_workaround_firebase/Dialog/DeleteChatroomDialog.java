package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityChat;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog
 * Created by ajesh on 14-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class DeleteChatroomDialog extends DialogFragment {
    private static final String logName = "FIREB-DIALOG-DEL-CHATROOM";

    private String mChatroomId;

    private TextView mDelete, mCancel;

    private DatabaseReference theFirebaseDB;

    public DeleteChatroomDialog() {
        super();
        setArguments(new Bundle());
        theFirebaseDB = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChatroomId = getArguments().getString(String.valueOf(R.string.field_chatroom_id));

        if (mChatroomId != null) {
            LogHelper.LogThreadId(logName, "onCreate: got the chatroom id: " + mChatroomId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.dialog_delete_chatroom, container, false);

        mDelete = theView.findViewById(R.id.confirm_delete);
        mCancel = theView.findViewById(R.id.cancel);

        mDelete.setOnClickListener(DeleteChat());
        mCancel.setOnClickListener(CancelChat());
        return theView;
    }


    private View.OnClickListener DeleteChat() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                theFirebaseDB
                        .child(String.valueOf(R.string.dbnode_chatrooms))
                        .child(mChatroomId)
                        .removeValue();

                getDialog().dismiss();
                ((ActivityChat) getActivity()).init();
            }
        };
        return listener;
    }

    private View.OnClickListener CancelChat() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        };
        return listener;
    }

}
