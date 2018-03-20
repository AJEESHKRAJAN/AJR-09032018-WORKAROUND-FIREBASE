package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.ActivityAdmin;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Models.User;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

import java.util.ArrayList;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Adapters
 * Created by ajesh on 20-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class EmployeeRecyclerAdapter extends RecyclerView.Adapter<EmployeeRecyclerAdapter.ViewHolder> {
    private static final String logName = "FIREB-ADAPTER-EMP-LIST";
    private ArrayList<User> mUsers;
    private Context mContext;

    public EmployeeRecyclerAdapter(Context context, ArrayList<User> users) {
        mUsers = users;
        mContext = context;
        LogHelper.LogThreadId(logName, "EmployeeRecyclerAdapter - Initiated");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        //inflate the custom layout
        View view = inflater.inflate(R.layout.layout_adapter_employee_list, parent, false);
        LogHelper.LogThreadId(logName, "EmployeeRecyclerAdapter - View is created");

        //return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ImageLoader.getInstance().displayImage(mUsers.get(position).getProfile_image(), holder.profileImage);
        holder.name.setText(mUsers.get(position).getName());
        holder.department.setText(mUsers.get(position).getDepartment());
        LogHelper.LogThreadId(logName, "EmployeeRecyclerAdapter - Image loader and its contents bound to view.");
    }

    @Override
    public int getItemCount() {
        LogHelper.LogThreadId(logName, "EmployeeRecyclerAdapter - Total users count : " + mUsers.size());
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImage;
        public TextView name, department;

        public ViewHolder(View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            department = itemView.findViewById(R.id.department);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogHelper.LogThreadId(logName, "EmployeeRecyclerAdapter.ViewHolder - onClick: selected employee : "
                            + mUsers.get(getAdapterPosition()));

                    //open a dialog for selecting a department
                    ((ActivityAdmin) mContext).setDepartmentDialog(mUsers.get(getAdapterPosition()));
                }
            });
        }
    }
}
