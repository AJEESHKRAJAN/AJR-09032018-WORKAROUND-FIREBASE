package com.workaround.ajeesh.ajr_09032018_workaround_firebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters.EmployeeRecyclerAdapter;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Dialog.NewDepartmentDialog;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging.Data;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.FirebaseCloudMessaging.FirebaseCloudMessage;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.ValidationHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper.VerticalSpacingDecorator;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Interface.FCM;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ActivityAdmin extends AppCompatActivity {
    private static final String logName = "FIREB-ACT-ADMIN";
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/";

    //Firebase
    private FirebaseUser theFireBaseUser;
    private DatabaseReference theFirebaseDb;

    //widgets
    private TextView mDepartments;
    private Button mAddDepartment, mSendMessage;
    private RecyclerView mRecyclerView;
    private EditText mMessage, mTitle;
    private AlertDialog.Builder mAlertDialogBuilder;

    //vars
    private ArrayList<String> mDepartmentsList;
    private Set<String> mSelectedDepartments;
    private EmployeeRecyclerAdapter mEmployeeRecyclerAdapter;
    private ArrayList<User> mUsers;
    private Set<String> mTokens;
    private String mServerKey;
    public static boolean isActivityRunning;
    private ValidationHelper theHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        LogHelper.LogThreadId(logName, "Admin Activity is initiated.");

        theFireBaseUser = FirebaseAuth.getInstance().getCurrentUser();
        theFirebaseDb = FirebaseDatabase.getInstance().getReference();
        theHelper = new ValidationHelper();

        mDepartments = findViewById(R.id.broadcast_departments);
        mAddDepartment = findViewById(R.id.add_department);
        mSendMessage = findViewById(R.id.btn_send_message);
        mRecyclerView = findViewById(R.id.recyclerView);
        mMessage = findViewById(R.id.input_message);
        mTitle = findViewById(R.id.input_title);

        setupEmployeeList();
        InitializeActivityContents();
        hideSoftKeyboard();
    }

    private void InitializeActivityContents() {
        LogHelper.LogThreadId(logName, "Admin Activity - InitializeActivityContents - Initiated.");

        mSelectedDepartments = new HashSet<>();
        mTokens = new HashSet<>();

        mDepartments.setOnClickListener(getSelectedDepartments());

        mAddDepartment.setOnClickListener(addDepartments());

        mSendMessage.setOnClickListener(sendMessage());

        getDepartments();

        getEmployeeList();

        getServerKey();

    }

    private View.OnClickListener getSelectedDepartments() {
        View.OnClickListener theListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialogBuilder = new AlertDialog.Builder(ActivityAdmin.this);
                mAlertDialogBuilder.setIcon(R.mipmap.ic_departments);
                mAlertDialogBuilder.setTitle("Select Departments...");

                // //create an array of the departments
                String[] departments = new String[mDepartmentsList.size()];
                LogHelper.LogThreadId(logName, "Admin Activity - Total Dept size - " + mDepartmentsList.size());
                for (int i = 0; i < mDepartmentsList.size(); i++) {
                    departments[i] = mDepartmentsList.get(i);
                }

                //get the departments that are already added to the list
                boolean[] checked = new boolean[mDepartmentsList.size()];
                for (int i = 0; i < mDepartmentsList.size(); i++) {
                    if (mSelectedDepartments.contains(mDepartmentsList.get(i))) {
                        checked[i] = true;
                    }
                }

                mAlertDialogBuilder.setPositiveButton("done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogHelper.LogThreadId(logName, "Admin Activity - User selected department; Hence dismissed.");
                        dialog.dismiss();
                    }
                });

                mAlertDialogBuilder.setMultiChoiceItems(departments, checked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            LogHelper.LogThreadId(logName, "Admin Activity: adding " + mDepartmentsList.get(which) + " to the list.");
                            mSelectedDepartments.add(mDepartmentsList.get(which));
                        } else {
                            LogHelper.LogThreadId(logName, "Admin Activity: removing " + mDepartmentsList.get(which) + " from the list.");
                            mSelectedDepartments.remove(mDepartmentsList.get(which));
                        }
                    }
                });

                AlertDialog theAlertDialog = mAlertDialogBuilder.create();
                theAlertDialog.show();

                theAlertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        LogHelper.LogThreadId(logName, "Admin Activity : onDismiss: dismissing dialog and refreshing token list.");
                        getDepartmentTokens();
                    }
                });
            }
        };

        return theListener;
    }

    /**
     * Get all the tokens of the users who are in the selected departments
     */
    private void getDepartmentTokens() {
        LogHelper.LogThreadId(logName, "Admin Activity : getDepartmentTokens: searching for tokens.");
        mTokens.clear(); //clear current token list in case admin has change departments

        theFirebaseDb = FirebaseDatabase.getInstance().getReference();

        for (String selectedDept : mSelectedDepartments) {
            LogHelper.LogThreadId(logName, "Admin Activity : getDepartmentTokens: department : " + selectedDept);

            Query theQuery = theFirebaseDb
                    .child(getString(R.string.dbnode_users))
                    .orderByChild(getString(R.string.field_department))
                    .equalTo(selectedDept);

            theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        String token = singleSnapshot.getValue(User.class).getMessaging_token();
                        LogHelper.LogThreadId(logName, "Admin Activity : onDataChange: got a token for user named: "
                                + singleSnapshot.getValue(User.class).getName());
                        mTokens.add(token);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    LogHelper.LogThreadId(logName, "Admin Activity - Cancelled retrieving tokens from db reason : "
                            + databaseError.getMessage() + " " + databaseError.getDetails());
                }
            });

        }
    }

    private View.OnClickListener addDepartments() {
        View.OnClickListener theListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "Admin Activity : opening dialog to add new department");
                NewDepartmentDialog theDeptDialog = new NewDepartmentDialog();
                theDeptDialog.show(getSupportFragmentManager(), getString(R.string.dialog_add_department));
            }
        };
        return theListener;
    }

    private View.OnClickListener sendMessage() {
        View.OnClickListener theListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.LogThreadId(logName, "Admin Activity : Attempting to send the message... ");

                String message = mMessage.getText().toString();
                String title = mTitle.getText().toString();
                if (!theHelper.isEmpty(message) && !theHelper.isEmpty(title)) {

                    //send message
                    sendMessageToDepartment(title, message);

                    mMessage.setText("");
                    mTitle.setText("");
                } else {
                    Toast.makeText(ActivityAdmin.this, "Fill out the title and message fields", Toast.LENGTH_SHORT).show();
                }
            }
        };
        return theListener;
    }

    private void sendMessageToDepartment(String title, String message) {
        LogHelper.LogThreadId(logName, "Admin Activity : Attempting to call Retrofit to " +
                "send the message to department over HTTP... ");

        Retrofit theRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        FCM iFcmApi = theRetrofit.create(FCM.class);

        //Attach the headers and tokens
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "key=" + mServerKey);

        //Send Message to all the tokens
        for (String token : mTokens) {
            LogHelper.LogThreadId(logName, "Admin Activity : Sending the message to token : " + token);

            Data theData = new Data();
            theData.setMessage(message);
            theData.setTitle(title);
            theData.setData_type(getString(R.string.data_type_admin_broadcast));

            FirebaseCloudMessage theCloudMessage = new FirebaseCloudMessage();
            theCloudMessage.setTo(token);
            theCloudMessage.setData(theData);

            Call<ResponseBody> callResponse = iFcmApi.send(headers, theCloudMessage);

            callResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    LogHelper.LogThreadId(logName, "Admin Activity : Retrofit message : onResponse: Server Response: "
                            + response.toString());
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    LogHelper.LogThreadId(logName, "Admin Activity : Retrofit Message : onFailure: " +
                            "Unable to send the message: " + t.getMessage());
                    Toast.makeText(ActivityAdmin.this, "error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void setupEmployeeList() {
        LogHelper.LogThreadId(logName, "Admin Activity - Setting up Employee Lists.");

        mUsers = new ArrayList<>();
        mEmployeeRecyclerAdapter = new EmployeeRecyclerAdapter(ActivityAdmin.this, mUsers);

        RecyclerView.LayoutManager rLayoutMgr = new LinearLayoutManager(getApplicationContext());
        LogHelper.LogThreadId(logName, "Admin Activity - RecyclerView Layout Manager is Initiated.");

        mRecyclerView.setLayoutManager(rLayoutMgr);
        mRecyclerView.addItemDecoration(new VerticalSpacingDecorator(15));
        mRecyclerView.setAdapter(mEmployeeRecyclerAdapter);
    }

    public void getDepartments() {
        mDepartmentsList = new ArrayList<>();

        theFirebaseDb = FirebaseDatabase.getInstance().getReference();

        Query theQuery = theFirebaseDb.child(getString(R.string.dbnode_departments));

        theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String theDepartment = singleSnapshot.getValue().toString();
                    LogHelper.LogThreadId(logName, "Admin Activity : onDataChange: found a department: " + theDepartment);

                    mDepartmentsList.add(theDepartment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "Admin Activity - Cancelled retrieving departments from db reason : "
                        + databaseError.getMessage() + " " + databaseError.getDetails());
            }
        });
    }

    private void getEmployeeList() throws NullPointerException {
        LogHelper.LogThreadId(logName, "Admin Activity - getEmployeeList: getting a list of all employees");

        theFirebaseDb = FirebaseDatabase.getInstance().getReference();

        Query theQuery = theFirebaseDb.child(getString(R.string.dbnode_users));

        theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    LogHelper.LogThreadId(logName, "Admin Activity - onDataChange: found a user: " + user.getName());
                    mUsers.add(user);

                    mEmployeeRecyclerAdapter.notifyDataSetChanged();
                    getDepartmentTokens();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "Admin Activity - Cancelled retrieving users from db reason : "
                        + databaseError.getMessage() + " " + databaseError.getDetails());
            }
        });
    }

    public void setDepartmentDialog(final User user) {

        LogHelper.LogThreadId(logName, "Admin Activity - setDepartmentDialog: setting the department of: " + user.getName());

        mAlertDialogBuilder = new AlertDialog.Builder(ActivityAdmin.this);
        mAlertDialogBuilder.setIcon(R.mipmap.ic_departments);
        mAlertDialogBuilder.setTitle("Set a Department for " + user.getName() + ":");

        mAlertDialogBuilder.setPositiveButton("done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LogHelper.LogThreadId(logName, "Admin Activity - Setting the department done; Hence closing the dialog.");
                dialog.dismiss();
            }
        });

        //get the index of the department (if the user has a department assigned)
        int index = -1;
        for (int i = 0; i < mDepartmentsList.size(); i++) {
            if (mDepartmentsList.contains(user.getDepartment())) {
                index = i;
            }
        }

        ListAdapter theListAdapter = new ArrayAdapter<String>(ActivityAdmin.this, android.R.layout.simple_list_item_1, mDepartmentsList);

        mAlertDialogBuilder.setSingleChoiceItems(theListAdapter, index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ActivityAdmin.this, "Department Saved", Toast.LENGTH_SHORT).show();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.child(getString(R.string.dbnode_users))
                        .child(user.getUser_id())
                        .child(getString(R.string.field_department))
                        .setValue(mDepartmentsList.get(which));

                LogHelper.LogThreadId(logName, "Admin Activity - Adding the user selected department " +
                        mDepartmentsList.get(which) + " to user's list");

                dialog.dismiss();
                //refresh the list with the new information
                mUsers.clear();
                getEmployeeList();
            }
        });

        mAlertDialogBuilder.show();
    }

    private void getServerKey() {
        LogHelper.LogThreadId(logName, "Admin Activity - getServerKey: retrieving server key.");

        theFirebaseDb = FirebaseDatabase.getInstance().getReference();

        Query theQuery = theFirebaseDb
                .child(getString(R.string.dbnode_server))
                .orderByValue();

        theQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LogHelper.LogThreadId(logName, "Admin Activity - onDataChange: got the server key.");
                DataSnapshot singleSnapshot = dataSnapshot.getChildren().iterator().next();
                mServerKey = singleSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                LogHelper.LogThreadId(logName, "Admin Activity - Cancelled retrieving servers from db reason : "
                        + databaseError.getMessage() + " " + databaseError.getDetails());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (theFireBaseUser != null) {
            checkAuthenticationState();
        }
    }

    private void checkAuthenticationState() {
        LogHelper.LogThreadId(logName, "Checking Authentication State");

        if (theFireBaseUser == null) {
            LogHelper.LogThreadId(logName, "User logged out from activity " + this.toString());

            Intent theIntent = new Intent(ActivityAdmin.this, ActivityMain.class);
            theIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(theIntent);
            finish();
        } else {
            LogHelper.LogThreadId(logName, "User is still active in " + ActivityAdmin.this.getTitle() + " activity.");
        }
    }
}
