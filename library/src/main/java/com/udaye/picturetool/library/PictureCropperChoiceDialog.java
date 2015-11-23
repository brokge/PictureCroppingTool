package com.udaye.picturetool.library;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.udaye.picturetool.library.util.PictureIntentHelper;


/**
 * 选择图片路径
 * Created by chenlinwei on 15/11/6.
 */
public class PictureCropperChoiceDialog extends DialogFragment {

    private TextView camTextView;
    private TextView galleryTextView;
    private Uri uri;

    public static PictureCropperChoiceDialog newInstance(Uri uri) {
        Bundle args = new Bundle();
        PictureCropperChoiceDialog fragment = new PictureCropperChoiceDialog();
        args.putParcelable("uri", uri);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.picture_choice_dialog, null);
        camTextView = (TextView) view.findViewById(R.id.picture_choice_dialog_camera);
        galleryTextView = (TextView) view.findViewById(R.id.picture_choice_dialog_gallery);
        camTextView.setOnClickListener(onClickListener);
        galleryTextView.setOnClickListener(onClickListener);
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        window.setGravity(Gravity.CENTER);
        window.setAttributes(layoutParams);
        return dialog;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        uri = bundle.getParcelable("uri");

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.picture_choice_dialog_gallery) {
                Intent intent = PictureIntentHelper.buildGalleryIntent(uri);
                getActivity().startActivityForResult(intent, PictureConstants.PICTURE_GALLERY);
            }
            if (id == R.id.picture_choice_dialog_camera) {
                Intent intent = PictureIntentHelper.buildCameraIntent(uri);
                getActivity().startActivityForResult(intent, PictureConstants.PICTURE_CAMMERA);

            }
        }
    };

}
