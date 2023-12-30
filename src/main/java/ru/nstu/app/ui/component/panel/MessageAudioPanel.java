package ru.nstu.app.ui.component.panel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;

import ru.nstu.app.R;
import ru.nstu.app.android.Callback;
import ru.nstu.app.android.Droid;
import ru.nstu.app.android.NativeProvider;
import ru.nstu.app.android.Track;
import ru.nstu.app.controller.DisplayController;
import ru.nstu.app.controller.FileController;
import ru.nstu.app.controller.LocaleController;
import ru.nstu.app.controller.MediaController;
import ru.nstu.app.ui.component.common.CircularProgressBar;

public class MessageAudioPanel extends MessagePanel {
    private static Drawable load_blue;
    private static Drawable pause_blue;
    private static Drawable play_blue;

    protected CircularProgressBar audioCircularProgressBar;
    protected SeekBar audioSeekBar;
    protected TextView audioTextView;

    protected TextPaint textPaint;

    public MessageAudioPanel(Context context) {
        super(context);

        audioCircularProgressBar = new CircularProgressBar(context);
        audioCircularProgressBar.setProgressColor(Droid.activity.getResources().getColor(R.color.system_blue));
        audioCircularProgressBar.setBackgroundColor(Droid.activity.getResources().getColor(R.color.system_blue));
        audioCircularProgressBar.setSize(CircularProgressBar.DIAMETER_BLUE);
        addView(audioCircularProgressBar);
        LayoutParams layoutParams = (LayoutParams)audioCircularProgressBar.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : 58);
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 : 16);
        layoutParams.topMargin = DisplayController.dp(24);
        layoutParams.width = DisplayController.dp(CircularProgressBar.DIAMETER_BLUE);
        layoutParams.height = DisplayController.dp(CircularProgressBar.DIAMETER_BLUE);
        audioCircularProgressBar.setLayoutParams(layoutParams);

        audioSeekBar = new SeekBar(context);
        audioSeekBar.setMax(100);
        addView(audioSeekBar);
        layoutParams = (LayoutParams)audioSeekBar.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 16 : 58 + CircularProgressBar.DIAMETER_BLUE + 4);
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 + CircularProgressBar.DIAMETER_BLUE + 4 : 16);
        layoutParams.topMargin = DisplayController.dp(24);
        layoutParams.width = LayoutParams.MATCH_PARENT;
        audioSeekBar.setLayoutParams(layoutParams);

        audioTextView = new TextView(context);
        addView(audioTextView);
        layoutParams = (LayoutParams)audioTextView.getLayoutParams();
        layoutParams.gravity = LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT;
        layoutParams.leftMargin = DisplayController.dp(LocaleController.isRTL ? 8 : 58);
        layoutParams.rightMargin = DisplayController.dp(LocaleController.isRTL ? 58 : 8);
        layoutParams.topMargin = DisplayController.dp(24);
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        audioTextView.setLayoutParams(layoutParams);
        audioTextView.setTextSize(DisplayController.sp(13));

        textPaint = new TextPaint();
        textPaint.setTextSize(DisplayController.sp(13));

        if(MessageAudioPanel.load_blue == null) {
            MessageAudioPanel.load_blue = context.getResources().getDrawable(R.drawable.ic_download_blue);
        }
        if(MessageAudioPanel.pause_blue == null) {
            MessageAudioPanel.pause_blue = context.getResources().getDrawable(R.drawable.ic_pause_blue);
        }
        if(MessageAudioPanel.play_blue == null) {
            MessageAudioPanel.play_blue = context.getResources().getDrawable(R.drawable.ic_play_blue);
        }
    }

    @Override
    public void update() {
        final TdApi.File file = message.getContentAudioFile();
        if(FileController.isExists(file)) {

            final Track track = new Track(message, new WeakReference<MessageAudioPanel>(MessageAudioPanel.this));
            MediaController mediaController = MediaController.getInstance();
            if(!mediaController.isPlayingAudio(track)) {
                audioCircularProgressBar.setContent(MessageAudioPanel.play_blue, false, false);
            } else if(mediaController.isPlayingAudio(track) && !mediaController.isAudioPaused()) {
                audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, false, false);
            } else if(mediaController.isPlayingAudio(track) && mediaController.isAudioPaused()) {
                audioCircularProgressBar.setContent(MessageAudioPanel.play_blue, false, false);
            }
            if(FileController.isCached(file)) {
                audioCircularProgressBar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        MediaController mediaController = MediaController.getInstance();
                        if(!mediaController.isPlayingAudio(track)) {
                            audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, false, false);
                            mediaController.playAudio(track);
                        } else if(mediaController.isPlayingAudio(track) && !mediaController.isAudioPaused()) {
                            audioCircularProgressBar.setContent(MessageAudioPanel.play_blue, false, false);
                            mediaController.pauseAudio(track);
                        } else if(mediaController.isPlayingAudio(track) && mediaController.isAudioPaused()) {
                            audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, false, false);
                            mediaController.resumeAudio(track);
                        }
                    }
                });
            } else if(FileController.isLoading(file)) {
                audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, true, true);
            } else {
                audioCircularProgressBar.setContent(MessageAudioPanel.load_blue, false, false);
                final WeakReference<CircularProgressBar> weakReferenceAudioCircularProgressBar = new WeakReference<CircularProgressBar>(audioCircularProgressBar);
                audioCircularProgressBar.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(FileController.isCached(file)) {

                            Track track = new Track(message, new WeakReference<MessageAudioPanel>(MessageAudioPanel.this));
                            MediaController mediaController = MediaController.getInstance();
                            if(!mediaController.isPlayingAudio(track)) {
                                audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, false, false);
                                mediaController.playAudio(track);
                            } else if(mediaController.isPlayingAudio(track) && !mediaController.isAudioPaused()) {
                                audioCircularProgressBar.setContent(MessageAudioPanel.play_blue, false, false);
                                mediaController.pauseAudio(track);
                            } else if(mediaController.isPlayingAudio(track) && mediaController.isAudioPaused()) {
                                audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, false, false);
                                mediaController.resumeAudio(track);
                            }

                            return;
                        } else if(FileController.isLoading(file)) {
                            return;
                        }
                        audioCircularProgressBar.setContent(MessageAudioPanel.pause_blue, true, false);
                        final int tag = FileController.getId(file);
                        MessageAudioPanel.this.setTag(R.id.TAG_0, tag);
                        FileController.load(FileController.getId(file), new Callback() {
                            @Override
                            public void call(final Object value) {
                                Droid.doRunnableUI(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!MessageAudioPanel.this.getTag(R.id.TAG_0).equals(Integer.valueOf(tag))) {
                                            return;
                                        }

                                        if(value instanceof TdApi.UpdateFileProgress) {
                                            if(weakReferenceAudioCircularProgressBar.get() != null) {
                                                weakReferenceAudioCircularProgressBar.get().setProgress(FileController.getProgress(((TdApi.UpdateFileProgress) value).fileId));
                                            }
                                        } else if(value instanceof TdApi.UpdateFile) {
                                            if(weakReferenceAudioCircularProgressBar.get() != null) {
                                                weakReferenceAudioCircularProgressBar.get().finish(MessageAudioPanel.play_blue);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }

            String duration = LocaleController.formatDate(message.getContentAudioDuration(), LocaleController.DateFormat.AUDIO);
            audioTextView.setText(duration);
            LayoutParams layoutParams = (LayoutParams)audioTextView.getLayoutParams();
            layoutParams.topMargin = DisplayController.dp(24) + DisplayController.dp(CircularProgressBar.DIAMETER_BLUE) / 2 - DisplayController.sp(13) / 2 - DisplayController.dp(2);
            audioTextView.setLayoutParams(layoutParams);


            layoutParams = (LayoutParams)audioSeekBar.getLayoutParams();
            layoutParams.rightMargin = DisplayController.dp(textPaint.measureText(duration));
            audioSeekBar.setLayoutParams(layoutParams);
        } else {
            audioCircularProgressBar.setVisibility(View.GONE);
            audioSeekBar.setVisibility(View.GONE);
        }
    }

    public SeekBar getAudioSeekBar() {
        return audioSeekBar;
    }
}
