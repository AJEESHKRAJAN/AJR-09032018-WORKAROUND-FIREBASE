package com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging
 * Created by ajesh on 20-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class Data {
    private String message;
    private String title;
    private String data_type;

    public Data(String message, String title, String data_type) {
        this.message = message;
        this.title = title;
        this.data_type = data_type;
    }

    public Data() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }

    @Override
    public String toString() {
        return "Data{" +
                "message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", data_type='" + data_type + '\'' +
                '}';
    }
}
