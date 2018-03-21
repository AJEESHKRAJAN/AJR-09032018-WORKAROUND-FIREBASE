package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Interface;

import com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging.FirebaseCloudMessage;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Interface
 * Created by ajesh on 20-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public interface FCM {
    @POST("send")
    Call<ResponseBody> send(
            @HeaderMap Map<String, String> headers,
            @Body FirebaseCloudMessage message
    );
}
