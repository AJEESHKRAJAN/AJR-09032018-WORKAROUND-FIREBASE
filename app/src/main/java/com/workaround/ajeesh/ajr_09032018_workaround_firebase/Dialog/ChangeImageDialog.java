package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog
 * Created by ajesh on 12-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class ChangeImageDialog extends DialogFragment {
    private static final String logName = "FIREB-DIALOG-IMAGE";

    private TextView mUploadImageText;
    private TextView mCaptureImageText;

    public static final int CAMERA_REQUEST_CODE = 9897;
    public static final int PICKFILE_REQUEST_CODE = 6457;

    public interface onPhotoReceivedListener {
        public void getImagePath(Uri imagePath);

        public void getImageBitmap(Bitmap bitmap);
    }

    onPhotoReceivedListener mPhotoListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LogHelper.LogThreadId(logName, "Change image dialog is initiated.");

        View theView = inflater.inflate(R.layout.change_image_dialog, container, false);

        mUploadImageText = theView.findViewById(R.id.dialogChoosePhoto);
        mCaptureImageText = theView.findViewById(R.id.dialogOpenCamera);

        mUploadImageText.setOnClickListener(uploadImage());
        mCaptureImageText.setOnClickListener(takePicture());

        return theView;
    }

    private View.OnClickListener uploadImage() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "User preferred image file");

                Intent theIntent = new Intent(Intent.ACTION_GET_CONTENT);
                theIntent.setType("image/*");
                startActivityForResult(theIntent, PICKFILE_REQUEST_CODE);
            }
        };
        return listener;
    }

    private View.OnClickListener takePicture() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "User preferred capturing image");
                Intent theIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(theIntent, CAMERA_REQUEST_CODE);
            }
        };
        return listener;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            LogHelper.LogThreadId(logName, "Processing image file by user selection from dialog");

            Uri imageFilePath = data.getData();
            LogHelper.LogThreadId(logName, "Processing image file path : " + imageFilePath);

            mPhotoListener.getImagePath(imageFilePath);
            getDialog().dismiss();

        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            LogHelper.LogThreadId(logName, "Capturing image file by user selection from dialog");

            Bitmap bitmap;
            bitmap = (Bitmap) data.getExtras().get("data");

            LogHelper.LogThreadId(logName, "Capturing image file Bitmap content : " + bitmap);

            mPhotoListener.getImageBitmap(bitmap);
            getDialog().dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        try {
            mPhotoListener = (onPhotoReceivedListener) getActivity();
        } catch (ClassCastException e) {
            LogHelper.LogThreadId(logName, "onAttach :  ClassCastException() : " + e.getCause());
        }
        super.onAttach(context);
    }
}
