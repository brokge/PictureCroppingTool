package com.udaye.picturetool.library.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udaye.picturetool.library.PictureCropperChoiceDialog;
import com.udaye.picturetool.library.entity.PictureCropParams;
import com.udaye.picturetool.library.util.PictureCropperListener;
import com.udaye.picturetool.library.util.PictureIntentHelper;


/**
 * 图片裁剪基类 需要继承，才能使用
 * Created by chenlinwei on 15/11/15.
 */
public class PictureBaseActivity extends AppCompatActivity implements PictureCropperListener {
    PictureCropParams mCropParams;
    PictureCropperChoiceDialog pictureCropperChoiceDialog;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        PictureIntentHelper.handleResult(this, this, requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCropParams = new PictureCropParams(this);
    }

    protected void showChoiceDialog() {
        mCropParams.refreshUri();
        pictureCropperChoiceDialog = PictureCropperChoiceDialog.newInstance(mCropParams.uri);
        pictureCropperChoiceDialog.show(getSupportFragmentManager(), "");
    }

    protected void cancelChoiceDialog() {
        if (pictureCropperChoiceDialog != null && !pictureCropperChoiceDialog.isDetached()) {
            pictureCropperChoiceDialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public void onPictureCropped(Uri uri) {
        cancelChoiceDialog();
    }

    @Override
    public void onCompressed(Uri uri) {
        cancelChoiceDialog();
    }

    @Override
    public void onCancel() {
        cancelChoiceDialog();
    }

    @Override
    public void onFailed(String message) {
        cancelChoiceDialog();
    }

    @Override
    public void handleIntent(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public PictureCropParams getCropParams() {
        return mCropParams;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PictureIntentHelper.clearCacheDir();
    }
}
