package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters.ChatroomMessageListAdapter;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.ChatMessage;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.Chatroom;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ActivityChatroom extends AppCompatActivity {

    private static final String logName = "FIREB-ACT-CHAT-ROOM";

    //Firebase
    private FirebaseUser theFirebaseUser;
    private DatabaseReference theFirebaseDB;
    private FirebaseAuth.AuthStateListener mAuthListener;

    //widgets
    private TextView mChatroomName;
    private ListView mListView;
    private EditText mMessage;
    private ImageView mCheckmark;

    //vars
    private Chatroom mChatroom;
    private List<ChatMessage> mMessagesList;
    private ChatroomMessageListAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        theFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        theFirebaseDB  = FirebaseDatabase.getInstance().getReference();

        mChatroomName = findViewById(R.id.text_chatroom_name);
        mListView = findViewById(R.id.listView);
        mMessage = findViewById(R.id.input_message);
        mCheckmark = findViewById(R.id.checkmark);

        getSupportActionBar().hide();
        mMessagesList = new ArrayList<>();
        LogHelper.LogThreadId(logName, "ActivityChatroom - Initiated");

        setupFirebaseAuth();
        getChatroom();
        init();
        hideSoftKeyboard();
    }

    private void getChatroom() {
        Intent theIntent = getIntent();

        if (theIntent.hasExtra(getString(R.string.intent_chatroom))) {
            LogHelper.LogThreadId(logName, "ActivityChatroom - intent has extras and so new chat room is loaded");
            Chatroom theChatroom = theIntent.getParcelableExtra(getString(R.string.intent_chatroom));

            mChatroom = theChatroom;
            mChatroomName.setText(mChatroom.getChatroom_name());

            enableChatroomListener();
        }
    }

    private void init() {
        mMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListView.setSelection(mAdapter.getCount() - 1); //scroll to the bottom of the list
            }
        });

        mCheckmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!mMessage.getText().toString().equals("")) {
                    String message = mMessage.getText().toString();
                    LogHelper.LogThreadId(logName, "Activity Chatroom - onClick: sending new message: " + message);

                    //create the new message object for inserting
                    ChatMessage newMessage = new ChatMessage();
                    newMessage.setMessage(message);
                    newMessage.setTimestamp(getTimestamp());
                    newMessage.setUser_id(theFirebaseUser.getUid());

                    //get a database reference
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.dbnode_chatrooms))
                            .child(mChatroom.getChatroom_id())
                            .child(getString(R.string.field_chatroom_messages));

                    //create the new messages id
                    String newMessageId = reference.push().getKey();
                    LogHelper.LogThreadId(logName, "ActivityChatroom - newMessageId" + newMessageId);
                    //insert the new message into the chatroom
                    reference
                            .child(newMessageId)
                            .setValue(newMessage);

                    //clear the EditText
                    mMessage.setText("");

                    //refresh the messages list? Or is it done by the listener??
                }

            }
        });
    }

    private void enableChatroomListener() {
         /*
            ---------- Listener that will watch the 'chatroom_messages' node ----------
         */
        LogHelper.LogThreadId(logName, "ActivityChatroom - Enable chatroom listener");
        theFirebaseDB = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_chatrooms))
                .child(mChatroom.getChatroom_id())
                .child(getString(R.string.field_chatroom_messages));

        theFirebaseDB.addValueEventListener(mValueEventListener);
    }

    ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            getChatroomMessages();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void getChatroomMessages() {
        mMessagesList = new ArrayList<>();
        if (mMessagesList.size() > 0) {
            mMessagesList.clear();
            mAdapter.clear();
        }
        LogHelper.LogThreadId(logName, "ActivityChatroom - getChatroomMessages - Started");
        theFirebaseDB = FirebaseDatabase.getInstance().getReference();
        Query theQuery = theFirebaseDB
                .child(getString(R.string.dbnode_chatrooms))
                .child(mChatroom.getChatroom_id())
                .child(getString(R.string.field_chatroom_messages));

        theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    try {//need to catch null pointer here because the initial welcome message to the
                        //chatroom has no user id
                        ChatMessage theChatMessage = new ChatMessage();

                        String userId = singleSnapshot.getValue(ChatMessage.class).getUser_id();
                        LogHelper.LogThreadId(logName, "ActivityChatroom - getChatroomMessages - UserID" + userId);
                        if (userId != null) {
                            theChatMessage.setMessage(singleSnapshot.getValue(ChatMessage.class).getMessage());
                            theChatMessage.setUser_id(singleSnapshot.getValue(ChatMessage.class).getUser_id());
                            theChatMessage.setTimestamp(singleSnapshot.getValue(ChatMessage.class).getTimestamp());
                        } else {
                            theChatMessage.setMessage(singleSnapshot.getValue(ChatMessage.class).getMessage());
                            theChatMessage.setTimestamp(singleSnapshot.getValue(ChatMessage.class).getTimestamp());
                        }
                        mMessagesList.add(theChatMessage);

                    } catch (NullPointerException e) {
                        LogHelper.LogThreadId(logName, "onDataChange: NullPointerException: " + e.getMessage());
                    }
                }
                //query the users node to get the profile images and names
                getUserDetails();
                initMessagesList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "ActivityChatroom - getChatroomMessages - Data cancelled");
            }
        });
    }

    private void getUserDetails() {
        for (int i = 0; i < mMessagesList.size(); i++) {
            final int j = i;
            if (mMessagesList.get(i).getUser_id() != null) {
                Query theQuery = theFirebaseDB
                        .child(getString(R.string.dbnode_users))
                        .orderByKey()
                        .equalTo(mMessagesList.get(i).getUser_id());

                theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                        LogHelper.LogThreadId(logName, "ActivityChatroom - getUserDetails - started");
                        mMessagesList.get(j).setProfile_image(singleSnapshot.getValue(User.class).getProfile_image());
                        mMessagesList.get(j).setName(singleSnapshot.getValue(User.class).getName());
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        LogHelper.LogThreadId(logName, "ActivityChatroom - getUserDetails - csncelled");
                    }
                });

            }
        }
    }

    private void initMessagesList() {
        LogHelper.LogThreadId(logName, "ActivityChatroom - initMessageList - Started");
        mAdapter = new ChatroomMessageListAdapter(ActivityChatroom.this, R.layout.layout_adapter_chatmessage_list, mMessagesList);
        mListView.setAdapter(mAdapter);
        LogHelper.LogThreadId(logName, "ActivityChatroom - initMessageList - ChatroomMessageListAdapter - Count : " + mAdapter.getCount());
        mListView.setSelection(mAdapter.getCount() - 1);
        LogHelper.LogThreadId(logName, "ActivityChatroom - initMessageList - Total messages count : " + mAdapter.getCount());
    }

    /*
            ----------------------------- Firebase setup ---------------------------------
    */

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    private void checkAuthenticationState() {
        LogHelper.LogThreadId(logName, "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            LogHelper.LogThreadId(logName, "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(ActivityChatroom.this, ActivityMain.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + this.getTitle() + " activity.");
        }
    }

    private void setupFirebaseAuth() {
        LogHelper.LogThreadId(logName, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    LogHelper.LogThreadId(logName, "onAuthStateChanged:signed_in:" + user.getUid());


                } else {
                    // User is signed out
                    LogHelper.LogThreadId(logName, "onAuthStateChanged:signed_out");
                    Toast.makeText(ActivityChatroom.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ActivityChatroom.this, ActivityMain.class);
                    startActivity(intent);
                    finish();
                }
            }
        };

    }

    /**
     * Return the current timestamp in the form of a string
     *
     * @return
     */
    private String getTimestamp() {
        LogHelper.LogThreadId(logName, "ActivityChatroom - getTimestamp - Started");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("IST"));
        LogHelper.LogThreadId(logName, "ActivityChatroom - getTimestamp - Time passed as : " + sdf.format(new Date()));
        return sdf.format(new Date());
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        LogHelper.LogThreadId(logName, "ActivityChatroom - OnDestroy - Started");
        super.onDestroy();
        theFirebaseDB.removeEventListener(mValueEventListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);
        LogHelper.LogThreadId(logName, "ActivityChatroom - OnStart - Started");
    }

    @Override
    public void onStop() {
        super.onStop();
        LogHelper.LogThreadId(logName, "ActivityChatroom - OnStop - Started");
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }
    }
}
