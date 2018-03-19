package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Services
 * Created by ajesh on 19-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class AjrFirebaseInstanceIdMessagingService extends FirebaseInstanceIdService {
    private static final String logName = "FIREB-SVC-FBSVC-ID-MSGN";
    private String refereshedToken = "";

    @Override
    public void onTokenRefresh() {
        refereshedToken = FirebaseInstanceId.getInstance().getToken();
        LogHelper.LogThreadId(logName,"OnTokenRefresh : " + refereshedToken);
        sendRegistrationToServer(refereshedToken);
    }

    private void sendRegistrationToServer(String token) {
        LogHelper.LogThreadId(logName,"sendRegistrationToServer - Initiated");

        DatabaseReference theFirebaseDb = FirebaseDatabase.getInstance().getReference();

        theFirebaseDb.child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(token);
        LogHelper.LogThreadId(logName,"sendRegistrationToServer - The token has been registered from the service");
    }
}
