package com.udaye.picturetool.library.entity;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.udaye.picturetool.library.util.PictureIntentHelper;


/**
 */
public class PictureCropParams {

    public Uri uri;
    public Context context;

    public PictureCropParams(Context context) {
        this.context = context;
        refreshUri();
    }

    public void refreshUri() {
        uri = PictureIntentHelper.generateUri();
        Log.d("picturetool:", "refreshUri=" + uri.toString());
    }
}
