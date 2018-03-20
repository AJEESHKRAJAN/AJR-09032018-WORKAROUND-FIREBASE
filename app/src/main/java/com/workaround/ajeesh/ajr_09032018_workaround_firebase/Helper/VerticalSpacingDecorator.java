package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper
 * Created by ajesh on 20-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class VerticalSpacingDecorator extends RecyclerView.ItemDecoration {
    private static final String logName = "FIREB-HLPR-VERTICAL-SPACING-DECORATOR";
    private final int verticalSpaceHeight;

    public VerticalSpacingDecorator(int verticalSpaceHeight) {
        LogHelper.LogThreadId(logName,"VerticalSpacingDecorator : Initiated");
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.bottom = verticalSpaceHeight;
        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = verticalSpaceHeight;
        }
    }
}
