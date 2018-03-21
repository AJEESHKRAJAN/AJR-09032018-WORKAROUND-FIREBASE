package com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging
 * Created by ajesh on 20-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class FirebaseCloudMessage {
    private String to;
    private Data data;

    public FirebaseCloudMessage(String to, Data data) {
        this.to = to;
        this.data = data;
    }

    public FirebaseCloudMessage() {
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "FirebaseCloudMessage{" +
                "to='" + to + '\'' +
                ", data=" + data +
                '}';
    }
}
