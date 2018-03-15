package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityChat;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.Chatroom;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

import java.util.List;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters
 * Created by ajesh on 14-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class ChatroomListAdapter extends ArrayAdapter<Chatroom> {
    private static final String logName = "FIREB-ADAPTER-CHATROOM-LIST";

    private int mLayoutResource;
    private Context mContext;
    private LayoutInflater mLayoutInflator;
    private FirebaseUser theFirebaseUser;
    private DatabaseReference theFirebaseDB;

    public ChatroomListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Chatroom> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResource = resource;
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        theFirebaseDB = FirebaseDatabase.getInstance().getReference();
        theFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        LogHelper.LogThreadId(logName, "ChatroomListAdapter - Adapter is created");
    }

    public static class ViewHolder {
        TextView name, creatorName, numberMessages;
        ImageView mProfileImage, mTrash;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder theViewHolder;

        if (convertView == null) {
            LogHelper.LogThreadId(logName, "ChatroomListAdapter - mLayoutResource : " + mLayoutResource);
            LogHelper.LogThreadId(logName, "ChatroomListAdapter - convertView : " + convertView);
            convertView = mLayoutInflator.inflate(mLayoutResource, parent, false);
            theViewHolder = new ViewHolder();

            theViewHolder.name = convertView.findViewById(R.id.name);
            theViewHolder.creatorName = convertView.findViewById(R.id.creator_name);
            theViewHolder.numberMessages = convertView.findViewById(R.id.number_chatmessages);
            theViewHolder.mProfileImage = convertView.findViewById(R.id.profile_image);
            theViewHolder.mTrash = convertView.findViewById(R.id.icon_trash);
            LogHelper.LogThreadId(logName, "ChatroomListAdapter - The view holder when convert view is not null : " + theViewHolder);
        } else {
            theViewHolder = (ViewHolder) convertView.getTag();
            LogHelper.LogThreadId(logName, "ChatroomListAdapter - The view holder when convert view is null : " + theViewHolder);
        }

        try {
            //set the chatroom name
            theViewHolder.name.setText(getItem(position).getChatroom_name());

            //set the number of chat messages
            String chatMessagesString = String.valueOf(getItem(position).getChatroom_messages().size())
                    + " messages";
            theViewHolder.numberMessages.setText(chatMessagesString);

            //Get the user Details who created Chat room
            Query theQuery = theFirebaseDB
                    .child(mContext.getString(R.string.dbnode_users))
                    .orderByKey()
                    .equalTo(getItem(position).getCreator_id());

            theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        LogHelper.LogThreadId(logName, "onDataChange: Found chat room creator: "
                                + singleSnapshot.getValue(User.class).toString());
                        String createdBy = "created by " + singleSnapshot.getValue(User.class).getName();
                        theViewHolder.creatorName.setText(createdBy);
                        ImageLoader.getInstance().displayImage(
                                singleSnapshot.getValue(User.class).getProfile_image(), theViewHolder.mProfileImage);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    LogHelper.LogThreadId(logName, "ChatroomListAdapter - Cancelled finding new chatroom");
                }
            });

            theViewHolder.mTrash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getItem(position).getCreator_id().equals(theFirebaseUser.getUid())) {
                        LogHelper.LogThreadId(logName, "onClick: asking for permission to delete icon.");
                        ((ActivityChat) mContext).showDeleteChatroomDialog(getItem(position).getChatroom_id());
                    } else {
                        Toast.makeText(mContext, "You didn't create this chatroom", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        } catch (NullPointerException e) {
            LogHelper.LogThreadId(logName, "getView: NullPointerException: " + e.getCause());
        }

        return convertView;
    }
}
