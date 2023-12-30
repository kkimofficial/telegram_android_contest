package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.ui.component.common.CircularProgressBar;

public class MessageDocumentPanel extends MessagePanel {
    private static Drawable load;
    private static Drawable pause;
    private static Drawable document;
    private static Drawable load_blue;
    private static Drawable pause_blue;

    protected ImageView documentImageView;
    protected TextView documentTitleTextView;
    protected TextView documentSizeTextView;
    protected CircularProgressBar documentCircularProgressBar;

    public MessageDocumentPanel(Context context) {
        super(context);

        documentImageView = new ImageView(context);
        addView(documentImageView);
        LayoutParams layoutParams = (LayoutParams) documentImageView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        documentImageView.setLayoutParams(layoutParams);

        documentTitleTextView = new TextView(context);
        documentTitleTextView.setLines(1);
        documentTitleTextView.setMaxLines(1);
        documentTitleTextView.setSingleLine(true);
        documentTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
        documentTitleTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(documentTitleTextView);
        layoutParams = (LayoutParams) documentTitleTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        documentTitleTextView.setLayoutParams(layoutParams);
        documentTitleTextView.setTextColor(0xff569ace);
        documentTitleTextView.setTextSize(DisplayController.sp(15));
        documentTitleTextView.setTypeface(documentTitleTextView.getTypeface(), Typeface.BOLD);

        documentSizeTextView = new TextView(context);
        documentSizeTextView.setLines(1);
        documentSizeTextView.setMaxLines(1);
        documentSizeTextView.setSingleLine(true);
        documentSizeTextView.setEllipsize(TextUtils.TruncateAt.END);
        documentSizeTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(documentSizeTextView);
        layoutParams = (LayoutParams) documentSizeTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.topMargin = DisplayController.dp(46 + verticalOffset);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        documentSizeTextView.setLayoutParams(layoutParams);
        documentSizeTextView.setTextColor(0xffb2b2b2);
        documentSizeTextView.setTextSize(DisplayController.sp(13));

        documentCircularProgressBar = new CircularProgressBar(context);
        documentCircularProgressBar.setProgressColor(context.getResources().getColor(R.color.white));
        addView(documentCircularProgressBar);
        layoutParams = (LayoutParams)documentCircularProgressBar.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        documentCircularProgressBar.setLayoutParams(layoutParams);

        if(MessageDocumentPanel.load == null) {
            MessageDocumentPanel.load = context.getResources().getDrawable(R.drawable.ic_download);
        }
        if(MessageDocumentPanel.pause == null) {
            MessageDocumentPanel.pause = context.getResources().getDrawable(R.drawable.ic_pause);
        }
        if(MessageDocumentPanel.document == null) {
            MessageDocumentPanel.document = context.getResources().getDrawable(R.drawable.ic_file);
        }
        if(MessageDocumentPanel.load_blue == null) {
            MessageDocumentPanel.load_blue = context.getResources().getDrawable(R.drawable.ic_download_blue);
        }
        if(MessageDocumentPanel.pause_blue == null) {
            MessageDocumentPanel.pause_blue = context.getResources().getDrawable(R.drawable.ic_pause_blue);
        }
    }

    @Override
    public void update() {
        TdApi.File thumb = message.getContentDocumentThumb().photo;
        int fixedWidth = DisplayController.dp(100);//(int)(DisplayController.screenWidth * 0.5);
        int width = message.getContentDocumentThumb().width;
        int height = message.getContentDocumentThumb().height;

        final boolean isPhoto = FileController.isExists(thumb) && message.isContentDocumentPhoto();
        final Drawable load = !isPhoto ? MessageDocumentPanel.load_blue : MessageDocumentPanel.load;
        final Drawable pause = !isPhoto ? MessageDocumentPanel.pause_blue : MessageDocumentPanel.pause;
        int documentCircularProgressBarSize;

        if(!isPhoto) {
            width = DisplayController.dp(CircularProgressBar.DIAMETER_BLUE);
            height = DisplayController.dp(CircularProgressBar.DIAMETER_BLUE);
            documentCircularProgressBar.setProgressColor(Droid.activity.getResources().getColor(R.color.system_blue));
            documentCircularProgressBar.setBackgroundColor(Droid.activity.getResources().getColor(R.color.system_blue));
            documentCircularProgressBarSize = CircularProgressBar.DIAMETER_BLUE;
        } else {
            height = fixedWidth * height / width;
            width = fixedWidth;
            documentCircularProgressBar.setProgressColor(Droid.activity.getResources().getColor(R.color.white));
            documentCircularProgressBar.setBackgroundColor(Droid.activity.getResources().getColor(R.color.black));
            documentCircularProgressBarSize = CircularProgressBar.DIAMETER_BLACK;
        }

        final int tag = FileController.getId(thumb);
        setTag(R.id.TAG_0, tag);

        documentImageView.setImageDrawable(null);
        LayoutParams layoutParams = (LayoutParams)documentImageView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        documentImageView.setLayoutParams(layoutParams);

        documentCircularProgressBar.reset();
        documentCircularProgressBar.setSize(documentCircularProgressBarSize);
        layoutParams = (LayoutParams)documentCircularProgressBar.getLayoutParams();
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + width / 2 - documentCircularProgressBarSize / 2));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + width / 2 - documentCircularProgressBarSize / 2) : 16);
        layoutParams.topMargin = DisplayController.dp(24 + verticalOffset + height / 2 - documentCircularProgressBarSize / 2);
        layoutParams.width = DisplayController.dp(documentCircularProgressBarSize);
        layoutParams.height = DisplayController.dp(documentCircularProgressBarSize);
        documentCircularProgressBar.setLayoutParams(layoutParams);

        if(isPhoto) {
            if(FileController.isCached(thumb)) {
                FileController.load(documentImageView, FileController.getPath(thumb));
            } else {
                final WeakReference<ImageView> weakReferenceDocumentImageView = new WeakReference<ImageView>(documentImageView);
                final WeakReference<TdApi.File> weakReferenceFile = new WeakReference<TdApi.File>(thumb);
                FileController.load(FileController.getId(thumb), new Callback() {
                    @Override
                    public void call(final Object value) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                if (!MessageDocumentPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                    return;
                                }
                                if (value instanceof TdApi.UpdateFile) {
                                    if (weakReferenceDocumentImageView.get() != null && weakReferenceFile.get() != null) {
                                        FileController.load(weakReferenceDocumentImageView.get(), FileController.getPath(weakReferenceFile.get()));
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }


        if(FileController.isCached(message.getContentDocumentFile())) {
            documentCircularProgressBar.setContent(isPhoto ? null : MessageDocumentPanel.document, false, false);
        } else if(FileController.isLoading(message.getContentDocumentFile())) {
            documentCircularProgressBar.setContent(pause, true, true);
        } else {
            documentCircularProgressBar.setContent(load, false, false);
            final WeakReference<CircularProgressBar> weakReferenceDocumentCircularProgressBar = new WeakReference<CircularProgressBar>(documentCircularProgressBar);
            documentCircularProgressBar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(FileController.isCached(message.getContentDocumentFile()) || FileController.isLoading(message.getContentDocumentFile())) {
                        return;
                    }
                    documentCircularProgressBar.setContent(pause, true, false);
                    final int tag = FileController.getId(message.getContentDocumentFile());
                    MessageDocumentPanel.this.setTag(R.id.TAG_1, tag);
                    FileController.load(FileController.getId(message.getContentDocumentFile()), new Callback() {
                        @Override
                        public void call(final Object value) {
                            Droid.doRunnableUI(new Runnable() {
                                @Override
                                public void run() {
                                    if(!MessageDocumentPanel.this.getTag(R.id.TAG_1).equals(Integer.valueOf(tag))) {
                                        return;
                                    }

                                    if(value instanceof TdApi.UpdateFileProgress) {
                                        if(weakReferenceDocumentCircularProgressBar.get() != null) {
                                            weakReferenceDocumentCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
                                        }
                                    } else if(value instanceof TdApi.UpdateFile) {
                                        if(weakReferenceDocumentCircularProgressBar.get() != null) {
                                            weakReferenceDocumentCircularProgressBar.get().finish(isPhoto ? null : MessageDocumentPanel.document);
                                        }
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }

        documentTitleTextView.setText("document." + MimeTypeMap.getSingleton().getExtensionFromMimeType(message.getContentDocumentMimeType()));//message.getContentDocumentName());
        layoutParams = (LayoutParams) documentTitleTextView.getLayoutParams();
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + 4 + width + 4));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + 4 + width + 4) : 16);
        documentTitleTextView.setLayoutParams(layoutParams);

        documentSizeTextView.setText(FileController.formatSize(message.getContentDocumentSize()));
        layoutParams = (LayoutParams) documentSizeTextView.getLayoutParams();
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : (58 + horizontalOffset + 4 + width + 4));
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? (58 + horizontalOffset + 4 + width + 4) : 16);
        documentSizeTextView.setLayoutParams(layoutParams);
    }
}
