package com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper;

import android.content.Context;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.Logger.LogHelper;
import com.workaround.ajeesh.ajr_09032018_workaround_firebase.R;

/**
 * Package Name : com.workaround.ajeesh.ajr_09032018_workaround_firebase.Helper
 * Created by ajesh on 13-03-2018.
 * Project Name : AJR-09032018-WORKAROUND-FIREBASE
 */

public class UniversalImageLoader {
    private static final String logName = "FIREB-HLPR-UNI-IMG-LDR";
    private static final int defaultImage = R.mipmap.ic_default_image_launcher_foreground;
    private Context mContext;

    public UniversalImageLoader(Context mContext) {
        this.mContext = mContext;
        LogHelper.LogThreadId(logName, "Universal Image loader has been initiated.");
    }

    public ImageLoaderConfiguration getConfig() {
        LogHelper.LogThreadId(logName, "Universal Image loader - Getting configuration");

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(defaultImage)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .cacheOnDisk(true).cacheInMemory(true)
                .cacheOnDisk(true).resetViewBeforeLoading(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        LogHelper.LogThreadId(logName, "Universal Image loader - Default options has been set up");

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024)
                .build();

        return config;
    }
}
