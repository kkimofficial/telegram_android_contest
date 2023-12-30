package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;
import java.util.Set;

import ru.nstu.app.R;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;

public abstract class GalleryPhotoPanel extends FrameLayout {
    private ImageView galleryPhotoImageView;
    private View galleryPhotoCoverView;
    private ImageView galleryPhotoCheckImageView;

    public GalleryPhotoPanel(Context context) {
        super(context);

        RecyclerView.LayoutParams layoutParams1 = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams1);

        galleryPhotoImageView = new ImageView(context);
        addView(galleryPhotoImageView);
        LayoutParams layoutParams = (LayoutParams)galleryPhotoImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(100);
        layoutParams.height = DisplayController.dp(100);
        galleryPhotoImageView.setLayoutParams(layoutParams);

        galleryPhotoCoverView = new View(context);
        galleryPhotoCoverView.setBackgroundColor(0x33000000);
        addView(galleryPhotoCoverView);
        layoutParams = (LayoutParams)galleryPhotoCoverView.getLayoutParams();
        layoutParams.width = DisplayController.dp(100);
        layoutParams.height = DisplayController.dp(100);
        galleryPhotoCoverView.setLayoutParams(layoutParams);

        galleryPhotoCheckImageView = new ImageView(context);
        galleryPhotoCheckImageView.setBackgroundResource(R.drawable.ic_attach_check);
        addView(galleryPhotoCheckImageView);
        layoutParams = (LayoutParams)galleryPhotoCheckImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(28);
        layoutParams.height = DisplayController.dp(28);
        layoutParams.topMargin = DisplayController.dp(4);
        layoutParams.rightMargin = DisplayController.dp(4);
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        galleryPhotoCheckImageView.setLayoutParams(layoutParams);
    }

    public void setData(final String path, List<String> pathsList, final Set<String> pathsSet) {
        FileController.loadAndCrop(galleryPhotoImageView, path);

        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams)getLayoutParams();
        layoutParams.setMargins(DisplayController.dp(6), DisplayController.dp(6), DisplayController.dp(pathsList.get(pathsList.size() - 1).equals(path) ? 6 : 0), DisplayController.dp(6));
        setLayoutParams(layoutParams);

        if(pathsSet.contains(path)) {
            galleryPhotoCoverView.setVisibility(View.VISIBLE);
            galleryPhotoCheckImageView.setVisibility(View.VISIBLE);
        } else {
            galleryPhotoCoverView.setVisibility(View.GONE);
            galleryPhotoCheckImageView.setVisibility(View.GONE);
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pathsSet.contains(path)) {
                    pathsSet.remove(path);
                } else {
                    pathsSet.add(path);
                }

                if(pathsSet.contains(path)) {
                    galleryPhotoCoverView.setVisibility(View.VISIBLE);
                    galleryPhotoCheckImageView.setVisibility(View.VISIBLE);
                } else {
                    galleryPhotoCoverView.setVisibility(View.GONE);
                    galleryPhotoCheckImageView.setVisibility(View.GONE);
                }

                GalleryPhotoPanel.this.onClick();
            }
        });

    }


    public abstract void onClick();
}
