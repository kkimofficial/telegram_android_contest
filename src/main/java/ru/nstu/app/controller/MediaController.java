/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package ru.nstu.app.controller;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import ru.nstu.app.android.Droid;
import ru.nstu.app.android.FileLog;
import ru.nstu.app.android.NativeProvider;
import ru.nstu.app.android.Track;

public class MediaController implements SensorEventListener {

    public static class DispatchQueue extends Thread {
        public volatile Handler handler = null;
        private final Object handlerSyncObject = new Object();

        public DispatchQueue(final String threadName) {
            setName(threadName);
            start();
        }

        private void sendMessage(Message msg, int delay) {
            if (handler == null) {
                try {
                    synchronized (handlerSyncObject) {
                        handlerSyncObject.wait();
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            if (handler != null) {
                if (delay <= 0) {
                    handler.sendMessage(msg);
                } else {
                    handler.sendMessageDelayed(msg, delay);
                }
            }
        }

        public void cancelRunnable(Runnable runnable) {
            if (handler == null) {
                synchronized (handlerSyncObject) {
                    if (handler == null) {
                        try {
                            handlerSyncObject.wait();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }

            if (handler != null) {
                handler.removeCallbacks(runnable);
            }
        }

        public void postRunnable(Runnable runnable) {
            postRunnable(runnable, 0);
        }

        public void postRunnable(Runnable runnable, long delay) {
            if (handler == null) {
                synchronized (handlerSyncObject) {
                    if (handler == null) {
                        try {
                            handlerSyncObject.wait();
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }

            if (handler != null) {
                if (delay <= 0) {
                    handler.post(runnable);
                } else {
                    handler.postDelayed(runnable, delay);
                }
            }
        }

        public void cleanupQueue() {
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
            }
        }

        public void run() {
            Looper.prepare();
            synchronized (handlerSyncObject) {
                handler = new Handler();
                handlerSyncObject.notify();
            }
            Looper.loop();
        }
    }

    public static int[] readArgs = new int[3];

    private class AudioBuffer {
        public AudioBuffer(int capacity) {
            buffer = ByteBuffer.allocateDirect(capacity);
            bufferBytes = new byte[capacity];
        }

        ByteBuffer buffer;
        byte[] bufferBytes;
        int size;
        int finished;
        long pcmOffset;
    }

    public final static String MIME_TYPE = "video/avc";
    private final static int PROCESSOR_TYPE_OTHER = 0;
    private final static int PROCESSOR_TYPE_QCOM = 1;
    private final static int PROCESSOR_TYPE_INTEL = 2;
    private final static int PROCESSOR_TYPE_MTK = 3;
    private final static int PROCESSOR_TYPE_SEC = 4;
    private final static int PROCESSOR_TYPE_TI = 5;
    private final Object videoConvertSync = new Object();

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean ignoreProximity;



    public static final int AUTODOWNLOAD_MASK_PHOTO = 1;
    public static final int AUTODOWNLOAD_MASK_AUDIO = 2;
    public static final int AUTODOWNLOAD_MASK_VIDEO = 4;
    public static final int AUTODOWNLOAD_MASK_DOCUMENT = 8;
    public int mobileDataDownloadMask = 0;
    public int wifiDownloadMask = 0;
    public int roamingDownloadMask = 0;
    private int lastCheckMask = 0;
    private boolean saveToGallery = true;

    private boolean isPaused = false;
    private MediaPlayer audioPlayer = null;
    private AudioTrack audioTrackPlayer = null;
    private int lastProgress = 0;
    private Track playingMessageObject;
    private int playerBufferSize = 0;
    private boolean decodingFinished = false;
    private long currentTotalPcmDuration;
    private long lastPlayPcm;
    private int ignoreFirstProgress = 0;
    private Timer progressTimer = null;
    private final Object progressTimerSync = new Object();
    private boolean useFrontSpeaker;

    private DispatchQueue fileDecodingQueue;
    private DispatchQueue playerQueue;
    private ArrayList<AudioBuffer> usedPlayerBuffers = new ArrayList<>();
    private ArrayList<AudioBuffer> freePlayerBuffers = new ArrayList<>();
    private final Object playerSync = new Object();
    private final Object playerObjectSync = new Object();

    private final Object sync = new Object();

    private ArrayList<ByteBuffer> recordBuffers = new ArrayList<>();
    private ByteBuffer fileBuffer;

    private static volatile MediaController Instance = null;
    public static MediaController getInstance() {
        MediaController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MediaController();
                }
            }
        }
        return localInstance;
    }

    public MediaController() {
        try {
            playerBufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (playerBufferSize <= 0) {
                playerBufferSize = 3840;
            }
            for (int a = 0; a < 5; a++) {
                ByteBuffer buffer = ByteBuffer.allocateDirect(4096);
                recordBuffers.add(buffer);
            }
            for (int a = 0; a < 3; a++) {
                freePlayerBuffers.add(new AudioBuffer(playerBufferSize));
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            sensorManager = (SensorManager) Droid.getSystemService(Context.SENSOR_SERVICE);
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        fileBuffer = ByteBuffer.allocateDirect(1920);
        playerQueue = new DispatchQueue("playerQueue");
        fileDecodingQueue = new DispatchQueue("fileDecodingQueue");

    }

    private void startProgressTimer() {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
            progressTimer = new Timer();
            progressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (sync) {
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                if (playingMessageObject != null && (audioPlayer != null || audioTrackPlayer != null) && !isPaused) {
                                    try {
                                        if (ignoreFirstProgress != 0) {
                                            ignoreFirstProgress--;
                                            return;
                                        }
                                        int progress = 0;
                                        float value = 0;
                                        if (audioPlayer != null) {
                                            progress = audioPlayer.getCurrentPosition();
                                            value = (float) lastProgress / (float) audioPlayer.getDuration();
                                            if (progress <= lastProgress) {
                                                return;
                                            }
                                        } else if (audioTrackPlayer != null) {
                                            progress = (int) (lastPlayPcm / 48.0f);
                                            value = (float) lastPlayPcm / (float) currentTotalPcmDuration;
                                            if (progress == lastProgress) {
                                                return;
                                            }
                                        }
                                        lastProgress = progress;
                                        playingMessageObject.audioProgress = value;
                                        playingMessageObject.audioProgressSec = lastProgress / 1000;
                                        playingMessageObject.notifyProgress(value);
//                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioProgressDidChanged, playingMessageObject.getId(), value);
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                }
                            }
                        });
                    }
                }
            }, 0, 17);
        }
    }

    private void stopProgressTimer() {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        }
    }










    private void checkDecoderQueue() {
        fileDecodingQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (decodingFinished) {
                    checkPlayerQueue();
                    return;
                }
                boolean was = false;
                while (true) {
                    AudioBuffer buffer = null;
                    synchronized (playerSync) {
                        if (!freePlayerBuffers.isEmpty()) {
                            buffer = freePlayerBuffers.get(0);
                            freePlayerBuffers.remove(0);
                        }
                        if (!usedPlayerBuffers.isEmpty()) {
                            was = true;
                        }
                    }
                    if (buffer != null) {
                        NativeProvider.readOpusFile(buffer.buffer, playerBufferSize, readArgs);
                        buffer.size = readArgs[0];
                        buffer.pcmOffset = readArgs[1];
                        buffer.finished = readArgs[2];
                        if (buffer.finished == 1) {
                            decodingFinished = true;
                        }
                        if (buffer.size != 0) {
                            buffer.buffer.rewind();
                            buffer.buffer.get(buffer.bufferBytes);
                            synchronized (playerSync) {
                                usedPlayerBuffers.add(buffer);
                            }
                        } else {
                            synchronized (playerSync) {
                                freePlayerBuffers.add(buffer);
                                break;
                            }
                        }
                        was = true;
                    } else {
                        break;
                    }
                }
                if (was) {
                    checkPlayerQueue();
                }
            }
        });
    }

    private void checkPlayerQueue() {
        playerQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (playerObjectSync) {
                    if (audioTrackPlayer == null || audioTrackPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                        return;
                    }
                }
                AudioBuffer buffer = null;
                synchronized (playerSync) {
                    if (!usedPlayerBuffers.isEmpty()) {
                        buffer = usedPlayerBuffers.get(0);
                        usedPlayerBuffers.remove(0);
                    }
                }

                if (buffer != null) {
                    int count = 0;
                    try {
                        count = audioTrackPlayer.write(buffer.bufferBytes, 0, buffer.size);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }

                    if (count > 0) {
                        final long pcm = buffer.pcmOffset;
                        final int marker = buffer.finished == 1 ? buffer.size : -1;
                        Droid.doRunnableUI(new Runnable() {
                            @Override
                            public void run() {
                                lastPlayPcm = pcm;
                                if (marker != -1) {
                                    if (audioTrackPlayer != null) {
                                        audioTrackPlayer.setNotificationMarkerPosition(1);
                                    }
                                }
                            }
                        });
                    }

                    if (buffer.finished != 1) {
                        checkPlayerQueue();
                    }
                }
                if (buffer == null || buffer != null && buffer.finished != 1) {
                    checkDecoderQueue();
                }

                if (buffer != null) {
                    synchronized (playerSync) {
                        freePlayerBuffers.add(buffer);
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (proximitySensor != null && audioTrackPlayer == null && audioPlayer == null || isPaused || (useFrontSpeaker == (event.values[0] < proximitySensor.getMaximumRange() / 10))) {
            return;
        }
        boolean newValue = event.values[0] < proximitySensor.getMaximumRange() / 10;
        try {
//            if (newValue && NotificationsController.getInstance().audioManager.isWiredHeadsetOn()) {
//                return;
//            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        ignoreProximity = true;
        useFrontSpeaker = newValue;
//        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioRouteChanged, useFrontSpeaker);
        Track currentMessageObject = playingMessageObject;
        float progress = playingMessageObject.audioProgress;
        clenupPlayer(false);
        currentMessageObject.audioProgress = progress;
        playAudio(currentMessageObject);
        ignoreProximity = false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void stopProximitySensor() {
        if (ignoreProximity) {
            return;
        }
        try {
            useFrontSpeaker = false;
//            NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioRouteChanged, useFrontSpeaker);
            if (sensorManager != null && proximitySensor != null) {
                sensorManager.unregisterListener(this);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    private void startProximitySensor() {
        if (ignoreProximity) {
            return;
        }
        try {
            if (sensorManager != null && proximitySensor != null) {
                sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    private void clenupPlayer(boolean notify) {
        stopProximitySensor();
        if (audioPlayer != null || audioTrackPlayer != null) {
            if (audioPlayer != null) {
                try {
                    audioPlayer.stop();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                try {
                    audioPlayer.release();
                    audioPlayer = null;
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            } else if (audioTrackPlayer != null) {
                synchronized (playerObjectSync) {
                    try {
                        audioTrackPlayer.pause();
                        audioTrackPlayer.flush();
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                    try {
                        audioTrackPlayer.release();
                        audioTrackPlayer = null;
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            }
            stopProgressTimer();
            lastProgress = 0;
            isPaused = false;
            Track lastFile = playingMessageObject;
            playingMessageObject.audioProgress = 0.0f;
            playingMessageObject.audioProgressSec = 0;
            playingMessageObject = null;
            if (notify) {
//                NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidReset, lastFile.getId());
            }
        }
    }

    private void seekOpusPlayer(final float progress) {
        if (currentTotalPcmDuration * progress == currentTotalPcmDuration) {
            return;
        }
        if (!isPaused) {
            audioTrackPlayer.pause();
        }
        audioTrackPlayer.flush();
        fileDecodingQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                NativeProvider.seekOpusFile(progress);
                synchronized (playerSync) {
                    freePlayerBuffers.addAll(usedPlayerBuffers);
                    usedPlayerBuffers.clear();
                }
                Droid.doRunnableUI(new Runnable() {
                    @Override
                    public void run() {
                        if (!isPaused) {
                            ignoreFirstProgress = 3;
                            lastPlayPcm = (long) (currentTotalPcmDuration * progress);
                            if (audioTrackPlayer != null) {
                                audioTrackPlayer.play();
                            }
                            lastProgress = (int) (currentTotalPcmDuration / 48.0f * progress);
                            checkPlayerQueue();
                        }
                    }
                });
            }
        });
    }

    public boolean seekToProgress(Track messageObject, float progress) {
        if (audioTrackPlayer == null && audioPlayer == null || messageObject == null || playingMessageObject == null || playingMessageObject != null && playingMessageObject.getId() != messageObject.getId()) {
            return false;
        }
        try {
            if (audioPlayer != null) {
                int seekTo = (int) (audioPlayer.getDuration() * progress);
                audioPlayer.seekTo(seekTo);
                lastProgress = seekTo;
            } else if (audioTrackPlayer != null) {
                seekOpusPlayer(progress);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return false;
        }
        return true;
    }

    public boolean playAudio(Track messageObject) {
        if (messageObject == null) {
            return false;
        }
        if ((audioTrackPlayer != null || audioPlayer != null) && playingMessageObject != null && messageObject.getId() == playingMessageObject.getId()) {
            if (isPaused) {
                resumeAudio(messageObject);
            }
            return true;
        }
//        NotificationCenter.getInstance().postNotificationName(NotificationCenter.audioDidStarted, messageObject);
        clenupPlayer(true);
        final File cacheFile = new File(FileController.getPath(messageObject.getAudioFile()));

        if (NativeProvider.isOpusFile(cacheFile.getAbsolutePath()) == 1) {
            synchronized (playerObjectSync) {
                try {
                    ignoreFirstProgress = 3;
                    final Semaphore semaphore = new Semaphore(0);
                    final Boolean[] result = new Boolean[1];
                    fileDecodingQueue.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            result[0] = NativeProvider.openOpusFile(cacheFile.getAbsolutePath()) != 0;
                            semaphore.release();
                        }
                    });
                    semaphore.acquire();

                    if (!result[0]) {
                        return false;
                    }
                    currentTotalPcmDuration = NativeProvider.getTotalPcmDuration();

                    audioTrackPlayer = new AudioTrack(useFrontSpeaker ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, playerBufferSize, AudioTrack.MODE_STREAM);
                    audioTrackPlayer.setStereoVolume(1.0f, 1.0f);
                    audioTrackPlayer.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                        @Override
                        public void onMarkerReached(AudioTrack audioTrack) {
                            clenupPlayer(true);
                        }

                        @Override
                        public void onPeriodicNotification(AudioTrack audioTrack) {

                        }
                    });
                    audioTrackPlayer.play();
                    startProgressTimer();
                    startProximitySensor();
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                    if (audioTrackPlayer != null) {
                        audioTrackPlayer.release();
                        audioTrackPlayer = null;
                        isPaused = false;
                        playingMessageObject = null;
                    }
                    return false;
                }
            }
        } else {
            try {
                audioPlayer = new MediaPlayer();
                audioPlayer.setAudioStreamType(useFrontSpeaker ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);
                audioPlayer.setDataSource(cacheFile.getAbsolutePath());
                audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        clenupPlayer(true);
                    }
                });
                audioPlayer.prepare();
                audioPlayer.start();
                startProgressTimer();
                startProximitySensor();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
                if (audioPlayer != null) {
                    audioPlayer.release();
                    audioPlayer = null;
                    isPaused = false;
                    playingMessageObject = null;
                }
                return false;
            }
        }

        isPaused = false;
        lastProgress = 0;
        lastPlayPcm = 0;
        playingMessageObject = messageObject;

        if (audioPlayer != null) {
            try {
                if (playingMessageObject.audioProgress != 0) {
                    int seekTo = (int) (audioPlayer.getDuration() * playingMessageObject.audioProgress);
                    audioPlayer.seekTo(seekTo);
                }
            } catch (Exception e2) {
                playingMessageObject.audioProgress = 0;
                playingMessageObject.audioProgressSec = 0;
                FileLog.e("tmessages", e2);
            }
        } else if (audioTrackPlayer != null) {
            if (playingMessageObject.audioProgress == 1) {
                playingMessageObject.audioProgress = 0;
            }
            fileDecodingQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (playingMessageObject != null && playingMessageObject.audioProgress != 0) {
                            lastPlayPcm = (long)(currentTotalPcmDuration * playingMessageObject.audioProgress);
                            NativeProvider.seekOpusFile(playingMessageObject.audioProgress);
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                    synchronized (playerSync) {
                        freePlayerBuffers.addAll(usedPlayerBuffers);
                        usedPlayerBuffers.clear();
                    }
                    decodingFinished = false;
                    checkPlayerQueue();
                }
            });
        }

        return true;
    }

    public void stopAudio() {
        stopProximitySensor();
        if (audioTrackPlayer == null && audioPlayer == null || playingMessageObject == null) {
            return;
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.pause();
                audioTrackPlayer.flush();
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            } else if (audioTrackPlayer != null) {
                synchronized (playerObjectSync) {
                    audioTrackPlayer.release();
                    audioTrackPlayer = null;
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        stopProgressTimer();
        playingMessageObject = null;
        isPaused = false;
    }

    public boolean pauseAudio(Track messageObject) {
        stopProximitySensor();
        if (audioTrackPlayer == null && audioPlayer == null || messageObject == null || playingMessageObject == null || playingMessageObject != null && playingMessageObject.getId() != messageObject.getId()) {
            return false;
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.pause();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.pause();
            }
            isPaused = true;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            isPaused = false;
            return false;
        }
        return true;
    }

    public boolean resumeAudio(Track messageObject) {
        startProximitySensor();
        if (audioTrackPlayer == null && audioPlayer == null || messageObject == null || playingMessageObject == null || playingMessageObject != null && playingMessageObject.getId() != messageObject.getId()) {
            return false;
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.start();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.play();
                checkPlayerQueue();
            }
            isPaused = false;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return false;
        }
        return true;
    }

    public boolean isPlayingAudio(Track messageObject) {
        return !(audioTrackPlayer == null && audioPlayer == null || messageObject == null || playingMessageObject == null || playingMessageObject != null && playingMessageObject.getId() != messageObject.getId());
    }

    public boolean isAudioPaused() {
        return isPaused;
    }

    private static boolean isRecognizedFormat(int colorFormat) {
        switch (colorFormat) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
            case MediaCodecInfo.CodecCapabilities.COLOR_TI_FormatYUV420PackedSemiPlanar:
                return true;
            default:
                return false;
        }
    }


}