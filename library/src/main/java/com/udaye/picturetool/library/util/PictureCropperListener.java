package com.udaye.picturetool.library.util;

import android.content.Intent;
import android.net.Uri;

import com.udaye.picturetool.library.entity.PictureCropParams;


/**
 * 监听接口
 * Created by chenlinwei on 15/11/7.
 */
public interface PictureCropperListener {

    void onPictureCropped(Uri uri);

    void onCompressed(Uri uri);

    void onCancel();

    void onFailed(String message);

    void handleIntent(Intent intent, int requestCode);

    PictureCropParams getCropParams();
}
