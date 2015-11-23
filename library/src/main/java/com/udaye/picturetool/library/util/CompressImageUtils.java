package com.udaye.picturetool.library.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 压缩图片公共类
 */
public class CompressImageUtils {

    public static final String TAG = "ChoosePhotoUtils";

    /**
     * 压缩图片
     *
     * @param compressWidth   宽
     * @param compressHeight  高
     * @param compressQuality 质量（1-100）
     * @param originUri       压缩前的uri
     * @param compressUri     压缩后uri
     */
    public static void compressImageFile(int compressWidth, int compressHeight, int compressQuality, Uri originUri, Uri compressUri) {
        Bitmap bitmap = null;
        OutputStream out = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originUri.getPath(), options);
            // Calculate inSampleSize
            int minSideLength = compressWidth > compressHeight
                    ? compressHeight : compressWidth;
            options.inSampleSize = computeSampleSize(options, minSideLength, compressWidth * compressHeight);
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(originUri.getPath(), options);
            File compressFile = new File(compressUri.getPath());
            if (!compressFile.exists()) {
                boolean result = compressFile.createNewFile();
                Log.d(TAG, "Target " + compressUri + " not exist, create a new one " + result);
            }
            out = new FileOutputStream(compressFile);
            boolean result = bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, out);
            Log.d(TAG, "Compress bitmap " + (result ? "succeed" : "failed"));
        } catch (Exception e) {
            Log.e(TAG, "compressInputStreamToOutputStream", e);
        } finally {
            if (bitmap != null)
                bitmap.recycle();
            try {
                if (out != null)
                    out.close();
            } catch (IOException ignore) {
            }
        }
    }


    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
