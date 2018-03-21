package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityAdmin;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityChat;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityChatroom;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityLaunchDashboard;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityMain;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityRegister;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivitySettings;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.UniversalImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.Chatroom;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Services
 * Created by ajesh on 19-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class AjrFirebaseMessagingService extends FirebaseMessagingService {
    private static final String logName = "FIREB-SVC-FBSVC-MSGN";
    private static final int BROADCAST_NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "AJR_NOTIFICATION_ID";

    private int mNumPendingMessages = 0;


    @Override
    public void onDeletedMessages() {
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - onDeletedMessages");
        super.onDeletedMessages();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        LogHelper.LogThreadId(logName, "onMessageReceived");

        /*------------This functionality is used when no notifications is set up & the
        ----------------notification details are sent from firebase console.---------------
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
        LogHelper.LogThreadId(logName, "onMessageReceived : Notification Body : " + notificationBody);*/

        //init image loader since this will be the first code that executes if they click a notification
        initImageLoader();
        createNewNotificationChannel();

        String identifyDataType = remoteMessage.getData().get(getString(R.string.data_type));
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Identify Data Type - " + identifyDataType);
        //SITUATION: Application is in foreground then only send priority notifications such as an admin notification
        if (isApplicationInForeground()) {
            if (identifyDataType.equals(getString(R.string.data_type_admin_broadcast))) {
                LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - App in foreground");
                //build admin broadcast notification
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                sendBroadcastNotification(title, message);
            }
        }
        //SITUATION: Application is in background or closed
        else if (!isApplicationInForeground()) {
            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - App is not in foreground");
            if (identifyDataType.equals(getString(R.string.data_type_admin_broadcast))) {

                //build admin broadcast notification
                String title = remoteMessage.getData().get(getString(R.string.data_title));
                String message = remoteMessage.getData().get(getString(R.string.data_message));
                sendBroadcastNotification(title, message);

            } else if (identifyDataType.equals(getString(R.string.data_type_chat_message))) {

                /*build chat message notification*/
                final String title = remoteMessage.getData().get(getString(R.string.data_title));
                final String message = remoteMessage.getData().get(getString(R.string.data_message));
                final String chatroomId = remoteMessage.getData().get(getString(R.string.data_chatroom_id));

                LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - onMessageReceived: chatroom id: " + chatroomId);
                Query query = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbnode_chatrooms))
                        .orderByKey()
                        .equalTo(chatroomId);

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getChildren().iterator().hasNext()) {
                            DataSnapshot snapshot = dataSnapshot.getChildren().iterator().next();

                            Chatroom chatroom = new Chatroom();
                            Map<String, Object> objectMap = (HashMap<String, Object>) snapshot.getValue();

                            if (objectMap != null) {
                                chatroom.setChatroom_id(objectMap.get(getString(R.string.field_chatroom_id)).toString());
                                chatroom.setChatroom_name(objectMap.get(getString(R.string.field_chatroom_name)).toString());
                                chatroom.setCreator_id(objectMap.get(getString(R.string.field_creator_id)).toString());
                                chatroom.setSecurity_level(objectMap.get(getString(R.string.field_security_level)).toString());
                            }


                            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - onDataChange: chatroom: " + chatroom);

                            int numMessagesSeen = Integer.parseInt(snapshot
                                    .child(getString(R.string.field_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.field_last_message_seen))
                                    .getValue().toString());

                            int numMessages = (int) snapshot
                                    .child(getString(R.string.field_chatroom_messages)).getChildrenCount();

                            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Total number of messages under" +
                                    " this chatroom : " + numMessages);
                            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Total number of messages seen " +
                                    "in this chatroom : " + numMessagesSeen);

                            mNumPendingMessages = (numMessages - numMessagesSeen);
                            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Total number of messages pending : "
                                    + mNumPendingMessages);


                            sendChatmessageNotification(title, message, chatroom);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Cancelled retrieving chat messages from db reason : "
                                + databaseError.getMessage() + " " + databaseError.getDetails());
                    }
                });
            }
        }

    }


    private boolean isApplicationInForeground() {
        //check all the activities to see if any of them are running
        boolean isActivityRunning = ActivityLaunchDashboard.isActivityRunning
                || ActivityChat.isActivityRunning || ActivityAdmin.isActivityRunning
                || ActivityChatroom.isActivityRunning || ActivityMain.isActivityRunning
                || ActivityRegister.isActivityRunning || ActivitySettings.isActivityRunning;
        if (isActivityRunning) {
            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - isApplicationInForeground : application is in foreground.");
            return true;
        }
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - isApplicationInForeground: application is in background or closed.");
        return false;
    }

    /**
     * init universal image loader
     */
    private void initImageLoader() {
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Image loader set up done.");
        UniversalImageLoader imageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void sendBroadcastNotification(String title, String message) {
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - sendBroadcastNotification: building a admin broadcast notification");

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.channel_id));
        // Creates an Intent for the Activity
        Intent notifyIntent = new Intent(this, ActivityLaunchDashboard.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent thePendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //add properties to the builder
        builder.setSmallIcon(R.mipmap.ajsys_logo_primary)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ajsys_logo_primary))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText(message)
                .setColor(getColor(R.color.blue4))
                .setAutoCancel(true);

        builder.setContentIntent(thePendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Broadcast a new notification message - " +
                    "Called from notification manager.");
            mNotificationManager.notify(BROADCAST_NOTIFICATION_ID, builder.build());
        }
    }

    /**
     * Build a push notification for a chat message
     *
     * @param title
     * @param message
     */
    private void sendChatmessageNotification(String title, String message, Chatroom chatroom) {
        LogHelper.LogThreadId(logName, "sendChatmessageNotification : building a chatmessage notification");

        //get the notification id
        int notificationId = buildNotificationId(chatroom.getChatroom_id());

        // Instantiate a Builder object.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.channel_id));
        // Creates an Intent for the Activity
        Intent pendingIntent = new Intent(this, ActivityLaunchDashboard.class);
        // Sets the Activity to start in a new, empty task
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //add properties to the builder
        builder.setSmallIcon(R.mipmap.ajsys_logo_primary)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ajsys_logo_primary))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentTitle(title)
                .setContentText("New messages in " + chatroom.getChatroom_name())
                .setColor(getColor(R.color.blue4))
                .setAutoCancel(true)
                .setSubText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("New messages in " + chatroom.getChatroom_name()).setSummaryText(message))
                .setNumber(mNumPendingMessages)
                .setOnlyAlertOnce(true);


        builder.setContentIntent(notifyPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null) {
            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - A new chat message notification is " +
                    "being called from notification manager.");
            mNotificationManager.notify(notificationId, builder.build());
        }

    }


    private void createNewNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Creating a new channel for notification.");

            // Create the NotificationChannel
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            String channelId = getString(R.string.channel_id);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                NotificationChannel theChannel = notificationManager.getNotificationChannel(getString(R.string.channel_id));
                LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - getNotificationChannel() - " + theChannel);
            }


            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            mChannel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this

            if (notificationManager != null) {
                LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - Registering a new notification" +
                        " channel with name as : " + name);
                notificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    private int buildNotificationId(String chatroom_id) {
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - buildNotificationId: building a notification id.");

        int notificationId = 0;
        for (int i = 0; i < 9; i++) {
            notificationId = notificationId + chatroom_id.charAt(0);
        }
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - buildNotificationId : chatroom_id : " + chatroom_id);
        LogHelper.LogThreadId(logName, "AjrFirebaseMessagingService - buildNotificationId : notification id : " + notificationId);
        return notificationId;
    }

}
