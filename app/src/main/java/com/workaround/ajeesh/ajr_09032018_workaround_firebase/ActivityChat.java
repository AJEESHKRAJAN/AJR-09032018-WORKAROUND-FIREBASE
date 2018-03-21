package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters.ChatroomListAdapter;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog.DeleteChatroomDialog;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog.NewChatroomDialog;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.ChatMessage;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.Chatroom;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityChat extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-CHAT";

    //Firebase
    private FirebaseUser theFireBaseUser;
    private DatabaseReference theFireBaseDB;

    //widgets
    private ListView mListView;
    private FloatingActionButton mFab;

    //Vars
    private ArrayList<Chatroom> mChatroomArrayList;
    private ChatroomListAdapter mChatroomListAdapter;
    public static boolean isActivityRunning;
    private HashMap<String, String> mNumChatroomMessages;
    private int mSecurityLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        theFireBaseUser = FirebaseAuth.getInstance().getCurrentUser();
        theFireBaseDB = FirebaseDatabase.getInstance().getReference();

        mListView = findViewById(R.id.listView);
        mFab = findViewById(R.id.fob);

        LogHelper.LogThreadId(logName, "ActivityChat - Initiated");

        init();
    }

    public void init() {
        LogHelper.LogThreadId(logName, "ActivityChat - Init method called.");
        getChatrooms();
        mFab.setOnClickListener(OpenChatroomListener());
    }


    private View.OnClickListener OpenChatroomListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "ActivityChat - Opening new chatroom dialog");
                NewChatroomDialog newChatroomDialog = new NewChatroomDialog();
                newChatroomDialog.show(getSupportFragmentManager(), getString(R.string.dialog_new_chatroom));
            }
        };
        return listener;
    }

    private AdapterView.OnItemClickListener ListChatsListener() {
        AdapterView.OnItemClickListener adapterListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent theIntent = new Intent(ActivityChat.this, ActivityChatroom.class);
                theIntent.putExtra(getString(R.string.intent_chatroom), mChatroomArrayList.get(position));
                LogHelper.LogThreadId(logName, "ActivityChat - Opened a new chatroom activity for the chat @ position " + position);
                startActivity(theIntent);
            }
        };
        return adapterListener;
    }

    private void getChatrooms() {
        LogHelper.LogThreadId(logName, "ActivityChat - getChatrooms()");
        mChatroomArrayList = new ArrayList<>();
        mNumChatroomMessages = new HashMap<>();
        if (mChatroomListAdapter != null) {
            mChatroomListAdapter.clear();
            mChatroomArrayList.clear();
        }
        Query theQuery = theFireBaseDB.child(getString(R.string.dbnode_chatrooms));
        LogHelper.LogThreadId(logName, "ActivityChat - getChatrooms() - the query : " + theQuery);

        theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Chatroom theChatroom = new Chatroom();

                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                    if (objectMap != null) {
                        theChatroom.setChatroom_id(objectMap.get(getString(R.string.field_chatroom_id)).toString());
                        theChatroom.setChatroom_name(objectMap.get(getString(R.string.field_chatroom_name)).toString());
                        theChatroom.setCreator_id(objectMap.get(getString(R.string.field_creator_id)).toString());
                        theChatroom.setSecurity_level(objectMap.get(getString(R.string.field_security_level)).toString());
                    }

                    /*---------------------Alternatively------------------
                    theChatroom.setChatroom_id(singleSnapshot.getValue(Chatroom.class).getChatroom_id());
                    theChatroom.setSecurity_level(singleSnapshot.getValue(Chatroom.class).getSecurity_level());
                    theChatroom.setCreator_id(singleSnapshot.getValue(Chatroom.class).getCreator_id());
                    theChatroom.setChatroom_name(singleSnapshot.getValue(Chatroom.class).getChatroom_name());
                    */
                    LogHelper.LogThreadId(logName, "ActivityChat - Chatroom retrieved from db" +
                            "");
                    //Get the chatroom messages
                    ArrayList<ChatMessage> messageArrayList = new ArrayList<>();
                    int numMessages = 0;
                    for (DataSnapshot innerSingleDataSnapshot : singleSnapshot.child(getString(R.string.field_chatroom_messages))
                            .getChildren()) {
                        ChatMessage theChatMessage = new ChatMessage();
                        theChatMessage.setMessage(innerSingleDataSnapshot.getValue(ChatMessage.class).getMessage());
                        theChatMessage.setTimestamp(innerSingleDataSnapshot.getValue(ChatMessage.class).getTimestamp());
                        theChatMessage.setUser_id(innerSingleDataSnapshot.getValue(ChatMessage.class).getUser_id());
                        messageArrayList.add(theChatMessage);
                        numMessages++;
                        LogHelper.LogThreadId(logName, "ActivityChat - Chat messages retrieved from db");
                    }
                    theChatroom.setChatroom_messages(messageArrayList);

                    //add the number of chatrooms messages to a hashmap for reference
                    mNumChatroomMessages.put(theChatroom.getChatroom_id(), String.valueOf(numMessages));

                    //get the list of users who have joined the chatroom
                    List<String> users = new ArrayList<String>();
                    for (DataSnapshot snapshot : singleSnapshot
                            .child(getString(R.string.field_users)).getChildren()) {
                        String user_id = snapshot.getKey();
                        LogHelper.LogThreadId(logName, "onDataChange: user currently in chatroom: " + user_id);
                        users.add(user_id);
                    }
                    if (users.size() > 0) {
                        theChatroom.setUsers(users);
                    }

                    mChatroomArrayList.add(theChatroom);

                    LogHelper.LogThreadId(logName, "ActivityChat - total chat messages for chatroom Id : " +
                            theChatroom.getChatroom_id() + " is " + messageArrayList.size());
                }
                LogHelper.LogThreadId(logName, "ActivityChat - Total Chatrooms in db for the user id " +
                        theFireBaseUser.getUid() + " is " + mChatroomArrayList.size());
                setupChatroomList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "ActivityChat - Cancelled retrieving chatrooms and its " +
                        "messages from db reason : " + databaseError.getMessage() + " " + databaseError.getDetails());
            }
        });
    }

    private void setupChatroomList() {
        mChatroomListAdapter = new ChatroomListAdapter(ActivityChat.this, R.layout.layout_adapter_chatroom_list, mChatroomArrayList);
        mListView.setAdapter(mChatroomListAdapter);
        LogHelper.LogThreadId(logName, "ActivityChat - setupChatroomList");
        mListView.setOnItemClickListener(ListChatsListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (theFireBaseUser != null) {
            checkAuthenticationState();
        }
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

    private void checkAuthenticationState() {
        LogHelper.LogThreadId(logName, "Checking Authentication State");

        if (theFireBaseUser == null) {
            LogHelper.LogThreadId(logName, "User logged out from activity " + this.toString());

            Intent theIntent = new Intent(ActivityChat.this, ActivityMain.class);
            theIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(theIntent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + this.getTitle() + " activity.");
        }
    }


    public void showDeleteChatroomDialog(String chatroom_id) {
        LogHelper.LogThreadId(logName, "ActivityChat - showDeleteChatroomDialog");
        DeleteChatroomDialog deleteChatroomDialog = new DeleteChatroomDialog();
        Bundle args = new Bundle();
        args.putString(getString(R.string.field_chatroom_id), chatroom_id);
        LogHelper.LogThreadId(logName, "ActivityChat - showDeleteChatroomDialog - Passing bundle as " + args);
        deleteChatroomDialog.setArguments(args);
        deleteChatroomDialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_chatroom));
    }

    public void joinChatroom(final Chatroom chatroom) {
        //make sure the chatroom exists before joining
        theFireBaseDB = FirebaseDatabase.getInstance().getReference();

        Query query = theFireBaseDB.child(getString(R.string.dbnode_chatrooms)).orderByKey();

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();
                    if (objectMap.get(getString(R.string.field_chatroom_id)).toString()
                            .equals(chatroom.getChatroom_id())) {
                        if (mSecurityLevel >= Integer.parseInt(chatroom.getSecurity_level())) {
                            LogHelper.LogThreadId(logName, "onItemClick: selected chatroom: " + chatroom.getChatroom_id());

                            //add user to the list of users who have joined the chatroom
                            addUserToChatroom(chatroom);

                            //navigate to the chatoom
                            Intent intent = new Intent(ActivityChat.this, ActivityChatroom.class);
                            intent.putExtra(getString(R.string.intent_chatroom), chatroom);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ActivityChat.this, "insufficient security level", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    }
                }
                getChatrooms();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "ActivityChat - Join chatroom- Cancelled retrieving chatrooms and its " +
                        "messages from db reason : " + databaseError.getMessage() + " " + databaseError.getDetails());
            }
        });
    }

    /**
     * add the current user to the list of users who have joined the chatroom.
     * Users who have joined the chatroom will receive notifications on chatroom activity.
     * They will receive notifications via a cloud functions sending a cloud message to the
     * chatroom ID (Sending via topic FCM)
     * @param chatroom
     */
    private void addUserToChatroom(Chatroom chatroom) {
        theFireBaseDB = FirebaseDatabase.getInstance().getReference();
        LogHelper.LogThreadId(logName, "addUserToChatroom : Adding users to the chatroom");

        theFireBaseDB.child(getString(R.string.dbnode_chatrooms))
                .child(chatroom.getChatroom_id())
                .child(getString(R.string.field_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_last_message_seen))
                .setValue(mNumChatroomMessages.get(chatroom.getChatroom_id()));
    }
}
