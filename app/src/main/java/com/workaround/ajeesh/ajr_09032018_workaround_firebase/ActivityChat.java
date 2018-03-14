package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        theFireBaseUser = FirebaseAuth.getInstance().getCurrentUser();
        theFireBaseDB = FirebaseDatabase.getInstance().getReference();

        mListView = findViewById(R.id.listView);
        mFab = findViewById(R.id.fob);


        init();
    }

    public void init() {
        getChatrooms();
        mFab.setOnClickListener(OpenChatroomListener());
    }


    private View.OnClickListener OpenChatroomListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewChatroomDialog newChatroomDialog = new NewChatroomDialog();
                newChatroomDialog.show(getSupportFragmentManager(), String.valueOf(R.string.dialog_new_chatroom));
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
                startActivity(theIntent);
            }
        };
        return adapterListener;
    }

    private void getChatrooms() {
        mChatroomArrayList = new ArrayList<>();

        Query theQuery = theFireBaseDB.child(String.valueOf(R.string.dbnode_chatrooms));

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

                    //Get the chatroom messages
                    ArrayList<ChatMessage> messageArrayList = new ArrayList<>();
                    for (DataSnapshot innerSingleDataSnapshot : singleSnapshot.child(String.valueOf(R.string.field_chatroom_messages))
                            .getChildren()) {
                        ChatMessage theChatMessage = new ChatMessage();
                        theChatMessage.setMessage(innerSingleDataSnapshot.getValue(ChatMessage.class).getMessage());
                        theChatMessage.setTimestamp(innerSingleDataSnapshot.getValue(ChatMessage.class).getTimestamp());
                        theChatMessage.setUser_id(innerSingleDataSnapshot.getValue(ChatMessage.class).getUser_id());
                        messageArrayList.add(theChatMessage);
                    }
                    theChatroom.setChatroom_messages(messageArrayList);
                    mChatroomArrayList.add(theChatroom);
                }
                setupChatroomList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupChatroomList() {
        mChatroomListAdapter = new ChatroomListAdapter(ActivityChat.this, R.layout.layout_adapter_chatroom_list, mChatroomArrayList);
        mListView.setAdapter(mChatroomListAdapter);
        mListView.setOnItemClickListener(ListChatsListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (theFireBaseUser != null) {
            checkAuthenticationState();
        }
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
            LogHelper.LogThreadId(logName, "User is still active in " + this.toString() + " activity.");
        }
    }


    public void showDeleteChatroomDialog(String chatroom_id) {
        DeleteChatroomDialog deleteChatroomDialog = new DeleteChatroomDialog();
        Bundle args = new Bundle();
        args.putString(String.valueOf(R.string.field_chatroom_id), chatroom_id);
        deleteChatroomDialog.setArguments(args);
        deleteChatroomDialog.show(getSupportFragmentManager(), getString(R.string.dialog_delete_chatroom));
    }
}
