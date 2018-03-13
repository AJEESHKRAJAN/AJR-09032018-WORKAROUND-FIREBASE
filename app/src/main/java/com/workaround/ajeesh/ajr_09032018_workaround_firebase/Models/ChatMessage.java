package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models
 * Created by ajesh on 13-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class ChatMessage {
    public ChatMessage() {
    }

    private String message;
    private String user_id;
    private String timestamp;
    private String profile_image;
    private String name;

    public ChatMessage(String message, String user_id, String timestamp, String profile_image, String name) {
        this.message = message;
        this.user_id = user_id;
        this.timestamp = timestamp;
        this.profile_image = profile_image;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "message='" + message + '\'' +
                ", user_id='" + user_id + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", profile_image='" + profile_image + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
