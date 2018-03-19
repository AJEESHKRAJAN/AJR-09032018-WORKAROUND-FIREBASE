package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Services
 * Created by ajesh on 19-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class AjrFirebaseMessagingService extends FirebaseMessagingService {
    private static final String logName = "FIREB-SVC-FBSVC-MSGN";


    @Override
    public void onDeletedMessages() {
        LogHelper.LogThreadId(logName, "onDeletedMessages");
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LogHelper.LogThreadId(logName, "onMessageReceived");
        String notificationBody = "";
        String notificationTitle = "";
        String notificationData = "";

        try {
            notificationData = remoteMessage.getData().toString();
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationBody = remoteMessage.getNotification().getBody();

        } catch (NullPointerException e) {
            LogHelper.LogThreadId(logName, "OnMessageReceived : Exception : " + e.getMessage());
        }

        LogHelper.LogThreadId(logName, "onMessageReceived : Notification Data : " + notificationData);
        LogHelper.LogThreadId(logName, "onMessageReceived : Notification Title : " + notificationTitle);
        LogHelper.LogThreadId(logName, "onMessageReceived : Notification Body : " + notificationBody);

    }
}
