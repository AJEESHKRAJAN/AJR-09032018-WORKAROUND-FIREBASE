package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models
 * Created by ajesh on 13-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class Chatroom implements Parcelable {
    public Chatroom() {
    }

    private String chatroom_name;
    private String chatroom_id;
    private String creator_id;
    private String security_level;
    private List<ChatMessage> chatroom_messages;

    public Chatroom(String chatroom_name, String chatroom_id, String creator_id, String security_level, List<ChatMessage> chatroom_messages) {
        this.chatroom_name = chatroom_name;
        this.chatroom_id = chatroom_id;
        this.creator_id = creator_id;
        this.security_level = security_level;
        this.chatroom_messages = chatroom_messages;
    }

    protected Chatroom(Parcel in) {
        chatroom_name = in.readString();
        chatroom_id = in.readString();
        creator_id = in.readString();
        security_level = in.readString();
    }

    public static final Creator<Chatroom> CREATOR = new Creator<Chatroom>() {
        @Override
        public Chatroom createFromParcel(Parcel in) {
            return new Chatroom(in);
        }

        @Override
        public Chatroom[] newArray(int size) {
            return new Chatroom[size];
        }
    };

    public String getChatroom_name() {
        return chatroom_name;
    }

    public void setChatroom_name(String chatroom_name) {
        this.chatroom_name = chatroom_name;
    }

    public String getChatroom_id() {
        return chatroom_id;
    }

    public void setChatroom_id(String chatroom_id) {
        this.chatroom_id = chatroom_id;
    }

    public String getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(String creator_id) {
        this.creator_id = creator_id;
    }

    public String getSecurity_level() {
        return security_level;
    }

    public void setSecurity_level(String security_level) {
        this.security_level = security_level;
    }

    public List<ChatMessage> getChatroom_messages() {
        return chatroom_messages;
    }

    public void setChatroom_messages(List<ChatMessage> chatroom_messages) {
        this.chatroom_messages = chatroom_messages;
    }

    @Override
    public String toString() {
        return "Chatroom{" +
                "chatroom_name='" + chatroom_name + '\'' +
                ", chatroom_id='" + chatroom_id + '\'' +
                ", creator_id='" + creator_id + '\'' +
                ", security_level='" + security_level + '\'' +
                ", chatroom_messages=" + chatroom_messages +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(chatroom_name);
        parcel.writeString(creator_id);
        parcel.writeString(security_level);
        parcel.writeString(chatroom_id);
    }
}
