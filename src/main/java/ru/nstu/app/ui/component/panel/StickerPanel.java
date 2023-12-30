package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import ru.nstu.app.R;
import ru.nstu.app.android.Box;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.NativeProvider;
import ru.nstu.app.android.Sticker;
import ru.nstu.app.api.action.SendMessageStickerAction;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.model.Dialog;
import ru.nstu.app.ui.component.common.BackendImageView;
import ru.nstu.app.ui.component.common.CircularProgressBar;
import ru.nstu.app.ui.fragment.FixedFrameLayout;

public class StickerPanel extends FixedFrameLayout {
    private static Drawable pause;

    private ImageView stickerImageView;
    private CircularProgressBar stickerCircularProgressBar;

    public static final int SIZE = DisplayController.dp(76);

    public StickerPanel(Context context) {
        super(context);

        stickerImageView = new ImageView(context);
        addView(stickerImageView);
        LayoutParams layoutParams = (LayoutParams)stickerImageView.getLayoutParams();
        layoutParams.width = DisplayController.dp(66);
        layoutParams.height = DisplayController.dp(66);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.topMargin = DisplayController.dp(5);
        stickerImageView.setLayoutParams(layoutParams);

        stickerCircularProgressBar = new CircularProgressBar(context);
        stickerCircularProgressBar.setProgressColor(context.getResources().getColor(R.color.white));
        stickerCircularProgressBar.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        stickerCircularProgressBar.setSize(CircularProgressBar.DIAMETER_BLUE);
        addView(stickerCircularProgressBar);
        layoutParams = (LayoutParams)stickerCircularProgressBar.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = DisplayController.dp(CircularProgressBar.DIAMETER_BLUE);
        layoutParams.height = DisplayController.dp(CircularProgressBar.DIAMETER_BLUE);
        stickerCircularProgressBar.setLayoutParams(layoutParams);
        stickerCircularProgressBar.setVisibility(View.GONE);

        if(StickerPanel.pause == null) {
            StickerPanel.pause = context.getResources().getDrawable(R.drawable.ic_pause);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(DisplayController.dp(76) + getPaddingLeft() + getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(DisplayController.dp(78), MeasureSpec.EXACTLY));
    }

    @Override
    public void setPressed(boolean pressed) {
//        if (imageView.getImageReceiver().getPressed() != pressed) {
//            imageView.getImageReceiver().setPressed(pressed);
//            imageView.invalidate();
//        }
        super.setPressed(pressed);
    }

    public void setData(final Sticker sticker) {
        if(sticker == null) {
            stickerImageView.setImageDrawable(null);
            return;
        }



//        if (document != null) {
//            document.thumb.location.ext = "webp";
//            imageView.setImage(document.thumb.location, null, (Drawable) null);
//        }
//        if (side == -1) {
//            setBackgroundResource(R.drawable.stickers_back_left);
//            setPadding(DisplayController.dp(7), 0, 0, 0);
//        } else if (side == 0) {
//            setBackgroundResource(R.drawable.stickers_back_center);
//            setPadding(0, 0, 0, 0);
//        } else if (side == 1) {
//            setBackgroundResource(R.drawable.stickers_back_right);
//            setPadding(0, 0, DisplayController.dp(7), 0);
//        } else if (side == 2) {
//            setBackgroundResource(R.drawable.stickers_back_all);
//            setPadding(DisplayController.dp(3), 0, DisplayController.dp(3), 0);
//        }
//        if (getBackground() != null) {
//            getBackground().setAlpha(230);
//        }
//        imageView.setImageBitmap(NativeProvider.loadWebpImage(null, 2, ));
        stickerImageView.setImageDrawable(null);
        final int tag = FileController.getId(sticker.getThumb().photo);
        setTag(R.id.TAG_0, tag);

        Dialog dialog = (Dialog) Box.get(Box.DIALOG);
        final long dialogId = dialog.getId();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FileController.isCached(sticker.getFile())) {
                    Droid.doAction(new SendMessageStickerAction(dialogId, FileController.getPath(sticker.getFile())));
                } else {
                    stickerCircularProgressBar.setContent(StickerPanel.pause, true, true);
                    stickerCircularProgressBar.reset();
                    stickerCircularProgressBar.setVisibility(View.VISIBLE);
                    final WeakReference<CircularProgressBar> weakReferenceStickerCircularProgressBar = new WeakReference<CircularProgressBar>(stickerCircularProgressBar);
                    final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(sticker.getFile());
                    FileController.load(FileController.getId(sticker.getFile()), new Callback() {
                        @Override
                        public void call(final Object value) {
                            Droid.doRunnableUI(new Runnable() {
                                @Override
                                public void run() {
                                    if(!StickerPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                        return;
                                    }

                                    if(value instanceof TdApi.UpdateFileProgress) {
                                        if(weakReferenceStickerCircularProgressBar.get() != null) {
                                            weakReferenceStickerCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
                                        }
                                    } else if(value instanceof TdApi.UpdateFile) {
                                        if(weakReferenceStickerCircularProgressBar.get() != null) {
                                            weakReferenceStickerCircularProgressBar.get().finish(null);
                                        }
                                        if(weakReferenceFile.get() != null)  {
                                            Droid.doAction(new SendMessageStickerAction(dialogId, FileController.getPath(weakReferenceFile.get())));
                                        }

                                    }
                                }
                            });
                        }
                    });
                }
            }
        });

        if(FileController.isCached(sticker.getThumb().photo)) {
            FileController.load(stickerImageView, FileController.getPath(sticker.getThumb().photo));
        } else {
            FileController.load(FileController.getId(sticker.getThumb().photo), new Callback() {
                @Override
                public void call(Object value) {
                    if(value instanceof TdApi.UpdateFile) {
//
                        if(StickerPanel.this.getTag(R.id.TAG_0).equals(tag)) {
                            FileController.load(stickerImageView, FileController.getPath(sticker.getThumb().photo));
                        }

                    }
                }
            });
        }

    }
}
