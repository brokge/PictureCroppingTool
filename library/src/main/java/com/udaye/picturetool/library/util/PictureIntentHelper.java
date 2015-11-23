package com.udaye.picturetool.library.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.udaye.picturetool.library.PictureConstants;
import com.udaye.picturetool.library.activity.PictureCropperActivity;
import com.udaye.picturetool.library.entity.PictureCropParams;

import java.io.File;


/**
 * Intent 帮助类
 * Created by chenlinwei on 15/11/6.
 */
public class PictureIntentHelper {
    private static String TAG = "PictureIntentHelper";

    public static final String CROP_CACHE_FOLDER = "cropperpicture";

    public static Uri generateUri() {
        File cacheFolder = new File(Environment.getExternalStorageDirectory() + File.separator + CROP_CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            try {
                boolean result = cacheFolder.mkdir();
                Log.d(TAG, "generateUri " + cacheFolder + " result: " + (result ? "succeeded" : "failed"));
            } catch (Exception e) {
                Log.e(TAG, "generateUri failed: " + cacheFolder, e);
            }
        }
        String name = String.format("dxy-cropper-%d.jpg", System.currentTimeMillis());
        return Uri
                .fromFile(cacheFolder)
                .buildUpon()
                .appendPath(name)
                .build();
    }

    /**
     * 构建camera intent
     *
     * @param uri uri
     * @return intent
     */

    public static Intent buildCameraIntent(Uri uri) {
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, uri);
    }

    /**
     * 构建gallery intent
     *
     * @param uri uri
     * @return intent
     */
    public static Intent buildGalleryIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("image/*")
                .putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }

    public static void handleResult(Context context, PictureCropperListener listener, int requestCode, int resultCode, Intent data) {
        if (listener == null) return;

        if (resultCode == Activity.RESULT_CANCELED) {
            listener.onCancel();
        } else if (resultCode == Activity.RESULT_OK) {
            PictureCropParams cropParams = listener.getCropParams();
            if (cropParams == null) {
                listener.onFailed("CropHandler's params MUST NOT be null!");
                return;
            }
            switch (requestCode) {
                case PictureConstants.PICTURE_GALLERY:
                    if (context != null) {
                        if (data != null && data.getData() != null) {
                            //获取文件所在的路径
                            String path = CropFileUtils.getSmartFilePath(context, data.getData());
                            //copy到自己定义的文件夹下
                            boolean result = CropFileUtils.copyFile(path, cropParams.uri.getPath());
                            Log.d("picturetool:", "PICTURE_CROP=" + cropParams.uri.toString());
                            if (!result) {
                                listener.onFailed("Copy file to cached folder failed");
                                break;
                            } else {
                                Intent intent = buildCropFromUriIntent(context, cropParams);
                                listener.handleIntent(intent, PictureConstants.PICTURE_CROP);
                            }
                        } else {
                            listener.onFailed("Returned data is null " + data);
                            break;
                        }
                    } else {
                        listener.onFailed("CropHandler's context MUST NOT be null!");
                    }
                    break;

                case PictureConstants.PICTURE_CROP: {
                    //裁剪过的图片需要特殊处理
                    if (isPhotoReallyCropped(data.getData())) {
                        Log.d(TAG, "Photo cropped!");
                        //裁剪过的图片需要压缩的话在这个方法里面
                        cropParams.uri = data.getData();
                        onPictureCropped(listener, cropParams);
                        Log.d("picturetool:", "PICTURE_CROP=" + cropParams.uri.toString());
                        break;
                    } else {
                        listener.onFailed("Returned data is null " + data);
                    }
                    break;
                }
                case PictureConstants.PICTURE_CAMMERA:
                    Log.d("picturetool:", "PICTURE_CAMMERA=" + cropParams.uri.toString());
                    // Send this Uri to Crop
                    Intent intent = buildCropFromUriIntent(context, cropParams);
                    listener.handleIntent(intent, PictureConstants.PICTURE_CROP);
                    break;
            }
        }
    }


    /**
     * 跳转到裁剪 窗口
     *
     * @param context
     * @param params
     * @return
     */
    private static Intent buildCropFromUriIntent(Context context, PictureCropParams params) {
        Intent intent = new Intent(context, PictureCropperActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("uri", params.uri);
        intent.putExtras(bundle);
        return intent;
    }

    /**
     * 验证是否被裁剪过
     *
     * @param uri
     * @return
     */
    public static boolean isPhotoReallyCropped(Uri uri) {
        File file = new File(uri.getPath());
        long length = file.length();
        return length > 0;
    }

    /**
     * 如果已经被裁剪过，则抛出接口
     *
     * @param listener
     * @param cropParams
     */
    private static void onPictureCropped(PictureCropperListener listener, PictureCropParams cropParams) {
     /*   if (cropParams.compress) {
            Uri originUri = cropParams.uri;
            Uri compressUri = PictureIntentHelper.generateUri();
            CompressImageUtils.compressImageFile(cropParams, originUri, compressUri);
            listener.onCompressed(compressUri);
        } else {*/
        listener.onPictureCropped(cropParams.uri);
        // }
    }

    /**
     * 清理缓存的文件夹
     *
     * @return
     */
    public static boolean clearCacheDir() {
        File cacheFolder = new File(Environment.getExternalStorageDirectory() + File.separator + CROP_CACHE_FOLDER);
        if (cacheFolder.exists() && cacheFolder.listFiles() != null) {
            for (File file : cacheFolder.listFiles()) {
                boolean result = file.delete();
                Log.d(TAG, "Delete " + file.getAbsolutePath() + (result ? " succeeded" : " failed"));
            }
            return true;
        }
        return false;
    }

    /**
     * 清理缓存过裁剪过的图片
     *
     * @param uri
     * @return
     */

    public static boolean clearCachedCropFile(Uri uri) {
        if (uri == null) return false;

        File file = new File(uri.getPath());
        if (file.exists()) {
            boolean result = file.delete();
            Log.d(TAG, "Delete " + file.getAbsolutePath() + (result ? " succeeded" : " failed"));
            return result;
        }
        return false;
    }

}
