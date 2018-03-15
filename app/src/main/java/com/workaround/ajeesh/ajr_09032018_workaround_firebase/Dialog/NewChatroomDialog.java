package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityChat;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.ChatMessage;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.Chatroom;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog
 * Created by ajesh on 14-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class NewChatroomDialog extends DialogFragment {
    private static final String logName = "FIREB-DIALOG-CHAT-NEW";

    //Firebase
    private FirebaseUser theFirebaseUser;
    private DatabaseReference theFbDbReference;

    //Widgets
    private EditText mChatRoomNameDialog;
    private SeekBar mSecurityLevelSeekbarDialog;
    private TextView mCreateChatroomDialog, mSecurityLevelDialog;
    private int mUserSecurityLevel;
    private int mSeekBarIndicatorValue;

    public NewChatroomDialog() {
        theFbDbReference = FirebaseDatabase.getInstance().getReference();
        theFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        LogHelper.LogThreadId(logName, "NewChatroomDialog - Initiated");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View theView = inflater.inflate(R.layout.new_chatroom_dialog, container, false);

        mChatRoomNameDialog = theView.findViewById(R.id.input_dialog_chatroom_name);
        mSecurityLevelSeekbarDialog = theView.findViewById(R.id.input_Dialog_security_level);
        mCreateChatroomDialog = theView.findViewById(R.id.create_dialog_chatroom);
        mSecurityLevelDialog = theView.findViewById(R.id.security_level);

        mSeekBarIndicatorValue = 0;
        mSecurityLevelDialog.setText(String.valueOf(mSeekBarIndicatorValue));
        LogHelper.LogThreadId(logName, "NewChatroomDialog - All values are assigned.");
        getUserSecurityLevel();

        mCreateChatroomDialog.setOnClickListener(CreateNewChatroomListener());
        mSecurityLevelSeekbarDialog.setOnSeekBarChangeListener(SeekbarChangeListener());
        return theView;
    }

    private SeekBar.OnSeekBarChangeListener SeekbarChangeListener() {
        LogHelper.LogThreadId(logName, "NewChatroomDialog - Seekbar listener - Started");
        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LogHelper.LogThreadId(logName, "NewChatroomDialog - Seekbar - OnPrgressChanged with : " + progress);
                mSeekBarIndicatorValue = progress;
                mSecurityLevelDialog.setText(String.valueOf(mSeekBarIndicatorValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                LogHelper.LogThreadId(logName, "NewChatroomDialog - Seekbar - OnStartTrackingTouch" + seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                LogHelper.LogThreadId(logName, "NewChatroomDialog - Seekbar - OnStopTrackingTouch " + seekBar);
            }
        };

        return listener;
    }

    private View.OnClickListener CreateNewChatroomListener() {
        LogHelper.LogThreadId(logName, "NewChatroomDialog - CreateNewChatroomListener - Initiated");
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mChatRoomNameDialog.getText().toString().equals("")) {
                    LogHelper.LogThreadId(logName, "NewChatroomDialog - CreateNewChatroomListener - Chatroom name is not empty");
                    if (mUserSecurityLevel >= mSecurityLevelSeekbarDialog.getProgress()) {
                        LogHelper.LogThreadId(logName, "NewChatroomDialog - CreateNewChatroomListener - chatroom security level passed");

                        //get the new chatroom unique id
                        String chatRoomId = theFbDbReference
                                .child(getString(R.string.dbnode_chatrooms))
                                .push()
                                .getKey();

                        LogHelper.LogThreadId(logName, "NewChatroomDialog Chatroom ID created as :" + chatRoomId);

                        //Create the Chatroom
                        Chatroom theChatroom = new Chatroom();
                        theChatroom.setChatroom_id(chatRoomId);
                        theChatroom.setCreator_id(theFirebaseUser.getUid());
                        theChatroom.setChatroom_name(mChatRoomNameDialog.getText().toString());
                        theChatroom.setSecurity_level(String.valueOf(mSecurityLevelSeekbarDialog.getProgress()));

                        //Insert the new chatroom into the database
                        theFbDbReference.child(getString(R.string.dbnode_chatrooms))
                                .child(chatRoomId)
                                .setValue(theChatroom);

                        //Create a unique id for the message
                        String messageId = theFbDbReference
                                .child(getString(R.string.dbnode_chatrooms))
                                .push()
                                .getKey();

                        LogHelper.LogThreadId(logName, "NewChatroomDialog - Message Id created as : " + messageId);


                        //Insert the First message into the chatroom
                        ChatMessage theChatMessage = new ChatMessage();
                        theChatMessage.setMessage("Welcome to New AJR Chatroom..!!!");
                        theChatMessage.setTimestamp(getTimestamp());


                        theFbDbReference
                                .child(getString(R.string.dbnode_chatrooms))
                                .child(chatRoomId)
                                .child(getString(R.string.field_chatroom_messages))
                                .child(messageId)
                                .setValue(theChatMessage);
                        LogHelper.LogThreadId(logName, "NewChatroomDialog - Finished creating new chatroom");

                        ((ActivityChat) getActivity()).init();
                        getDialog().dismiss();
                    } else {
                        Toast.makeText(getActivity(), "insuffient security level", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    LogHelper.LogThreadId(logName, "NewChatroomDialog - CreateNewChatroomListener - Chatroom dialog name cannot be empty");
                    Toast.makeText(getActivity(), "Please enter chatroom name", Toast.LENGTH_SHORT).show();
                }
            }
        };

        return listener;
    }

    private void getUserSecurityLevel() {
        Query theQuery = theFbDbReference.child(getString(R.string.dbnode_users))
                .orderByKey()
                .equalTo(theFirebaseUser.getUid());

        theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    LogHelper.LogThreadId(logName, "NewChatroomDialog - User given security level : " +
                            singleSnapshot.getValue(User.class).getSecurity_level());
                    mUserSecurityLevel = Integer.parseInt(singleSnapshot.getValue(User.class).getSecurity_level());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "NewChatroomDialog - Retriving security level for user has been cancelled.");
            }
        });
    }

    /**
     * Return the current timestamp in the form of a string
     *
     * @return
     */
    private String getTimestamp() {
        LogHelper.LogThreadId(logName, "NewChatroomDialog - getTimestamp - Started");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        LogHelper.LogThreadId(logName, "NewChatroomDialog - getTimestamp - Time passed as : " + sdf.format(new Date()));
        return sdf.format(new Date());
    }
}
