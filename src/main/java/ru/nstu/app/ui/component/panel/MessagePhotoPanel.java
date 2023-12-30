package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.component.common.CircularProgressBar;

public class MessagePhotoPanel extends MessagePanel {
    private static Drawable pause;

    protected ImageView photoImageView;
    protected CircularProgressBar photoCircularProgressBar;

    public MessagePhotoPanel(Context context) {
        super(context);

        photoImageView = new ImageView(context);
        addView(photoImageView);
        LayoutParams layoutParams = (LayoutParams)photoImageView.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        photoImageView.setLayoutParams(layoutParams);

        photoCircularProgressBar = new CircularProgressBar(context);
        photoCircularProgressBar.setProgressColor(context.getResources().getColor(R.color.white));
        photoCircularProgressBar.setSize(CircularProgressBar.DIAMETER_BLACK);
        addView(photoCircularProgressBar);
        layoutParams = (LayoutParams)photoCircularProgressBar.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.width = DisplayController.dp(CircularProgressBar.DIAMETER_BLACK);
        layoutParams.height = DisplayController.dp(CircularProgressBar.DIAMETER_BLACK);
        photoCircularProgressBar.setLayoutParams(layoutParams);

        if(MessagePhotoPanel.pause == null) {
//            MessagePhotoPanel.pause = context.getResources().getDrawable(R.drawable.photopause);
            MessagePhotoPanel.pause = context.getResources().getDrawable(R.drawable.ic_pause);
        }
    }

    @Override
    public void update() {
        TdApi.File file = null;
        int fixedWidth = DisplayController.dp(200);//(int)(DisplayController.screenWidth * 0.5);
        int width = 0;
        int height = 0;

        for(TdApi.PhotoSize photoSize : message.getContentPhotoSizes()) {
            if(file == null) {
                file = photoSize.photo;
                width = photoSize.width;
                height = photoSize.height;
                continue;
            }
            if(photoSize.width <= fixedWidth) {
                if(width < fixedWidth && fixedWidth - width > fixedWidth - photoSize.width) {
                    file = photoSize.photo;
                    width = photoSize.width;
                    height = photoSize.height;
                }
            }
            if(photoSize.width > fixedWidth) {
                if(width < fixedWidth || width - fixedWidth > photoSize.width - fixedWidth) {
                    file = photoSize.photo;
                    width = photoSize.width;
                    height = photoSize.height;
                }
            }
        }

        if(width != 0 && height != 0) {
            height = fixedWidth * height / width;
            width = fixedWidth;
        } else {
            width = fixedWidth;
            height = fixedWidth;
        }

        final int tag = FileController.getId(file);
        setTag(R.id.TAG_0, tag);


        photoImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Droid.activity.getMessagesCreator().showPhotoView(Droid.activity, message);
            }
        });

        photoImageView.setImageDrawable(null);
        LayoutParams layoutParams = (LayoutParams)photoImageView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        photoImageView.setLayoutParams(layoutParams);

        photoCircularProgressBar.setContent(MessagePhotoPanel.pause, true, true);
        photoCircularProgressBar.reset();
        layoutParams = (LayoutParams)photoCircularProgressBar.getLayoutParams();
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + width / 2 - CircularProgressBar.DIAMETER_BLACK / 2));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + width / 2 - CircularProgressBar.DIAMETER_BLACK / 2) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset + height / 2 - CircularProgressBar.DIAMETER_BLACK / 2);
        photoCircularProgressBar.setLayoutParams(layoutParams);

        if(message.isSending()) {
            photoCircularProgressBar.setVisibility(View.VISIBLE);
            FileController.loadAndCrop(photoImageView, FileController.getPath(file));
            final WeakReference<CircularProgressBar> weakReferencePhotoCircularProgressBar = new WeakReference<CircularProgressBar>(photoCircularProgressBar);
            FileController.addCallback(FileController.getId(file), new Callback() {
                @Override
                public void call(final Object value) {
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            if(!MessagePhotoPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                return;
                            }

                            if(value instanceof TdApi.UpdateFileProgress) {
                                if(weakReferencePhotoCircularProgressBar.get() != null) {
                                    weakReferencePhotoCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
                                }
                            } else if(value instanceof TdApi.UpdateFile) {
                                if(weakReferencePhotoCircularProgressBar.get() != null) {
                                    weakReferencePhotoCircularProgressBar.get().finish(null);
                                }
                            }
                        }
                    });
                }
            });
            return;
        }

        if(FileController.isCached(file)) {
            photoCircularProgressBar.setVisibility(View.GONE);
            FileController.load(photoImageView, FileController.getPath(file));
        } else {
            photoCircularProgressBar.setVisibility(View.VISIBLE);
            final WeakReference<ImageView> weakReferencePhotoImageView = new WeakReference<ImageView>(photoImageView);
            final WeakReference<CircularProgressBar> weakReferencePhotoCircularProgressBar = new WeakReference<CircularProgressBar>(photoCircularProgressBar);
            final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(file);
            FileController.load(FileController.getId(file), new Callback() {
                @Override
                public void call(final Object value) {
                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            if(!MessagePhotoPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                return;
                            }

                            if(value instanceof TdApi.UpdateFileProgress) {
                                if(weakReferencePhotoCircularProgressBar.get() != null) {
                                    weakReferencePhotoCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
                                }
                            } else if(value instanceof TdApi.UpdateFile) {
                                if(weakReferencePhotoCircularProgressBar.get() != null) {
                                    weakReferencePhotoCircularProgressBar.get().finish(null);
                                }
                                if(weakReferencePhotoImageView.get() != null && weakReferenceFile.get() != null) {
                                    FileController.load(weakReferencePhotoImageView.get(), FileController.getPath(weakReferenceFile.get()));
                                }
                            }
                        }
                    });
                }
            });
        }
    }
}
