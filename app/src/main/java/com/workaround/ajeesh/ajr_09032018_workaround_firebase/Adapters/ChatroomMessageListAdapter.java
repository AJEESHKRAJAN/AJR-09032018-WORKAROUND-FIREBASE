package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.ChatMessage;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

import java.util.List;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters
 * Created by ajesh on 15-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class ChatroomMessageListAdapter extends ArrayAdapter<ChatMessage> {
    private static final String logName = "FIREB-ADAPTER-CHAT-MESSAGE-LIST";

    private int mLayoutResource;
    private Context mContext;

    public ChatroomMessageListAdapter(@NonNull Context context, int resource, @NonNull List<ChatMessage> objects) {
        super(context, resource, objects);
        mLayoutResource = resource;
        mContext = context;
        LogHelper.LogThreadId(logName, "ChatroomMessageListAdapter - Adapter is created");
    }

    public static class ViewHolder {
        TextView name, message;
        ImageView mProfileImage;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final ViewHolder theViewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mLayoutResource, parent, false);
            theViewHolder = new ViewHolder();

            theViewHolder.name = convertView.findViewById(R.id.name);
            theViewHolder.message = convertView.findViewById(R.id.message);
            theViewHolder.mProfileImage = convertView.findViewById(R.id.profile_image);

            convertView.setTag(theViewHolder);

            LogHelper.LogThreadId(logName, "ChatroomMessageListAdapter - The view holder when convert view is not null : " + theViewHolder);
        } else {
            theViewHolder = (ViewHolder) convertView.getTag();
            theViewHolder.name.setText("");
            theViewHolder.message.setText("");
            LogHelper.LogThreadId(logName, "ChatroomMessageListAdapter - The view holder when convert view is null : " + theViewHolder);
        }

        try {
            theViewHolder.message.setText(getItem(position).getMessage());

            ImageLoader.getInstance().displayImage(getItem(position).getProfile_image(), theViewHolder.mProfileImage);

            theViewHolder.name.setText(getItem(position).getName());
        } catch (NullPointerException e) {
            LogHelper.LogThreadId(logName, "ChatroomMessageListAdapter - getView: NullPointerException: " + e.getCause());

        }
        return convertView;
    }
}
