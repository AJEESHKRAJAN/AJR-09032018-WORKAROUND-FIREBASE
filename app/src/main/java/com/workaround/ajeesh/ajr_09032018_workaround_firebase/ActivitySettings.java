package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog.ChangeImageDialog;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.UniversalImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ActivitySettings extends AppCompatActivity implements ChangeImageDialog.onPhotoReceivedListener {
    private static final String logName = "FIREB-ACT-SETTINGS";
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private ValidationHelper helper;
    private ChangeImageDialog mChangeImageDialog;

    private EditText mUserName, mUserPhone, mEmail, mCurrentPassword;
    private Button mSave;
    private ProgressBar mProgressBar;
    private TextView mResetPasswordLink;
    private DatabaseReference mDbFirebaseReference;
    private ImageView mProfileImage;

    private boolean mStoragePermission;
    private static final int REQUEST_CODE = 1234;

    private Uri mImageFilePath;
    private Bitmap mImageBitmap;
    private Bitmap mBitmap = null;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    private static final String mFilePath = "images/users";
    private byte[] mBytes;
    private double mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LogHelper.LogThreadId(logName, "User Settings Activity has been initiated.");
        user = FirebaseAuth.getInstance().getCurrentUser();
        helper = new ValidationHelper();
        mDbFirebaseReference = FirebaseDatabase.getInstance().getReference();
        setupFirebaseAuth();
        initUniversalImageLoader();

        mUserName = findViewById(R.id.input_user_name);
        mUserPhone = findViewById(R.id.input_user_phone);
        mEmail = findViewById(R.id.input_email);
        mCurrentPassword = findViewById(R.id.input_password);
        mSave = findViewById(R.id.btn_save);
        mProgressBar = findViewById(R.id.progressBar);
        mResetPasswordLink = findViewById(R.id.change_password);
        mProfileImage = findViewById(R.id.profile_image);

        checkPermissions();
        retrieveUserValuesOnSettings();

        mSave.setOnClickListener(SaveUserChanges());
        mResetPasswordLink.setOnClickListener(ResetPassword());
        mProfileImage.setOnClickListener(uploadProfileImage());

    }

    private void initUniversalImageLoader() {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    private void checkPermissions() {
        LogHelper.LogThreadId(logName, "Checking permissions for camera & storage");

        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            LogHelper.LogThreadId(logName, "Permissions granted for camera & storage");
            mStoragePermission = true;
        } else {
            LogHelper.LogThreadId(logName, "Yet to give permissions for camera & storage");
            ActivityCompat.requestPermissions(ActivitySettings.this, permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        LogHelper.LogThreadId(logName, "onRequestPermissionsResult - Called");

        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LogHelper.LogThreadId(logName, "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0]);
                }
                break;
            default:
                break;
        }
    }

    private View.OnClickListener ResetPassword() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "ResetPassword : started.");
                if (user.getEmail() != null) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    LogHelper.LogThreadId(logName, "Password reset link has been sent to registered email.");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    LogHelper.LogThreadId(logName, "Failed in resetting password.");
                                }
                            });
                } else {
                    Toast.makeText(ActivitySettings.this, "User email not found", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return listener;

    }

    private View.OnClickListener SaveUserChanges() {


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "SaveUserChanges : started.");
                if (!helper.isEmpty(mEmail.getText().toString()) && !helper.isEmpty(mCurrentPassword.getText().toString())) {
                    if (helper.isValidDomain(mEmail.getText().toString())) {
                        if (!user.getEmail().equalsIgnoreCase(mEmail.getText().toString())) {

                            LogHelper.LogThreadId(logName, "Changing registered email is triggered.");
                            showDialog();

                            AuthCredential currentCredentials = EmailAuthProvider.getCredential(user.getEmail(), mCurrentPassword.getText().toString());

                            LogHelper.LogThreadId(logName, "Re-authenticating with:  \n Registered Email " + user.getEmail()
                                    + " \n Password: " + mCurrentPassword.getText().toString());


                            user.reauthenticate(currentCredentials)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(ActivitySettings.this, "Change of Email initiated", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().fetchProvidersForEmail(mEmail.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                                            if (task.isSuccessful()) {
                                                                LogHelper.LogThreadId(logName,
                                                                        "Entered email is not a part of Firebase. Good to change.");
                                                                int noOfEmailFound = task.getResult().getProviders().size();

                                                                if (noOfEmailFound == 1) {
                                                                    LogHelper.LogThreadId(logName,
                                                                            "Entered email is already in use.");
                                                                    hideDialog();
                                                                    Toast.makeText(ActivitySettings.this, "Email already in use",
                                                                            Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    LogHelper.LogThreadId(logName,
                                                                            "Entered email is all set for updating.");

                                                                    user.updateEmail(mEmail.getText().toString())
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        LogHelper.LogThreadId(logName,
                                                                                                "Email has been changed");
                                                                                        Toast.makeText(ActivitySettings.this, "Email Changed",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        hideDialog();
                                                                                        SendVerificationEmail();
                                                                                        FirebaseAuth.getInstance().signOut();
                                                                                        checkAuthenticationState();
                                                                                    }
                                                                                }
                                                                            })
                                                                            .addOnFailureListener(new OnFailureListener() {
                                                                                @Override
                                                                                public void onFailure(@NonNull Exception e) {
                                                                                    LogHelper.LogThreadId(logName,
                                                                                            "Error in updating email.");
                                                                                    Toast.makeText(ActivitySettings.this,
                                                                                            "Error in updating email",
                                                                                            Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            hideDialog();
                                                            Toast.makeText(ActivitySettings.this,
                                                                    "Unable to update email", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(ActivitySettings.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            /*---------Changing User name and phone number in Firebase db ------------*/
                            if (!helper.isEmpty(mUserName.getText().toString())) {

                                mDbFirebaseReference
                                        .child(getString(R.string.dbnode_users))
                                        .child(user.getUid())
                                        .child(getString(R.string.field_name))
                                        .setValue(mUserName.getText().toString());
                                LogHelper.LogThreadId(logName,
                                        "User name is updated");

                            }
                            if (!helper.isEmpty(mUserPhone.getText().toString())) {

                                mDbFirebaseReference
                                        .child(getString(R.string.dbnode_users))
                                        .child(user.getUid())
                                        .child(getString(R.string.field_phone))
                                        .setValue(mUserPhone.getText().toString());
                                LogHelper.LogThreadId(logName,
                                        "User phone is updated.");

                            }
                            if (mImageFilePath != null) {
                                uploadNewPhoto(mImageFilePath);
                            } else if (mImageBitmap != null) {
                                uploadNewPhoto(mImageBitmap);
                            }
                        }
                    } else {
                        Toast.makeText(ActivitySettings.this, "Invalid Domain", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(ActivitySettings.this, "Email/Password is mandate to authenticate.", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return listener;
    }


    private View.OnClickListener uploadProfileImage() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "User clicked to update profile image");
                if (mStoragePermission) {
                    LogHelper.LogThreadId(logName, "Dialog is opened for uploading image.");
                    mChangeImageDialog = new ChangeImageDialog();
                    mChangeImageDialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
                } else {
                    checkPermissions();
                }
            }
        };
        return listener;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            checkAuthenticationState();
        }
    }

    private void setupFirebaseAuth() {
        LogHelper.LogThreadId(logName, "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user != null) {
                    LogHelper.LogThreadId(logName, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    LogHelper.LogThreadId(logName, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(ActivitySettings.this, ActivityMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        };
    }

    private void checkAuthenticationState() {
        LogHelper.LogThreadId(logName, "Checking Authentication State");

        if (user == null) {
            LogHelper.LogThreadId(logName, "User logged out from activity " + this.toString());

            Intent theIntent = new Intent(ActivitySettings.this, ActivityMain.class);
            theIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(theIntent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + this.toString() + " activity.");
        }
    }

    private void SendVerificationEmail() {
        LogHelper.LogThreadId(logName, "Send a verification email to the user.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivitySettings.this, "Sent verification email", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ActivitySettings.this, "Couldn't send verification email", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    public void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideDialog() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void retrieveUserValuesOnSettings() {
        LogHelper.LogThreadId(logName, "Retrieving user information from database.");

        Query queryA = mDbFirebaseReference
                .child(getString(R.string.dbnode_users))
                .orderByKey()
                .equalTo(user.getUid());

        queryA.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.LogThreadId(logName, "User name & Phone retrieved.");
                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                    User theUser = singleSnapShot.getValue(User.class);

                    mUserName.setText(theUser.getName());
                    mUserPhone.setText(theUser.getPhone());
                    ImageLoader.getInstance().displayImage(theUser.getProfile_image(), mProfileImage);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "User name & Phone retrieve - Cancelled.");
            }
        });

       /* Query queryB = mDbFirebaseReference
                .child(getString(R.string.dbnode_users))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(user.getUid());

        queryB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.LogThreadId(logName, "User name & phone retrieved.");
                for (DataSnapshot singleSnapShot : dataSnapshot.getChildren()) {
                    User theUser = singleSnapShot.getValue(User.class);

                    mUserName.setText(theUser.getName());
                    mUserPhone.setText(theUser.getPhone());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "User name & phone retrieve - Cancelled.");
            }
        });*/

        mEmail.setText(user.getEmail());
    }


    @Override
    public void getImagePath(Uri imagePath) {
        if (!imagePath.toString().equals("")) {
            LogHelper.LogThreadId(logName, "Retrieving user selected image path from activity");
            mImageBitmap = null;
            mImageFilePath = imagePath;

            LogHelper.LogThreadId(logName, "All set to display image as per user selection");
            ImageLoader.getInstance().displayImage(imagePath.toString(), mProfileImage);
        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            LogHelper.LogThreadId(logName, "Retrieving user image by image capturing");
            mImageFilePath = null;
            mImageBitmap = bitmap;

            LogHelper.LogThreadId(logName, "All set to display image from camera");
            mProfileImage.setImageBitmap(bitmap);
        }
    }


    private void uploadNewPhoto(Uri mImageFilePath) {
        LogHelper.LogThreadId(logName, "Uploaded user selected image as profile pic");

        BackgroundImageCompressor imageCompressor = new BackgroundImageCompressor(null);
        imageCompressor.execute(mImageFilePath);

    }

    private void uploadNewPhoto(Bitmap mImageBitmap) {
        LogHelper.LogThreadId(logName, "Uploaded user taken picture from camera as profile pic");

        BackgroundImageCompressor imageCompressor = new BackgroundImageCompressor(mImageBitmap);
        Uri uri = null;
        imageCompressor.execute(uri);
    }


    public class BackgroundImageCompressor extends AsyncTask<Uri, Integer, byte[]> {
        private static final String logName = "FIREB-HLPR-BKGRD-IMG-CMPRSR";


        public BackgroundImageCompressor(Bitmap bm) {
            if (bm != null) {
                LogHelper.LogThreadId(logName, "Background Image compressor helper class has been called.");
                mBitmap = bm;
                showDialog();

            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
            LogHelper.LogThreadId(logName, "Background image compressing has been started.");
            Toast.makeText(ActivitySettings.this, "Background image compressing has been started", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            LogHelper.LogThreadId(logName, "Background image compressing - In Progress");
            if (mBitmap == null) {
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(ActivitySettings.this.getContentResolver(), uris[0]);
                    LogHelper.LogThreadId(logName, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount() / MB + " MB");
                } catch (IOException e) {
                    e.printStackTrace();
                    LogHelper.LogThreadId(logName, "Background image compressing  - doInBackground - Exception thrown." + e.getMessage());
                    Toast.makeText(ActivitySettings.this, "Background image compressing  - doInBackground - Exception thrown." +
                                    e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++) {
                if (i == 10) {
                    Toast.makeText(ActivitySettings.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap, 100 / i);
                LogHelper.LogThreadId(logName, "doInBackground: megabytes: (" + (11 - i) + "0%) " + bytes.length / MB + " MB");
                if (bytes.length / MB < MB_THRESHHOLD) {
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            LogHelper.LogThreadId(logName, "Background Image compressor finished.");
            hideDialog();
            mBytes = bytes;
            executeUploadTask();
        }

        private byte[] getBytesFromBitmap(Bitmap _bitmap, int quality) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            _bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            return stream.toByteArray();
        }

        private void executeUploadTask() {
            showDialog();
            LogHelper.LogThreadId(logName, "Executing upload task.");
            //specify where the photo will be stored
            StorageReference theStorageReference =
                    FirebaseStorage.getInstance().getReference()
                            .child(mFilePath + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/rintaaje");

            LogHelper.LogThreadId(logName, "The storage Reference : " + theStorageReference);
            if (mBytes.length / MB < MB_THRESHHOLD) {

                StorageMetadata theStorageMetadata = new StorageMetadata.Builder()
                        .setContentType("image/jpg")
                        .setContentLanguage("en")
                        .setCustomMetadata("Image description", "RintaAje")
                        .setCustomMetadata("Location", "India")
                        .build();

                UploadTask uploadTask = null;
                uploadTask = theStorageReference.putBytes(mBytes, theStorageMetadata);

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //Inserting the download url into the firebase storage

                        Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                        LogHelper.LogThreadId(logName, "Image download url in firebase : " + firebaseUrl);
                        Toast.makeText(ActivitySettings.this, "Uploading image in firebase successful", Toast.LENGTH_SHORT).show();
                        FirebaseDatabase.getInstance().getReference()
                                .child(getString(R.string.dbnode_users))
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .child(getString(R.string.field_profile_image))
                                .setValue(firebaseUrl.toString());
                        hideDialog();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        LogHelper.LogThreadId(logName, "Executing upload image task failed with exception : " + e.getMessage());
                        Toast.makeText(ActivitySettings.this, "Uploading image in firebase failed", Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        if (currentProgress > mProgress + 15) {
                            mProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            LogHelper.LogThreadId(logName, "onProgress: Upload is " + mProgress + "% done");
                            Toast.makeText(ActivitySettings.this, mProgress + "%", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                Toast.makeText(ActivitySettings.this, "Image is too Large", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
