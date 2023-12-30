package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.component.common.CircularProgressBar;

public class MessageVideoPanel extends MessagePanel {
    private static Drawable load;
    private static Drawable pause;
    private static Drawable play;

    protected ImageView videoImageView;
    protected CircularProgressBar videoCircularProgressBar;

    public MessageVideoPanel(Context context) {
        super(context);

        videoImageView = new ImageView(context);
        addView(videoImageView);
        LayoutParams layoutParams = (LayoutParams)videoImageView.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        videoImageView.setLayoutParams(layoutParams);

        videoCircularProgressBar = new CircularProgressBar(context);
        videoCircularProgressBar.setProgressColor(context.getResources().getColor(R.color.white));
        videoCircularProgressBar.setSize(CircularProgressBar.DIAMETER_BLACK);
        addView(videoCircularProgressBar);
        layoutParams = (LayoutParams)videoCircularProgressBar.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.width = DisplayController.dp(CircularProgressBar.DIAMETER_BLACK);
        layoutParams.height = DisplayController.dp(CircularProgressBar.DIAMETER_BLACK);
        videoCircularProgressBar.setLayoutParams(layoutParams);

        if(MessageVideoPanel.load == null) {
//            MessageVideoPanel.load = context.getResources().getDrawable(R.drawable.photoload);
            MessageVideoPanel.load = context.getResources().getDrawable(R.drawable.ic_download);
        }
        if(MessageVideoPanel.pause == null) {
//            MessageVideoPanel.pause = context.getResources().getDrawable(R.drawable.photopause);
            MessageVideoPanel.pause = context.getResources().getDrawable(R.drawable.ic_pause);
        }
        if(MessageVideoPanel.play == null) {
//            MessageVideoPanel.play = context.getResources().getDrawable(R.drawable.playvideo);
            MessageVideoPanel.play = context.getResources().getDrawable(R.drawable.ic_play);
        }
    }

    @Override
    public void update() {
        TdApi.File thumb = message.getContentVideoThumb().photo;
        int fixedWidth = DisplayController.dp(200);//(int)(DisplayController.screenWidth * 0.5);
        int width = message.getContentVideoThumb().width;
        int height = message.getContentVideoThumb().height;

        if(!FileController.isExists(thumb)) {
            width = DisplayController.dp(CircularProgressBar.DIAMETER_BLACK);
            height = DisplayController.dp(CircularProgressBar.DIAMETER_BLACK);
        } else {
            height = fixedWidth * height / width;
            width = fixedWidth;
        }

        final int tag = FileController.getId(thumb);
        setTag(R.id.TAG_0, tag);

        videoImageView.setImageDrawable(null);
        LayoutParams layoutParams = (LayoutParams)videoImageView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        videoImageView.setLayoutParams(layoutParams);

        videoCircularProgressBar.reset();
        layoutParams = (LayoutParams)videoCircularProgressBar.getLayoutParams();
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + width / 2 - CircularProgressBar.DIAMETER_BLACK / 2));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + width / 2 - CircularProgressBar.DIAMETER_BLACK / 2) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset + height / 2 - CircularProgressBar.DIAMETER_BLACK / 2);
        videoCircularProgressBar.setLayoutParams(layoutParams);

        if(FileController.isExists(thumb)) {
            if(FileController.isCached(thumb)) {
                FileController.load(videoImageView, FileController.getPath(thumb));
            } else {
                final WeakReference<ImageView> weakReferenceVideoImageView = new WeakReference<ImageView>(videoImageView);
                final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(thumb);
                FileController.load(FileController.getId(thumb), new Callback() {
                    @Override
                    public void call(final Object value) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                if(!MessageVideoPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                    return;
                                }
                                if(value instanceof TdApi.UpdateFile) {
                                    if(weakReferenceVideoImageView.get() != null && weakReferenceFile.get() != null) {
                                        FileController.load(weakReferenceVideoImageView.get(), FileController.getPath(weakReferenceFile.get()));
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }


        if(FileController.isCached(message.getContentVideoFile())) {
            videoCircularProgressBar.setContent(MessageVideoPanel.play, false, false);
        } else if(FileController.isLoading(message.getContentVideoFile())) {
            videoCircularProgressBar.setContent(MessageVideoPanel.pause, true, true);
            videoCircularProgressBar.setProgress(FileController.getProgress(FileController.getId(message.getContentVideoFile())));
        } else {
            videoCircularProgressBar.setContent(MessageVideoPanel.load, false, false);
            final WeakReference<CircularProgressBar> weakReferenceVideoCircularProgressBar = new WeakReference<CircularProgressBar>(videoCircularProgressBar);
            videoCircularProgressBar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(FileController.isCached(message.getContentVideoFile()) || FileController.isLoading(message.getContentVideoFile())) {
                        return;
                    }
                    videoCircularProgressBar.setContent(MessageVideoPanel.pause, true, false);
                    final int tag = FileController.getId(message.getContentVideoFile());
                    MessageVideoPanel.this.setTag(R.id.TAG_1, tag);
                    FileController.load(FileController.getId(message.getContentVideoFile()), new Callback() {
                        @Override
                        public void call(final Object value) {
                            Droid.doRunnableUI(new Runnable() {
                                @Override
                                public void run() {
                                    if(!MessageVideoPanel.this.getTag(R.id.TAG_1).equals(Integer.valueOf(tag))) {
                                        return;
                                    }

                                    if(value instanceof TdApi.UpdateFileProgress) {
                                        if(weakReferenceVideoCircularProgressBar.get() != null) {
                                            weakReferenceVideoCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
                                        }
                                    } else if(value instanceof TdApi.UpdateFile) {
                                        if(weakReferenceVideoCircularProgressBar.get() != null) {
                                            weakReferenceVideoCircularProgressBar.get().finish(MessageVideoPanel.play);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}
