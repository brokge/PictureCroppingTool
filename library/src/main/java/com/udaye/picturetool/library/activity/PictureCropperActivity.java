package com.udaye.picturetool.library.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.udaye.picturetool.library.R;
import com.udaye.picturetool.library.util.BitmapUtil;
import com.udaye.picturetool.library.util.PictureIntentHelper;
import com.udaye.picturetool.library.widget.ClipView;


/**
 * 裁剪界面
 * <p/>
 * Created by chenlinwei on 15/11/6.
 */
public class PictureCropperActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView srcPic;
    private ClipView clipview;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    /**
     * 动作标志：无
     */
    private static final int NONE = 0;
    /**
     * 动作标志：拖动
     */
    private static final int DRAG = 1;
    /**
     * 动作标志：缩放
     */
    private static final int ZOOM = 2;
    /**
     * 初始化动作标志
     */
    private int mode = NONE;

    /**
     * 记录起始坐标
     */
    private PointF start = new PointF();
    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mid = new PointF();
    private float oldDist = 1f;

    private Bitmap bitmap;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getExtras();
        setContentView(R.layout.picture_activity_main);
        initToolbar();
        srcPic = (ImageView) this.findViewById(R.id.src_pic);
        initView();
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.picture_toolbar);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("图片裁剪");
        }

    }

    private void getExtras() {
        Bundle bundle = getIntent().getExtras();
        Uri uri = bundle.getParcelable("uri");
        if (uri != null) {
            bitmap = BitmapUtil.decodeUriAsBitmap(this, uri);
            try {
                int degree = BitmapUtil.getBitmapDegree(uri.getPath());
                // bitmap.get
                if (0 != degree)
                    if (bitmap != null) {
                        BitmapUtil.rotateBitmapByDegree(bitmap, 0);
                    }
            } catch (Exception e) {
            }

        }

    }

    private void initView() {
        srcPic.setOnTouchListener(this);
        ViewTreeObserver observer = srcPic.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            public void onGlobalLayout() {
                srcPic.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initClipView(srcPic.getTop());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.picture_menu_sure) {
            onClick();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 初始化截图区域，并将源图按裁剪框比例缩放
     *
     * @param top
     */
    private void initClipView(int top) {
        if (bitmap != null) {
            clipview = new ClipView(PictureCropperActivity.this);
            clipview.setCustomTopBarHeight(top);
            clipview.addOnDrawCompleteListener(new ClipView.OnDrawListenerComplete() {

                public void onDrawCompelete() {
                    clipview.removeOnDrawCompleteListener();
                    int clipHeight = clipview.getClipHeight();
                    int clipWidth = clipview.getClipWidth();
                    int midX = clipview.getClipLeftMargin() + (clipWidth / 2);
                    int midY = clipview.getClipTopMargin() + (clipHeight / 2);

                    int imageWidth = bitmap.getWidth();
                    int imageHeight = bitmap.getHeight();
                    // 按裁剪框求缩放比例
                    float scale = (clipWidth * 1.0f) / imageWidth;
                    if (imageWidth > imageHeight) {
                        scale = (clipHeight * 1.0f) / imageHeight;
                    }

                    // 起始中心点
                    float imageMidX = imageWidth * scale / 2;
                    float imageMidY = clipview.getCustomTopBarHeight()
                            + imageHeight * scale / 2;
                    srcPic.setScaleType(ImageView.ScaleType.MATRIX);

                    // 缩放
                    matrix.postScale(scale, scale);
                    // 平移
                    matrix.postTranslate(midX - imageMidX, midY - imageMidY);

                    srcPic.setImageMatrix(matrix);
                    srcPic.setImageBitmap(bitmap);
                }
            });

            this.addContentView(clipview, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                // 设置开始点位置
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY()
                            - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        view.setImageMatrix(matrix);
        return true;
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     *
     * @param event
     * @return
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     *
     * @param point
     * @param event
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 获取裁剪框内截图
     *
     * @return
     */
    private Bitmap getBitmap() {
        // 获取截屏
        View view = this.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        Bitmap finalBitmap = Bitmap.createBitmap(view.getDrawingCache(),
                clipview.getClipLeftMargin(), clipview.getClipTopMargin()
                        + statusBarHeight, clipview.getClipWidth(),
                clipview.getClipHeight());

        // 释放资源
        view.destroyDrawingCache();
        return finalBitmap;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onContextItemSelected(item);
    }

    /**
     * 确认点击事件
     */
    public void onClick() {
        Bitmap clipBitmap = getBitmap();
        Uri uri = PictureIntentHelper.generateUri();
        Uri saveUri = BitmapUtil.saveBitmapAsUri(clipBitmap, uri);
        Intent intent = new Intent();
        intent.setData(saveUri);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


}
