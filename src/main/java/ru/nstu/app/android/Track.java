package ru.nstu.app.android;

import org.drinkless.td.libcore.telegram.TdApi;

import java.lang.ref.WeakReference;

import ru.nstu.app.model.Message;
import ru.nstu.app.ui.component.panel.MessageAudioPanel;
import ru.nstu.app.ui.component.panel.MessagePanel;

public class Track {
    private Message message;
    private WeakReference<MessageAudioPanel> weakReferenceMessagePanel;

    public float audioProgress;
    public int audioProgressSec;

    public Track(Message message, WeakReference<MessageAudioPanel> weakReferenceMessagePanel) {
        this.message = message;
        this.weakReferenceMessagePanel = weakReferenceMessagePanel;
    }

    public int getId() {
        return message.getId();
    }

    public TdApi.File getAudioFile() {
        return message.getContentAudioFile();
    }

    public void notifyProgress(float progress) {
        MessageAudioPanel messageAudioPanel = weakReferenceMessagePanel.get();
        if(messageAudioPanel != null) {
            messageAudioPanel.getAudioSeekBar().setProgress((int)(progress * 100));
        }
    }
}
