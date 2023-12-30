package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Gravity;
import android.widget.ImageView;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.NativeProvider;
import ru.nstu.app.android.Sticker;
import ru.nstu.app.api.action.Action;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.component.common.CircularProgressBar;

public class MessageStickerPanel extends MessagePanel {
    protected ImageView stickerImageView;

    public MessageStickerPanel(Context context) {
        super(context);

        stickerImageView = new ImageView(context);
        addView(stickerImageView);
        LayoutParams layoutParams = (LayoutParams)stickerImageView.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);

        stickerImageView.setLayoutParams(layoutParams);
    }

    @Override
    public void update() {
        final Sticker sticker = message.getContentSticker();
        int fixedWidth = DisplayController.dp(82);
        LayoutParams layoutParams = (LayoutParams)stickerImageView.getLayoutParams();
        if(sticker.getWidth() == 0 || sticker.getHeight() == 0) {
            layoutParams.width = fixedWidth;
            layoutParams.height = 512 * fixedWidth / 350;
        } else {
            layoutParams.height = fixedWidth * sticker.getHeight() / sticker.getWidth();
            layoutParams.width = fixedWidth;
        }

        stickerImageView.setLayoutParams(layoutParams);
        stickerImageView.setImageBitmap(null);
        stickerImageView.setImageDrawable(null);

        final int tag = FileController.getId(sticker.getFile());
        setTag(R.id.TAG_0, tag);

        final boolean ableWebp = Build.VERSION.SDK_INT > 17;

        if(FileController.isCached(sticker.getFile())) {
            if(ableWebp) {
                FileController.load(stickerImageView, FileController.getPath(sticker.getFile()));
            } else {
                Droid.doAction(new Action() {
                    @Override
                    public void run(Client client) throws Exception {
                        final Bitmap bitmap = loadWebp(sticker.getFile());
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                stickerImageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                });

            }
        } else {
            if(FileController.isCached(sticker.getThumb().photo) && !FileController.isCached(sticker.getFile())) {
                if(ableWebp) {
                    FileController.load(stickerImageView, FileController.getPath(sticker.getThumb().photo));
                } else {
                    Droid.doAction(new Action() {
                        @Override
                        public void run(Client client) throws Exception {
                            final Bitmap bitmap = loadWebp(sticker.getThumb().photo);
                            Droid.doRunnableUI(new Runnable() {
                                @Override
                                public void run() {
                                    stickerImageView.setImageBitmap(bitmap);
                                }
                            });
                        }
                    });

                }
            } else {
                final WeakReference<ImageView> weakReferenceStickerImageView = new WeakReference<ImageView>(stickerImageView);
                final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(sticker.getThumb().photo);
                FileController.load(FileController.getId(sticker.getThumb().photo), new Callback() {
                    @Override
                    public void call(final Object value) {
                        if(!MessageStickerPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                            return;
                        }

                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                if(value instanceof TdApi.UpdateFile) {
                                    if(weakReferenceStickerImageView.get() != null && weakReferenceFile.get() != null) {
                                        if(ableWebp) {
                                            FileController.load(weakReferenceStickerImageView.get(), FileController.getPath(weakReferenceFile.get()));
                                        } else {
                                            Droid.doAction(new Action() {
                                                @Override
                                                public void run(Client client) throws Exception {
                                                    final Bitmap bitmap = loadWebp(weakReferenceFile.get());
                                                    Droid.doRunnableUI(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            weakReferenceStickerImageView.get().setImageBitmap(bitmap);
                                                        }
                                                    });
                                                }
                                            });
                                        }

                                    }
                                }
                            }
                        });
                    }
                });
            }

            final WeakReference<ImageView> weakReferenceStickerImageView = new WeakReference<ImageView>(stickerImageView);
            final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(sticker.getFile());
            FileController.load(FileController.getId(sticker.getFile()), new Callback() {
                @Override
                public void call(final Object value) {
                    if(!MessageStickerPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                        return;
                    }

                    Droid.doRunnableUI(new Runnable() {
                        @Override
                        public void run() {
                            if(value instanceof TdApi.UpdateFile) {
                                if(weakReferenceStickerImageView.get() != null && weakReferenceFile.get() != null) {
                                    if(ableWebp) {
                                        FileController.load(weakReferenceStickerImageView.get(), FileController.getPath(weakReferenceFile.get()));
                                    } else {
                                        weakReferenceStickerImageView.get().setImageBitmap(loadWebp(weakReferenceFile.get()));
                                    }

                                }
                            }
                        }
                    });

                }
            });
        }
    }

    private Bitmap loadWebp(TdApi.File f) {
        try {
            File stickerFile = new File(FileController.getPath(f));
            RandomAccessFile randomAccessFile = new RandomAccessFile(stickerFile, "r");
            final ByteBuffer byteBuffer = randomAccessFile.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, stickerFile.length());
            return NativeProvider.loadWebpImage(byteBuffer, byteBuffer.limit(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
